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

        // 应用CSS样式
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/style.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("login-card");

        // 添加确认按钮样式
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

        // 应用CSS样式
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/style.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("login-card");

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

        // 应用CSS样式
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/style.css").toExternalForm()
        );
        dialog.getDialogPane().getStyleClass().add("login-card");

        return dialog;
    }

    private Parent createDialogContent() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.getStyleClass().add("classification-box");

        // 创建交易类型下拉框（收入/支出）
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("收入", "支出");
        typeComboBox.setPromptText("选择类型");
        typeComboBox.setId("comboBox_type");
        typeComboBox.getStyleClass().add("input-field");
        typeComboBox.setPrefWidth(250);

        // 创建分类下拉框
        dialogCategoryField = new ComboBox<>();
        // 从CategoryManager获取分类
        CategoryManager categoryManager = new CategoryManager(Session.getCurrentUser());
        List<Category> categories = categoryManager.getCategories();
        // 排除"All"分类
        ObservableList<Category> categoryList = FXCollections.observableArrayList(
                categories.stream()
                        .filter(c -> !"ALL".equals(c.getType()))
                        .collect(Collectors.toList())
        );
        dialogCategoryField.setItems(categoryList);
        dialogCategoryField.setPromptText("选择分类");
        dialogCategoryField.getStyleClass().add("input-field");
        dialogCategoryField.setPrefWidth(250);
        dialogCategoryField.setConverter(new StringConverter<Category>() {
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

        // 创建金额输入框
        TextField amountField = new TextField();
        amountField.setPromptText("请输入金额");
        amountField.setId("textField_amount");
        amountField.getStyleClass().add("input-field");
        amountField.setPrefWidth(250);

        // 创建日期选择器
        dialogDatePickerField = new DatePicker(LocalDate.now());
        dialogDatePickerField.setPromptText("选择日期");
        dialogDatePickerField.setId("datePicker_date");
        dialogDatePickerField.getStyleClass().add("input-field");
        dialogDatePickerField.setPrefWidth(250);
        dialogDatePickerField.setConverter(new StringConverter<LocalDate>() {
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

        // 创建备注输入框
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("请输入备注说明");
        descriptionField.setId("textField_description");
        descriptionField.getStyleClass().add("input-field");
        descriptionField.setPrefWidth(250);

        // 创建标签并设置样式
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

        // 设置标签样式
        typeLabel.setStyle("-fx-font-size: 14px;");
        categoryLabel.setStyle("-fx-font-size: 14px;");
        amountLabel.setStyle("-fx-font-size: 14px;");
        dateLabel.setStyle("-fx-font-size: 14px;");
        descriptionLabel.setStyle("-fx-font-size: 14px;");

        // 布局配置
        grid.add(typeLabel, 0, 0);
        grid.add(typeComboBox, 1, 0);

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
        // 设置按钮类型
        ButtonType confirmButtonType = createButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = createButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(confirmButtonType, cancelButtonType);

        // 设置按钮样式
        Button confirmButton = (Button) dialog.getDialogPane().lookupButton(confirmButtonType);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);

        confirmButton.getStyleClass().add("button-blue");
        cancelButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155;");

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
        ComboBox<String> typeComboBox = (ComboBox<String>) grid.lookup("#comboBox_type");
        TextField amountField = (TextField) grid.lookup("#textField_amount");
        TextField descriptionField = (TextField) grid.lookup("#textField_description");

        // 获取已经在类中持有引用的控件
        ComboBox<Category> categoryField = dialogCategoryField;
        DatePicker datePicker = dialogDatePickerField;

        // 类型验证
        String type = typeComboBox.getValue();
        if (type == null || !List.of("收入", "支出").contains(type)) {
            throw new IllegalArgumentException("请选择有效的交易类型（收入/支出）");
        }

        // 分类验证
        Category selectedCategory = categoryField.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            throw new IllegalArgumentException("请选择有效分类");
        }
        String category = selectedCategory.getName();

        // 金额验证
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

        // 日期处理
        LocalDate selectedDate = datePicker.getValue();
        LocalDateTime dateTime;
        if (selectedDate == null) {
            // 如果未选择日期，则��用当前系统时间
            dateTime = LocalDateTime.now();
        } else {
            // 如果已选择日期，则使用所选日期和当前时间
            dateTime = LocalDateTime.of(selectedDate, LocalDateTime.now().toLocalTime());
        }
        String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 获取备注信息
        String description = descriptionField.getText().trim();

        // 创建交易记录并保存
        TransactionManager transactionManager = new TransactionManager(Session.getCurrentUser());
        boolean success = transactionManager.addTransaction(
                type,
                category,
                amount.doubleValue(),
                formattedDate,
                description);  // 传递备注信息

        if (!success) {
            throw new IllegalStateException("添加交易记录失败");
        }

        // 创建返回的交易对象
        Transaction newTransaction = new Transaction(
                type,
                category,
                amount.doubleValue(),
                formattedDate,
                Session.getCurrentUser(),
                description);  // 传递备注信息

        // 更新UI
        updateTransactionList(newTransaction);

        // 成功提示
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
        // 清空字段逻辑（需根据实际组件ID实现）
    }

    // 优化后的错误提示方法
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // 应用CSS样式
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/style.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("login-card");

        ButtonType okButton = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        Button okButtonNode = (Button) alert.getDialogPane().lookupButton(okButton);
        okButtonNode.getStyleClass().add("button-blue");

        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.initOwner(null);  // 确保无父窗口时正常显示
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

            // 调用TransactionManager删除记录
            boolean success = transactionManager.deleteTransaction(selectedTransaction);

            if (success) {
                // 从UI列表中移除
                transactionsData.remove(selectedTransaction);
                transactionsTable.refresh();
                showInfoDialog("操作成功", "交易记录已成功删除");
            } else {
                showErrorDialog("操作失败", "删除交易记录时发生错误");
            }
        }
    }

}
