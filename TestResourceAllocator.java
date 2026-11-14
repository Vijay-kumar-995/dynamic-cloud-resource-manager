public class TestResourceAllocator {
    public static void main(String[] args) {

        ResourcePool pool = new ResourcePool(4.0, 4096);
        ResourceAllocator allocator = new ResourceAllocator(pool);

        System.out.println("=== Resource Allocator Test ===");

        // Test 1: Initial allocation
        System.out.println("\n=== Initial Allocation ===");

        allocator.allocateForWorkload(
                "Workload-A", "m8-test", 1.0, 512);

        allocator.displayAllocations();
        pool.displayResources();


        // Test 2: Increase resources
        System.out.println("\n=== Resource Increase Test ===");

        allocator.reallocateForWorkload(
                "Workload-A", 2.0, 1024);

        allocator.displayAllocations();
        pool.displayResources();


        // Test 3: Decrease resources
        System.out.println("\n=== Resource Decrease Test ===");

        allocator.reallocateForWorkload(
                "Workload-A", 0.5, 256);

        allocator.displayAllocations();
        pool.displayResources();


        // Test 4: Release resources
        System.out.println("\n=== Resource Release Test ===");

        allocator.releaseForWorkload("Workload-A");

        allocator.displayAllocations();
        pool.displayResources();
    }
}