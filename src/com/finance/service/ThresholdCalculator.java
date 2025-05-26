package com.finance.service;

import com.finance.manager.TransactionManager;
import com.finance.model.Transaction;
import java.util.List;

public class ThresholdCalculator {
    private final TransactionManager transactionManager;

    public ThresholdCalculator(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    // Calculate the total spending
    public double calculateTotalExpenditure() {
        List<Transaction> transactions = transactionManager.getAllTransactions();
        return transactions.stream()
                .filter(t -> "Expenditure".equals(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // Calculate the total revenue
    public double calculateTotalIncome() {
        List<Transaction> transactions = transactionManager.getAllTransactions();
        return transactions.stream()
                .filter(t -> "Income".equals(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // Calculate the remaining amount
    public double calculateRemaining() {
        return calculateTotalIncome() - calculateTotalExpenditure();
    }
}