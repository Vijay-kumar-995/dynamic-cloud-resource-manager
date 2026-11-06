import java.io.BufferedReader;
import java.io.InputStreamReader;

public class DockerMonitor {

    public static void monitorContainers() throws Exception {

        ProcessBuilder builder = new ProcessBuilder(
                "docker", "stats", "--no-stream"
        );

        builder.redirectErrorStream(true);

        Process process = builder.start();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        );

        String line;

        System.out.println("\n====== DOCKER RESOURCE MONITOR ======");

        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        process.waitFor();
        reader.close();
    }

    public static void main(String[] args) throws Exception {

        while (true) {

            monitorContainers();

            Thread.sleep(3000);
        }
    }
}