package com.finance.controller;

import com.finance.utils.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AddIncomeController {

    @FXML private TextField categoryField;
    @FXML private TextField amountField;
    @FXML private Label     errorLabel;

    @FXML
    private void handleSave() {
        // TODO: 实际保存逻辑
        SceneSwitcher.switchScene("/view/Home.fxml");
    }

    @FXML
    private void handleCancel() {
        SceneSwitcher.switchScene("/view/Home.fxml");
    }
}