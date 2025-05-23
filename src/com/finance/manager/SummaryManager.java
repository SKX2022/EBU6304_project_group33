package com.finance.manager;

import com.finance.model.Transaction;

import java.util.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
public class SummaryManager {

    private TransactionManager transactionManager;
    private CategoryManager categoryManager;

    public SummaryManager(TransactionManager transactionManager, CategoryManager categoryManager) {
        this.transactionManager = transactionManager;
        this.categoryManager = categoryManager;
    }

    // 计算总收入和总支出
    public double getTotalIncome() {
        BigDecimal totalIncome = BigDecimal.ZERO;
        for (Transaction transaction : transactionManager.getAllTransactions()) {
            if (transaction.getType().equals("收入")) {
                totalIncome = totalIncome.add(BigDecimal.valueOf(transaction.getAmount()));
            }
        }
        return totalIncome.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public double getTotalExpenditure() {
        BigDecimal totalExpenditure = BigDecimal.ZERO;
        for (Transaction transaction : transactionManager.getAllTransactions()) {
            if (transaction.getType().equals("支出")) {
                totalExpenditure = totalExpenditure.add(BigDecimal.valueOf(transaction.getAmount()));
            }
        }
        return totalExpenditure.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    // 获取总收支和本月收支汇总
    public Map<String, Map<String, Double>> getSummary() {
        // 总收支
        double totalIncome = getTotalIncome();
        double totalExpenditure = getTotalExpenditure();
        double totalSurplus = totalIncome - totalExpenditure;

        // 本月收支
        double monthlyIncome = transactionManager.getMonthlyIncome();
        double monthlyExpenditure = transactionManager.getMonthlyExpenditure();
        double monthlySurplus = monthlyIncome - monthlyExpenditure;

        // 构建汇总结果
        Map<String, Double> totalSummary = new HashMap<>();
        totalSummary.put("总收入", totalIncome);
        totalSummary.put("总支出", totalExpenditure);
        totalSummary.put("总剩余", totalSurplus);

        Map<String, Double> monthlySummary = new HashMap<>();
        monthlySummary.put("本月收入", monthlyIncome);
        monthlySummary.put("本月支出", monthlyExpenditure);
        monthlySummary.put("本月剩余", monthlySurplus);

        Map<String, Map<String, Double>> summary = new HashMap<>();
        summary.put("总收支", totalSummary);
        summary.put("本月收支", monthlySummary);

        return summary;
    }
}