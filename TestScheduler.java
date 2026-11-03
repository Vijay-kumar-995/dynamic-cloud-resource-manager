public class TestScheduler {

    public static void main(String[] args) {

        // Initialize the resource pool
        ResourcePool pool = new ResourcePool(4.0, 4096);

        // Initialize the resource allocator
        ResourceAllocator allocator = new ResourceAllocator(pool);

        // Initialize the scheduler
        Scheduler scheduler = new Scheduler(allocator);

        System.out.println("====== CLOUD RESOURCE SCHEDULER TEST ======");

        // Schedule workloads
        scheduler.scheduleWorkload("Workload-A", 1.0, 512);
        scheduler.scheduleWorkload("Workload-B", 2.0, 1024);
        scheduler.scheduleWorkload("Workload-C", 2.0, 1024);

        // Display current allocations
        allocator.displayAllocations();

        // Display resource pool status
        System.out.println("\n====== RESOURCE POOL STATUS ======");
        pool.displayResources();

        // Release a workload
        System.out.println("\n====== WORKLOAD RELEASE TEST ======");
        scheduler.releaseWorkload("Workload-A");

        // Display updated status
        allocator.displayAllocations();
        pool.displayResources();
    }
}