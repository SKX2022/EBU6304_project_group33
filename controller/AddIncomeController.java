package com.finance.controller;

import com.finance.manager.CategoryManager;
import com.finance.model.User;
import com.finance.session.Session;
import com.finance.utils.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AddIncomeController {

    @FXML private TextField categoryField;
    @FXML private Label errorLabel;

    private CategoryManager categoryManager;

    @FXML
    public void initialize() {
        User user = Session.getCurrentUser();
        if (user == null) {
            SceneSwitcher.switchScene("/view/Login.fxml");
            return;
        }
        categoryManager = new CategoryManager(user);
    }

    @FXML
    private void handleSave() {
        String name = categoryField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setText("分类名称不能为空");
            errorLabel.setVisible(true);
            return;
        }
        boolean ok = categoryManager.addCategory("收入", name);
        if (!ok) {
            errorLabel.setText("该分类已存在");
            errorLabel.setVisible(true);
            return;
        }
        SceneSwitcher.switchScene("/view/Home.fxml");
    }

    @FXML
    private void handleCancel() {
        SceneSwitcher.switchScene("/view/Home.fxml");
    }
}