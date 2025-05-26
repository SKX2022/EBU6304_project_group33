package com.finance.utils;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import java.io.IOException;

public final class SceneSwitcher {

    private static double BASE_W, BASE_H;       // The current page base width and height
    private static Stage  stage;
    private static Group  rootGroup;
    private static final Scale SCALE = new Scale(1, 1, 0, 0);

    private SceneSwitcher() {}

    /** Initialize – call MainApplication only once */
    public static void init(Stage primaryStage, String firstFxml,
                            double baseWidth, double baseHeight) throws IOException {

        BASE_W = baseWidth;
        BASE_H = baseHeight;

        stage     = primaryStage;
        rootGroup = new Group();
        rootGroup.getTransforms().add(SCALE);

        Parent firstRoot = FXMLLoader.load(SceneSwitcher.class.getResource(firstFxml));
        rootGroup.getChildren().setAll(firstRoot);

        Scene scene = new Scene(rootGroup);
        stage.setScene(scene);
        stage.setWidth(baseWidth);   // ► Let Stage start with the design width
        stage.setHeight(baseHeight); // ► Let the stage start is the design draft


        /* Listen for window changes to maintain proportional scaling */
        ChangeListener<Number> listener = (o, ov, nv) -> refreshScale();
        stage.widthProperty().addListener(listener);
        stage.heightProperty().addListener(listener);

        stage.sizeToScene();   // The window matches the size of the fold first
        refreshScale();        // Initial calculations
        stage.show();
    }

    /** Default toggle: Inherit the current base size */
    public static void switchScene(String fxmlPath) {
        try {
            Parent newRoot = FXMLLoader.load(SceneSwitcher.class.getResource(fxmlPath));
            rootGroup.getChildren().setAll(newRoot);

           /* Remeasure the original size of the page, update the datum */
            newRoot.applyCss();
            newRoot.layout();
            BASE_W = newRoot.prefWidth(-1)  > 0 ? newRoot.prefWidth(-1)  : newRoot.getLayoutBounds().getWidth();
            BASE_H = newRoot.prefHeight(-1) > 0 ? newRoot.prefHeight(-1) : newRoot.getLayoutBounds().getHeight();

            //stage.sizeToScene();  // Let the window just wrap around the new page
            refreshScale();       // Rescale, center

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /* Manually specify the overload for the new datum size (optional) */
    public static void switchScene(String fxmlPath, double newBaseW, double newBaseH) {
        BASE_W = newBaseW;
        BASE_H = newBaseH;
        switchScene(fxmlPath);
    }

   /* Calculate Scale Factor & Center */
    private static void refreshScale() {
        double factor = Math.min(stage.getWidth() / BASE_W, stage.getHeight() / BASE_H);
        SCALE.setX(factor);
        SCALE.setY(factor);
        rootGroup.setTranslateX((stage.getWidth()  - BASE_W  * factor) / 2);
        rootGroup.setTranslateY((stage.getHeight() - BASE_H * factor) / 2);
    }
}