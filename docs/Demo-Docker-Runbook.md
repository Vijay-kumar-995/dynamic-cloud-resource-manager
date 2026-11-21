# Demonstration Runbook
## Dynamic Cloud Resource Pooling and Allocation System

This runbook describes how to demonstrate static Docker resource limits and Java-controlled dynamic CPU and memory allocation on Ubuntu 24.04.

**Prerequisites:** Java 21, Docker, and the project source files must be available.

> Run all commands inside the Ubuntu VM. Use either `docker` or `sudo docker` consistently, depending on Docker permissions.

---

## 1. Prepare the Environment

```bash
java -version
javac -version
docker --version
docker ps
```

Navigate to the project directory and compile the Java files:

```bash
cd ~/cloud-resource-manager
javac *.java
```

Confirm that the required test files exist:

```bash
ls TestM9Dynamic.java TestM9MemoryDynamic.java
```

Remove any old demonstration containers:

```bash
docker rm -f demo-static-cpu-busy demo-static-cpu-idle demo-static-memory m9-dynamic-A m9-memory-dynamic 2>/dev/null || true
```

---

# PART A — STATIC ALLOCATION

## 2. Create Static CPU Containers

**CPU-intensive container:**

```bash
docker run -d \
  --name demo-static-cpu-busy \
  --cpus="0.5" \
  --memory="128m" \
  --memory-swap="256m" \
  ubuntu:24.04 \
  bash -c "timeout 300s yes > /dev/null"
```

**Idle container:**

```bash
docker run -d \
  --name demo-static-cpu-idle \
  --cpus="0.5" \
  --memory="128m" \
  --memory-swap="256m" \
  ubuntu:24.04 \
  sleep infinity
```

## 3. Inspect Static CPU Allocation

Check configured limits:

```bash
docker inspect --format='Name={{.Name}} | NanoCPUs={{.HostConfig.NanoCpus}} | Memory={{.HostConfig.Memory}} bytes | Status={{.State.Status}}' demo-static-cpu-busy demo-static-cpu-idle
```

View actual resource usage:

```bash
docker stats --no-stream demo-static-cpu-busy demo-static-cpu-idle
```

Both containers have the same configured limits, but their actual CPU usage should differ because their workloads are different.

## 4. Create a Static Memory Container

```bash
docker run -d \
  --name demo-static-memory \
  --cpus="0.5" \
  --memory="256m" \
  --memory-swap="512m" \
  python:3.12-slim \
  python -c "import time; data=bytearray(100*1024*1024); print('Allocated approximately 100 MiB', flush=True); time.sleep(300)"
```

Inspect its configured limit and actual usage:

```bash
docker inspect --format='Memory={{.HostConfig.Memory}} bytes | Status={{.State.Status}}' demo-static-memory
docker stats --no-stream demo-static-memory
```

**Static allocation observation:** Resource limits are configured when containers are created and remain unchanged during this demonstration. Actual usage may be lower than the configured limits.

## 5. Remove Static Containers

```bash
docker rm -f demo-static-cpu-busy demo-static-cpu-idle demo-static-memory
```

Verify:

```bash
docker ps -a
```

---

# PART B — JAVA-CONTROLLED DYNAMIC ALLOCATION

Dynamic allocation is performed through explicit Java calls to the resource allocator and Docker controller. It is not autonomous workload-demand detection.

Use two terminals:
- **Terminal 1:** Run Java test programs.
- **Terminal 2:** Inspect Docker containers and resource limits.

## 6. Dynamic CPU Allocation

The CPU test expects a container named `m9-dynamic-A`. Create it before running the Java program:

```bash
docker run -d \
  --name m9-dynamic-A \
  --cpus="0.5" \
  --memory="256m" \
  --memory-swap="512m" \
  ubuntu:24.04 \
  bash -c "timeout 600s yes > /dev/null"
```

**Terminal 1 — Run the Java test:**

```bash
cd ~/cloud-resource-manager
java TestM9Dynamic
```

**Terminal 2 — Inspect the container:**

```bash
docker ps
docker inspect --format='CPU={{.HostConfig.NanoCpus}} | Memory={{.HostConfig.Memory}} bytes' m9-dynamic-A
docker stats --no-stream m9-dynamic-A
```

Check the limits at each stage of the Java test.

| Stage | CPU limit | Memory limit |
|---|---:|---:|
| Initial | 0.5 CPU | 256 MiB |
| Increased | 1 CPU | 512 MiB |
| Decreased | 0.5 CPU | 256 MiB |

Expected `docker inspect` values:

| Stage | NanoCPUs | Memory (bytes) |
|---|---:|---:|
| Initial / Decreased | `500000000` | `268435456` |
| Increased | `1000000000` | `536870912` |

The test may move between stages quickly. Inspect the container while the Java program is running.

## 7. Dynamic Memory Allocation

Before running the memory test, check `TestM9MemoryDynamic.java` for the exact container name it expects. The following creation command uses `m9-memory-dynamic`:

```bash
docker run -d \
  --name m9-memory-dynamic \
  --cpus="0.5" \
  --memory="192m" \
  --memory-swap="384m" \
  python:3.12-slim \
  python -c "import time; data=bytearray(100*1024*1024); print('Allocated approximately 100 MiB', flush=True); time.sleep(900)"
```

**Terminal 1 — Run the Java test:**

```bash
cd ~/cloud-resource-manager
java TestM9MemoryDynamic
```

**Terminal 2 — Inspect the container:**

```bash
docker ps
docker inspect --format='Memory={{.HostConfig.Memory}} bytes' m9-memory-dynamic
docker stats --no-stream m9-memory-dynamic
```

Expected memory limits:

| Stage | Memory limit | Memory (bytes) |
|---|---:|---:|
| Initial | 192 MiB | `201326592` |
| Increased | 384 MiB | `402653184` |
| Decreased | 192 MiB | `201326592` |

The workload's actual memory usage may remain approximately constant while its configured limit changes.

---

# PART C — CLEANUP

## 8. Remove Dynamic Test Containers

After both tests finish:

```bash
docker rm -f m9-dynamic-A m9-memory-dynamic 2>/dev/null || true
```

Check remaining containers:

```bash
docker ps -a
```

Only remove containers belonging to this project demonstration. Avoid commands that remove every Docker container on the system.

---

# PART D — VERIFICATION AND KEY CONCEPTS

## 9. Useful Docker Commands

| Purpose | Command |
|---|---|
| List running containers | `docker ps` |
| List all containers | `docker ps -a` |
| View live usage | `docker stats CONTAINER_NAME` |
| Take a stats snapshot | `docker stats --no-stream CONTAINER_NAME` |
| Inspect configuration | `docker inspect CONTAINER_NAME` |
| Inspect CPU and memory limits | `docker inspect --format='CPU={{.HostConfig.NanoCpus}} Memory={{.HostConfig.Memory}}' CONTAINER_NAME` |
| View logs | `docker logs CONTAINER_NAME` |
| Stop and remove | `docker rm -f CONTAINER_NAME` |

## 10. Important Concepts

- **Allocation limit:** The maximum CPU or memory resource a container is allowed to use.
- **Utilization:** The amount of CPU or memory the workload is actually using.
- **Static allocation:** Limits are configured at container creation and remain fixed in this demonstration.
- **Dynamic allocation:** Java updates a running container's resource limits through the Docker controller.
- **Docker stats:** Shows actual resource usage.
- **Docker inspect:** Shows configured resource limits.

Memory limits are caps, not necessarily physical RAM reserved for a container. CPU percentages reported by Docker stats should also not be interpreted as a direct percentage of the configured CPU quota.

## 11. System Demonstration Summary

| Feature | Static allocation | Dynamic allocation |
|---|---|---|
| Resource limits | Fixed | Updated at runtime |
| CPU example | 0.5 CPU | 0.5 → 1.0 → 0.5 CPU |
| Memory example | Fixed limits | 192 → 384 → 192 MiB |
| Controller | Docker configuration | Java resource allocator and Docker controller |
| Verification | `docker inspect`, `docker stats` | Java output, `docker inspect`, `docker stats` |

**Implementation scope:** The current system demonstrates explicit Java-controlled runtime reallocation. It does not yet autonomously detect workload demand and decide when to resize containers.