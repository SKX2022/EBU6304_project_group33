package com.finance;

import com.finance.utils.SceneSwitcher;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) {
        SceneSwitcher.setPrimaryStage(stage);
        SceneSwitcher.switchScene("/view/Login.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }
}