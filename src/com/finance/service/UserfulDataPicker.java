package com.finance.service;

import com.finance.session.Session;
import com.finance.model.User;
import com.finance.manager.TransactionManager;
import com.finance.model.Transaction;

import java.time.LocalDate; // 导入 LocalDate
import java.time.format.DateTimeParseException; // 导入日期解析异常
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.time.format.DateTimeFormatter; // 导入日期格式化器
import java.util.TreeMap; // 使用 TreeMap 保持日期排序


// This category is used to filter the daily expenditure, income, and balance data of a certain period
public class UserfulDataPicker {
    // Use TreeMap to ensure that the date keys in the Map are sorted
    private Map<String, Double> income = new TreeMap<>();
    private Map<String, Double> expenditure = new TreeMap<>();
    //Daily balance. We also sort by date and make sure it's a net balance on a particular day
    private Map<String, Double> dailySurplus = new TreeMap<>();
    //Convert all the details of income and expenditure in the time period into strings
    private List<String> transactionDetails = new ArrayList<>();


    //The maximum and minimum values should be calculated for each day's income, expenses, and surplus, not cumulative values or individual transactions
    private double maxOverallValue = 0.0;
    private double minOverallValue = 0.0; // Default initial value

    // Define a date formatter consistent with "yyyy-MM-dd" used in GraphController
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public UserfulDataPicker(String startDateStr, String endDateStr) {
        //Parse the incoming date string as a LocalDate object
        LocalDate startDate = null;
        LocalDate endDate = null;
        try {
            startDate = LocalDate.parse(startDateStr, DATE_FORMATTER);
            endDate = LocalDate.parse(endDateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing date strings in UserfulDataPicker: " + e.getMessage());
            // Handle date parsing errors, such as throwing an exception or setting a default value
            return; // If the date is invalid, the processing is not continued
        }

        // Get the transaction history of the current user
        User currentUser = Session.getCurrentUser();
        List<Transaction> transactions;
        TransactionManager transactionManager = new TransactionManager(currentUser);
        transactions = transactionManager.getAllTransactions();

        // Temporary storage of daily aggregate data
        Map<String, Double> tempIncome = new HashMap<>();
        Map<String, Double> tempExpenditure = new HashMap<>();


        // Step 1: Go through all the transactions to summarize income and expenses by date
        for (Transaction transaction : transactions) {
            // Parse the date of the transaction. Let's say transaction.getDate() returns a string in the format "yyyy-MM-dd".
            String transactionDateStr = transaction.getDate();
            LocalDate transactionDate;
            try {
                // Try to parse, if transaction.getDate() contains time, you need to intercept it first
                if (transactionDateStr.length() > 10) { //Check if there is a time part
                    transactionDate = LocalDate.parse(transactionDateStr.substring(0, 10), DATE_FORMATTER);
                } else {
                    transactionDate = LocalDate.parse(transactionDateStr, DATE_FORMATTER);
                }
            } catch (DateTimeParseException e) {
                System.err.println("Error parsing transaction date: " + transactionDateStr + " - " + e.getMessage());
                continue; // Skip transactions that can't be resolved
            }


            //Check if the transaction date is within the specified range
            if (!transactionDate.isBefore(startDate) && !transactionDate.isAfter(endDate)) {
                String formattedDate = transactionDate.format(DATE_FORMATTER); // Uniform format
                // Convert transaction information into strings and store them
                transactionDetails.add(String.format("日期: %s, 类型: %s, 分类：%s, 金额: %.2f", formattedDate, transaction.getType(),transaction.getCategory(), transaction.getAmount()));
                // Record the amount and type of transaction
                double amount = transaction.getAmount();
                String type = transaction.getType();

                if ("Expenditure".equals(type)) {
                    tempExpenditure.put(formattedDate, tempExpenditure.getOrDefault(formattedDate, 0.0) + amount);
                } else if ("Income".equals(type)) { // Let's assume that there are only two types: "income" and "expense".
                    tempIncome.put(formattedDate, tempIncome.getOrDefault(formattedDate, 0.0) + amount);
                }
            }
        }

        // Step 2: Fill in the final income, expenditure, dailySurplus Map, and calculate the maximum and minimum values
        //Iterate through each day in the selected range, ensuring that even if there are no transactions on a particular day, its value is 0
        LocalDate currentDay = startDate;
        while (!currentDay.isAfter(endDate)) {
            String formattedDay = currentDay.format(DATE_FORMATTER);

            double dailyInc = tempIncome.getOrDefault(formattedDay, 0.0);
            double dailyExp = tempExpenditure.getOrDefault(formattedDay, 0.0);
            double dailySur = dailyInc - dailyExp;

            this.income.put(formattedDay, dailyInc);
            this.expenditure.put(formattedDay, dailyExp);
            this.dailySurplus.put(formattedDay, dailySur); // Store your daily net balance

            //Updates the maximum and minimum values
            maxOverallValue = Math.max(maxOverallValue, dailyInc);
            maxOverallValue = Math.max(maxOverallValue, dailyExp);
            maxOverallValue = Math.max(maxOverallValue, dailySur); // Positive surplus
            minOverallValue = Math.min(minOverallValue, dailySur); // Negative surplus

            currentDay = currentDay.plusDays(1);
        }
    }

    // GET METHOD
    public Map<String, Double> getIncome() {
        return income;
    }

    public Map<String, Double> getExpenditure() {
        return expenditure;
    }

    // Provides a daily surplus map sorted by date
    public Map<String, Double> getDailySurplusMap() {
        return dailySurplus;
    }


    // Previously getSurplus() returned a List<Double>, now we have a Map by date

    // If the GraphController still needs a list<Double>, it can be converted like this:
    public List<Double> getSurplus() {
        return new ArrayList<>(dailySurplus.values());
    }

    // Gets the maximum amount or surplus value for the selected date range
    public double getMax() {
        return maxOverallValue;
    }

    // Gets the minimum amount or surplus value for the selected date range
    public double getMin() {
        return minOverallValue;
    }

    // Get a list of transaction details
    public List<String> getTransactionDetails() {
        return transactionDetails;
    }
}