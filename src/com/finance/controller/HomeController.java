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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;

import java.time.LocalDate;
import java.util.List;

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
        double surplusMonth = incomeMonth - expenseMonth;

        monthlyIncomeLabel.setText("本月收入：¥ " + incomeMonth);
        monthlyExpenseLabel.setText("本月支出：¥ " + expenseMonth);
        monthlySurplusLabel.setText("本月盈余：¥ " + surplusMonth);

        // 总收支
        double incomeTotal = summaryManager.getTotalIncome();
        double expenseTotal = summaryManager.getTotalExpenditure();
        double surplusTotal = incomeTotal - expenseTotal;

        totalIncomeLabel.setText("总收入：¥ " + incomeTotal);
        totalExpenseLabel.setText("总支出：¥ " + expenseTotal);
        totalSurplusLabel.setText("总盈余：¥ " + surplusTotal);

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
        double max = Math.max(Math.max(incomeTotal, expenseTotal), 1.0);
        incomeProgressBar.setProgress(incomeTotal / max);
        expenseProgressBar.setProgress(expenseTotal / max);
        surplusProgressBar.setProgress(Math.max(surplusTotal / max, 0));
    }

    @FXML
    private void goIncomeAnalysis(ActionEvent event) {
        SceneSwitcher.switchScene("view/IncomeAnalysis.fxml");
    }

    @FXML
    private void goExpenditureAnalysis(ActionEvent event) {
        SceneSwitcher.switchScene("view/ExpenditureAnalysis.fxml");
    }
}