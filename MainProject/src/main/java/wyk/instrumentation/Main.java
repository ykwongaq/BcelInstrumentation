package wyk.instrumentation;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        // Configuration
        final String javaHomePath = Main.getFullJavaHome();
        final String javaAgentPath = Paths.get("MethodNameInstrumentator", "target", "instrumentator.jar").toString();
        final String classPath = Paths.get("TargetProject", "classes").toString();
        final String targetProject = Paths.get("TargetProject", "src", "main", "java", "wyk", "instrumentation", "target", "Main.java").toString();

        // Set up commend
        List<String> commend = new ArrayList<>();
        commend.add(javaHomePath);
        commend.add("-javaagent:" + javaAgentPath);
        commend.add("-noverify");
        commend.add("-classpath");
        commend.add(classPath);
        commend.add(targetProject);

        final ProcessBuilder processBuilder = new ProcessBuilder(commend);

        // Redirect the process's output to the current console
        processBuilder.inheritIO();

        try {
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getFullJavaHome() {
        final String javaHome = System.getProperty("java.home");
        final String osName = System.getProperty("os.name").toLowerCase();

        String javaExe;
        if (osName.contains("win")) {
            javaExe = Paths.get(javaHome, "bin", "java.exe").toString();
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("mac")) {
            javaExe = Paths.get(javaHome, "bin", "java").toString();
        } else {
            throw new UnsupportedOperationException("Unsupported operation system: " + osName);
        }
        return javaExe;
    }
}
