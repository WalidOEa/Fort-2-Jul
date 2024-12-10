package uk.ac.soton.comp3200.fort2jul.controller;

import javafx.stage.FileChooser;
import org.apache.log4j.Logger;
import uk.ac.soton.comp3200.fort2jul.linter.FortranLinter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Responsible for managing both source and target files obtained from view. Reads content of files and returns
 * it to the view. Currently, linter is deprecated as well as saving files.
 */
public class FileController {

    /**
     * Logger of file controller
     */
    private static final Logger logger = Logger.getLogger(FileController.class);

    /**
     * Selects file and returns path in view
     */
    private final FileChooser fileChooser;

    @Deprecated
    private FortranLinter fortranLinter;

    /**
     * File path of selected file
     */
    private String filePath;

    @Deprecated
    private static final String RECENT_FILES = "recentFiles";

    @Deprecated
    private final Preferences prefs;

    /**
     * Constructor file controller instantiates file chooser
     */
    public FileController() {
        fileChooser = new FileChooser();
        //fortranLinter = new FortranLinter();

        prefs = Preferences.userNodeForPackage(FileController.class);
    }

    /**
     * Opens selected file
     */
    public void openFile() {
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fortran 77, Julia Files", "*.f", "*.for", "*.FOR"));

        //if (selectedFile != null) {
        //    filePath = selectedFile.getAbsolutePath();

        //    saveRecentFile(filePath);
        //}
    }

    @Deprecated
    private void saveRecentFile(String filePath) {
        for (int i = 0; i < 10; i++) {
            String file = prefs.get(RECENT_FILES + i, null);
            if (file == null || file.equals(filePath)) {
                for (int j = 9; j > i; j--) {
                    prefs.put(RECENT_FILES + j, prefs.get(RECENT_FILES + (j - 1), null));
                }

                prefs.put(RECENT_FILES + i, filePath);
                break;
            }
        }
    }

    @Deprecated
    public List<String> getRecentFiles() {
        List<String> recentFiles = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String file = prefs.get(RECENT_FILES + i, null);
            if (file != null) {
                recentFiles.add(file);
            }
        }
        return recentFiles;
    }

    /**
     * Reads content of selected file and returns to view
     * @return Contents of file
     */
    public String readContent() {
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            filePath = selectedFile.getAbsolutePath();

            try {
                return Files.readString(Path.of(selectedFile.getAbsolutePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Deprecated
    public void saveFile(String content) {
        logger.info("Saving file to " + filePath);

        try {
            Files.writeString(Path.of(filePath), content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public String lintFortran() {
        return fortranLinter.lint(filePath);
    }

    /**
     * Get file path
     * @return file path
     */
    public String getFilePath() {
        return filePath;
    }
}
