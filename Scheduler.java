public class Scheduler {

    private ResourceAllocator allocator;

    public Scheduler(ResourceAllocator allocator) {
        this.allocator = allocator;
    }

    public boolean scheduleWorkload(String workloadName, double cpu, long memoryMB) {

        System.out.println("\n=== Scheduling Workload ===");
        System.out.println("Workload: " + workloadName);
        System.out.println("Requested CPU: " + cpu);
        System.out.println("Requested Memory: " + memoryMB + " MB");

        boolean scheduled = allocator.allocateForWorkload(
                workloadName,
                cpu,
                memoryMB
        );

        if (scheduled) {
            System.out.println("Scheduling successful for " + workloadName + ".");
        } else {
            System.out.println("Scheduling failed for " + workloadName + ".");
        }

        return scheduled;
    }

    public boolean releaseWorkload(String workloadName) {
        return allocator.releaseForWorkload(workloadName);
    }
}