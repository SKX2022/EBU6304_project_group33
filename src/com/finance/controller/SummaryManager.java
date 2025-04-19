package com.finance.controller;

import java.util.*;
import com.finance.model.Transaction;
public class SummaryManager {

    private TransactionManager transactionManager;
    private CategoryManager categoryManager;

    public SummaryManager(TransactionManager transactionManager, CategoryManager categoryManager) {
        this.transactionManager = transactionManager;
        this.categoryManager = categoryManager;
    }

    // 计算总收入和总支出
    public double calculateTotalIncome() {
        double totalIncome = 0;
        for (Transaction transaction : transactionManager.getAllTransactions()) {
            if (transaction.getType().equals("收入")) {
                totalIncome += transaction.getAmount();
            }
        }
        return totalIncome;
    }

    public double calculateTotalExpenditure() {
        double totalExpenditure = 0;
        for (Transaction transaction : transactionManager.getAllTransactions()) {
            if (transaction.getType().equals("支出")) {
                totalExpenditure += transaction.getAmount();
            }
        }
        return totalExpenditure;
    }

    // 获取月度汇总
    public Map<String, Double> getMonthlySummary() {
        double totalIncome = calculateTotalIncome();
        double totalExpenditure = calculateTotalExpenditure();
        double savingsGoal = totalIncome - totalExpenditure;

        Map<String, Double> summary = new HashMap<>();
        summary.put("总收入", totalIncome);
        summary.put("总支出", totalExpenditure);
        summary.put("剩余", savingsGoal);
        return summary;
    }
}