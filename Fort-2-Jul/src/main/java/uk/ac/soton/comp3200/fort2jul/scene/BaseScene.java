package uk.ac.soton.comp3200.fort2jul.scene;

import javafx.scene.Scene;
import uk.ac.soton.comp3200.fort2jul.ui.Pane;
import uk.ac.soton.comp3200.fort2jul.ui.View;

import java.util.Objects;

/**
 * Abstract class of base scene. Any scene extends from this class.
 *
 * Responsible for providing the framework for any subsequent scene. Specifies universal layout of widgets regardless
 * of scene.
 */
public abstract class BaseScene {

    /**
     * View class
     */
    protected final View view;

    /**
     * JavaFX Scene
     */
    protected Scene scene;

    /**
     * Pane class
     */
    protected Pane root;

    /**
     * Constructor of base scene
     * @param view View
     */
    public BaseScene(View view) {
        this.view = view;
    }

    /**
     * Initialise anything before constructing view
     */
    public abstract void initialise();

    /**
     * Programmatically build view
     */
    public abstract void build();

    /**
     * Set scene
     * @return Scene
     */
    public Scene setScene() {
        Scene previous = view.getScene();
        Scene scene = new Scene(root, previous.getWidth(), previous.getHeight());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/main.css")).toExternalForm());        this.scene = scene;

        return scene;
    }
}
