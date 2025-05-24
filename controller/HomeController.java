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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;


import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.text.DecimalFormat;
import java.math.BigDecimal;
import java.math.RoundingMode;

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

    @FXML
    public void initialize() {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) return;

        TransactionManager transactionManager = new TransactionManager(currentUser);
        CategoryManager categoryManager = new CategoryManager(currentUser);
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
        List<Category> allCategories = categoryManager.loadCategories();
        for (Category c : allCategories) {
            if ("收入".equals(c.getType())) {
                incomeCategoryList.getItems().add(c.getName());
            } else if ("支出".equals(c.getType())) {
                expenseCategoryList.getItems().add(c.getName());
            }
        }

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


    @FXML
    private void goIncomeAnalysis(ActionEvent event) {
        SceneSwitcher.switchScene("view/IncomeAnalysis.fxml");
    }

    @FXML
    private void goExpenditureAnalysis(ActionEvent event) {
        SceneSwitcher.switchScene("view/ExpenditureAnalysis.fxml");
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
            dialog.show();                // 非模态；想阻塞主窗用 showAndWait()

        } catch (IOException e) {
            e.printStackTrace();
        }
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
    }}