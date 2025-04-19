package com.finance.ui;

import com.finance.controller.CategoryManager;
import com.finance.controller.Register;
import com.finance.controller.TransactionManager;
import com.finance.model.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Stack;

public class FinanceTrackerUI extends Application {

    private TransactionManager transactionManager;
    private CategoryManager categoryManager;
    private Register register = new Register();  // 使用 Register 对象

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("个人财务管理系统");

        // 创建根布局（使用 StackPane 来布局控件）
         Pane root = new  Pane();

        // 设置背景图
        setBackgroundImage(root, "/Users/wangruizhi/Downloads/pexels-tirachard-kumtanom-112571-733852.jpg");

        // 登录和注册界面组件
        TextField loginUsernameField = new TextField();
        PasswordField loginPasswordField = new PasswordField();
        Button loginButton = new Button("login");
        Button registerButton = new Button("register");
        Button switchToRegisterButton = new Button("No account? Register");

        // 设置文本框的样式
        styleTextField(loginUsernameField);
        styleTextField(loginPasswordField);

        // 设置按钮的样式
        styleButton(loginButton);
        styleButton(registerButton);
        styleButton(switchToRegisterButton);
// 创建标签并设置字体
        Label loginUsernameLabel = new Label("Username：");
        Label loginPasswordLabel = new Label("Password：");
        loginUsernameLabel.setAlignment(Pos.CENTER);
        loginPasswordLabel.setAlignment(Pos.CENTER);
// 设置字体大小、字体样式、加粗
        loginUsernameLabel.setStyle("-fx-font-family: 'Helvetica'; -fx-font-size: 34px; -fx-text-fill: black;-fx-font-weight: bold;");
        loginPasswordLabel.setStyle("-fx-font-family: 'Helvetica'; -fx-font-size: 34px; -fx-text-fill: black;-fx-font-weight: bold;");

        VBox loginBox = new VBox(15);
        loginBox.setAlignment(Pos.CENTER);  // 设置所有子控件在 VBox 中居中对齐
        loginBox.getChildren().addAll(loginUsernameLabel, loginUsernameField, loginPasswordLabel, loginPasswordField, loginButton, switchToRegisterButton);

        // 注册界面组件
        TextField registerUsernameField = new TextField();
        PasswordField registerPasswordField = new PasswordField();

        // 设置注册界面组件的样式
        styleTextField(registerUsernameField);
        styleTextField(registerPasswordField);

        // 注册界面布局
        VBox registerBox = new VBox(15);
        registerBox.getChildren().addAll(new Label("用户名："), registerUsernameField, new Label("密码："), registerPasswordField, registerButton);

        // 登录界面默认显示
        root.getChildren().add(loginBox);

        // 登录按钮事件
        loginButton.setOnAction(e -> {
            String username = loginUsernameField.getText();
            String password = loginPasswordField.getText();
            // 用户登录验证逻辑
            User loggedInUser = null;
            for (User user : register.getUsers()) { // 使用 Register 对象的 getUsers 方法
                if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                    loggedInUser = user;
                    break;
                }
            }

            if (loggedInUser != null) {
                showAlert("登录成功", "欢迎回来，" + username + "！");
                categoryManager = new CategoryManager(loggedInUser);  // 传递 loggedInUser 对象
                transactionManager = new TransactionManager(loggedInUser);  // 传递 loggedInUser 对象
                showFinancePage(root, loggedInUser);  // 登录成功后跳转到记账界面
            } else {
                showAlert("登录失败", "用户名或密码错误！");
            }
        });

        // 注册按钮事件
        registerButton.setOnAction(e -> {
            String username = registerUsernameField.getText();
            String password = registerPasswordField.getText();

            // 调用 registerUser 方法注册新用户
            if (register.registerUser(username, password)) {
                showAlert("注册成功", "用户名：" + username + " 已注册！");
                root.getChildren().clear();
                root.getChildren().add(loginBox);  // 切换回登录界面
            } else {
                showAlert("注册失败", "用户名已存在！");
            }
        });

        // 切换到注册界面
        switchToRegisterButton.setOnAction(e -> {
            root.getChildren().clear();
            root.getChildren().add(registerBox);  // 切换到注册界面
        });

        // 设置场景和舞台
        Scene scene = new Scene(root, 791, 551);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // 设置背景图的方法
    private void setBackgroundImage(Pane pane, String imagePath) {
        // 使用 file: 前缀来确保这是一个有效的文件路径
        Image image = new Image("file:" + imagePath);  // 加上 file: 前缀

        // 创建 ImageView 控件并设置图片
        ImageView imageView = new ImageView(image);

        // 让图片适应 Pane 尺寸
        imageView.setFitWidth(pane.getWidth());
        imageView.setFitHeight(pane.getHeight());
        imageView.setPreserveRatio(true);

        // 如果已经有背景图，先清除它
        if (!pane.getChildren().isEmpty()) {
            pane.getChildren().remove(0);  // 移除已存在的背景图
        }

        // 将 ImageView 作为背景添加到 Pane
        pane.getChildren().add(0, imageView); // 确保背景图在最底层

        // 监听窗口大小变化，自动调整背景图的尺寸
        pane.widthProperty().addListener((observable, oldValue, newValue) -> imageView.setFitWidth(newValue.doubleValue()));
        pane.heightProperty().addListener((observable, oldValue, newValue) -> imageView.setFitHeight(newValue.doubleValue()));
    }

    // 样式设置：美化文本框
    private void styleTextField(TextField textField) {
        textField.setStyle("-fx-background-color: white; -fx-border-radius: 5px; -fx-padding: 10px; -fx-font-size: 14px;");
        textField.setMaxWidth(300);  // 限制文本框的最大宽度
        textField.setPromptText("请输入内容"); // 设置提示文本
    }

    private void stylePasswordField(PasswordField passwordField) {
        passwordField.setStyle("-fx-background-color: white; -fx-border-radius: 5px; -fx-padding: 10px; -fx-font-size: 14px;");
        passwordField.setMaxWidth(300);  // 限制密码框的最大宽度
        passwordField.setPromptText("请输入密码"); // 设置提示文本
    }

    // 设置字体加粗和变大
    private void styleLabelAndFont(Label loginUsernameLabel, Label loginPasswordLabel) {
        loginUsernameLabel.setStyle("-fx-font-size: 160px; -fx-font-weight: bold;");
        loginPasswordLabel.setStyle("-fx-font-size: 160px; -fx-font-weight: bold;");
    }

    // 样式设置：美化按钮
    private void styleButton(Button button) {
        button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-radius: 5px; -fx-padding: 10px;");
    }

    // 弹出提示框
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    // 跳转到记账页面
    private void showFinancePage(Pane root, User loggedInUser) {
        // 创建背景图像
        Image backgroundImage = new Image("file:/Users/wangruizhi/Downloads/pexels-tirachard-kumtanom-112571-733852.jpg");  // 使用图片路径
        // 使用 setStyle() 设置背景图样式
        root.setStyle("-fx-background-image: url('" + backgroundImage.getUrl() + "'); " +
                "-fx-background-size: 100% 100%; " +  // 背景图覆盖整个布局
                "-fx-background-position: center center; " +  // 背景图居中
                "-fx-background-repeat: no-repeat;");  // 背景图不重复

        // 交易记录输入框
        TextField transactionAmountField = new TextField();
        ComboBox<String> transactionTypeComboBox = new ComboBox<>();
        transactionTypeComboBox.getItems().addAll("收入", "支出");

        // 分类选择框（初始为空）
        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().add("请选择分类");

        Button addTransactionButton = new Button("添加交易");

        // 本月收支汇总
        Label totalIncomeLabel = new Label("总收入：¥" + transactionManager.getTotalIncome());
        Label totalExpenditureLabel = new Label("总支出：¥" + transactionManager.getTotalExpenditure());
        Label surplusLabel = new Label("剩余：¥" + (transactionManager.getTotalIncome() - transactionManager.getTotalExpenditure()));

        // 添加分类按钮
        Button addCategoryButton = new Button("添加分类");
        addCategoryButton.setOnAction(e -> showAddCategoryDialog("收入", categoryComboBox));

        // 交易记录列表
        ListView<String> transactionRecordList = new ListView<>();
        transactionRecordList.setPrefWidth(400);   // 设置宽度为 400
        transactionRecordList.setPrefHeight(200);  // 设置高度为 200
        Button showTransactionButton = new Button("查看交易记录");

        // 交易记录布局
        VBox transactionBox = new VBox(10);
        transactionBox.setAlignment(Pos.CENTER);
        transactionBox.setPadding(new Insets(20, 20, 20, 20));
        transactionBox.getChildren().addAll(
                new Label("输入金额："), transactionAmountField,
                new Label("选择类型："), transactionTypeComboBox,
                new Label("选择类别："), categoryComboBox,
                addTransactionButton,
                addCategoryButton,
                showTransactionButton
        );

        // 本月汇总和交易记录布局
        VBox summaryAndRecordsBox = new VBox(10);
        summaryAndRecordsBox.setAlignment(Pos.CENTER);
        summaryAndRecordsBox.setPadding(new Insets(20, 20, 20, 20));
        summaryAndRecordsBox.getChildren().addAll(
                totalIncomeLabel, totalExpenditureLabel, surplusLabel, transactionRecordList
        );

        // 主容器，使用一个垂直布局 (VBox) 来安排两个部分：交易输入部分和汇总部分
        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setPadding(new Insets(30, 30, 30, 30)); // 给整个布局添加内边距
        mainLayout.getChildren().addAll(transactionBox, summaryAndRecordsBox);

        // 更新根布局
        root.getChildren().clear();
        root.getChildren().add(mainLayout);

        // 加载分类数据并更新分类下拉框
        updateCategoryComboBox("收入", categoryComboBox);

        // 监听收支类型变化，更新分类
        transactionTypeComboBox.setOnAction(e -> {
            String selectedType = transactionTypeComboBox.getValue();
            updateCategoryComboBox(selectedType, categoryComboBox);
        });

        // 添加交易按钮事件
        addTransactionButton.setOnAction(e -> {
            String type = transactionTypeComboBox.getValue();
            String category = categoryComboBox.getValue();
            double amount = Double.parseDouble(transactionAmountField.getText());
            String date = getCurrentDate(); // 获取当前时间
            transactionManager.addTransaction(type, category, amount, date);
            showTransactionRecord(type, category, amount, transactionRecordList);

            // 更新总收入和总支出
            totalIncomeLabel.setText("总收入：¥" + transactionManager.getTotalIncome());
            totalExpenditureLabel.setText("总支出：¥" + transactionManager.getTotalExpenditure());
            surplusLabel.setText("剩余：¥" + (transactionManager.getTotalIncome() - transactionManager.getTotalExpenditure()));
        });

        // 查看交易记录按钮
        showTransactionButton.setOnAction(e -> {
            loadTransactionRecords(transactionRecordList);
        });
    }

    // 获取当前系统时间
    private String getCurrentDate() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    // 更新分类选择框
    private void updateCategoryComboBox(String type, ComboBox<String> categoryComboBox) {
        categoryComboBox.getItems().clear();
        categoryComboBox.getItems().add("请选择分类");

        // 获取并显示用户的所有分类
        for (Category category : categoryManager.getCategories()) {
            categoryComboBox.getItems().add(category.getName());
        }
    }

    // 展示单个交易记录
    private void showTransactionRecord(String type, String category, double amount, ListView<String> transactionRecordList) {
        String record = type + ": " + category + " ¥" + amount + " 时间: " + getCurrentDate();
        transactionRecordList.getItems().add(record);  // 添加到交易记录列表中
    }

    // 加载交易记录
    private void loadTransactionRecords(ListView<String> transactionRecordList) {
        List<Transaction> transactions = transactionManager.getAllTransactions();
        for (Transaction transaction : transactions) {
            String record = transaction.getType() + ": " + transaction.getCategory() + " ¥" + transaction.getAmount() + " 时间: " + transaction.getDate();
            transactionRecordList.getItems().add(record);  // 加载所有交易记录
        }
    }

    // 弹出提示框

    // 添加收入或支出类别对话框
    private void showAddCategoryDialog(String categoryType, ComboBox<String> categoryComboBox) {
        // 创建文本输入对话框
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("添加分类");
        dialog.setHeaderText("请输入新的" + categoryType + "类别");

        // 显示对话框并获取用户输入
        dialog.showAndWait().ifPresent(newCategory -> {
            // 将用户输入的类别添加到分类管理器
            categoryManager.addCategory(newCategory);  // 这里调用了 CategoryManager 中的 addCategory 方法

            // 更新分类选择框
            updateCategoryComboBox(categoryType, categoryComboBox);
        });
    }
}