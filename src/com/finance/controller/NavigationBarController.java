package com.finance.controller;

import com.finance.utils.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class NavigationBarController /*implements Initializable*/ {

    public void goHome(ActionEvent event) {
        SceneSwitcher.switchScene("/view/Home.fxml");
    }

    public void goFlow(ActionEvent event) {
        SceneSwitcher.switchScene("/view/Flow.fxml");
    }

    public void goIncomeAnalysis(ActionEvent event) {
        SceneSwitcher.switchScene("/view/IncomeAnalysis.fxml");
    }

    public void goRevenue(ActionEvent event) {
        SceneSwitcher.switchScene("/view/Revenue.fxml");
    }

    public void goExpenditureAnalysis(ActionEvent event) {
        SceneSwitcher.switchScene("/view/ExpenditureAnalysis.fxml");
    }

    public void goGraph(ActionEvent event) {
        SceneSwitcher.switchScene("/view/Graph.fxml");
    }

    public void goLocalFinanceSettings(ActionEvent event) {
        SceneSwitcher.switchScene("/view/LocalFinanceSettings.fxml");
    }

    /*@FXML
    private HBox navigationBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始隐藏导航栏
        navigationBar.setVisible(false);
        navigationBar.setManaged(false);

        // 添加悬停监听器
        navigationBar.setOnMouseEntered(event -> showNavigationBar());
        navigationBar.setOnMouseExited(event -> hideNavigationBar());
    }

    private void showNavigationBar() {
        navigationBar.setVisible(true);
        navigationBar.setManaged(true);
    }

    private void hideNavigationBar() {
        navigationBar.setVisible(false);
        navigationBar.setManaged(false);
    }*/
}

