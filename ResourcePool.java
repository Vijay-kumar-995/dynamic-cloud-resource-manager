public class ResourcePool {

    private final double totalCpu;
    private final long totalMemoryMB;

    private double availableCpu;
    private long availableMemoryMB;

    private static final double EPSILON = 0.0000001;

    public ResourcePool(double totalCpu, long totalMemoryMB) {
        if (!Double.isFinite(totalCpu) || totalCpu <= 0 || totalMemoryMB <= 0) {
            throw new IllegalArgumentException("Invalid resource pool capacity.");
        }

        this.totalCpu = totalCpu;
        this.totalMemoryMB = totalMemoryMB;
        this.availableCpu = totalCpu;
        this.availableMemoryMB = totalMemoryMB;
    }

    public synchronized boolean allocateResources(double cpu, long memoryMB) {

        if (!Double.isFinite(cpu) || cpu < 0 ||
                memoryMB < 0 || (cpu == 0 && memoryMB == 0)) {
            System.out.println("Invalid resource request.");
            return false;
        }

        if (cpu > availableCpu + EPSILON ||
                memoryMB > availableMemoryMB) {
            System.out.println("Insufficient resources available.");
            return false;
        }

        availableCpu -= cpu;
        availableMemoryMB -= memoryMB;

        System.out.println("Resources allocated successfully.");
        return true;
    }

    public synchronized boolean releaseResources(double cpu, long memoryMB) {

        if (!Double.isFinite(cpu) || cpu < 0 ||
                memoryMB < 0 || (cpu == 0 && memoryMB == 0)) {
            System.out.println("Invalid resource release request.");
            return false;
        }

        if (availableCpu + cpu > totalCpu + EPSILON ||
                availableMemoryMB + memoryMB > totalMemoryMB) {
            System.out.println("Cannot release more resources than pool capacity.");
            return false;
        }

        availableCpu = Math.min(totalCpu, availableCpu + cpu);
        availableMemoryMB += memoryMB;

        System.out.println("Resources released successfully.");
        return true;
    }

    public synchronized void displayResources() {
        System.out.println("Total CPU: " + totalCpu);
        System.out.println("Available CPU: " + availableCpu);
        System.out.println("Total Memory: " + totalMemoryMB + " MB");
        System.out.println("Available Memory: " + availableMemoryMB + " MB");
    }
}