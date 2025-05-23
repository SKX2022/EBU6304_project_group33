package com.finance.controller;

import com.finance.utils.SceneSwitcher;
import javafx.event.ActionEvent;

public class NavigationBarController {

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
}