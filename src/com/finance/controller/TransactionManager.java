package com.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.model.Transaction;
import com.finance.model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionManager {

    private User user;
    private List<Transaction> transactions = new ArrayList<>();
    private static final String TRANSACTIONS_FILE_PREFIX = "transactions_";

    // 构造函数接收 User 参数
    public TransactionManager(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        this.user = user;
        this.transactions = loadTransactions();  // 加载用户的交易记录
    }

    // 加载用户的交易记录
    private List<Transaction> loadTransactions() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File(TRANSACTIONS_FILE_PREFIX + user.getUsername() + ".json");
            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();  // 文件为空，返回空列表
            }
            return objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, Transaction.class));
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();  // 发生异常时返回空列表
        }
    }

    // 保存用户的交易记录
    private void saveTransactions() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(new File(TRANSACTIONS_FILE_PREFIX + user.getUsername() + ".json"), transactions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 添加交易记录
    public void addTransaction(String type, String category, double amount, String date) {
        Transaction transaction = new Transaction(type, category, amount, date, user);
        transactions.add(transaction);
        saveTransactions();  // 保存交易记录到文件
    }

    // 获取所有交易记录
    public List<Transaction> getAllTransactions() {
        return transactions;
    }

    // 计算本月总收入
    public double getTotalIncome() {
        return transactions.stream()
                .filter(t -> t.getType().equals("收入"))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // 计算本月总支出
    public double getTotalExpenditure() {
        return transactions.stream()
                .filter(t -> t.getType().equals("支出"))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
}