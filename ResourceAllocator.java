import java.util.HashMap;
import java.util.Map;

public class ResourceAllocator {

    private final ResourcePool resourcePool;
    private final Map<String, Allocation> allocations = new HashMap<>();

    private static class Allocation {
        String containerName;

        double cpu;
        long memoryMB;

        double reservedCpu;
        long reservedMemoryMB;

        Allocation(String containerName, double cpu, long memoryMB) {
            this.containerName = containerName;
            this.cpu = cpu;
            this.memoryMB = memoryMB;
            this.reservedCpu = cpu;
            this.reservedMemoryMB = memoryMB;
        }
    }

    public ResourceAllocator(ResourcePool resourcePool) {
        if (resourcePool == null) {
            throw new IllegalArgumentException("Resource pool cannot be null.");
        }

        this.resourcePool = resourcePool;
    }

    private boolean validRequest(double cpu, long memoryMB) {
        return Double.isFinite(cpu) && cpu > 0 &&
                memoryMB > 0 && memoryMB <= Integer.MAX_VALUE / 2L;
    }

    public synchronized boolean allocateForWorkload(
            String workloadName,
            String containerName,
            double cpu,
            long memoryMB) {

        System.out.println("\nAllocating resources for: " + workloadName);

        if (workloadName == null || workloadName.isBlank() ||
                containerName == null || containerName.isBlank() ||
                !validRequest(cpu, memoryMB)) {
            System.out.println("Invalid workload or resource request.");
            return false;
        }

        if (allocations.containsKey(workloadName)) {
            System.out.println("Workload already has an allocation.");
            return false;
        }

        try {
            if (!DockerController.isContainerRunning(containerName)) {
                System.out.println("Container is not running.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Container check failed: " + e.getMessage());
            return false;
        }

        if (!resourcePool.allocateResources(cpu, memoryMB)) {
            System.out.println("Insufficient resources in the pool.");
            return false;
        }

        try {
            DockerController.updateResources(
                    containerName, cpu, (int) memoryMB);

            allocations.put(
                    workloadName,
                    new Allocation(containerName, cpu, memoryMB));

            System.out.println("Resources assigned to " + workloadName + ".");
            return true;

        } catch (Exception e) {
            resourcePool.releaseResources(cpu, memoryMB);
            System.out.println("Docker update failed: " + e.getMessage());
            System.out.println("Resource reservation rolled back.");
            return false;
        }
    }

    public synchronized boolean releaseForWorkload(String workloadName) {

        System.out.println("\nReleasing resources for: " + workloadName);

        Allocation allocation = allocations.get(workloadName);

        if (allocation == null) {
            System.out.println("No allocation found for this workload.");
            return false;
        }

        try {
            boolean running = DockerController.isContainerRunning(
                    allocation.containerName);

            if (running) {
                DockerController.stopContainer(allocation.containerName);
            }

            boolean released = resourcePool.releaseResources(
                    allocation.reservedCpu,
                    allocation.reservedMemoryMB);

            if (!released) {
                System.out.println("Resource release failed. Allocation retained.");
                return false;
            }

            allocations.remove(workloadName);

            System.out.println("Resources reclaimed for " + workloadName + ".");
            return true;

        } catch (Exception e) {
            System.out.println("Resource reclamation failed: " + e.getMessage());
            return false;
        }
    }

    public synchronized boolean reallocateForWorkload(
            String workloadName,
            double newCpu,
            long newMemoryMB) {

        System.out.println("\nReallocating resources for: " + workloadName);

        Allocation allocation = allocations.get(workloadName);

        if (allocation == null) {
            System.out.println("No allocation found for this workload.");
            return false;
        }

        if (!validRequest(newCpu, newMemoryMB)) {
            System.out.println("Invalid resource request.");
            return false;
        }

        try {
            if (!DockerController.isContainerRunning(allocation.containerName)) {
                System.out.println("Container is not running.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Container check failed: " + e.getMessage());
            return false;
        }

        double deltaCpu = newCpu - allocation.cpu;
        long deltaMemory = newMemoryMB - allocation.memoryMB;

        double increaseCpu = Math.max(deltaCpu, 0);
        long increaseMemory = Math.max(deltaMemory, 0);

        double decreaseCpu = Math.max(-deltaCpu, 0);
        long decreaseMemory = Math.max(-deltaMemory, 0);

        if (increaseCpu > 0 || increaseMemory > 0) {
            if (!resourcePool.allocateResources(increaseCpu, increaseMemory)) {
                System.out.println("Insufficient resources for reallocation.");
                return false;
            }

            allocation.reservedCpu += increaseCpu;
            allocation.reservedMemoryMB += increaseMemory;
        }

        try {
            DockerController.updateResources(
                    allocation.containerName,
                    newCpu,
                    (int) newMemoryMB);

        } catch (Exception e) {

            if (increaseCpu > 0 || increaseMemory > 0) {
                resourcePool.releaseResources(increaseCpu, increaseMemory);
                allocation.reservedCpu -= increaseCpu;
                allocation.reservedMemoryMB -= increaseMemory;
            }

            System.out.println("Reallocation failed: " + e.getMessage());
            System.out.println("Additional reservation rolled back.");
            return false;
        }

        allocation.cpu = newCpu;
        allocation.memoryMB = newMemoryMB;

        if (decreaseCpu > 0 || decreaseMemory > 0) {
            boolean released = resourcePool.releaseResources(
                    decreaseCpu, decreaseMemory);

            if (released) {
                allocation.reservedCpu -= decreaseCpu;
                allocation.reservedMemoryMB -= decreaseMemory;
            } else {
                System.out.println(
                        "Warning: Docker limits changed, but the pool "
                                + "retains the previous reservation.");
            }
        }

        System.out.println("Resources reallocated successfully.");
        return true;
    }

    public synchronized void displayAllocations() {

        System.out.println("\n=== Current Workload Allocations ===");

        if (allocations.isEmpty()) {
            System.out.println("No active allocations.");
            return;
        }

        for (Map.Entry<String, Allocation> entry : allocations.entrySet()) {

            Allocation allocation = entry.getValue();

            System.out.println(
                    "Workload: " + entry.getKey()
                            + " | Container: " + allocation.containerName
                            + " | CPU: " + allocation.cpu
                            + " | Memory: " + allocation.memoryMB + " MB"
            );
        }
    }
}