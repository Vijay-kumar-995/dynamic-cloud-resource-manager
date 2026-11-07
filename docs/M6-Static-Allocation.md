# M6 — Static Resource Allocation

## Objective
Study fixed resource allocation in Docker containers and identify resource wastage.

## Environment
- OS: Ubuntu 24.04
- CPU cores: 4
- Available RAM before experiment: 1.8 GiB
- Docker: Installed and running

## Configuration

| Container | CPU Limit | Memory Limit |
|---|---:|---:|
| static-A | 0.5 CPU | 128 MiB |
| static-B | 0.5 CPU | 128 MiB |

## Observations

| Metric | static-A | static-B |
|---|---:|---:|
| CPU usage (first reading) | 85.62% | 0.00% |
| CPU usage (second reading) | 0.00% | 0.00% |
| Memory usage | 580 KiB | 436 KiB |
| Memory limit | 128 MiB | 128 MiB |

## Allocation Verification

Docker inspection confirmed:
- CPU NanoCpus: 500000000 per container
- Memory limit: 134217728 bytes per container

## Analysis
Both containers received identical fixed resource limits despite having different workload requirements.

The combined memory allocation was 256 MiB, while observed memory usage was approximately 1 MiB.

This demonstrates substantial unused memory capacity.

## Conclusion
Static allocation provides predictable resource limits but cannot automatically adjust allocations according to workload demand.

This experiment establishes a baseline for comparison with dynamic resource allocation.