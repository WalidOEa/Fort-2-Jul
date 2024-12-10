package uk.ac.soton.comp3200.fort2jul.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Deprecated
public class Python3Installer {

    public void installDependencies() {
        installPython();
        installCython();
    }

    private void installPython() {
        String osName = System.getProperty("os.name").toLowerCase();
        String architecture = System.getProperty("os.arch");
        String installerUrl;
        String installerPath;

        if (isPythonInstalled()) {
            System.out.println("Python is already installed.");
            return;
        }

        if (osName.contains("win")) {
            if (architecture.contains("64")) {
                installerUrl = "https://www.python.org/ftp/python/3.11.5/python-3.11.5-amd64.exe";
            } else if (architecture.contains("arm")) {
                installerUrl = "https://www.python.org/ftp/python/3.11.5/python-3.11.5-embed-arm64.zip";
            } else {
                installerUrl = "https://www.python.org/ftp/python/3.11.5/python-3.11.5.exe";
            }

            installerPath = "python-installer.exe";
        } else if (osName.contains("mac")) {
            installerUrl = "https://www.python.org/ftp/python/3.11.5/python-3.11.5-macos11.pkg";
            installerPath = "python-installer.pkg";
        } else {
            System.out.println("Unsupported operating system.");
            return;
        }

        try (InputStream in = new URL(installerUrl).openStream()) {
            Files.copy(in, Paths.get(installerPath), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("Failed to download Python installer.");
            e.printStackTrace();
            return;
        }

        // Run the Python installer
        if (osName.contains("win")) {
            ProcessBuilder pb = new ProcessBuilder(installerPath);
            try {
                pb.start();
            } catch (IOException e) {
                System.out.println("Failed to run Python installer.");
                e.printStackTrace();
            }
        } else if (osName.contains("mac")) {
            ProcessBuilder pb = new ProcessBuilder("installer", "-pkg", installerPath, "-target", "/");
            try {
                pb.start();
            } catch (IOException e) {
                System.out.println("Failed to install Python.");
                e.printStackTrace();
            }
        }
    }

    private void installCython() {
        ProcessBuilder pb = new ProcessBuilder("pip", "install", "cython");
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }

        } catch (IOException e) {
            System.out.println("Failed to install Cython.");
            e.printStackTrace();
        }
    }

    private boolean isPythonInstalled() {
        ProcessBuilder pb = new ProcessBuilder("python", "--version");
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = bufferedReader.readLine();

            if (line != null && line.startsWith("Python")) {
                return true;
            }
        } catch (IOException e) {
            // Python is not installed
        }

        return false;
    }
}
