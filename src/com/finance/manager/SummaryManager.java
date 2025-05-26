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

    // Calculate total revenue and total expenses
    public double getTotalIncome() {
        BigDecimal totalIncome = BigDecimal.ZERO;
        for (Transaction transaction : transactionManager.getAllTransactions()) {
            if (transaction.getType().equals("Income")) {
                totalIncome = totalIncome.add(BigDecimal.valueOf(transaction.getAmount()));
            }
        }
        return totalIncome.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public double getTotalExpenditure() {
        BigDecimal totalExpenditure = BigDecimal.ZERO;
        for (Transaction transaction : transactionManager.getAllTransactions()) {
            if (transaction.getType().equals("Expenditure")) {
                totalExpenditure = totalExpenditure.add(BigDecimal.valueOf(transaction.getAmount()));
            }
        }
        return totalExpenditure.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    // Get a summary of total income and expenditure and income and expenditure for the current month
    public Map<String, Map<String, Double>> getSummary() {
        // Total income and expenditure
        double totalIncome = getTotalIncome();
        double totalExpenditure = getTotalExpenditure();
        double totalSurplus = totalIncome - totalExpenditure;

        //Income and expenditure for the month
        double monthlyIncome = transactionManager.getMonthlyIncome();
        double monthlyExpenditure = transactionManager.getMonthlyExpenditure();
        double monthlySurplus = monthlyIncome - monthlyExpenditure;

        // Build a summary of the results
        Map<String, Double> totalSummary = new HashMap<>();
        totalSummary.put("GROSS INCOME", totalIncome);
        totalSummary.put("Total Expenditure", totalExpenditure);
        totalSummary.put("Total Remainder", totalSurplus);

        Map<String, Double> monthlySummary = new HashMap<>();
        monthlySummary.put("This month's income", monthlyIncome);
        monthlySummary.put("Expenditure this month", monthlyExpenditure);
        monthlySummary.put("Remaining for the month", monthlySurplus);

        Map<String, Map<String, Double>> summary = new HashMap<>();
        summary.put("Total income and expenditure", totalSummary);
        summary.put("Income and expenditure for the month", monthlySummary);

        return summary;
    }
}