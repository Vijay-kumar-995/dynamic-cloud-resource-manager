public class TestM8Failures {

    public static void main(String[] args) {

        ResourcePool pool = new ResourcePool(1.0, 256);
        ResourceAllocator allocator = new ResourceAllocator(pool);

        System.out.println("=== M8 Failure Tests ===");

        System.out.println("\n1. Invalid container test:");
        System.out.println("Result: " +
                allocator.allocateForWorkload(
                        "Test-A", "nonexistent-container", 0.5, 128));

        System.out.println("\n2. Invalid CPU test (NaN):");
        System.out.println("Result: " +
                allocator.allocateForWorkload(
                        "Test-B", "test-container", Double.NaN, 128));

        System.out.println("\n3. Invalid memory test (zero):");
        System.out.println("Result: " +
                allocator.allocateForWorkload(
                        "Test-C", "test-container", 0.5, 0));

        System.out.println("\n4. Insufficient resource test:");

        ResourcePool limitedPool = new ResourcePool(1.0, 256);

        limitedPool.allocateResources(1.0, 256);

        System.out.println("Extra allocation result: " +
                limitedPool.allocateResources(0.5, 128));

        System.out.println("\n5. Excessive release test:");

        System.out.println("Result: " +
                pool.releaseResources(0.1, 1));
    }
}