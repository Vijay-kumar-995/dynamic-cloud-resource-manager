# M5 — Java-Docker Monitoring Integration

## Objective
Connect the Java application with Docker to monitor container resource usage.

## Implementation
Created `DockerMonitor.java` using Java's ProcessBuilder.

The program executes:

docker stats --no-stream

It retrieves and displays:
- Container ID and name
- CPU usage
- Memory usage and limit
- Memory utilization
- Network I/O
- Block I/O
- Number of processes

## Testing
Created a test container named `monitor-test` with:
- CPU limit: 0.5 cores
- Memory limit: 128 MB

The Java monitor successfully displayed the container's resource statistics every 3 seconds.

## Result
Java successfully monitored Docker container resource usage.

M5 completed successfully.