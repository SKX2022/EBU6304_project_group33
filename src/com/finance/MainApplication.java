package com.finance;

import com.finance.utils.SceneSwitcher;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        SceneSwitcher.init(stage, "/view/Login.fxml", 900, 600);

    }

    public static void main(String[] args) { launch(args); }
}