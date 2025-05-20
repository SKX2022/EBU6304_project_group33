package com.finance.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneSwitcher {
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void switchScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(fxmlPath));
            Parent root = loader.load();
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("无法加载 FXML: " + fxmlPath);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}