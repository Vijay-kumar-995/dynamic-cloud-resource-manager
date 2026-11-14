# M8 – Dynamic Resource Allocation, Reallocation and Reclamation

## 1. Objective

The objective of Milestone 8 is to implement dynamic resource
management for Docker containers.

The system can allocate CPU and memory resources, increase or
decrease allocations according to workload requirements, and
reclaim resources when a workload finishes.

## 2. Components Implemented

### ResourcePool.java
- Maintains total and available CPU and memory.
- Allocates resources when sufficient capacity is available.
- Releases resources when workloads finish or allocations decrease.
- Prevents invalid requests and resource over-release.
- Uses synchronized methods for safer concurrent access.

### DockerController.java
- Executes Docker commands using Java ProcessBuilder.
- Updates container CPU and memory limits.
- Checks whether a container is running.
- Stops containers during resource reclamation.
- Handles Docker command errors.

### ResourceAllocator.java
- Connects the ResourcePool with DockerController.
- Allocates resources to workloads.
- Reallocates resources when requirements change.
- Releases resources after workload completion.
- Rolls back additional reservations when Docker updates fail.
- Maintains workload allocation records.

### Scheduler.java
- Receives workload scheduling requests.
- Delegates allocation and release operations to ResourceAllocator.

## 3. Allocation Workflow

1. A workload requests CPU and memory.
2. The allocator validates the request.
3. The system checks whether the Docker container is running.
4. ResourcePool checks available capacity.
5. Resources are reserved in the pool.
6. DockerController applies the resource limits.
7. The allocation is recorded.

If the Docker update fails, the additional reservation is rolled back.

## 4. Reallocation Workflow

1. The workload requests new CPU and memory limits.
2. The allocator calculates the resource difference.
3. Additional resources are reserved if required.
4. DockerController updates the container limits.
5. Resources no longer required are returned to the pool.

If the Docker update fails, additional reservations are rolled back.

## 5. Resource Reclamation Workflow

1. The allocator finds the workload allocation.
2. It checks whether the container is running.
3. If running, the container is stopped.
4. Reserved resources are returned to ResourcePool.
5. The workload allocation record is removed.

## 6. Testing and Verification

### Failure Tests

| Test | Expected Result | Observed Result |
|---|---|---|
| Nonexistent container | Reject request | Passed |
| Invalid CPU (NaN) | Reject request | Passed |
| Invalid memory (zero) | Reject request | Passed |
| Insufficient resources | Reject allocation | Passed |
| Excessive resource release | Reject release | Passed |

### Normal Allocation Tests

The following operations were tested using Docker container m8-test.

| Operation | CPU Limit | Memory Limit |
|---|---:|---:|
| Initial allocation | 1.0 CPU | 512 MB |
| Resource increase | 2.0 CPU | 1024 MB |
| Resource decrease | 0.5 CPU | 256 MB |
| Resource release | Reclaimed | Reclaimed |

After the final release:
- No active workload allocations remained.
- Available CPU returned to 4.0.
- Available memory returned to 4096 MB.
- The Docker container was stopped successfully.

## 7. Limitations

- Resource allocation records are maintained in memory.
- Allocation state is not persistent after application restart.
- Resource changes currently depend on explicit allocation requests.
- The system does not yet implement automatic demand prediction.
- Resource accounting is designed for a single manager instance.
- Docker memory limits represent container usage caps, not physically
  reserved host RAM.

## 8. Conclusion

Milestone 8 successfully implements dynamic resource allocation,
reallocation and reclamation for Docker workloads.

The system can modify container CPU and memory limits at runtime,
manage available resource capacity, handle invalid requests and
reclaim resources after workload completion.

The implementation is ready for experimental evaluation in M9.