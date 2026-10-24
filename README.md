# Dynamic Cloud Resource Pooling and Allocation System

A Java-based project for monitoring, managing, and dynamically allocating CPU and memory resources to workloads running in Docker containers.

## Overview

Cloud workloads do not always require the same amount of computing resources. Assigning fixed CPU and memory limits can result in underutilized resources for some workloads and insufficient resources for others.

This project explores a resource management approach that maintains a shared pool of CPU and memory resources and allocates them according to workload requirements.

The long-term goal is to develop a Java-based resource manager that monitors resource usage, manages allocation decisions, and supports dynamic resource allocation, reallocation, and reclamation.

## Problem Statement

Static resource allocation assigns predetermined CPU and memory limits to workloads. When workload requirements change, these fixed allocations can lead to:

- Underutilization of available resources.
- Inefficient CPU and memory usage.
- Reduced flexibility when workload demands change.
- Limited opportunities to redistribute unused resources.

## Objectives

- Monitor CPU and memory usage on Linux.
- Explore Docker containers and Linux control groups (cgroups).
- Maintain a shared pool of available CPU and memory resources.
- Develop scheduling logic for resource requests.
- Implement resource allocation and release mechanisms.
- Explore dynamic resource reallocation and reclamation.
- Evaluate resource utilization under different allocation strategies.

## Proposed Architecture

```text
           Workloads / Users
                  |
                  v
       Java Resource Manager
                  |
          +-------+-------+
          |       |       |
          v       v       v
      Resource  Scheduler Resource
       Monitor            Allocator
          |       |       |
          +-------+-------+
                  |
                  v
        Shared Resource Pool
          (CPU and Memory)
                  |
                  v
          Docker Containers
                  |
                  v
             Linux cgroups
                  |
                  v
              Linux Kernel
```

**Note:** This represents the proposed architecture. Some components are still under development, and the Java resource pool currently manages resource availability in application logic rather than directly changing Docker container limits.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java | Resource management logic |
| Docker | Containerized workloads |
| Linux | Operating system and resource environment |
| Linux cgroups | Container resource-control mechanisms |
| Ubuntu | Development and testing environment |
| VirtualBox | Virtual machine environment |
| Git and GitHub | Version control and project documentation |

## Current Implementation

- Java development environment configured.
- Docker installed and tested.
- Linux cgroups v2 environment explored.
- CPU monitoring implemented using Linux system information.
- Memory monitoring implemented using `/proc/meminfo`.
- Basic Java resource monitor implemented.
- Resource pool implemented with CPU and memory accounting.
- Allocation, release, and invalid-release scenarios tested.

## Project Roadmap

### Milestones 1–6: Environment and Monitoring

- [x] Project requirements and resource allocation concepts
- [x] Java development environment setup
- [x] Docker setup and container testing
- [x] Linux cgroups exploration
- [x] CPU and memory monitoring
- [x] Basic resource monitoring implementation

### Milestone 7: Resource Pooling and Allocation Logic

- [x] Resource pool implementation
- [x] Basic allocation and release operations
- [x] Validation of resource requests
- [ ] Scheduler implementation
- [ ] Resource allocator implementation
- [ ] Integrated testing of the resource management components

### Milestone 8: Dynamic Resource Management

- [ ] Docker workload management
- [ ] Dynamic resource allocation
- [ ] Resource reallocation
- [ ] Resource reclamation
- [ ] Integration with Docker and Linux cgroups

### Milestone 9: Experiments and Evaluation

- [ ] Static allocation baseline
- [ ] Workload experiments
- [ ] Resource utilization measurements
- [ ] Comparison of allocation strategies
- [ ] Metrics and visualization

### Milestone 10: Finalization

- [ ] Integration testing
- [ ] Error handling and validation
- [ ] Final documentation
- [ ] Results and performance analysis
- [ ] Project presentation

## Project Structure

```text
cloud-resource-manager/
├── Main.java
├── ResourceMonitor.java
├── ResourcePool.java
├── TestCPU.java
├── TestProc.java
├── README.md
├── ROADMAP.md
└── .gitignore
```

Additional Java classes will be introduced as the scheduler and resource allocator are implemented.

## Getting Started

### Prerequisites

- Ubuntu or another compatible Linux environment.
- Java Development Kit (JDK).
- Docker for container-related experiments.

### Clone the Repository

```bash
git clone https://github.com/Vijay-kumar-995/dynamic-cloud-resource-manager.git
cd dynamic-cloud-resource-manager
```

### Compile the Java Files

Compile the main project and its current supporting classes:

```bash
javac Main.java ResourceMonitor.java ResourcePool.java TestCPU.java TestProc.java
```

### Run the Main Program

```bash
java Main
```

To run the resource pool tests, use the appropriate test class and its `main` method, if present:

```bash
java ResourcePool
```

The last command applies only if `ResourcePool.java` contains a runnable `main` method. Otherwise, run the class that contains your resource pool test code.

## Current Limitations

- The resource pool currently tracks CPU and memory availability in Java.
- Resource allocation decisions are not yet fully integrated with Docker container limits.
- The scheduler and resource allocator are still being developed.
- Dynamic reallocation and reclamation remain future implementation goals.
- Performance comparisons and workload experiments have not yet been completed.

## Future Work

The project will progress toward an integrated resource manager that connects workload monitoring, scheduling, resource accounting, and Docker resource controls. Later experiments will evaluate resource utilization and compare the proposed approach with static allocation.

## License

A license has not yet been selected for this project.
