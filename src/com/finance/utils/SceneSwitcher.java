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

    private static double BASE_W, BASE_H;       // 当前页面基准宽高
    private static Stage  stage;
    private static Group  rootGroup;
    private static final Scale SCALE = new Scale(1, 1, 0, 0);

    private SceneSwitcher() {}

    /** 初始化 – 仅在 MainApplication 调一次 */
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

        /* 监听窗口变化保持等比缩放 */
        ChangeListener<Number> listener = (o, ov, nv) -> refreshScale();
        stage.widthProperty().addListener(listener);
        stage.heightProperty().addListener(listener);

        stage.sizeToScene();   // 窗口先匹配首屏尺寸
        refreshScale();        // 初次计算
        stage.show();
    }

    /** 默认切换：沿用当前基准尺寸 */
    public static void switchScene(String fxmlPath) {
        try {
            Parent newRoot = FXMLLoader.load(SceneSwitcher.class.getResource(fxmlPath));
            rootGroup.getChildren().setAll(newRoot);

            /* 重新测量页面原始尺寸，更新基准 */
            newRoot.applyCss();
            newRoot.layout();
            BASE_W = newRoot.prefWidth(-1)  > 0 ? newRoot.prefWidth(-1)  : newRoot.getLayoutBounds().getWidth();
            BASE_H = newRoot.prefHeight(-1) > 0 ? newRoot.prefHeight(-1) : newRoot.getLayoutBounds().getHeight();

            stage.sizeToScene();  // 让窗口刚好包住新页面
            refreshScale();       // 重新缩放、居中

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* 手动指定新基准尺寸的重载（可选） */
    public static void switchScene(String fxmlPath, double newBaseW, double newBaseH) {
        BASE_W = newBaseW;
        BASE_H = newBaseH;
        switchScene(fxmlPath);
    }

    /* 计算缩放因子 & 居中 */
    private static void refreshScale() {
        double factor = Math.min(stage.getWidth() / BASE_W, stage.getHeight() / BASE_H);
        SCALE.setX(factor);
        SCALE.setY(factor);
        rootGroup.setTranslateX((stage.getWidth()  - BASE_W  * factor) / 2);
        rootGroup.setTranslateY((stage.getHeight() - BASE_H * factor) / 2);
    }
}