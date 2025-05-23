package com.finance.controller;

import com.finance.model.Register;
import com.finance.utils.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private final Register registerService = new Register();

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("请输入用户名和密码。", Alert.AlertType.WARNING);
            return;
        }

        boolean success = registerService.registerUser(username, password);
        if (success) {
            showAlert("注册成功，请登录。", Alert.AlertType.INFORMATION);
            SceneSwitcher.switchScene("/view/Login.fxml");
        } else {
            showAlert("用户名已存在。", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleGoToLogin(ActionEvent event) {
        SceneSwitcher.switchScene("//view/Login.fxml");
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("注册提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void goToLogin(ActionEvent event) {
        SceneSwitcher.switchScene("/view/Login.fxml");
    }

}
