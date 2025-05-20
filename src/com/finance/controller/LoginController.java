
package com.finance.controller;

import com.finance.model.User;
import com.finance.model.Login;
import com.finance.session.Session;
import com.finance.utils.DataPersistence;
import com.finance.utils.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.List;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        List<User> users = DataPersistence.loadUsers();
        Login login = new Login(users);

        boolean success = login.loginUser(username, password);
        if (success) {
            // 查找用户对象并存入 Session
            for (User u : users) {
                if (u.getUsername().equals(username)) {
                    Session.setCurrentUser(u);
                    break;
                }
            }
            SceneSwitcher.switchScene("/view/Home.fxml");
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("登录失败");
            alert.setHeaderText(null);
            alert.setContentText("用户名或密码错误");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleGoToRegister(ActionEvent event) {
        SceneSwitcher.switchScene("/view/Register.fxml");
    }
}
