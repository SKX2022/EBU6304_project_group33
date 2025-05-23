package com.finance.controller;

import com.finance.manager.CategoryManager;
import com.finance.manager.SummaryManager;
import com.finance.manager.TransactionManager;
import com.finance.model.Category;
import com.finance.model.User;
import com.finance.session.Session;
import com.finance.utils.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.event.EventHandler;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.text.DecimalFormat;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class HomeController {

    @FXML private Label monthlyIncomeLabel;
    @FXML private Label monthlyExpenseLabel;
    @FXML private Label monthlySurplusLabel;
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpenseLabel;
    @FXML private Label totalSurplusLabel;

    @FXML private ProgressBar incomeProgressBar;
    @FXML private ProgressBar expenseProgressBar;
    @FXML private ProgressBar surplusProgressBar;

    @FXML private ListView<String> incomeCategoryList;
    @FXML private ListView<String> expenseCategoryList;

    private CategoryManager categoryManager;
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = Session.getCurrentUser();
        if (currentUser == null) return;

        categoryManager = new CategoryManager(currentUser);
        TransactionManager transactionManager = new TransactionManager(currentUser);
        SummaryManager summaryManager = new SummaryManager(transactionManager, categoryManager);

        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        // 月收支
        double incomeMonth = transactionManager.getMonthlyIncome();
        double expenseMonth = transactionManager.getMonthlyExpenditure();

        // 使用BigDecimal计算盈余，确保精度
        BigDecimal surplusMonth = BigDecimal.valueOf(incomeMonth)
                .subtract(BigDecimal.valueOf(expenseMonth))
                .setScale(2, RoundingMode.HALF_UP);

        // 格式化显示
        DecimalFormat df = new DecimalFormat("#,##0.00");

        monthlyIncomeLabel.setText("Month Income：¥ " + df.format(incomeMonth));
        monthlyExpenseLabel.setText("Month Expenditure：¥ " + df.format(expenseMonth));
        monthlySurplusLabel.setText("Month Surplus：¥ " + df.format(surplusMonth));

        // 总收支
        double incomeTotal = summaryManager.getTotalIncome();
        double expenseTotal = summaryManager.getTotalExpenditure();

        // 使用BigDecimal计算盈余
        BigDecimal surplusTotal = BigDecimal.valueOf(incomeTotal)
                .subtract(BigDecimal.valueOf(expenseTotal))
                .setScale(2, RoundingMode.HALF_UP);

        totalIncomeLabel.setText("Total Income：¥ " + df.format(incomeTotal));
        totalExpenseLabel.setText("Total Expenditure：¥ " + df.format(expenseTotal));
        totalSurplusLabel.setText("Total Surplus：¥ " + df.format(surplusTotal));

        // 分类展示
        updateCategoryLists();

        // 设置进度条（基于最大值）
        BigDecimal bdIncomeTotal = BigDecimal.valueOf(incomeTotal);
        BigDecimal bdExpenseTotal = BigDecimal.valueOf(expenseTotal);
        BigDecimal maxValue = bdIncomeTotal.max(bdExpenseTotal).max(BigDecimal.ONE);

        // 计算比例并转换为double用于进度条
        incomeProgressBar.setProgress(bdIncomeTotal.divide(maxValue, 10, RoundingMode.HALF_UP).doubleValue());
        expenseProgressBar.setProgress(bdExpenseTotal.divide(maxValue, 10, RoundingMode.HALF_UP).doubleValue());

        // 盈余进度条处理，确保不为负
        BigDecimal surplusRatio = surplusTotal.divide(maxValue, 10, RoundingMode.HALF_UP);
        double surplusProgress = Math.max(surplusRatio.doubleValue(), 0);
        surplusProgressBar.setProgress(surplusProgress);
    }

    // 更新分类列表方法
    private void updateCategoryLists() {
        // 清空现有列表
        incomeCategoryList.getItems().clear();
        expenseCategoryList.getItems().clear();

        // 创建新的CategoryManager实例以确保获取最新数据
        categoryManager = new CategoryManager(currentUser);

        // 获取最新的分类列表
        List<Category> allCategories = categoryManager.getCategories();
        for (Category c : allCategories) {
            if ("收入".equals(c.getType())) {
                incomeCategoryList.getItems().add(c.getName());
            } else if ("支出".equals(c.getType())) {
                expenseCategoryList.getItems().add(c.getName());
            }
        }
    }

    // 创建一个窗口关闭事件监听器，用于在添加分类窗口关闭后刷新分类列表
    private EventHandler<WindowEvent> createWindowCloseHandler() {
        return event -> updateCategoryLists();
    }

    @FXML
    private void goIncomeAnalysis(ActionEvent event) {
        SceneSwitcher.switchScene("view/IncomeAnalysis.fxml");
    }

    @FXML
    private void goExpenditureAnalysis(ActionEvent event) {
        SceneSwitcher.switchScene("view/ExpenditureAnalysis.fxml");
    }

    @FXML
    private void deleteExpenseCategory() {
        String selectedCategory = expenseCategoryList.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            showAlert("警告", "请先选择一个要删除的支出分类", Alert.AlertType.WARNING);
            return;
        }

        // 弹出确认对话框
        boolean confirmed = showConfirmationDialog("删除分类",
                "确定要删除支出分类 \"" + selectedCategory + "\" 吗？\n删除后相关数据将无法恢复。");

        if (confirmed) {
            boolean deleted = categoryManager.deleteCategory("支出", selectedCategory);
            if (deleted) {
                updateCategoryLists(); // 更新列表显示
                showAlert("成功", "支出分类已删除", Alert.AlertType.INFORMATION);
            } else {
                showAlert("错误", "删除分类失败", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void deleteIncomeCategory() {
        String selectedCategory = incomeCategoryList.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            showAlert("警告", "请先选择一个要删除的收入分类", Alert.AlertType.WARNING);
            return;
        }

        // 弹出确认对话框
        boolean confirmed = showConfirmationDialog("删除分类",
                "确定要删除收入分类 \"" + selectedCategory + "\" 吗？\n删除后相关数据将无法恢复。");

        if (confirmed) {
            boolean deleted = categoryManager.deleteCategory("收入", selectedCategory);
            if (deleted) {
                updateCategoryLists(); // 更新列表显示
                showAlert("成功", "收入分类已删除", Alert.AlertType.INFORMATION);
            } else {
                showAlert("错误", "删除分类失败", Alert.AlertType.ERROR);
            }
        }
    }

    // 显示确认对话框
    private boolean showConfirmationDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // 显示提示信息
    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goAddExpense(ActionEvent event) {
        Window owner = ((Node) event.getSource()).getScene().getWindow();
        showAddDialog("/view/AddExpense.fxml", "Add Expense Category", owner);
    }

    @FXML
    private void goAddIncome(ActionEvent event) {
        Window owner = ((Node) event.getSource()).getScene().getWindow();
        showAddDialog("/view/AddIncome.fxml", "Add Income Category", owner);
    }

    private void showAddDialog(String fxmlPath, String title, Window owner) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));

            Stage dialog = new Stage();
            dialog.setTitle(title);
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initOwner(owner);      // 由调用者传入的主窗口
            dialog.setResizable(false);

            // 添加窗口关闭事件监听器，当窗口关闭时刷新分类列表
            dialog.setOnHidden(createWindowCloseHandler());

            dialog.show();                // 非模态；想阻塞主窗用 showAndWait()

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
