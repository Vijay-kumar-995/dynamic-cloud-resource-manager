# M9 – Experimental Analysis: Static vs Dynamic Cloud Resource Allocation

## 1. Objective

The objective of Milestone 9 is to evaluate the behaviour of static and dynamic resource allocation using Docker containers controlled by the Java-based Cloud Resource Manager.

The experiments examine:

- CPU usage under fixed and changing CPU limits.
- Memory usage under fixed and changing memory limits.
- The effect of changing container resource limits while a workload is running.
- The extent to which configured resource limits differ from the resources actually used by a workload.

The results are based on the four M9 CSV datasets collected during the experiments.

## 2. Experimental Environment

The experiments were conducted in an Ubuntu virtual machine using Java, Docker, and Linux cgroups.

| Component | Environment |
|---|---|
| Operating system | Ubuntu 24.04.4 LTS |
| Programming language | Java |
| Container runtime | Docker |
| Resource control | Linux cgroups v2 |
| Virtualization | VirtualBox |
| CPU experiment workload | CPU-intensive and idle containers |
| Memory experiment workload | Memory-intensive container |

The CPU and memory experiments were performed separately. Their measurements should therefore be interpreted within their respective experiment setups.

## 3. Experimental Methodology

### 3.1 Static CPU allocation

Two containers were started with fixed resource limits:

| Container | Workload | CPU limit | Memory limit |
|---|---|---:|---:|
| `m9-static-A` | CPU-intensive | 0.5 CPU | 128 MiB |
| `m9-static-B` | Idle | 0.5 CPU | 128 MiB |

Five samples were collected from both containers while the limits remained unchanged.

### 3.2 Dynamic CPU allocation

A CPU-intensive container, `m9-dynamic-A`, was tested through three allocation stages:

| Stage | CPU allocation | Memory limit |
|---|---:|---:|
| Initial | 0.5 CPU | 256 MiB |
| Increased | 1.0 CPU | 512 MiB |
| Decreased | 0.5 CPU | 256 MiB |

The Java resource allocator changed the container's configured limits. CPU usage was sampled during these stages.

### 3.3 Static memory allocation

A memory-intensive container, `m9-memory-static`, was assigned a fixed memory limit of 256 MiB.

Three samples were collected while the workload was running.

### 3.4 Dynamic memory allocation

A memory-intensive container, `m9-memory-dynamic`, was tested through three stages:

| Stage | Memory limit |
|---|---:|
| Initial | 192 MiB |
| Increased | 384 MiB |
| Decreased | 192 MiB |

Ten valid samples were recorded across these stages. The workload's actual memory consumption remained approximately constant during the observations.

## 4. CPU Experiment Analysis

### 4.1 Static CPU allocation results

The static CPU experiment used two containers. The CPU-intensive container consistently used CPU resources, while the idle container reported zero CPU usage.

| Container | Workload | Number of samples | Average CPU usage |
|---|---|---:|---:|
| `m9-static-A` | CPU-intensive | 5 | 53.71% |
| `m9-static-B` | Idle | 5 | 0.00% |

The average for the CPU-intensive container was calculated as:

\[
\text{Average CPU usage} =
\frac{58.38 + 52.37 + 52.30 + 52.51 + 52.99}{5}
= 53.71\%
\]

**Observations:**

- The CPU-intensive container showed an average reported CPU usage of 53.71%.
- The idle container consistently reported 0.00% CPU usage.
- Both containers retained their fixed 0.5 CPU limits throughout the experiment.
- The idle container had an available CPU quota that it did not use during the observation period.

The unused quota of an idle container represents unused allowed capacity. It should not be interpreted as a physical CPU core exclusively reserved by Docker.

### 4.2 Dynamic CPU allocation results

The dynamic CPU experiment recorded the following values:

| Stage | CPU allocation | Samples | Average reported CPU usage |
|---|---:|---:|---:|
| Initial | 0.5 CPU | 1 | 125.31%* |
| Increased | 1.0 CPU | 4 | 112.39% |
| Decreased | 0.5 CPU | 6 | 56.05% |

The increased-stage average is:

\[
\frac{114.88 + 111.03 + 110.11 + 113.55}{4}
= 112.39\%
\]

The decreased-stage average is:

\[
\frac{56.08 + 56.34 + 54.89 + 54.97 + 58.60 + 55.43}{6}
= 56.05\%
\]

\* **Initial-stage anomaly:** The initial sample reported 125.31% CPU usage with a configured limit of 0.5 CPU. This reading is inconsistent with the configured quota and is based on only one sample. It is retained in the raw dataset but should not be treated as a reliable representative average or used as evidence of normal initial-stage behaviour.

**Observations:**

- The Java allocator successfully changed the configured CPU allocation from 0.5 CPU to 1.0 CPU and then back to 0.5 CPU.
- The average reported CPU usage during the increased stage was 112.39%.
- During the decreased stage, the average reported CPU usage was 56.05%.
- The readings show different CPU usage levels across allocation stages, but CPU usage is workload demand/consumption, not the amount of CPU allocated.
- The initial reading requires caution because it is anomalous.

### 4.3 Static vs dynamic CPU comparison

| Metric | Static experiment | Dynamic experiment |
|---|---:|---:|
| CPU-intensive workload | Average 53.71% | Increased stage: 112.39%; decreased stage: 56.05% |
| Idle workload | Average 0.00% | Not included |
| CPU allocation | Fixed at 0.5 CPU per container | Changed from 0.5 to 1.0 and back to 0.5 CPU |
| Allocation changes | None | Two changes |

**Interpretation:**

The static experiment demonstrates that an idle container can have a configured CPU quota while making no use of CPU during the measured period. The dynamic experiment demonstrates that the Java resource allocator can change a running container's CPU limit.

The CPU percentages should not be compared as if they were percentages of the configured CPU quota. Docker's CPU percentage is reported relative to CPU capacity and can exceed 100%. The initial dynamic sample of 125.31% is also anomalous. Therefore, the data demonstrates resource-limit changes, but does not by itself establish a precise improvement in CPU efficiency over static allocation.

## 5. Memory Experiment Analysis

### 5.1 Static memory allocation results

The static memory workload was assigned a fixed limit of 256 MiB.

| Sample | Memory limit | Actual memory usage | Utilization |
|---:|---:|---:|---:|
| 1 | 256 MiB | 104.4 MiB | 40.80% |
| 2 | 256 MiB | 104.4 MiB | 40.80% |
| 3 | 256 MiB | 104.4 MiB | 40.80% |
| **Average** | **256 MiB** | **104.4 MiB** | **40.80%** |

The average memory usage is:

\[
\frac{104.4 + 104.4 + 104.4}{3} = 104.4\text{ MiB}
\]

The utilization is:

\[
\text{Memory utilization} =
\frac{104.4}{256} \times 100 = 40.78\%
\]

The CSV reports 40.80%, which is the value retained in the recorded observations. The small difference is due to rounding or the reporting precision of Docker.

**Observations:**

- Memory usage remained stable at 104.4 MiB across all three samples.
- The configured memory limit remained fixed at 256 MiB.
- Reported memory utilization remained at 40.80%.

### 5.2 Dynamic memory allocation results

The dynamic memory experiment produced the following readings:

| Stage | Memory limit | Number of samples | Average actual usage | Recorded utilization |
|---|---:|---:|---:|---:|
| Initial | 192 MiB | 2 | 104.4 MiB | 54.40% |
| Increased | 384 MiB | 5 | 104.4 MiB | 27.20% |
| Decreased | 192 MiB | 3 | 104.4 MiB | 54.40% |

The actual memory usage remained constant at approximately 104.4 MiB while the configured limit changed.

**Observations:**

- The initial memory limit was 192 MiB.
- The Java allocator increased the limit to 384 MiB.
- The allocator subsequently decreased the limit to 192 MiB.
- Actual memory usage remained at 104.4 MiB in all recorded samples.
- When the limit doubled from 192 MiB to 384 MiB, reported utilization fell from 54.40% to 27.20%.
- When the limit returned to 192 MiB, reported utilization returned to 54.40%.

This demonstrates that the container's memory limit was changed while the workload continued running.

### 5.3 Static vs dynamic memory comparison

| Metric | Static memory | Dynamic memory |
|---|---:|---:|
| Configured limit | Fixed at 256 MiB | 192 → 384 → 192 MiB |
| Average actual usage | 104.4 MiB | 104.4 MiB across all stages |
| Utilization | 40.80% recorded | 54.40% → 27.20% → 54.40% |
| Number of samples | 3 | 10 |
| Allocation changes | None | Increased and decreased |

**Interpretation:**

The static experiment maintained a constant memory limit. The dynamic experiment changed the limit according to the experiment stages while actual workload consumption remained stable.

The increase to 384 MiB reduced the measured utilization percentage because the same approximately 104.4 MiB of memory was being measured against a larger limit. Returning the limit to 192 MiB restored the original utilization percentage.

This is evidence of successful memory-limit adjustment, not evidence that the workload itself consumed less memory.

## 6. Memory Headroom Analysis

For an additional comparison, the difference between the configured memory limit and actual memory usage can be calculated:

\[
\text{Memory headroom} = \text{Memory limit} - \text{Actual memory usage}
\]

| Allocation stage | Memory limit | Actual usage | Headroom | Headroom as % of limit |
|---|---:|---:|---:|---:|
| Static | 256 MiB | 104.4 MiB | 151.6 MiB | 59.22% |
| Dynamic – Initial | 192 MiB | 104.4 MiB | 87.6 MiB | 45.63% |
| Dynamic – Increased | 384 MiB | 104.4 MiB | 279.6 MiB | 72.81% |
| Dynamic – Decreased | 192 MiB | 104.4 MiB | 87.6 MiB | 45.63% |

**Important clarification:** Memory headroom here means the difference between the container's configured memory limit and its measured usage. A Docker memory limit is a maximum usage constraint; it does not mean that the entire limit is physically reserved for the container. Therefore, headroom is an indicator of unused limit capacity, not a direct measurement of physical RAM wastage.

## 7. Overall Findings

The four datasets support the following findings:

1. **Static allocation remains unchanged:** In the static CPU and memory experiments, container resource limits stayed fixed during measurement.
2. **Dynamic CPU adjustment worked:** The Java resource allocator changed the CPU limit from 0.5 CPU to 1.0 CPU and then back to 0.5 CPU.
3. **Dynamic memory adjustment worked:** The Java resource allocator changed the memory limit from 192 MiB to 384 MiB and then back to 192 MiB.
4. **Idle CPU capacity was unused:** The static idle container reported 0.00% CPU usage in all five samples despite having a configured CPU limit.
5. **Memory consumption remained stable:** The memory-intensive workload used approximately 104.4 MiB in both static and dynamic experiments.
6. **Utilization depends on the configured limit:** In the dynamic memory experiment, the utilization percentage changed when the memory limit changed, even though actual memory usage remained constant.
7. **Resources were returned to the pool in the Java experiment:** The dynamic allocation test included a release operation after the allocation stages. The resource pool reported its full available capacity after release.

## 8. Limitations and Experimental Considerations

The following limitations should be considered when interpreting the results:

- The static memory experiment contains three samples, while the dynamic memory experiment contains ten samples. The sample counts are not equal.
- The static memory limit was 256 MiB, whereas the dynamic memory limit changed between 192 MiB and 384 MiB. The experiments therefore do not use identical limits.
- The dynamic CPU experiment has only one initial-stage reading. Its value of 125.31% is anomalous relative to the configured 0.5 CPU limit.
- CPU utilization percentages are reported by Docker and should not be interpreted directly as a percentage of the configured CPU quota.
- The experiments were conducted in a virtual machine, so measurements may be affected by virtualization and other system activity.
- The memory workload used approximately the same amount of memory in each stage. The experiment tests limit adjustment, not changing memory demand.
- The current implementation performs reallocation through explicit Java method calls. It does not automatically detect workload demand and independently decide when to reallocate resources.
- The experiments do not provide a controlled measurement of workload completion time or application throughput. No performance improvement in execution time can therefore be claimed from these datasets alone.

## 9. Conclusion

The M9 experiments successfully demonstrated the core resource-management operations of the Dynamic Cloud Resource Pooling and Allocation System.

In the static experiments, CPU and memory limits remained fixed even when the workload did not use all of the available limit capacity. In the dynamic experiments, the Java-based resource allocator successfully increased and decreased Docker container CPU and memory limits while the workloads were running.

The memory experiment showed that actual memory usage remained approximately 104.4 MiB while the configured limit changed from 192 MiB to 384 MiB and back to 192 MiB. Consequently, measured utilization changed from 54.40% to 27.20% and returned to 54.40%.

The CPU experiment also demonstrated configurable CPU-limit changes, although one initial reading was anomalous and should be treated cautiously.

Overall, the results validate the implementation of dynamic resource allocation, reallocation, and resource reclamation. They provide evidence that the system can adjust container resource limits through Java and maintain a shared resource pool. However, further controlled experiments with equal sample counts, repeated workload-demand patterns, and execution-time measurements would be required to quantify improvements in overall resource efficiency and workload performance.

---

## 10. Dataset Reference

The analysis is based on these four experiment files:

| Dataset | Description |
|---|---|
| `m9-static-cpu-util.csv` | CPU usage of static CPU-intensive and idle containers |
| `m9-dynamic-cpu-util.csv` | CPU usage across dynamic CPU allocation stages |
| `m9-static-memery-util.csv` | Memory usage under a fixed memory limit |
| `m9-dynamic-memory-util.csv` | Memory usage across dynamic memory allocation stages |

*Note: The filename `m9-static-memery-util.csv` is retained as supplied. If the repository filename is later corrected to `m9-static-memory-util.csv`, update this reference accordingly.*
