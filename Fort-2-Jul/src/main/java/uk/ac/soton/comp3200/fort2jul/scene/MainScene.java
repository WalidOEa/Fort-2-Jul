package uk.ac.soton.comp3200.fort2jul.scene;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;
import uk.ac.soton.comp3200.fort2jul.controller.FileController;
import uk.ac.soton.comp3200.fort2jul.controller.SourceController;
import uk.ac.soton.comp3200.fort2jul.ui.Pane;
import uk.ac.soton.comp3200.fort2jul.ui.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Responsible for programmatically constructing the contents and layout of the main scene, displaying text areas
 * formatting the source and target code. Here we transpile the source code into the target code. Furthermore, main
 * scene extends base scene
 *
 * It is important to note, despite the presence of the linter button, currently it offers no functionality.
 */
public class MainScene extends BaseScene {

    /**
     * Logger of main scene
     */
    private static final Logger logger = Logger.getLogger(MainScene.class);

    /**
     * Responsible for manage files, both source and target
     */
    private final FileController fileController;

    /**
     * Responsible for transpiling, passing the source contents onto the transpiler itself
     */
    private final SourceController sourceController;

    /**
     * Content of transpiled contents
     */
    private String targetContent;

    /**
     * Constructor of main scene, inheriting construct of view class
     * @param view View
     */
    public MainScene(View view) {
        super(view);

        fileController = new FileController();
        sourceController = new SourceController();
    }

    /**
     * No method body given
     */
    @Override
    public void initialise() {

    }

    /**
     * Override build method of base scene, responsible for programmatic construct of view
     */
    @Override
    public void build() {
        logger.info("Building MainScene");

        root = new Pane(view.getWidth(), view.getHeight());

        TextArea leftTextArea = new TextArea();
        leftTextArea.setPrefSize(780, 840);
        leftTextArea.setLayoutX(14);
        leftTextArea.setLayoutY(14);

        //Button leftLintButton = new Button("Lint");
        //leftLintButton.setOnAction(event -> {
        //    logger.info("Linting Fortran Program");

        //    String fortranLint = fileController.lintFortran();

        //    leftTextArea.setText(fortranLint);
        //});

        TextArea rightTextArea = new TextArea();
        rightTextArea.setPrefSize(780, 840);
        rightTextArea.setLayoutX(808);
        rightTextArea.setLayoutY(14);

        ButtonBar leftButtonBar = new ButtonBar();
        Button transpilerButton = new Button("Transpile");
        transpilerButton.setOnAction(event -> {
            logger.info("Transpiling file at path " + fileController.getFilePath());

            String fullPath = fileController.getFilePath(); // Assume this returns the full file path
            Path path = Paths.get(fullPath);
            Path directory = path.getParent();

            targetContent = sourceController.transpileContent(leftTextArea.getText(), directory.toString());

            if (!leftTextArea.getText().equals("")) {
                rightTextArea.setText(targetContent);
            }
        });

        leftButtonBar.getButtons().addAll(transpilerButton);

        VBox leftVBox = new VBox();
        leftVBox.getChildren().addAll(leftTextArea, leftButtonBar); //, leftLintButton

        ButtonBar rightButtonBar = new ButtonBar();
        Button lintButton = new Button("Refactor Julia");

        rightButtonBar.getButtons().addAll(lintButton);

        VBox rightVBox = new VBox();
        rightVBox.getChildren().addAll(rightTextArea, rightButtonBar);

        HBox hBox = new HBox(25);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(leftVBox, rightVBox);

        MenuBar menuBar = new MenuBar();

        MenuItem openMenuItem;

        //Menu openRecentMenuItem;

        //MenuItem saveMenuItem;
        MenuItem quitMenuItem;

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(
                openMenuItem = new MenuItem("Open…"),
                //openRecentMenuItem = new Menu("Open Recent"),
                //saveMenuItem = new MenuItem("Save"),
                new SeparatorMenuItem(),
                quitMenuItem = new MenuItem("Quit")
        );

        openMenuItem.setOnAction(event -> {
            logger.info("Opening file");

            fileController.openFile();
            String content = fileController.readContent();

            if (content != null) {
                logger.info("Updating leftTextArea with content");

                leftTextArea.setText(content);
                rightTextArea.setText("");
            }
        });

        //saveMenuItem.setOnAction(event -> {
        //    fileController.saveFile(leftTextArea.getText());
        //});

        quitMenuItem.setOnAction(event -> {
            logger.info("Quitting application with exit code 0");

            Platform.exit();
            System.exit(0);
        });

        MenuItem docMenuItem;

        Menu helpMenu = new Menu("Help");
        helpMenu.getItems().addAll(
                docMenuItem = new MenuItem("Documentation")
        );

        docMenuItem.setOnAction(event -> {
            String readmeContent = readReadmeContent();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Documentation");
            alert.setHeaderText("Help and Documentation");
            alert.setContentText("Here you can find the help and documentation about the transpiler.");

            TextArea textArea = new TextArea(readmeContent);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            ScrollPane scrollPane = new ScrollPane(textArea);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);

            alert.getDialogPane().setExpandableContent(scrollPane);
            alert.showAndWait();
        });

        menuBar.getMenus().addAll(fileMenu, helpMenu);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(menuBar, hBox);

        root.getChildren().addAll(vBox);
    }

    private String readReadmeContent() {
        StringBuilder contentBuilder = new StringBuilder();
        try (InputStream inputStream = getClass().getResourceAsStream("/gui/README.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            logger.error("Error reading README file", e);
            return "Failed to load README file.";
        }
        return contentBuilder.toString();
    }
}
