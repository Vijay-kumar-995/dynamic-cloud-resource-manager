public class TestResourceAllocator {

    public static void main(String[] args) {

        ResourcePool pool = new ResourcePool(4.0, 4096);
        ResourceAllocator allocator = new ResourceAllocator(pool);

        System.out.println("=== Resource Allocator Test ===");

        // Allocate resources to two workloads
        allocator.allocateForWorkload("Workload-A", 1.0, 512);
        allocator.allocateForWorkload("Workload-B", 2.0, 1024);

        // Display allocations and remaining resources
        allocator.displayAllocations();
        pool.displayResources();

        // Test duplicate workload allocation
        System.out.println("\n=== Duplicate Allocation Test ===");
        allocator.allocateForWorkload("Workload-A", 0.5, 256);

        // Release one workload's resources
        System.out.println("\n=== Resource Release Test ===");
        allocator.releaseForWorkload("Workload-A");

        // Display updated state
        allocator.displayAllocations();
        pool.displayResources();

        // Test releasing a workload that no longer has an allocation
        System.out.println("\n=== Invalid Release Test ===");
        allocator.releaseForWorkload("Workload-A");
    }
}