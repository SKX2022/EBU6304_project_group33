package com.finance.controller;

import com.finance.manager.CategoryManager;
import com.finance.manager.TransactionManager;
import com.finance.model.Category;
import com.finance.model.ExcelImporter;
import com.finance.model.Transaction;
import com.finance.model.User;
import com.finance.session.Session;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
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

    private ObservableList<Transaction> transactionsData = FXCollections.observableArrayList();

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


    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
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


}
