package com.finance.service;

import com.finance.manager.TransactionManager;
import com.finance.model.Transaction;
import java.util.List;

public class ThresholdCalculator {
    private final TransactionManager transactionManager;

    public ThresholdCalculator(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    // 计算总支出
    public double calculateTotalExpenditure() {
        List<Transaction> transactions = transactionManager.getAllTransactions();
        return transactions.stream()
                .filter(t -> "支出".equals(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // 计算总收入
    public double calculateTotalIncome() {
        List<Transaction> transactions = transactionManager.getAllTransactions();
        return transactions.stream()
                .filter(t -> "收入".equals(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // 计算剩余金额
    public double calculateRemaining() {
        return calculateTotalIncome() - calculateTotalExpenditure();
    }
}