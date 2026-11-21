public class TestM9CpuDynamic {

    public static void main(String[] args) throws InterruptedException {

        ResourcePool pool = new ResourcePool(4.0, 4096);
        ResourceAllocator allocator = new ResourceAllocator(pool);

        System.out.println("========== M9 DYNAMIC ALLOCATION EXPERIMENT ==========");

        System.out.println("\n--- STEP 1: INITIAL ALLOCATION ---");

        allocator.allocateForWorkload(
            "M9-Workload-A", "m9-dynamic-A", 0.5, 256
        );

        allocator.displayAllocations();
        pool.displayResources();

        System.out.println("\nCollect Docker statistics now. Waiting 30 seconds...");
        Thread.sleep(30000);

        System.out.println("\n--- STEP 2: INCREASE ALLOCATION ---");

        allocator.reallocateForWorkload("M9-Workload-A", 1.0, 512);

        allocator.displayAllocations();
        pool.displayResources();

        System.out.println("\nCollect Docker statistics now. Waiting 30 seconds...");
        Thread.sleep(30000);

        System.out.println("\n--- STEP 3: DECREASE ALLOCATION ---");

        allocator.reallocateForWorkload("M9-Workload-A", 0.5, 256);

        allocator.displayAllocations();
        pool.displayResources();

        System.out.println("\nCollect Docker statistics now. Waiting 30 seconds...");
        Thread.sleep(30000);

        System.out.println("\n--- STEP 4: RELEASE ALLOCATION ---");

        allocator.releaseForWorkload("M9-Workload-A");

        allocator.displayAllocations();
        pool.displayResources();

        System.out.println("\n========== M9 EXPERIMENT COMPLETED ==========");
    }
}