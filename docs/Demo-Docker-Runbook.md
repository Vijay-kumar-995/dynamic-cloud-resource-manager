# Demonstration Runbook
## Dynamic Cloud Resource Pooling and Allocation System

This document is a chronological, copy-paste-ready guide for demonstrating **static allocation** and **Java-controlled dynamic allocation** using Docker on the Ubuntu 24.04 VirtualBox VM.

> **Important:** Run these commands inside the Ubuntu VM, not Windows PowerShell. Keep Docker Desktop/other Docker environments separate from the Ubuntu VM used for this project.

---

## 1. demonstration

You will show two approaches:

| Demonstration | What happens | What you will show |
|---|---|---|
| Static allocation | Containers start with fixed CPU and memory limits. The limits do not change during the demonstration. | `docker inspect` and `docker stats` |
| Dynamic CPU allocation | The Java test program changes a container's CPU and memory limits from 0.5 CPU / 256 MB to 1 CPU / 512 MB, then back to 0.5 CPU / 256 MB. | Java program output, `docker inspect`, and `docker stats` |
| Dynamic memory allocation | The Java test program changes the memory limit from 192 MB to 384 MB, then back to 192 MB. | Java program output, `docker inspect`, and `docker stats` |

The dynamic demonstration is **Java-controlled**: the test program explicitly calls the allocator to change the limits. Do not describe this particular demo as an autonomous AI/automatic demand-detection system.

### Important concept to explain

Docker's `--cpus` and `--memory` values are **resource limits/caps**, not physical resources permanently reserved for a container. A container may use less than its configured limit. The demonstration shows that static limits remain fixed while the Java allocator can update the limits at runtime.

---

## 2. prepare the VM

Open the Ubuntu VM and sign in.

### Terminal 1 — check the environment

```bash
java -version
javac -version
docker --version
docker info
```

Confirm that Java is version 21 and Docker is running.

Check Docker access:

```bash
docker ps
```

If Docker reports a permission error, use:

```bash
sudo docker ps
```

For the rest of the demonstration, use either `docker` consistently (if your user has Docker permissions) or `sudo docker` consistently. Do not mix the two because they may behave differently with permissions.

### Go to the project directory

```bash
cd ~/cloud-resource-manager
```

Check that the project and M9 test files are present:

```bash
ls
```

You should see the project Java files, including:

```text
ResourcePool.java
ResourceAllocator.java
DockerController.java
TestM9Dynamic.java
TestM9MemoryDynamic.java
```

If the M9 test files are not present, do not proceed with the dynamic Java demonstration until they have been restored to the project directory.

### Compile the Java files

If your project uses the default Java package and its source files are in this directory:

```bash
javac *.java
```

If compilation reports errors, stop and resolve them before the faculty demonstration. Do not try to hide compilation errors by moving on to the Docker commands.

---

## 3. Clean up old demo containers

First inspect what is already running:

```bash
docker ps -a
```

The static demo below uses these names:

- `demo-static-cpu-busy`
- `demo-static-cpu-idle`
- `demo-static-memory`

Remove any old containers with those names before creating new ones:

```bash
docker rm -f demo-static-cpu-busy demo-static-cpu-idle demo-static-memory 2>/dev/null || true
```

This command only targets the three named demo containers. It does not remove all Docker containers.

---

# PART A — STATIC ALLOCATION DEMONSTRATION

## 4. Create the static CPU containers

Explain that both containers receive fixed limits at creation time. One will run a CPU workload, while the other remains idle.

### 4.1 Create the CPU-intensive container

```bash
docker run -d \
  --name demo-static-cpu-busy \
  --cpus="0.5" \
  --memory="128m" \
  --memory-swap="256m" \
  ubuntu:24.04 \
  bash -c "timeout 300s yes > /dev/null"
```

What this means:

- `-d`: run in the background.
- `--name`: assign a readable container name.
- `--cpus="0.5"`: cap CPU usage at half of one CPU.
- `--memory="128m"`: set the memory limit to 128 MiB.
- `--memory-swap="256m"`: set the combined memory-plus-swap limit to 256 MiB.
- `yes > /dev/null`: generate continuous CPU work.
- `timeout 300s`: stop the workload after five minutes.

### 4.2 Create the idle container

```bash
docker run -d \
  --name demo-static-cpu-idle \
  --cpus="0.5" \
  --memory="128m" \
  --memory-swap="256m" \
  ubuntu:24.04 \
  sleep infinity
```

This container has the same configured limits but does not intentionally perform CPU-intensive work.

## 5. Inspect static CPU allocation

### 5.1 Show the configured limits

```bash
docker inspect --format='Name={{.Name}} | CPU quota={{.HostConfig.NanoCpus}} | Memory limit={{.HostConfig.Memory}} bytes | Status={{.State.Status}}' demo-static-cpu-busy demo-static-cpu-idle
```

The CPU quota value is represented internally in nanoseconds per second. For `--cpus=0.5`, the expected `NanoCpus` value is `500000000`. The memory limit should be `134217728` bytes (128 MiB).

You can also inspect one container in detail:

```bash
docker inspect demo-static-cpu-busy
```

### 5.2 Show live resource usage

```bash
docker stats demo-static-cpu-busy demo-static-cpu-idle
```

Let the output run for a few seconds, then press `Ctrl+C`.

For a single snapshot:

```bash
docker stats --no-stream demo-static-cpu-busy demo-static-cpu-idle
```

**What to point out:**

- Both containers have the same configured CPU and memory limits.
- The busy container should show CPU usage because it is running a CPU workload.
- The idle container should show very low or near-zero CPU usage.
- The configured limit is not the same thing as actual usage.

> CPU percentage shown by Docker stats can be above 100% on multi-core systems. It is not a direct percentage of the configured `--cpus` quota.

## 6. Create and inspect a static memory workload

This container uses a Python process to hold approximately 100 MiB of allocated memory for five minutes.

```bash
docker run -d \
  --name demo-static-memory \
  --cpus="0.5" \
  --memory="256m" \
  --memory-swap="512m" \
  python:3.12-slim \
  python -c "import time; data=bytearray(100*1024*1024); print('Allocated approximately 100 MiB', flush=True); time.sleep(300)"
```

Inspect the configured memory limit:

```bash
docker inspect --format='Name={{.Name}} | Memory limit={{.HostConfig.Memory}} bytes | Status={{.State.Status}}' demo-static-memory
```

Inspect its live usage:

```bash
docker stats --no-stream demo-static-memory
```

To keep watching it:

```bash
docker stats demo-static-memory
```

Press `Ctrl+C` to stop the stats display. This does **not** stop the container.

**What to explain:** The memory limit remains fixed at 256 MiB even though the process uses only part of that limit. The unused portion is headroom under the configured limit; it is not necessarily RAM physically reserved for the container.

## 7. Summarize the static demonstration

Run:

```bash
docker ps
```

You should see the three static demo containers.

You can say:

> "In static allocation, the limits are configured when the containers are created. They remain unchanged while the workloads run. The actual usage can be lower than the configured limits, as we can see from Docker stats."

Do not claim that Docker has physically reserved 128 MiB or 256 MiB of RAM for each container. These are maximum limits.

---

# PART B — JAVA-CONTROLLED DYNAMIC ALLOCATION

## 8. Prepare two terminals

For a clear demonstration, use two terminal windows:

- **Terminal 1:** run the Java test program.
- **Terminal 2:** inspect the Docker containers and their changing limits/usage.

In both terminals:

```bash
cd ~/cloud-resource-manager
```

Before running a test, check whether an old test container is still present:

```bash
docker ps -a
```

If the previous test left behind a container with the same name, remove only that old test container before rerunning. The exact name can be read from the Java program's Docker container creation code or from `docker ps -a`.

## 9. Demonstrate dynamic CPU allocation

### 9.1 Terminal 1 — start the Java test

```bash
java TestM9Dynamic
```

The M9 CPU test is designed to demonstrate these allocation stages:

| Stage | CPU limit | Memory limit |
|---|---:|---:|
| Initial | 0.5 CPU | 256 MiB |
| Increased | 1.0 CPU | 512 MiB |
| Decreased | 0.5 CPU | 256 MiB |

Watch the Java output. It should report the allocation/reallocation steps and resource measurements.

### 9.2 Terminal 2 — inspect the container

As soon as the Java program creates its container, run:

```bash
docker ps
```

Find the container created by the test. Use its exact name in the following commands.

Inspect the configured CPU and memory limits:

```bash
docker inspect --format='Name={{.Name}} | NanoCPUs={{.HostConfig.NanoCpus}} | Memory={{.HostConfig.Memory}} bytes | Status={{.State.Status}}' CONTAINER_NAME
```

Replace `CONTAINER_NAME` with the actual container name shown by `docker ps`.

Watch live usage:

```bash
docker stats CONTAINER_NAME
```

Press `Ctrl+C` to stop the stats display only.

To take a single snapshot:

```bash
docker stats --no-stream CONTAINER_NAME
```

### 9.3 Show the limit changing

Run the `docker inspect` command again at each stage while the Java program is running:

```bash
docker inspect --format='CPU quota={{.HostConfig.NanoCpus}} | Memory={{.HostConfig.Memory}} bytes' CONTAINER_NAME
```

Expected values:

| Stage | `NanoCpus` | Memory bytes |
|---|---:|---:|
| Initial / Decreased | `500000000` | `268435456` |
| Increased | `1000000000` | `536870912` |

The Java test may move through stages quickly. Keep the inspect command ready and execute it while each stage is active. If the program completes before you can inspect a stage, rerun the test and start monitoring immediately in Terminal 2.

**What to point out:**

1. Java creates the workload container with initial limits.
2. Java calls the resource allocator to increase the limits.
3. Docker's container configuration reflects the new limits.
4. Java later decreases the limits again.
5. The container is eventually stopped/released by the test.

The important evidence is the change in the configured limits, not just a change in the CPU usage percentage.

## 10. Demonstrate dynamic memory allocation

### 10.1 Terminal 1 — start the Java memory test

```bash
java TestM9MemoryDynamic
```

The memory test is designed to demonstrate:

| Stage | Memory limit | Approximate workload memory use |
|---|---:|---:|
| Initial | 192 MiB | 104.4 MiB |
| Increased | 384 MiB | 104.4 MiB |
| Decreased | 192 MiB | 104.4 MiB |

### 10.2 Terminal 2 — locate and inspect the container

```bash
docker ps
```

Find the container created by the Java test, then inspect it:

```bash
docker inspect --format='Name={{.Name}} | Memory={{.HostConfig.Memory}} bytes | Status={{.State.Status}}' CONTAINER_NAME
```

Watch the live memory usage:

```bash
docker stats CONTAINER_NAME
```

Take a snapshot when needed:

```bash
docker stats --no-stream CONTAINER_NAME
```

### 10.3 Show the memory limit changing

Run this during each stage:

```bash
docker inspect --format='Memory limit={{.HostConfig.Memory}} bytes' CONTAINER_NAME
```

Expected values:

| Stage | Memory limit in bytes |
|---|---:|
| Initial / Decreased (192 MiB) | `201326592` |
| Increased (384 MiB) | `402653184` |

**What to point out:**

- The workload's actual memory use stays approximately the same.
- The configured memory limit doubles from 192 MiB to 384 MiB.
- The reported memory-utilization percentage therefore decreases.
- When the limit returns to 192 MiB, the percentage returns to its earlier level.

This is a useful way to show why utilization percentage must be interpreted together with the configured limit and actual memory usage.

---

## 11. Compare static and dynamic allocation

Use these points when explaining the difference:

| Feature | Static demonstration | Dynamic demonstration |
|---|---|---|
| When limits are set | At container creation | At creation and later by Java |
| Can limits change while running? | Not in this demonstration | Yes |
| CPU limit example | Fixed at 0.5 CPU | 0.5 → 1.0 → 0.5 CPU |
| Memory limit example | Fixed at 128 MiB or 256 MiB | 256 → 512 → 256 MiB (CPU test); 192 → 384 → 192 MiB (memory test) |
| Who changes the limits? | No runtime change | Java resource allocator through Docker |
| Evidence | `docker inspect`, `docker stats` | Java output, `docker inspect`, `docker stats` |

### A concise explanation to give the faculty

> "The static containers are created with fixed resource limits. Even if a workload uses less than its limit, the configured limit remains the same. In the dynamic demonstration, our Java resource manager uses the Docker controller to update a running container's CPU and memory limits. We verify those changes directly through Docker inspect and observe actual usage through Docker stats. The current implementation demonstrates explicit Java-controlled reallocation rather than autonomous workload-demand detection."

---

# PART C — CLEANUP

## 12. Stop and remove the static demo containers

After the static demonstration:

```bash
docker rm -f demo-static-cpu-busy demo-static-cpu-idle demo-static-memory
```

Verify that they are removed:

```bash
docker ps -a
```

## 13. Stop and remove dynamic test containers

First list all containers:

```bash
docker ps -a
```

Identify the containers created by `TestM9Dynamic` and `TestM9MemoryDynamic`. Remove only those test containers using their exact names:

```bash
docker rm -f CONTAINER_NAME
```

Replace `CONTAINER_NAME` with the actual test container name. Repeat for the other test container if it still exists.

Do not use `docker rm -f $(docker ps -aq)` during the faculty demonstration. That command would remove every container on the VM, including containers unrelated to this project.

## 14. Final verification

```bash
docker ps
```

If the demonstration containers have all been removed, they should no longer appear in the running-container list.

---

# PART D — QUICK COMMAND CHEAT SHEET

Keep this section open during the presentation.

| Purpose | Command |
|---|---|
| List running containers | `docker ps` |
| List all containers | `docker ps -a` |
| Show live stats | `docker stats CONTAINER_NAME` |
| Show one stats snapshot | `docker stats --no-stream CONTAINER_NAME` |
| Inspect full configuration | `docker inspect CONTAINER_NAME` |
| Inspect CPU/memory limits | `docker inspect --format='CPU={{.HostConfig.NanoCpus}} Memory={{.HostConfig.Memory}}' CONTAINER_NAME` |
| View container logs | `docker logs CONTAINER_NAME` |
| Stop a container | `docker stop CONTAINER_NAME` |
| Remove a stopped container | `docker rm CONTAINER_NAME` |
| Force stop and remove | `docker rm -f CONTAINER_NAME` |

---

# PART E — QUESTIONS THE FACULTY MAY ASK

### 1. What is the difference between allocation and utilization?

**Allocation/limit** is the resource limit configured for the container. **Utilization** is how much resource the workload is actually using at a particular time.

### 2. Does `--memory=256m` mean Docker reserves 256 MiB of physical RAM?

No. It sets a memory limit/cap for the container. The process may use less than that amount.

### 3. Why can CPU usage be 100% or more in `docker stats`?

Docker's CPU percentage can be measured relative to one CPU core. On a multi-core system, a container using more than one core can show a value above 100%. It should not be interpreted as a percentage of the configured CPU quota.

### 4. How does Java change Docker resource limits?

The Java resource manager calls its Docker controller, which issues the corresponding Docker resource update. Docker applies the updated limits through Linux container resource controls (cgroups).

### 5. Is the current system fully automatic?

The current demonstration uses explicit Java calls to increase or decrease the limits. It demonstrates runtime reallocation, but it does not yet autonomously detect workload demand and decide when to resize containers.

### 6. How do you verify that reallocation really happened?

Use `docker inspect` to check the configured limits before and after the Java update. Use `docker stats` to observe actual resource usage. The inspect output is the clearest evidence that the configured limit changed.

### 7. Does a higher CPU limit guarantee higher performance?

No. It gives the container permission to use more CPU capacity when available. Actual performance also depends on workload behavior and host resources. This demonstration proves that the limit can be changed; it does not by itself prove a throughput improvement.

---

## Final demonstration checklist

Before starting:

- [ ] Ubuntu VM is running.
- [ ] Java 21 is available.
- [ ] Docker is running and `docker ps` works.
- [ ] Project directory is available at `~/cloud-resource-manager`.
- [ ] `TestM9Dynamic.java` and `TestM9MemoryDynamic.java` are present.
- [ ] `javac *.java` completes successfully.
- [ ] Old containers with the demo names have been removed.
- [ ] Two terminal windows are open for the dynamic demonstrations.

During the presentation:

- [ ] Create the static CPU busy and idle containers.
- [ ] Inspect their fixed limits with `docker inspect`.
- [ ] Show actual usage with `docker stats`.
- [ ] Create and inspect the static memory container.
- [ ] Run `java TestM9Dynamic` and show CPU/memory limit changes.
- [ ] Run `java TestM9MemoryDynamic` and show memory limit changes.
- [ ] Explain the difference between configured limits and actual usage.

After the presentation:

- [ ] Remove the static demo containers.
- [ ] Remove any remaining dynamic test containers.
- [ ] Verify with `docker ps -a`.

**Presentation tip:** Practice the dynamic tests once before the faculty demonstration. Keep Terminal 2 ready with the `docker ps`, `docker inspect`, and `docker stats` commands so you can inspect each stage while the Java test is running. The Java tests may complete and remove their containers; if that happens before you inspect a stage, rerun the test and start observing immediately.
