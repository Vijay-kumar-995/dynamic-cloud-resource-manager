import java.util.HashMap;
import java.util.Map;

public class ResourceAllocator {

    private ResourcePool resourcePool;

    private Map<String, Allocation> allocations = new HashMap<>();

    private static class Allocation {
        double cpu;
        long memoryMB;

        Allocation(double cpu, long memoryMB) {
            this.cpu = cpu;
            this.memoryMB = memoryMB;
        }
    }

    public ResourceAllocator(ResourcePool resourcePool) {
        this.resourcePool = resourcePool;
    }

    public boolean allocateForWorkload(String workloadName, double cpu, long memoryMB) {

        System.out.println("\nAllocating resources for: " + workloadName);

        if (allocations.containsKey(workloadName)) {
            System.out.println("Workload already has an allocation.");
            return false;
        }

        boolean allocated = resourcePool.allocateResources(cpu, memoryMB);

        if (allocated) {
            allocations.put(workloadName, new Allocation(cpu, memoryMB));
            System.out.println("Resources assigned to " + workloadName + ".");
            return true;
        }

        System.out.println("Allocation failed for " + workloadName + ".");
        return false;
    }

    public boolean releaseForWorkload(String workloadName) {

        System.out.println("\nReleasing resources for: " + workloadName);

        Allocation allocation = allocations.get(workloadName);

        if (allocation == null) {
            System.out.println("No allocation found for this workload.");
            return false;
        }

        boolean released = resourcePool.releaseResources(
                allocation.cpu,
                allocation.memoryMB
        );

        if (released) {
            allocations.remove(workloadName);
            System.out.println("Resources released for " + workloadName + ".");
            return true;
        }

        System.out.println("Resource release failed for " + workloadName + ".");
        return false;
    }

    public void displayAllocations() {

        System.out.println("\n=== Current Workload Allocations ===");

        if (allocations.isEmpty()) {
            System.out.println("No active allocations.");
            return;
        }

        for (Map.Entry<String, Allocation> entry : allocations.entrySet()) {
            Allocation allocation = entry.getValue();

            System.out.println(
                    entry.getKey()
                    + " | CPU: " + allocation.cpu
                    + " | Memory: " + allocation.memoryMB + " MB"
            );
        }
    }
}