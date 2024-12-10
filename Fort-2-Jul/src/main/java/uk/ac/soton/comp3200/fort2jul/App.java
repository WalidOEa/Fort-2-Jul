package uk.ac.soton.comp3200.fort2jul;

import javafx.application.Application;
import javafx.stage.Stage;
import uk.ac.soton.comp3200.fort2jul.ui.View;

/**
 * Extends JavaFX Application, opens window and launches JavaFX application
 */
public class App extends Application {

    /**
     * Instance of app
     */
    private static App instance;

    /**
     * Stage of view
     */
    private Stage stage;

    /**
     * Launches application
     * @param args
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Starts the instance
     * @param stage
     */
    @Override
    public void start(Stage stage) {
        instance = this;
        this.stage = stage;

        openWindow();
    }

    /**
     * Opens the window and shows stage
     */
    private void openWindow() {
        int width = 1600;
        int height = 900;

        var view = new View(stage, width, height);

        stage.setResizable(false);
        stage.show();
    }

    /**
     * Shutdown window
     */
    public void shutdown() {
        System.exit(0);
    }

    /**
     * Get instance
     * @return App
     */
    public static App getInstance() {
        return instance;
    }
}
