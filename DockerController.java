import java.io.BufferedReader;
import java.io.InputStreamReader;

public class DockerController {

    private static String runDockerCommand(String... command) throws Exception {

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);

        Process process = builder.start();

        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException(output.toString().trim());
        }

        return output.toString().trim();
    }

    private static void validateContainerName(String containerName) {
        if (containerName == null ||
                !containerName.matches("[a-zA-Z0-9][a-zA-Z0-9_.-]*")) {
            throw new IllegalArgumentException("Invalid Docker container name.");
        }
    }

    public static void updateResources(
            String containerName, double cpu, int memoryMB) throws Exception {

        validateContainerName(containerName);

        if (!Double.isFinite(cpu) || cpu <= 0 ||
                memoryMB <= 0 || memoryMB > Integer.MAX_VALUE / 2) {
            throw new IllegalArgumentException("Invalid Docker resource limits.");
        }

        if (!isContainerRunning(containerName)) {
            throw new RuntimeException("Container is not running: " + containerName);
        }

        String output = runDockerCommand(
                "docker", "update",
                "--cpus=" + cpu,
                "--memory=" + memoryMB + "m",
                "--memory-swap=" + ((long) memoryMB * 2) + "m",
                containerName
        );

        System.out.println(output);
    }

    public static boolean isContainerRunning(String containerName) throws Exception {

        validateContainerName(containerName);

        String output = runDockerCommand(
                "docker", "inspect",
                "--format={{.State.Running}}",
                containerName
        );

        return "true".equalsIgnoreCase(output);
    }

    public static void stopContainer(String containerName) throws Exception {

        validateContainerName(containerName);

        String output = runDockerCommand(
                "docker", "stop", containerName
        );

        System.out.println(output);
    }
}