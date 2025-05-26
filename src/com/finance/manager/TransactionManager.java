package com.finance.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.model.Transaction;
import com.finance.model.User;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class TransactionManager {

    private User user;
    private List<Transaction> transactions = new ArrayList<>();
    private static final String TRANSACTIONS_FILE_PREFIX = "transactions_";

    // The constructor receives the User parameter
    public TransactionManager(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        this.user = user;
        this.transactions = loadTransactions();  // Load the user's transaction history
    }

    // Load the user's transaction history
    private List<Transaction> loadTransactions() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File(TRANSACTIONS_FILE_PREFIX + user.getUsername() + ".json");
            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();  // If the file is empty, an empty list is returned
            }
            return objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, Transaction.class));
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();  // An empty list is returned when an exception occurs
        }
    }

    //Keep a record of the user's transactions
    private void saveTransactions() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(new File(TRANSACTIONS_FILE_PREFIX + user.getUsername() + ".json"), transactions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add transactions (with remarks)
    public boolean addTransaction(String type, String category, double amount, String date, String project) {
        try {
            Transaction transaction = new Transaction(type, category, amount, date, user, project);
            transactions.add(transaction);
            saveTransactions();  // Save the transaction to a file
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Maintain method compatibility with older versions
    public boolean addTransaction(String type, String category, double amount, String date) {
        return addTransaction(type, category, amount, date, "");
    }

    // Delete the transaction history
    public boolean deleteTransaction(Transaction transaction) {
        if (transaction == null) {
            return false;
        }

        try {
            // Find and delete matching transactions in the list
            boolean removed = false;
            for (int i = 0; i < transactions.size(); i++) {
                Transaction t = transactions.get(i);
                if (isSameTransaction(t, transaction)) {
                    transactions.remove(i);
                    removed = true;
                    break;
                }
            }

            if (removed) {
                saveTransactions(); // Save the updated transactions to a file
                return true;
            } else {
                return false; // No matching transactions found
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Compare whether the two transactions are the same
    private boolean isSameTransaction(Transaction t1, Transaction t2) {
        return t1.getType().equals(t2.getType()) &&
               t1.getCategory().equals(t2.getCategory()) &&
               Math.abs(t1.getAmount() - t2.getAmount()) < 0.001 && // 浮点数比较
               t1.getDate().equals(t2.getDate());
    }

    // Get a record of all transactions
    public List<Transaction> getAllTransactions() {
        return transactions;
    }


    // Modified the method of calculating the total revenue for the month to use BigDecimal
    public double getMonthlyIncome() {
        BigDecimal monthlyIncome = BigDecimal.ZERO;
        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("Income") && isCurrentMonth(transaction.getDate())) {
                monthlyIncome = monthlyIncome.add(BigDecimal.valueOf(transaction.getAmount()));
            }
        }
        return monthlyIncome.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    // Changed the method of calculating the total spend for the month to use BigDecimal
    public double getMonthlyExpenditure() {
        BigDecimal monthlyExpenditure = BigDecimal.ZERO;
        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("Expenditure") && isCurrentMonth(transaction.getDate())) {
                monthlyExpenditure = monthlyExpenditure.add(BigDecimal.valueOf(transaction.getAmount()));
            }
        }
        return monthlyExpenditure.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    // Determine whether the transaction date belongs to the current month
    private boolean isCurrentMonth(String date) {
        LocalDate transactionDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDate now = LocalDate.now();
        return transactionDate.getYear() == now.getYear() && transactionDate.getMonth() == now.getMonth();
    }

}
