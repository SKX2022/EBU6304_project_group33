package com.finance.controller;

import com.finance.manager.CategoryManager;
import com.finance.manager.TransactionManager;
import com.finance.model.Category;
import com.finance.model.ExcelImporter;
import com.finance.model.Transaction;
import com.finance.model.User;
import com.finance.service.LlmService; // 添加LlmService的导入
import com.finance.session.Session;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FlowController {
    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private ComboBox<Category> categoryComboBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Button deleteButton;

    @FXML
    private TableView<Transaction> transactionsTable;

    @FXML
    private TableColumn<Transaction, String> typeColumn;

    @FXML
    private TableColumn<Transaction, String> projectColumn;

    @FXML
    private TableColumn<Transaction, LocalDate> timeColumn;

    @FXML
    private TableColumn<Transaction, String> categoryColumn;

    @FXML
    private TableColumn<Transaction, Double> amountColumn;

    @FXML
    private Button aiSmartAddButton;

    private final ObservableList<Transaction> transactionsData = FXCollections.observableArrayList();
    private ComboBox<Category> dialogCategoryField;
    private DatePicker dialogDatePickerField;

    @FXML
    public void initialize() {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) return;

        CategoryManager categoryManager = new CategoryManager(currentUser);
        transactionsTable.setItems(transactionsData);
        categoryComboBox.setOnAction(event -> applyFilters(new ActionEvent()));
        filterComboBox.setOnAction(this::applyFilters);
        startDatePicker.setOnAction(this::applyFilters);
        endDatePicker.setOnAction(this::applyFilters);

        filterComboBox.getItems().addAll("All", "Income", "Expenditure");
        filterComboBox.getSelectionModel().select(0);
        refreshTransactions();
        List<Category> categories = categoryManager.getCategories();
        categories.add(0, new Category("All", "ALL"));
        ObservableList<Category> categoryList = FXCollections.observableArrayList(categories);

        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters(new ActionEvent()));

        transactionsData.addListener((Observable observable) -> {});

        categoryComboBox.setItems(categoryList);

        categoryComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Category category) {
                return category != null ? category.getName() : "";
            }

            @Override
            public Category fromString(String string) {
                return categoryList.stream()
                        .filter(c -> c.getName().equalsIgnoreCase(string.trim()))
                        .findFirst()
                        .orElse(null);
            }
        });

        categoryComboBox.getSelectionModel().selectFirst();

        startDatePicker.setConverter(new StringConverter<>() {
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });

        endDatePicker.setConverter(new StringConverter<>() {
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });

        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        projectColumn.setCellValueFactory(new PropertyValueFactory<>("project"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        refreshTransactions();
        transactionsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> deleteButton.setDisable(newSelection == null));

        aiSmartAddButton.setOnAction(event -> handleAiSmartAddTransaction());
    }

    @FXML
    private void refreshTransactions() {
        TransactionManager transactionManager = new TransactionManager(Session.getCurrentUser());
        List<Transaction> filteredTransactions = transactionManager.getAllTransactions();

        transactionsData.setAll(filteredTransactions);
        transactionsTable.refresh();
        applyFilters(new ActionEvent());
    }

    @FXML
    private void importTransactions(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导入Excel交易记录");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel 文件", "*.xlsx")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                List<String> errorLogs = ExcelImporter.importTransactions(
                        Session.getCurrentUser(),
                        selectedFile.getAbsolutePath()
                );

                if (!errorLogs.isEmpty()) {
                    showErrorDialog("导入错误", String.join("\n", errorLogs));
                } else {
                    showInfoDialog("导入成功", "成功导入 " + selectedFile.getName());
                    refreshTransactions();
                }
            } catch (Exception e) {
                showErrorDialog("系统错误", "导入过程中发生未预期异常:\n" + e);
            }
        }
    }

    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        try {
            alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
            alert.getDialogPane().getStyleClass().add("login-card");
        } catch (NullPointerException e) {
            System.err.println("无法加载CSS样式: " + e.getMessage());
        }

        ButtonType okButton = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        Button okButtonNode = (Button) alert.getDialogPane().lookupButton(okButton);
        okButtonNode.getStyleClass().add("button-blue");

        alert.showAndWait();
    }

    @FXML
    private void applyFilters(ActionEvent event) {
        TransactionManager tm = new TransactionManager(Session.getCurrentUser());
        CategoryManager cm = new CategoryManager(Session.getCurrentUser());
        List<Transaction> rawData = tm.getAllTransactions();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        Category selectedCategoryObj = categoryComboBox.getSelectionModel().getSelectedItem();

        transactionsData.setAll(rawData.stream()
                .filter(t -> {
                    LocalDate tradeDate = parseDate(t.getDate());
                    if (tradeDate == null) return false;

                    boolean typeMatch = switch (filterComboBox.getValue()) {
                        case "All" -> true;
                        case "Income" -> "收入".equals(t.getType());
                        case "Expenditure" -> "支出".equals(t.getType());
                        default -> false;
                    };

                    boolean dateMatch = true;
                    if (startDate != null) {
                        dateMatch = !tradeDate.isBefore(startDate);
                    }
                    if (endDate != null) {
                        dateMatch = dateMatch && !tradeDate.isAfter(endDate);
                    }

                    boolean categoryMatch = true;
                    if (selectedCategoryObj != null && !"ALL".equals(selectedCategoryObj.getName())) {
                        categoryMatch = selectedCategoryObj.getName().equals(t.getCategory());
                    }
                    return typeMatch && dateMatch && categoryMatch;
                })
                .collect(Collectors.toList()));
        transactionsTable.refresh();
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException e) {
            System.err.println("日期解析错误: " + dateStr + " - " + e.getMessage());
            return null;
        }
    }

    private boolean showConfirmDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        try {
            alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
            alert.getDialogPane().getStyleClass().add("login-card");
        } catch (NullPointerException e) {
            System.err.println("无法加载CSS样式: " + e.getMessage());
        }

        ButtonType yesButton = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, cancelButton);

        Button yesButtonNode = (Button) alert.getDialogPane().lookupButton(yesButton);
        Button cancelButtonNode = (Button) alert.getDialogPane().lookupButton(cancelButton);

        yesButtonNode.getStyleClass().add("button-blue");
        cancelButtonNode.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155;");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yesButton;
    }

    @FXML
    private void handleAddTransaction(ActionEvent event) {
        try {
            TextInputDialog dialog = createNewDialog();
            configureDialogContent(dialog);
            dialog.showAndWait();
        } catch (Exception e) {
            showErrorDialog("系统错误", "弹窗初始化失败：" + e.getMessage());
        }
    }

    private TextInputDialog createNewDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("新增交易记录");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setContent(createDialogContent());

        try {
            dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
            dialog.getDialogPane().getStyleClass().add("login-card");
        } catch (NullPointerException e) {
            System.err.println("无法加载CSS样式: " + e.getMessage());
        }
        return dialog;
    }

    private Parent createDialogContent() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.getStyleClass().add("classification-box");

        ComboBox<String> typeComboBoxLocal = new ComboBox<>();
        typeComboBoxLocal.getItems().addAll("收入", "支出");
        typeComboBoxLocal.setPromptText("选择类型");
        typeComboBoxLocal.setId("comboBox_type");
        typeComboBoxLocal.getStyleClass().add("input-field");
        typeComboBoxLocal.setPrefWidth(250);

        dialogCategoryField = new ComboBox<>();
        CategoryManager categoryManager = new CategoryManager(Session.getCurrentUser());
        List<Category> categories = categoryManager.getCategories();
        ObservableList<Category> categoryList = FXCollections.observableArrayList(
                categories.stream()
                        .filter(c -> !"ALL".equals(c.getType()))
                        .collect(Collectors.toList())
        );
        dialogCategoryField.setItems(categoryList);
        dialogCategoryField.setPromptText("选择分类");
        dialogCategoryField.getStyleClass().add("input-field");
        dialogCategoryField.setPrefWidth(250);
        dialogCategoryField.setConverter(new StringConverter<>() {
            @Override
            public String toString(Category category) {
                return category != null ? category.getName() : "";
            }

            @Override
            public Category fromString(String string) {
                return categoryList.stream()
                        .filter(c -> c.getName().equalsIgnoreCase(string.trim()))
                        .findFirst()
                        .orElse(null);
            }
        });

        TextField amountField = new TextField();
        amountField.setPromptText("请输入金额");
        amountField.setId("textField_amount");
        amountField.getStyleClass().add("input-field");
        amountField.setPrefWidth(250);

        dialogDatePickerField = new DatePicker(LocalDate.now());
        dialogDatePickerField.setPromptText("选择日期");
        dialogDatePickerField.setId("datePicker_date");
        dialogDatePickerField.getStyleClass().add("input-field");
        dialogDatePickerField.setPrefWidth(250);
        dialogDatePickerField.setConverter(new StringConverter<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(LocalDate date) {
                return date != null ? formatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty()
                        ? LocalDate.parse(string, formatter)
                        : null;
            }
        });

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("请输入备注说明");
        descriptionField.setId("textField_description");
        descriptionField.getStyleClass().add("input-field");
        descriptionField.setPrefWidth(250);

        Label typeLabel = new Label("交易类型:");
        Label categoryLabel = new Label("分类:");
        Label amountLabel = new Label("金额:");
        Label dateLabel = new Label("日期:");
        Label descriptionLabel = new Label("备注:");

        typeLabel.getStyleClass().add("label-section");
        categoryLabel.getStyleClass().add("label-section");
        amountLabel.getStyleClass().add("label-section");
        dateLabel.getStyleClass().add("label-section");
        descriptionLabel.getStyleClass().add("label-section");

        typeLabel.setStyle("-fx-font-size: 14px;");
        categoryLabel.setStyle("-fx-font-size: 14px;");
        amountLabel.setStyle("-fx-font-size: 14px;");
        dateLabel.setStyle("-fx-font-size: 14px;");
        descriptionLabel.setStyle("-fx-font-size: 14px;");

        grid.add(typeLabel, 0, 0);
        grid.add(typeComboBoxLocal, 1, 0);

        grid.add(categoryLabel, 0, 1);
        grid.add(dialogCategoryField, 1, 1);

        grid.add(amountLabel, 0, 2);
        grid.add(amountField, 1, 2);

        grid.add(dateLabel, 0, 3);
        grid.add(dialogDatePickerField, 1, 3);

        grid.add(descriptionLabel, 0, 4);
        grid.add(descriptionField, 1, 4);

        return grid;
    }

    private void configureDialogContent(TextInputDialog dialog) {
        ButtonType confirmButtonType = createButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = createButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(confirmButtonType, cancelButtonType);

        Button confirmButton = (Button) dialog.getDialogPane().lookupButton(confirmButtonType);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);

        confirmButton.getStyleClass().add("button-blue");
        cancelButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == null) return null;

            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                try {
                    return processValidInput(dialog).toString();
                } catch (Exception e) {
                    showErrorDialog("输入错误", e.getMessage());
                    return null;
                }
            }
            return null;
        });
    }

    private ButtonType createButtonType(String text, ButtonBar.ButtonData data) {
        return new ButtonType(text, data);
    }

    private Object processValidInput(TextInputDialog dialog) {
        GridPane grid = (GridPane) dialog.getDialogPane().getContent();
        ComboBox<String> typeComboBoxLocal = (ComboBox<String>) grid.lookup("#comboBox_type");
        TextField amountField = (TextField) grid.lookup("#textField_amount");
        TextField descriptionField = (TextField) grid.lookup("#textField_description");

        ComboBox<Category> categoryField = dialogCategoryField;
        DatePicker datePicker = dialogDatePickerField;

        String type = typeComboBoxLocal.getValue();
        if (type == null || !List.of("收入", "支出").contains(type)) {
            throw new IllegalArgumentException("请选择有效的交易类型（收入/支出）");
        }

        Category selectedCategory = categoryField.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            throw new IllegalArgumentException("请选择有效分类");
        }
        String category = selectedCategory.getName();

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountField.getText().trim())
                    .setScale(2, RoundingMode.HALF_UP);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("金额必须大于零");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("请输入有效的金额数字");
        }

        LocalDate selectedDate = datePicker.getValue();
        LocalDateTime dateTime;
        if (selectedDate == null) {
            dateTime = LocalDateTime.now();
        } else {
            dateTime = LocalDateTime.of(selectedDate, LocalDateTime.now().toLocalTime());
        }
        String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String description = descriptionField.getText().trim();

        TransactionManager transactionManager = new TransactionManager(Session.getCurrentUser());
        boolean success = transactionManager.addTransaction(
                type,
                category,
                amount.doubleValue(),
                formattedDate,
                description);

        if (!success) {
            throw new IllegalStateException("添加交易记录失败");
        }

        Transaction newTransaction = new Transaction(
                type,
                category,
                amount.doubleValue(),
                formattedDate,
                Session.getCurrentUser(),
                description);

        updateTransactionList(newTransaction);

        Platform.runLater(() -> showInfoDialog("操作成功", "交易记录已成功添加"));

        return newTransaction;
    }

    private void updateTransactionList(Transaction transaction) {
        Platform.runLater(() -> {
            transactionsData.add(transaction);
            transactionsTable.scrollTo(transaction);
            clearFormFields();
        });
    }

    private void clearFormFields() {
        // 清空字段逻辑
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        try {
            alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
            alert.getDialogPane().getStyleClass().add("login-card");
        } catch (NullPointerException e) {
            System.err.println("无法加载CSS样式: " + e.getMessage());
        }

        ButtonType okButton = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        Button okButtonNode = (Button) alert.getDialogPane().lookupButton(okButton);
        okButtonNode.getStyleClass().add("button-blue");

        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.initOwner(null);
        alert.showAndWait();
    }

    @FXML
    private void handleDeleteTransaction(ActionEvent event) {
        Transaction selectedTransaction = transactionsTable.getSelectionModel().getSelectedItem();
        if (selectedTransaction == null) {
            showErrorDialog("提示", "请先选择要删除的交易记录");
            return;
        }

        boolean confirmed = showConfirmDialog("确认删除", "确定要删除所选交易记录吗？此操作无法撤销。");
        if (confirmed) {
            TransactionManager transactionManager = new TransactionManager(Session.getCurrentUser());
            boolean success = transactionManager.deleteTransaction(selectedTransaction);

            if (success) {
                transactionsData.remove(selectedTransaction);
                transactionsTable.refresh();
                showInfoDialog("操作成功", "交易记录已成功删除");
            } else {
                showErrorDialog("操作失败", "删除交易记录时发生错误");
            }
        }
    }

    private void handleAiSmartAddTransaction() {
        try {
            TextInputDialog dialog = createAiInputDialog();
            dialog.showAndWait();
        } catch (Exception e) {
            showErrorDialog("系统错误", "AI智能录入初始化失败：" + e.getMessage());
        }
    }

    private TextInputDialog createAiInputDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("AI智能录入交易");
        dialog.setHeaderText(null);
        dialog.setContentText("请描述您的交易（例如：昨天在餐厅吃饭花了88元）：");

        try {
            dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
            dialog.getDialogPane().getStyleClass().add("login-card");
        } catch (NullPointerException e) {
            System.err.println("无法加载CSS样式: " + e.getMessage());
        }

        ButtonType confirmButtonType = new ButtonType("识别并添加", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(confirmButtonType, cancelButtonType);

        Button confirmButton = (Button) dialog.getDialogPane().lookupButton(confirmButtonType);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);

        confirmButton.getStyleClass().add("button-blue");
        cancelButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == null) return null;
            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                String userInput = dialog.getEditor().getText().trim();
                if (userInput.isEmpty()) {
                    showErrorDialog("输入错误", "请输入交易描述");
                    return null;
                }
                processAiTransaction(userInput);
                return userInput;
            }
            return null;
        });

        return dialog;
    }

    private void processAiTransaction(String userDescription) {
        showInfoDialog("处理中", "正在使用AI分析您的交易，请稍候...");

        CompletableFuture.runAsync(() -> {
            try {
                String prompt = "你是一个财务分析助手。请从用户输入中提取以下信息，格式为JSON：\n" +
                        "1. 交易类型(type)：'收入'或'支出'\n" +
                        "2. 分类(category)：例如'餐饮'、'工资'等\n" +
                        "3. 金额(amount)：数字\n" +
                        "4. 日期(date)：如果有明确日期，则提取；如果是'昨天'，转换为具体日期；如果没有提及日期，使用当天\n" +
                        "5. 备注(note)：交易的其他描述信息\n\n" +
                        "用户输入: \"" + userDescription + "\"\n" +
                        "只返回JSON格式，不要有其他解释。";

                LlmService llmService = new LlmService(prompt);
                llmService.callLlmApi();
                String result = llmService.getAnswer().trim();

                Platform.runLater(() -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode rootNode = mapper.readTree(result);

                        String type = rootNode.path("type").asText();
                        String category = rootNode.path("category").asText();
                        double amount = rootNode.path("amount").asDouble();
                        String dateStr = rootNode.path("date").asText();
                        String note = rootNode.path("note").asText();

                        LocalDateTime transactionDateTime;
                        try {
                            transactionDateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        } catch (Exception e) {
                            try {
                                LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                transactionDateTime = LocalDateTime.of(date, LocalDateTime.now().toLocalTime());
                            } catch (Exception e2) {
                                transactionDateTime = LocalDateTime.now();
                            }
                        }
                        String formattedDate = transactionDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                        CategoryManager categoryManager = new CategoryManager(Session.getCurrentUser());
                        boolean categoryExists = categoryManager.getCategories().stream()
                                                .anyMatch(c -> c.getType().equals(type) && c.getName().equals(category));

                        if (!categoryExists) {
                            boolean createCategory = showConfirmDialog(
                                "创建新分类",
                                "系统未找到分类 \"" + category + "\"，是否创建此分类？"
                            );

                            if (createCategory) {
                                categoryManager.addCategory(type, category);
                                showInfoDialog("分类创建成功", "已创建新分类：" + category);
                            } else {
                                showCategorySelectionDialog(type, category, amount, formattedDate, note);
                                return;
                            }
                        }

                        TransactionManager transactionManager = new TransactionManager(Session.getCurrentUser());
                        boolean success = transactionManager.addTransaction(type, category, amount, formattedDate, note);

                        if (success) {
                            Transaction newTransaction = new Transaction(
                                type, category, amount, formattedDate, Session.getCurrentUser(), note
                            );
                            updateTransactionList(newTransaction);
                            showInfoDialog("交易添加成功",
                                          "已添加" + type + "记录：\n" +
                                          "分类: " + category + "\n" +
                                          "金额: " + amount + "\n" +
                                          "日期: " + formattedDate.substring(0, 10) + "\n" +
                                          "备注: " + note);
                            refreshTransactions();
                        } else {
                            showErrorDialog("交易添加失败", "无法添加交易记录，请重试");
                        }

                    } catch (Exception e) {
                        showErrorDialog("解析错误", "无法解析AI返回的结果: " + e.getMessage() + "\n原始返回: " + result);
                    }
                });
            } catch (IOException | InterruptedException | LlmService.LlmServiceException e) {
                Platform.runLater(() -> showErrorDialog("AI服务错误", "调用AI服务失败: " + e.getMessage()));
            }
        });
    }

    private void showCategorySelectionDialog(String type, String suggestedCategory, double amount, String date, String note) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("选择分类");
        dialog.setHeaderText("请为您的" + type + "交易选择一个分类");

        CategoryManager categoryManager = new CategoryManager(Session.getCurrentUser());
        List<Category> categories = categoryManager.getCategories().stream()
                                    .filter(c -> c.getType().equals(type))
                                    .toList();

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(
            categories.stream().map(Category::getName).toList()
        );

        if (!categoryCombo.getItems().isEmpty()) {
            categoryCombo.getSelectionModel().select(0);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("选择分类:"), 0, 0);
        grid.add(categoryCombo, 1, 0);

        dialog.getDialogPane().setContent(grid);

        ButtonType confirmButtonType = new ButtonType("确认", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return categoryCombo.getValue();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selectedCategory -> {
            TransactionManager transactionManager = new TransactionManager(Session.getCurrentUser());
            boolean success = transactionManager.addTransaction(type, selectedCategory, amount, date, note);

            if (success) {
                Transaction newTransaction = new Transaction(
                    type, selectedCategory, amount, date, Session.getCurrentUser(), note
                );
                updateTransactionList(newTransaction);
                showInfoDialog("交易添加成功",
                              "已添加" + type + "记录：\n" +
                              "分类: " + selectedCategory + "\n" +
                              "金额: " + amount + "\n" +
                              "日期: " + date.substring(0, 10) + "\n" +
                              "备注: " + note);
                refreshTransactions();
            } else {
                showErrorDialog("交易添加失败", "无法添加交易记录，请重试");
            }
        });
    }
}
