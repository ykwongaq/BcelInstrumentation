package wyk.instrumentation;

import org.apache.maven.shared.invoker.*;

import java.io.File;
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

        int exitCode;
        exitCode = compileTargetProject();
        if (exitCode != 0) {
            System.out.println("Failed to compile target project. Exiting program");
            return;
        }

        exitCode = packageInstrumentator();
        if (exitCode != 0) {
            System.out.println("Filed to package instrumentation. Exiting program");
            return;
        }

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

    public static String getFullMavenExe() {
        String mavenHome = System.getenv("MAVEN_HOME");
        if (mavenHome == null) {
            mavenHome = System.getenv("M2_HOME");
        }

        if (mavenHome == null) {
            throw new RuntimeException("MAVEN_HOME is not set");
        }
        return Paths.get(mavenHome, "mvn").toString();
    }

    public static int compileTargetProject() {
        final String pomFilePath = Paths.get("TargetProject", "pom.xml").toString();
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(pomFilePath));
        request.setGoals(List.of("compile", "--quiet"));

        DefaultInvoker invoker = new DefaultInvoker();
        invoker.setMavenExecutable(new File(getFullMavenExe()));
        try {
            System.out.println("Compiling target project ...");
            InvocationResult result = invoker.execute(request);
            System.out.println("Finish compiling target project ...");
            return result.getExitCode();
        } catch (MavenInvocationException e) {
            throw new RuntimeException(e);
        }
    }

    public static int packageInstrumentator() {
        final String pomFilePath = Paths.get("MethodNameInstrumentator", "pom.xml").toString();
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(pomFilePath));
        request.setGoals(List.of("clean", "package", "--quiet"));

        DefaultInvoker invoker = new DefaultInvoker();
        invoker.setMavenExecutable(new File(getFullMavenExe()));
        try {
            InvocationResult result = invoker.execute(request);
            return result.getExitCode();
        } catch (MavenInvocationException e) {
            throw new RuntimeException(e);
        }
    }
}
