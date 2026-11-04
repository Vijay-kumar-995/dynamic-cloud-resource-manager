# M5 — Docker Workload Experiments

## Objective
To understand how Docker runs workloads with controlled CPU and memory limits and how resource consumption can be monitored.

## Environment
- Operating System: Ubuntu 24.04 LTS
- Docker Version: 29.1.3
- CPU Cores: 4
- Total RAM: 4.7 GiB
- Cgroup Version: 2

## Experiment 1: CPU-intensive workload

Created an Ubuntu container with the following configuration:

- Container Name: `m5-cpu-workload`
- Docker Image: `ubuntu:24.04`
- CPU Limit: 0.5 core
- Memory Limit: 256 MiB

A continuous loop was executed to generate CPU activity.

Resource usage was monitored using:

`docker stats --no-stream m5-cpu-workload`

An observed snapshot showed CPU utilization of 67.83%.

## Experiment 2: Memory-intensive workload

Created a Python container with the following configuration:

- Container Name: `m5-memory-workload`
- Docker Image: `python:3.12-alpine`
- Memory Limit: 192 MiB
- Memory-Swap Limit: 192 MiB
- Memory Requested by Program: Approximately 128 MiB

Observed resource usage:

- Memory Usage: 131.8 MiB
- Memory Limit: 192 MiB
- Memory Utilization: 68.66%

## Verification

Docker resource limits were inspected using `docker inspect`.

The memory limit was verified as 201326592 bytes (192 MiB).

The memory-swap limit was also verified as 201326592 bytes (192 MiB).

## Concepts Learned

1. Running workloads inside Docker containers.
2. Applying CPU limits using Docker.
3. Applying memory limits using Docker.
4. Monitoring resource consumption using `docker stats`.
5. Verifying container configurations using `docker inspect`.
6. Stopping and removing experimental containers.

## Conclusion

The experiments demonstrated how Docker can run isolated workloads with configured CPU and memory limits.

These concepts provide the foundation for integrating Docker workloads with the Java-based Cloud Resource Manager.