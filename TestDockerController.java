public class TestDockerController {

    public static void main(String[] args) {

        try {
            DockerController.updateResources("m8-test", 0.75, 192);

            System.out.println("Resource update successful!");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}