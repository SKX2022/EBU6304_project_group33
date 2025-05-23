package com.finance.controller;

import com.finance.manager.CategoryManager;
import com.finance.manager.TransactionManager;
import com.finance.model.Category;
import com.finance.model.ExcelImporter;
import com.finance.model.Transaction;
import com.finance.model.User;
import com.finance.session.Session;
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
import java.util.UUID;
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
    private Button addButton;

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
    private ComboBox<String> typeComboBox;



    private ObservableList<Transaction> transactionsData = FXCollections.observableArrayList();
    private ComboBox<Category> dialogCategoryField;
    private DatePicker dialogDatePickerField;

    @FXML
    public void initialize() {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) return;

        TransactionManager transactionManager = new TransactionManager(currentUser);
        CategoryManager categoryManager = new CategoryManager(currentUser);
        LocalDate now = LocalDate.now();
        transactionsTable.setItems(transactionsData);
        categoryComboBox.setOnAction(event -> applyFilters(new ActionEvent()));
        filterComboBox.setOnAction(this::applyFilters);
        startDatePicker.setOnAction(this::applyFilters);
        endDatePicker.setOnAction(this::applyFilters);
        // 设置筛选条件
        filterComboBox.getItems().addAll("All", "Income", "Expenditure");
        filterComboBox.getSelectionModel().select(0);
        refreshTransactions();
        List<Category> categories = categoryManager.getCategories();
        categories.add(0, new Category("All", "ALL"));
        ObservableList<Category> categoryList = FXCollections.observableArrayList(categories);
        // 监听筛选器值变化
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("筛选器值变化: " + newVal);
            applyFilters(new ActionEvent()); // 触发过滤
        });

        // 监听交易数据变化
        transactionsData.addListener((Observable observable) -> {
            System.out.println("交易数据更新，当前数量: " + transactionsData.size());
        });
        // 设置数据源
        categoryComboBox.setItems(categoryList);

        // 设置显示转换器
        categoryComboBox.setConverter(new StringConverter<Category>() {
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

        // 设置默认选中项
        categoryComboBox.getSelectionModel().selectFirst();

        // 设置日期选择器格式
        startDatePicker.setConverter(new StringConverter<LocalDate>() {
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

        endDatePicker.setConverter(new StringConverter<LocalDate>() {
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

        // 设置表格列
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        projectColumn.setCellValueFactory(new PropertyValueFactory<>("project"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        // 加载数据
        refreshTransactions();
        transactionsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    deleteButton.setDisable(newSelection == null);
                });
    }

    @FXML
    /*private void applyFilters(ActionEvent event) {
        refreshTransactions();
    }*/

    private void refreshTransactions() {
        // 获取筛选条件
        String filterType = filterComboBox.getSelectionModel().getSelectedItem();
        Category selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        // 加载交易数据
        TransactionManager transactionManager = new TransactionManager(Session.getCurrentUser());
        List<Transaction> filteredTransactions = transactionManager.getAllTransactions(
        );

        transactionsData.setAll(filteredTransactions);
        transactionsTable.refresh();
        applyFilters(new ActionEvent()); // 显式触发过滤
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
                showErrorDialog("系统错误", "导入过程中发生未预期异常:\n" + e.toString());
            }
        }

    }

    /**
     * 显示信息对话框
     * @param title 对话框标题
     * @param message 提示信息
     */
    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // 添加确认按钮样式
        ButtonType okButton = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);


        alert.showAndWait();
    }



    @FXML
    private void applyFilters(ActionEvent event) {
        TransactionManager tm = new TransactionManager(Session.getCurrentUser());
        CategoryManager cm = new CategoryManager(Session.getCurrentUser());
        List<Transaction> rawData = tm.getAllTransactions();
        List<Category> data = cm.getCategories();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        // 正确获取分类名称（来自 categoryComboBox 的选中项）
        Category selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();

        transactionsData.setAll(rawData.stream()
                .filter(t -> {
                    LocalDate tradeDate = parseDate(t.getDate());

                    // 修正后的类型匹配逻辑
                    boolean typeMatch = switch (filterComboBox.getValue()) {
                        case "All" -> true;
                        case "Income" -> "收入".equals(t.getType());
                        case "Expenditure" -> "支出".equals(t.getType());
                        default -> false; // 处理未知类型
                    };

                    // 增强型日期匹配（处理空值和格式问题）
                    boolean dateMatch = true;

                    if (startDate != null) {
                        dateMatch = !tradeDate.isBefore(startDate);
                    }
                    if (endDate != null) {
                        dateMatch = dateMatch  && !tradeDate.isAfter(endDate);
                    }

                    boolean categoryMatch = true;
                    if (selectedCategory != null && !"ALL".equals(selectedCategory.getName())) {
                        // 直接比较字符串（假设 category 存储的是分类类型名称）
                        categoryMatch = selectedCategory.getName().equals(t.getCategory());
                    }
                    return typeMatch && dateMatch && categoryMatch ;
                })
                .collect(Collectors.toList()));
        transactionsTable.refresh();
    }

    // 辅助方法：安全解析日期
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException e) {
            // 处理解析异常（例如记录日志或返回默认值）
            return null;
        }
    }

    private boolean showConfirmDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType yesButton = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(yesButton, ButtonType.CANCEL);

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
        return dialog;
    }

    private Parent createDialogContent() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // 创建所有字段
        TextField typeField = createLabeledField("类型（收入/支出）", "textField_type");
        ComboBox<Category> categoryField = new ComboBox<>();
        categoryField.setItems(categoryComboBox.getItems());  // 复用主界面的分类数据
        categoryField.setPromptText("选择分类");
        categoryField.setConverter(categoryComboBox.getConverter());
        categoryField.setId(generateUniqueId("comboBox_category"));
        TextField amountField = createLabeledField("金额", "textField_amount");
        TextField descriptionField = createLabeledField("备注", "textField_description");

        dialogCategoryField = new ComboBox<>();
        dialogCategoryField.setItems(categoryComboBox.getItems());
        dialogCategoryField.setConverter(categoryComboBox.getConverter());
        grid.add(new Label("分类:"), 0, 1);
        grid.add(dialogCategoryField, 1, 1); // 布局位置根据实际调整

        // 布局配置
        grid.add(new Label("类型:"), 0, 0);
        grid.add(typeField, 1, 0);

        /*grid.add(new Label("分类:"), 0, 1);
        grid.add(categoryField, 1, 1);*/

        grid.add(new Label("金额:"), 0, 2);
        grid.add(amountField, 1, 2);

        grid.add(new Label("备注:"), 0, 4);
        grid.add(descriptionField, 1, 4);

        return grid;
    }

    // 通用组件创建方法
    private TextField createLabeledField(String labelText, String id) {
        TextField field = new TextField();
        field.setPromptText(labelText);
        field.setId(id);
        return field;
    }

    private ComboBox<String> createComboBox(String promptText, ObservableList<String> items) {
        ComboBox<String> comboBox = new ComboBox<>(items);
        comboBox.setPromptText(promptText);
        comboBox.setId(generateUniqueId("comboBox_"));
        comboBox.setPrefWidth(200);
        return comboBox;
    }

    private DatePicker createDatePicker(String promptText) {
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText(promptText);
        datePicker.setId(generateUniqueId("datePicker_"));
        datePicker.setConverter(new StringConverter<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : formatter.format(date);
            }

            @Override
            public LocalDate fromString(String string) {
                return string == null || string.isEmpty()
                        ? null
                        : LocalDate.parse(string, formatter);
            }
        });
        return datePicker;
    }


    private String generateUniqueId(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "");
    }

    private void configureDialogContent(TextInputDialog dialog) {
        // 设置唯一按钮类型
        dialog.getDialogPane().getButtonTypes().setAll(
                createButtonType("确定", ButtonBar.ButtonData.OK_DONE),
                createButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE)
        );

        // 添加验证逻辑
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
        ButtonType type = new ButtonType(text, data);
        return type;
    }

    private Object processValidInput(TextInputDialog dialog) {
        GridPane grid = (GridPane) dialog.getDialogPane().getContent();
        TextField typeField = (TextField) grid.lookup("#textField_type");
        TextField amountField = (TextField) grid.lookup("#textField_amount");
        ComboBox<Category> categoryField = dialogCategoryField; // 直接使用引用
        // 输入验证
        String type = typeField.getText().trim();
        if (!List.of("收入", "支出").contains(type)) {
            throw new IllegalArgumentException("无效的交易类型");
        }

        // 分类验证
        Category selectedCategory = categoryField.getSelectionModel().getSelectedItem();
        if (selectedCategory == null || "ALL".equals(selectedCategory.getName())) {
            throw new IllegalArgumentException("请选择有效分类");
        }
        String category = selectedCategory.getName();

        // 金额验证
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountField.getText().trim())
                    .setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("金额必须为有效数字");
        }


        // 创建交易记录
        Transaction newTransaction = new Transaction(
                type,
                category,
                amount.doubleValue(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), // 修正时间字段
                Session.getCurrentUser()
        );
        TransactionManager transactionManager = new TransactionManager(Session.getCurrentUser());
        transactionManager.addTransaction(
                newTransaction.getType(),
                newTransaction.getCategory(),
                newTransaction.getAmount(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        // 更新UI
        updateTransactionList(newTransaction);
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
        // 清空字段逻辑（需根据实际组件ID实现）
    }

    // 优化后的错误提示方法
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.initOwner(null);  // 确保无父窗口时正常显示
        alert.showAndWait();
    }

}



