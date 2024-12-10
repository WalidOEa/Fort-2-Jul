package uk.ac.soton.comp3200.fort2jul.linter;

import uk.ac.soton.comp3200.fort2jul.util.Python3Installer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Deprecated
public class FortranLinter {
    public FortranLinter() {
        Python3Installer python3Installer = new Python3Installer();
        python3Installer.installDependencies();

        installFortranLinter();
    }

    private void installFortranLinter() {
        ProcessBuilder pb = new ProcessBuilder("python", "src/main/resources/python/packages.py");
        pb.redirectErrorStream(true);

        if (isFortranLinterInstalled()) {
            System.out.println("fortran-linter is already installed.");
            return;
        }

        try {
            Process process = pb.start();
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Failed to install fortran-linter.");
            e.printStackTrace();
        }
    }

    public String lint(String filePath) {
        ProcessBuilder pb = new ProcessBuilder("fortran-linter", filePath, "--stdout");
        pb.redirectErrorStream(true);
        StringBuilder output = new StringBuilder();

        try {
            Process process = pb.start();
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                output.append(line).append("\n");
            }
        } catch (IOException e) {
            System.out.println("Failed to lint the Fortran program.");
            e.printStackTrace();
        }

        return output.toString();
    }

    private boolean isFortranLinterInstalled() {
        ProcessBuilder pb = new ProcessBuilder("fortran-linter", "--version");
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = bufferedReader.readLine();

            if (line != null && line.startsWith("fortran-linter")) {
                return true;
            }
        } catch (IOException e) {
            // fortran-linter is not installed
        }

        return false;
    }
}
