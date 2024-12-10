package uk.ac.soton.comp3200.fort2jul.ui;

import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * Responsible for how children are laid out in any scene. Ensures correct scaling and transformations during
 * resizing.
 */
public class Pane extends StackPane {

    /**
     * Width of window
     */
    private final int width;

    /**
     * Height of window
     */
    private final int height;

    /**
     * Scalar factor
     */
    private double scalar = 1;

    /**
     * Constructs pane specifying dimensions in width and height
     * @param width Width
     * @param height Height
     */
    public Pane(int width, int height) {
        super();

        this.width = width;
        this.height = height;

        setAlignment(Pos.TOP_LEFT);
    }

    /**
     * Sets transformation scalar
     * @param scalar Scalar
     */
    protected void setScalar(double scalar) {
        this.scalar = scalar;
    }

    /**
     * Manages layout of children
     */
    @Override
    public void layoutChildren() {
        super.layoutChildren();

        boolean autoScale = true;
        if(!autoScale) {
            return;
        }

        var scaleFactorHeight = getHeight() / height;
        var scaleFactorWidth = getWidth() / width;

        setScalar(Math.min(scaleFactorHeight, scaleFactorWidth));

        Scale scale = new Scale(scalar,scalar);

        var parentWidth = getWidth();
        var parentHeight = getHeight();

        var paddingLeft = (parentWidth - (width * scalar)) / 2.0;
        var paddingTop = (parentHeight - (height * scalar)) / 2.0;

        Translate translate = new Translate(paddingLeft, paddingTop);
        scale.setPivotX(0);
        scale.setPivotY(0);
        getTransforms().setAll(translate, scale);
    }
}
