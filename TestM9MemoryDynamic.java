public class TestM9MemoryDynamic {
    public static void main(String[] args) throws InterruptedException {

        ResourcePool pool = new ResourcePool(4.0, 4096);
        ResourceAllocator allocator = new ResourceAllocator(pool);

        System.out.println("===== DYNAMIC MEMORY EXPERIMENT =====");

        // Initial allocation
        System.out.println("\n--- INITIAL: 192 MB ---");
        allocator.allocateForWorkload(
            "Memory-Workload",
            "m9-memory-dynamic",
            0.5,
            192
        );

        System.out.println("Collect Docker stats now. Waiting 20 seconds...");
        Thread.sleep(20000);

        // Increase memory
        System.out.println("\n--- INCREASE: 384 MB ---");
        allocator.reallocateForWorkload(
            "Memory-Workload", 0.5, 384
        );

        System.out.println("Collect Docker stats now. Waiting 20 seconds...");
        Thread.sleep(20000);

        // Decrease memory
        System.out.println("\n--- DECREASE: 192 MB ---");
        allocator.reallocateForWorkload(
            "Memory-Workload", 0.5, 192
        );

        System.out.println("Collect Docker stats now. Waiting 20 seconds...");
        Thread.sleep(20000);

        // Release
        System.out.println("\n--- RELEASE ---");
        allocator.releaseForWorkload("Memory-Workload");

        pool.displayResources();

        System.out.println("\n===== EXPERIMENT COMPLETED =====");
    }
}