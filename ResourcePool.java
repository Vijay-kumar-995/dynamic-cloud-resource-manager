public  class ResourcePool {
    private final double totalCpu;
    private final long totalMemoryMB;

    private  double availableCpu;
    private long availableMemoryMB;

    public ResourcePool(double totalCpu, long totalMemory){
        this.totalCpu = totalCpu;
        this.totalMemoryMB = totalMemory;

        this.availableCpu = totalCpu;
        this.availableMemoryMB = totalMemory;
    }

    public boolean allocateResources(double cpu, long memoryMB){
        if( cpu <=0 || memoryMB <=0){
            System.out.println("Invalid resource request.");
            return false;
        }
        
        if (cpu > availableCpu || memoryMB > availableMemoryMB) {
            System.out.println("Insufficient resource available.");
            return false;
        }
        
        availableCpu -= cpu;
        availableMemoryMB -= memoryMB;

        System.out.println("Resource allocated successfully.");
        return true;
    }

    public boolean releaseResources( double cpu, long memoryMB){
        if (cpu <= 0 || memoryMB <= 0) {
            System.out.println("Invalid resource release request.");
            return false;
        }

        if (availableCpu + cpu > totalCpu || availableMemoryMB + memoryMB > totalMemoryMB) {
            System.out.println("Cannot release more resources than the pool capacity.");
            return false;
        }

        availableCpu += cpu;
        availableMemoryMB += memoryMB;

        System.out.println("Resources released successfully.");
        return true;
    }

    public void displayResources(){
        System.out.println("Total CPU: " + totalCpu);
        System.out.println("Available CPU: " + availableCpu);

        System.out.println("Total Memory: " + totalMemoryMB + " MB");
        System.out.println("Available Memory: " + availableMemoryMB + " MB");
    }

    public  static void main(String[] args){
        ResourcePool pool = new ResourcePool(4.0, 4096);
        System.out.println("Resource Pool initialized successfully.");

        pool.displayResources();

        System.out.println("\n--- Allocation ---");
        pool.allocateResources(1.5, 1024);
        pool.displayResources();

        System.out.println("\n--- Release Allocated Resources ---");
        pool.releaseResources(1.5, 1024);
        pool.displayResources();

        System.out.println("\n--- Invalid Release Test ---");
        pool.releaseResources(1.0, 512);
        pool.displayResources();

        // System.out.println("\n--- Second Allocation ---");
        // pool.allocateResources(3.0, 2048);
        // pool.displayResources();
    }
}