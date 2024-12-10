package uk.ac.soton.comp3200.fort2jul.ui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import uk.ac.soton.comp3200.fort2jul.App;
import uk.ac.soton.comp3200.fort2jul.scene.BaseScene;
import uk.ac.soton.comp3200.fort2jul.scene.MainScene;

/**
 * Manages each scene and stage
 */
public class View {

    /**
     * Stage
     */
    private final Stage stage;

    /**
     * Scene
     */
    private Scene scene;

    /**
     * Current scene displaying
     */
    private BaseScene currentScene;

    /**
     * Width of window
     */
    private final int width;

    /**
     * Height of window
     */
    private final int height;

    /**
     * Establishes stage and sets the first scene to display
     * @param stage Stage
     * @param width Width
     * @param height Height
     */
    public View(Stage stage, int width, int height) {
        this.stage = stage;
        this.width = width;
        this.height = height;

        setupStage();
        setupDefaultStage();
        startMainScene();
    }

    /**
     * Set up stage
     */
    private void setupStage() {
        stage.setTitle("Fort-2-Jul");
        stage.setMinWidth(width);
        stage.setMinHeight(height + 20);
        stage.setOnCloseRequest(ev -> App.getInstance().shutdown());
    }

    /**
     * Establishes default scene
     */
    private void setupDefaultStage() {
        this.scene = new Scene(new Pane(), width, height);
        stage.setScene(scene);
    }

    /**
     * Loads specified scene to display in window
     * @param newScene New scene
     */
    public void loadScene(BaseScene newScene) {
        newScene.build();
        currentScene = newScene;
        scene = newScene.setScene();
        stage.setScene(scene);

        Platform.runLater(() -> currentScene.initialise());
    }

    /**
     * Starts the main scene
     */
    private void startMainScene() {
        loadScene(new MainScene(this));
    }

    /**
     * Gets scene
     * @return Scene
     */
    public Scene getScene() {
        return this.scene;
    }

    /**
     * Width of dimension
     * @return Width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Height of dimension
     * @return Height
     */
    public int getHeight() {
        return this.height;
    }
}
