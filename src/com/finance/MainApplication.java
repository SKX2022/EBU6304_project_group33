package com.finance;

import com.finance.utils.SceneSwitcher;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        /* 1280×800 为设计稿参考尺寸，按需修改 */
        SceneSwitcher.init(stage, "/view/Login.fxml", 1200, 800);
        stage.setWidth(1200);   // 你想要的启动宽度
        stage.setHeight(800);   // 你想要的启动高度
        stage.centerOnScreen(); // 可选：让窗口居屏幕中央
    }

    public static void main(String[] args) { launch(args); }
}