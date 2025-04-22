package com.finance.ui;

import com.finance.controller.*;
import com.finance.model.*;
import com.finance.service.ThresholdCalculator;
import com.finance.findByDate.*;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FinanceTrackerUI extends Application {

    private SummaryManager summaryManager;
    private TransactionManager transactionManager;
    private CategoryManager categoryManager;
    private Register register = new Register();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("个人财务管理系统");

        // 创建根布局（使用 StackPane 来布局控件）
        StackPane root = new StackPane();

        // 设置背景图
        setBackgroundImage(root, "/Users/wangruizhi/Downloads/pexels-tirachard-kumtanom-112571-733852.jpg");

        // 登录和注册界面组件
        TextField loginUsernameField = new TextField();
        PasswordField loginPasswordField = new PasswordField();
        Button loginButton = new Button("登录");
        Button registerButton = new Button("注册");
        Button switchToRegisterButton = new Button("没有账号？点击注册");

        // 设置文本框的样式
        styleTextField(loginUsernameField);
        styleTextField(loginPasswordField);

        // 设置按钮的样式
        styleButton(loginButton);
        styleButton(registerButton);
        styleButton(switchToRegisterButton);

        // 创建标签并设置字体
        Label loginUsernameLabel = new Label("用户名：");
        Label loginPasswordLabel = new Label("密码：");

        // 设置标签样式
        styleLabelWithShadow(loginUsernameLabel);
        styleLabelWithShadow(loginPasswordLabel);

        // 登录面板
        VBox loginBox = new VBox(15);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(20));
        loginBox.setMaxWidth(400);
        loginBox.setMaxHeight(500);
        loginBox.getStyleClass().add("login-panel");
        loginBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.85); -fx-background-radius: 10;");

        loginBox.getChildren().addAll(
                createHeader("财务管理系统"),
                loginUsernameLabel, loginUsernameField,
                loginPasswordLabel, loginPasswordField,
                loginButton, switchToRegisterButton
        );

        // 注册界面组件
        TextField registerUsernameField = new TextField();
        PasswordField registerPasswordField = new PasswordField();
        Button switchToLoginButton = new Button("返回登录");

        // 设置注册界面组件的样式
        styleTextField(registerUsernameField);
        styleTextField(registerPasswordField);
        styleButton(switchToLoginButton);

        // 创建标签并设置字体
        Label registerUsernameLabel = new Label("设置用户名：");
        Label registerPasswordLabel = new Label("设置密码：");

        // 设置标签样式
        styleLabelWithShadow(registerUsernameLabel);
        styleLabelWithShadow(registerPasswordLabel);

        // 注册界面布局
        VBox registerBox = new VBox(15);
        registerBox.setAlignment(Pos.CENTER);
        registerBox.setPadding(new Insets(20));
        registerBox.setMaxWidth(400);
        registerBox.setMaxHeight(500);
        registerBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.85); -fx-background-radius: 10;");

        registerBox.getChildren().addAll(
                createHeader("用户注册"),
                registerUsernameLabel, registerUsernameField,
                registerPasswordLabel, registerPasswordField,
                registerButton, switchToLoginButton
        );

        // 将登录面板添加到根布局
        root.getChildren().add(loginBox);
        root.setAlignment(Pos.CENTER);

        // 登录按钮事件
        loginButton.setOnAction(e -> {
            String username = loginUsernameField.getText();
            String password = loginPasswordField.getText();

            // 用户登录验证逻辑
            User loggedInUser = null;
            for (User user : register.getUsers()) {
                if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                    loggedInUser = user;
                    break;
                }
            }

            if (loggedInUser != null) {
                showAlert(Alert.AlertType.INFORMATION, "登录成功", "欢迎回来，" + username + "！");
                categoryManager = new CategoryManager(loggedInUser);
                transactionManager = new TransactionManager(loggedInUser);
                showFinancePage(root, loggedInUser);
            } else {
                showAlert(Alert.AlertType.INFORMATION, "登录失败", "用户名或密码错误！");
            }
        });

        // 注册按钮事件
        registerButton.setOnAction(e -> {
            String username = registerUsernameField.getText();
            String password = registerPasswordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "注册失败", "用户名和密码不能为空！");
                return;
            }

            // 调用 registerUser 方法注册新用户
            if (register.registerUser(username, password)) {
                showAlert(Alert.AlertType.INFORMATION, "注册成功", "用户名：" + username + " 已注册！");
                root.getChildren().clear();
                root.getChildren().add(loginBox);
            } else {
                showAlert(Alert.AlertType.INFORMATION, "注册失败", "用户名已存在！");
            }
        });

        // 切换到注册界面
        switchToRegisterButton.setOnAction(e -> {
            root.getChildren().clear();
            root.getChildren().add(registerBox);
        });

        // 切换回登录界面
        switchToLoginButton.setOnAction(e -> {
            root.getChildren().clear();
            root.getChildren().add(loginBox);
        });

        // 设置场景和舞台
        Scene scene = new Scene(root, 791, 551);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // 创建标题
    private Label createHeader(String text) {
        Label header = new Label(text);
        header.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        header.setStyle("-fx-text-fill: #2c3e50; -fx-padding: 0 0 20 0;");
        return header;
    }

    // 设置背景图的方法
    private void setBackgroundImage(StackPane pane, String imagePath) {
        BackgroundImage backgroundImage = new BackgroundImage(
                new Image("file:" + imagePath),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(1.0, 1.0, true, true, false, false)
        );
        pane.setBackground(new Background(backgroundImage));
    }

    // 样式设置：美化文本框
    private void styleTextField(TextField textField) {
        textField.setStyle("-fx-background-color: white; -fx-border-radius: 5px; " +
                "-fx-padding: 10px; -fx-font-size: 14px; -fx-font-family: 'Microsoft YaHei';");
        textField.setMaxWidth(300);
        textField.setPrefHeight(40);
    }

    // 样式设置：美化按钮
    private void styleButton(Button button) {
        button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-family: 'Microsoft YaHei'; -fx-font-weight: bold; " +
                "-fx-border-radius: 5px; -fx-padding: 10px 20px; -fx-cursor: hand;");
        button.setPrefHeight(40);
        button.setMaxWidth(300);
    }

    // 样式设置：为标签添加阴影效果
    private void styleLabelWithShadow(Label label) {
        label.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        label.setTextFill(Color.rgb(44, 62, 80));
        label.setStyle("-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 2, 0, 0, 1);");
    }

    // 弹出提示框
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 跳转到记账页面
    private void showFinancePage(StackPane root, User loggedInUser) {
        root.getChildren().clear();

        // 创建主容器
        BorderPane mainPane = new BorderPane();
        mainPane.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-background-radius: 10;");
        mainPane.setPadding(new Insets(20));
        mainPane.setPrefSize(700, 500);

        // 创建标题
        Label titleLabel = new Label("个人财务管理");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 22));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");
        HBox titleBox = new HBox(titleLabel);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(0, 0, 15, 0));

        // 交易记录输入框
        TextField transactionAmountField = new TextField();
        styleTextField(transactionAmountField);
        transactionAmountField.setPromptText("请输入金额");

        ComboBox<String> transactionTypeComboBox = new ComboBox<>();
        transactionTypeComboBox.getItems().addAll("请选择分类", "收入", "支出");
        transactionTypeComboBox.setValue("请选择分类");
        transactionTypeComboBox.setStyle("-fx-font-size: 14px; -fx-font-family: 'Microsoft YaHei';");

        // 分类选择框（初始为空）
        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().add("请选择分类");
        categoryComboBox.setValue("请选择分类");
        categoryComboBox.setStyle("-fx-font-size: 14px; -fx-font-family: 'Microsoft YaHei';");

        // 初始化管理器
        categoryManager = new CategoryManager(loggedInUser);
        transactionManager = new TransactionManager(loggedInUser);
        summaryManager = new SummaryManager(transactionManager, categoryManager);

        Button addTransactionButton = new Button("添加交易");
        styleButton(addTransactionButton);

        // 总收支汇总
        Label totalIncomeLabel = new Label("总收入：¥" + summaryManager.getTotalIncome());
        Label totalExpenditureLabel = new Label("总支出：¥" + summaryManager.getTotalExpenditure());
        Label totalSurplusLabel = new Label("剩余：¥" + (summaryManager.getTotalIncome() - summaryManager.getTotalExpenditure()));
        Label monthlyIncomeLabel = new Label("月度收入：¥" + transactionManager.getMonthlyIncome());
        Label monthlyExpenditureLabel = new Label("月度支出：¥" + transactionManager.getMonthlyExpenditure());
        Label monthlySurplusLabel = new Label("月度剩余：¥" + (transactionManager.getMonthlyIncome() - transactionManager.getMonthlyExpenditure()));

        // 设置标签样式
        styleSummaryLabel(totalIncomeLabel);
        styleSummaryLabel(totalExpenditureLabel);
        styleSummaryLabel(totalSurplusLabel);
        styleSummaryLabel(monthlyIncomeLabel);
        styleSummaryLabel(monthlyExpenditureLabel);
        styleSummaryLabel(monthlySurplusLabel);

        // 添加分类按钮
        Button addCategoryButton = new Button("添加分类");
        styleButton(addCategoryButton);
        addCategoryButton.setOnAction(e -> showAddCategoryDialog("收入", categoryComboBox));

        // 交易记录列表
        ListView<String> transactionRecordList = new ListView<>();
        transactionRecordList.setPrefWidth(400);
        transactionRecordList.setPrefHeight(200);
        transactionRecordList.setStyle("-fx-font-size: 13px; -fx-font-family: 'Microsoft YaHei';");

        ListView<String> transactionRecord2List = new ListView<>();
        transactionRecord2List.setPrefWidth(400);
        transactionRecord2List.setPrefHeight(200);
        transactionRecord2List.setStyle("-fx-font-size: 13px; -fx-font-family: 'Microsoft YaHei';");

        Button showTransactionButton = new Button("查看交易记录");
        styleButton(showTransactionButton);

        Button showDialogButton = new Button("查看类别交易记录");
        styleButton(showDialogButton);

        // 设置和导入按钮
        Button settingsButton = new Button("⚙ 设置阈值");
        styleButton(settingsButton);

        Button importExcelButton = new Button("📥 从Excel导入");
        styleButton(importExcelButton);

        Button findByDateButton = new Button("📅 按日期查询");
        styleButton(findByDateButton);

        // 交易记录布局
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(15);
        inputGrid.setPadding(new Insets(10));

        // 添加输入控件
        inputGrid.add(new Label("输入金额："), 0, 0);
        inputGrid.add(transactionAmountField, 1, 0);
        inputGrid.add(new Label("选择类型："), 0, 1);
        inputGrid.add(transactionTypeComboBox, 1, 1);
        inputGrid.add(new Label("选择类别："), 0, 2);
        inputGrid.add(categoryComboBox, 1, 2);
        inputGrid.add(addTransactionButton, 1, 3);

        // 设置标签样式
        inputGrid.getChildren().filtered(node -> node instanceof Label)
                .forEach(node -> ((Label) node).setFont(Font.font("Microsoft YaHei", 14)));

        // 按钮布局
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(addCategoryButton, showTransactionButton, showDialogButton);

        // 工具按钮布局
        HBox toolBox = new HBox(10);
        toolBox.setAlignment(Pos.CENTER);
        toolBox.getChildren().addAll(settingsButton, importExcelButton, findByDateButton);

        // 汇总信息布局
        VBox summaryBox = new VBox(10);
        summaryBox.setPadding(new Insets(10));
        summaryBox.setAlignment(Pos.CENTER_LEFT);
        summaryBox.getChildren().addAll(
                totalIncomeLabel,
                totalExpenditureLabel,
                totalSurplusLabel,
                monthlyIncomeLabel,
                monthlyExpenditureLabel,
                monthlySurplusLabel
        );

        // 整合左侧面板
        VBox leftPanel = new VBox(20);
        leftPanel.getChildren().addAll(inputGrid, buttonBox, toolBox);

        // 整合右侧面板
        VBox rightPanel = new VBox(20);
        rightPanel.getChildren().addAll(summaryBox, transactionRecordList);

        // 设置主布局
        mainPane.setTop(titleBox);
        mainPane.setLeft(leftPanel);
        mainPane.setRight(rightPanel);

        // 将主面板添加到根布局
        root.getChildren().add(mainPane);

        // 更新分类下拉框
        updateCategoryComboBox("收入", categoryComboBox);

        // 监听收支类型变化，更新分类
        transactionTypeComboBox.setOnAction(e -> {
            String selectedType = transactionTypeComboBox.getValue();
            updateCategoryComboBox(selectedType, categoryComboBox);
        });

        // 阈值设置按钮事件
        settingsButton.setOnAction(e -> {
            UserThreshold threshold = ThresholdManager.loadThreshold(loggedInUser.getUsername());
            showThresholdSettingsDialog(threshold);
        });

        // 添加交易按钮事件
        addTransactionButton.setOnAction(e -> {
            try {
                String type = transactionTypeComboBox.getValue();
                String category = categoryComboBox.getValue();

                if ("请选择分类".equals(type) || "请选择分类".equals(category)) {
                    showAlert(Alert.AlertType.WARNING, "添加失败", "请选择交易类型和分类！");
                    return;
                }

                String amountText = transactionAmountField.getText().trim();
                if (amountText.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "添加失败", "请输入金额！");
                    return;
                }

                double amount = Double.parseDouble(amountText);
                String date = getCurrentDate();
                transactionManager.addTransaction(type, category, amount, date);
                showTransactionRecord(type, category, amount, transactionRecordList);

                // 清空输入框
                transactionAmountField.clear();

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
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.WARNING, "添加失败", "请输入有效的金额！");
            }
        });

        // 查看交易记录按钮事件
        // 查看交易记录按钮事件
        showTransactionButton.setOnAction(e -> {
            // 创建新的ListView以显示所有交易记录
            ListView<String> allTransactionsListView = new ListView<>();
            allTransactionsListView.setPrefWidth(400);
            allTransactionsListView.setPrefHeight(300);
            allTransactionsListView.setStyle("-fx-font-size: 13px; -fx-font-family: 'Microsoft YaHei';");

            // 加载所有交易记录（不过滤类别）
            List<Transaction> transactions = loadTransactionRecords(allTransactionsListView, null);

            // 计算总收支
            double totalIncome = 0;
            double totalExpense = 0;

            for (Transaction transaction : transactions) {
                if ("收入".equals(transaction.getType())) {
                    totalIncome += transaction.getAmount();
                } else if ("支出".equals(transaction.getType())) {
                    totalExpense += transaction.getAmount();
                }
            }

            // 创建并显示对话框
            Dialog<Void> dialog = new Dialog<>();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(String.format("总收入：¥%.2f   总支出：¥%.2f", totalIncome, totalExpense));
            dialog.getDialogPane().setContent(allTransactionsListView);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.getDialogPane().setPrefSize(500, 400);
            dialog.showAndWait();

            // 更新主界面收支标签
            monthlyIncomeLabel.setText("月收入：¥" + transactionManager.getMonthlyIncome());
            monthlyExpenditureLabel.setText("月支出：¥" + transactionManager.getMonthlyExpenditure());
            totalIncomeLabel.setText("总收入：¥" + summaryManager.getTotalIncome());
            totalExpenditureLabel.setText("总支出：¥" + summaryManager.getTotalExpenditure());
            totalSurplusLabel.setText("总剩余：¥" + (summaryManager.getTotalIncome() - summaryManager.getTotalExpenditure()));
        });

// 类别交易记录查看按钮事件保持不变
        showDialogButton.setOnAction(e -> {
            String category = categoryComboBox.getValue();
            if ("请选择分类".equals(category)) {
                showAlert(Alert.AlertType.WARNING, "查看失败", "请先选择一个分类！");
                return;
            }

            List<Transaction> transactions = loadTransactionRecords(transactionRecord2List, category);

            String inStr = "";
            String outStr = "";

            if (transactions.size() >= 0 && category != null) {
                double in = 0d;
                double out = 0d;
                for (Transaction transaction : transactions) {
                    if ("收入".equals(transaction.getType())) {
                        in += transaction.getAmount();
                    }
                    if ("支出".equals(transaction.getType())) {
                        out += transaction.getAmount();
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
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(inStr + "   " + outStr);
            dialog.getDialogPane().setContent(transactionRecord2List);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
        });
        // Excel导入按钮事件
        importExcelButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择Excel交易记录文件");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel文件", "*.xlsx")
            );

            File selectedFile = fileChooser.showOpenDialog(root.getScene().getWindow());
            if (selectedFile != null) {
                try {
                    List<String> errors = ExcelImporter.importTransactions(
                            loggedInUser,
                            selectedFile.getAbsolutePath()
                    );

                    if (errors.isEmpty()) {
                        showAlert(Alert.AlertType.INFORMATION, "导入成功", "成功导入交易记录！");
                    } else {
                        showErrorDialog("导入完成（含错误）", errors);
                    }
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "导入失败", "错误信息: " + ex.getMessage());
                }
            }
        });

        // 按日期查询按钮事件
        findByDateButton.setOnAction(e -> {
            Stage findByDateStage = new Stage();
            findByDateUI findByDatePage = new findByDateUI(loggedInUser);
            findByDatePage.start(findByDateStage);
        });
    }

    // 设置汇总标签样式
    private void styleSummaryLabel(Label label) {
        label.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        label.setTextFill(Color.rgb(44, 62, 80));
    }

    // 阈值设置对话框
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
                    // 保存阈值设置
                    ThresholdManager.saveThreshold(threshold);
                    showAlert(Alert.AlertType.INFORMATION, "设置成功", "消费提醒阈值已更新！");
                    return true;
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "格式错误", "请输入有效的数字！");
                }
            }
            return false;
        });

        dialog.showAndWait();
    }

    // 更新分类下拉框
    private void updateCategoryComboBox(String type, ComboBox<String> categoryComboBox) {
        if ("请选择分类".equals(type)) {
            categoryComboBox.getItems().clear();
            categoryComboBox.getItems().add("请选择分类");
            categoryComboBox.setValue("请选择分类");
        } else {
            categoryComboBox.getItems().clear();
            categoryComboBox.getItems().add("请选择分类");

            // 将 List<Category> 转换为 List<String>
            List<String> categoryNames = categoryManager.getCategories().stream()
                    .filter(category -> category.getType().equals(type)) // 根据类型过滤
                    .map(Category::getName) // 提取分类名称
                    .toList();

            if (categoryNames != null && !categoryNames.isEmpty()) {
                categoryComboBox.getItems().addAll(categoryNames);
            }

            categoryComboBox.setValue("请选择分类");
        }
    }

    // 显示添加分类对话框
    private void showAddCategoryDialog(String type, ComboBox<String> categoryComboBox) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("添加" + (type.equals("收入") ? "收入" : "支出") + "分类");

        // 创建分类选择和输入控件
        ComboBox<String> typeSelector = new ComboBox<>();
        typeSelector.getItems().addAll("收入", "支出");
        typeSelector.setValue(type);

        TextField categoryNameField = new TextField();
        categoryNameField.setPromptText("输入分类名称");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.add(new Label("分类类型："), 0, 0);
        grid.add(typeSelector, 1, 0);
        grid.add(new Label("分类名称："), 0, 1);
        grid.add(categoryNameField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        ButtonType addButtonType = new ButtonType("添加", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // 处理添加按钮事件
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String categoryName = categoryNameField.getText().trim();
                if (categoryName.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "添加失败", "分类名称不能为空！");
                    return null;
                }

                String selectedType = typeSelector.getValue();
                if (categoryManager.addCategory(selectedType, categoryName)) {
                    updateCategoryComboBox(selectedType, categoryComboBox);
                    return categoryName;
                } else {
                    showAlert(Alert.AlertType.WARNING, "添加失败", "分类已存在！");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    // 显示交易记录
    private void showTransactionRecord(String type, String category, double amount, ListView<String> recordListView) {
        String record = String.format("%s: %s  ¥%.2f  %s", type, category, amount, getCurrentDate());
        recordListView.getItems().add(record);
    }

    // 加载交易记录
    private List<Transaction> loadTransactionRecords(ListView<String> recordListView, String filterCategory) {
        recordListView.getItems().clear();
        List<Transaction> allTransactions = transactionManager.getAllTransactions();
        List<Transaction> filteredTransactions = new ArrayList<>();

        for (Transaction transaction : allTransactions) {
            boolean shouldAdd = filterCategory == null || filterCategory.equals("请选择分类") ||
                    (transaction.getCategory() != null && transaction.getCategory().equals(filterCategory));

            if (shouldAdd) {
                String record = String.format("%s: %s  ¥%.2f  %s",
                        transaction.getType(),
                        transaction.getCategory() != null ? transaction.getCategory() : "无分类",
                        transaction.getAmount(),
                        transaction.getDate());
                recordListView.getItems().add(record);
                filteredTransactions.add(transaction);
            }
        }

        return filteredTransactions;
    }

    // 获取当前日期时间
    private String getCurrentDate() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    // 检查阈值
    private void checkThresholds(UserThreshold threshold, double totalExpense, double remaining) {
        if (threshold != null) {
            Double expenseThreshold = threshold.getTotalExpenseThreshold();
            Double remainingThreshold = threshold.getRemainingThreshold();

            if (expenseThreshold != null && totalExpense > expenseThreshold) {
                showAlert(Alert.AlertType.WARNING, "支出警告",
                        String.format("本月总支出 (¥%.2f) 已超过设定阈值 (¥%.2f)！",
                                totalExpense, expenseThreshold));
            }

            if (remainingThreshold != null && remaining < remainingThreshold) {
                showAlert(Alert.AlertType.WARNING, "余额警告",
                        String.format("剩余金额 (¥%.2f) 已低于设定阈值 (¥%.2f)！",
                                remaining, remainingThreshold));
            }
        }
    }

    // 显示错误对话框
    private void showErrorDialog(String title, List<String> errors) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);

        ListView<String> errorListView = new ListView<>();
        errorListView.getItems().addAll(errors);
        errorListView.setPrefHeight(200);
        errorListView.setPrefWidth(400);

        dialog.getDialogPane().setContent(errorListView);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }
}