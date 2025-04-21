package com.finance.ui;

import com.finance.controller.*;
import com.finance.model.*;
import com.finance.service.ThresholdCalculator; // 新增服务类

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class FinanceTrackerUI extends Application {


    private SummaryManager summaryManager;
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
        transactionTypeComboBox.getItems().addAll("请选择分类", "收入", "支出");

        // 分类选择框（初始为空）
        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().add("请选择分类");

        Button addTransactionButton = new Button("添加交易");
// 在类中声明
        SummaryManager summaryManager = new SummaryManager(transactionManager, categoryManager);
        // 总收支汇总
        Label totalIncomeLabel = new Label("总收入：¥" + summaryManager.getTotalIncome());
        Label totalExpenditureLabel = new Label("总支出：¥" + summaryManager.getTotalExpenditure());
        Label totalSurplusLabel = new Label("剩余：¥" + (summaryManager.getTotalIncome() - summaryManager.getTotalExpenditure()));
        Label monthlyIncomeLabel = new Label("月度收入：¥" + transactionManager.getMonthlyIncome());
        Label monthlyExpenditureLabel = new Label("月度支出：¥" + transactionManager.getMonthlyExpenditure());
        Label monthlySurplusLabel = new Label("月度剩余：¥" + (summaryManager.getTotalIncome() - summaryManager.getTotalExpenditure()));

        // 添加分类按钮
        Button addCategoryButton = new Button("添加分类");
        addCategoryButton.setOnAction(e -> showAddCategoryDialog("收入", categoryComboBox));

        // 交易记录列表
        ListView<String> transactionRecordList = new ListView<>();
        transactionRecordList.setPrefWidth(400);   // 设置宽度为 400
        transactionRecordList.setPrefHeight(200);  // 设置高度为 200

        ListView<String> transactionRecord2List = new ListView<>();
        transactionRecord2List.setPrefWidth(400);   // 设置宽度为 400
        transactionRecord2List.setPrefHeight(200);  // 设置高度为 200

        Button showTransactionButton = new Button("查看交易记录");
        Button showDialogButton = new Button("查看类别交易记录");

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
                showTransactionButton,
                showDialogButton
        );

        // 总汇总和交易记录布局
        VBox summaryAndRecordsBox = new VBox(10);
        summaryAndRecordsBox.setAlignment(Pos.CENTER);
        summaryAndRecordsBox.setPadding(new Insets(20, 20, 20, 20));
        summaryAndRecordsBox.getChildren().addAll(
                totalIncomeLabel, totalExpenditureLabel, totalSurplusLabel, transactionRecordList
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
        // 1. 添加设置阈值按钮
        Button settingsButton = new Button("⚙ 设置阈值");
        styleButton(settingsButton);
        transactionBox.getChildren().add(settingsButton); // 将按钮添加到交易输入区域

// 2. 设置按钮点击事件
        settingsButton.setOnAction(e -> {
            // 加载当前用户的阈值配置
            UserThreshold threshold = ThresholdManager.loadThreshold(loggedInUser.getUsername());
            showThresholdSettingsDialog(threshold);
        });

        // 添加交易按钮事件
        addTransactionButton.setOnAction(e -> {
            String type = transactionTypeComboBox.getValue();
            String category = categoryComboBox.getValue();
            double amount = Double.parseDouble(transactionAmountField.getText());
            String date = getCurrentDate(); // 获取当前时间
            transactionManager.addTransaction(type, category, amount, date);
            showTransactionRecord(type, category, amount, transactionRecordList);


            // 更新本月收支
            monthlyIncomeLabel.setText("本月总收入：¥" + transactionManager.getMonthlyIncome());
            monthlyExpenditureLabel.setText("本月总支出：¥" + transactionManager.getMonthlyExpenditure());
            monthlySurplusLabel.setText("本月剩余：¥" + (transactionManager.getMonthlyIncome() - transactionManager.getMonthlyExpenditure()));

            // 更新总收支
            ThresholdCalculator updatedCalculator = new ThresholdCalculator(transactionManager);
            totalIncomeLabel.setText("总收入：¥" + summaryManager.getTotalIncome());
            totalExpenditureLabel.setText("总支出：¥" + summaryManager.getTotalExpenditure());
            totalSurplusLabel.setText("总剩余：¥" + (summaryManager.getTotalIncome() - summaryManager.getTotalExpenditure()));
            UserThreshold currentThreshold = ThresholdManager.loadThreshold(loggedInUser.getUsername());
            checkThresholds(
                    currentThreshold,
                    updatedCalculator.calculateTotalExpenditure(),
                    updatedCalculator.calculateRemaining()
            );



            




        });

        // 查看交易记录按钮
        showTransactionButton.setOnAction(e -> {
            String category = categoryComboBox.getValue();
            List<Transaction> transactions = loadTransactionRecords(transactionRecordList, null);

            if (transactions.size() >= 0 && category != null) {
                double in = 0d;
                double out = 0d;
                for (int i = 0; i < transactions.size(); i++) {
                    if ("收入".equals(transactions.get(i).getType())) {
                        in += transactions.get(i).getAmount();
                    }
                    if ("支出".equals(transactions.get(i).getType())) {
                        out += transactions.get(i).getAmount();
                    }
                }
                if (in > 0) {
                    totalIncomeLabel.setText("总收入：¥" + summaryManager.getTotalIncome() + "  " + category + "收入：¥" + in);
                } else {
                    totalIncomeLabel.setText("总收入：¥" + summaryManager.getTotalIncome());
                }

                if (out > 0) {
                    totalExpenditureLabel.setText("总支出：¥" + summaryManager.getTotalExpenditure() + "  " + category + "支出：¥" + out);
                } else {
                    totalExpenditureLabel.setText("总支出：¥" + summaryManager.getTotalExpenditure());
                }
            } else {
                totalIncomeLabel.setText("总收入：¥" +summaryManager.getTotalIncome());
                totalExpenditureLabel.setText("总支出：¥" + summaryManager.getTotalExpenditure());
            }

        });

        showDialogButton.setOnAction(e -> {
            String category = categoryComboBox.getValue();
            List<Transaction> transactions = loadTransactionRecords(transactionRecord2List, category);

            String inStr = "";
            String outStr = "";

            if (transactions.size() >= 0 && category != null) {
                double in = 0d;
                double out = 0d;
                for (int i = 0; i < transactions.size(); i++) {
                    if ("收入".equals(transactions.get(i).getType())) {
                        in += transactions.get(i).getAmount();
                    }
                    if ("支出".equals(transactions.get(i).getType())) {
                        out += transactions.get(i).getAmount();
                    }
                }
                if (in > 0) {
                    inStr = category + "收入：¥" + in;
                }

                if (out > 0) {
                    outStr = category + "支出：¥" + out;
                }
            }

            Dialog<Void> dialog = new Dialog<>();
            dialog.initModality(Modality.APPLICATION_MODAL); // 设置为模态对话框
            dialog.setTitle(inStr + "   " + outStr);
            dialog.getDialogPane().setContent(transactionRecord2List); // 设置内容为ListView
            dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE); // 添加关闭按钮
            dialog.showAndWait(); // 显示对话框并等待关闭
        });
    }
    // 在 FinanceTrackerUI 类中添加以下方法
    private void showThresholdSettingsDialog(UserThreshold threshold) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("设置消费提醒阈值");

        // 创建输入字段
        TextField expenseField = new TextField();
        TextField remainingField = new TextField();
        expenseField.setPromptText("例：5000.0");
        remainingField.setPromptText("例：1000.0");

        // 显示当前值
        if (threshold.getTotalExpenseThreshold() != null) {
            expenseField.setText(String.valueOf(threshold.getTotalExpenseThreshold()));
        }
        if (threshold.getRemainingThreshold() != null) {
            remainingField.setText(String.valueOf(threshold.getRemainingThreshold()));
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("月总支出警戒线:"), expenseField);
        grid.addRow(1, new Label("最低余额警戒线:"), remainingField);
        dialog.getDialogPane().setContent(grid);

        // 添加按钮
        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // 保存处理
        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                try {
                    threshold.setTotalExpenseThreshold(Double.parseDouble(expenseField.getText()));
                    threshold.setRemainingThreshold(Double.parseDouble(remainingField.getText()));
                    ThresholdManager.saveThreshold(threshold);
                    return true;
                } catch (NumberFormatException ex) {
                    showAlert("输入错误", "请输入有效数字！");
                }
            }
            return null;
        });



        dialog.showAndWait();
    }
    // 在 FinanceTrackerUI 类中添加以下方法
    private void checkThresholds(UserThreshold threshold, double totalExpense, double remaining) {
        if (threshold.getTotalExpenseThreshold() != null &&
                totalExpense > threshold.getTotalExpenseThreshold()) {
            showAlert("超额警告", "本月总支出已超过设定阈值！\n当前支出：" + totalExpense);
        }

        if (threshold.getRemainingThreshold() != null &&
                remaining < threshold.getRemainingThreshold()) {
            showAlert("余额不足", "️剩余金额低于安全线！\n当前余额：" + remaining);
        }
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
    private List<Transaction> loadTransactionRecords(ListView<String> transactionRecordList, String category) {
        transactionRecordList.getItems().clear();
        List<Transaction> transactions = transactionManager.getAllTransactions();
        List<Transaction> list = new ArrayList<>();
        for (Transaction transaction : transactions) {

            if ( category != null && transaction != null && !"".equals(category) && !"请选择分类".equals(category)
                    && !category.equals(transaction.getCategory())) {
                transaction = null;
            }
            if (transaction != null) {
                String record = transaction.getType() + ": " + transaction.getCategory() + " ¥" + transaction.getAmount() + " 时间: " + transaction.getDate();
                transactionRecordList.getItems().add(record);  // 加载所有交易记录
                list.add(transaction);
            }

        }

        return list;
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