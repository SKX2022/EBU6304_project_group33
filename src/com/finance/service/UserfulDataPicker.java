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

// 本类用于筛选某一段日期内，每日的支出、收入和余额数据
public class UserfulDataPicker {
    // 使用 TreeMap 确保 Map 中的日期键是排序的
    private Map<String, Double> income = new TreeMap<>();
    private Map<String, Double> expenditure = new TreeMap<>();
    // 每日余额。我们也按日期排序，并确保是某一天的净余额
    private Map<String, Double> dailySurplus = new TreeMap<>();

    // 最大最小值应计算每日的收入、支出和盈余，而不是累计值或单笔交易
    private double maxOverallValue = 0.0;
    private double minOverallValue = 0.0; // 默认初始值

    // 定义日期格式化器，与 GraphController 中使用的 "yyyy-MM-dd" 保持一致
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public UserfulDataPicker(String startDateStr, String endDateStr) {
        // 解析传入的日期字符串为 LocalDate 对象
        LocalDate startDate = null;
        LocalDate endDate = null;
        try {
            startDate = LocalDate.parse(startDateStr, DATE_FORMATTER);
            endDate = LocalDate.parse(endDateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing date strings in UserfulDataPicker: " + e.getMessage());
            // 处理日期解析错误，例如抛出异常或设置默认值
            return; // 如果日期无效，则不继续处理
        }

        // 获取当前用户的交易记录
        User currentUser = Session.getCurrentUser();
        List<Transaction> transactions;
        TransactionManager transactionManager = new TransactionManager(currentUser);
        transactions = transactionManager.getAllTransactions();

        // 临时存储每日汇总数据
        Map<String, Double> tempIncome = new HashMap<>();
        Map<String, Double> tempExpenditure = new HashMap<>();

        // 步骤1: 遍历所有交易，按日期汇总收入和支出
        for (Transaction transaction : transactions) {
            // 解析交易日期。假设 transaction.getDate() 返回的是 "yyyy-MM-dd" 格式的字符串
            // 如果它返回的是 "yyyy-MM-dd HH:mm:ss"，则需要先截取或解析
            String transactionDateStr = transaction.getDate();
            LocalDate transactionDate;
            try {
                // 尝试解析，如果 transaction.getDate() 含有时间，需要先截取
                if (transactionDateStr.length() > 10) { // 检查是否有时间部分
                    transactionDate = LocalDate.parse(transactionDateStr.substring(0, 10), DATE_FORMATTER);
                } else {
                    transactionDate = LocalDate.parse(transactionDateStr, DATE_FORMATTER);
                }
            } catch (DateTimeParseException e) {
                System.err.println("Error parsing transaction date: " + transactionDateStr + " - " + e.getMessage());
                continue; // 跳过无法解析的交易
            }


            // 检查交易日期是否在指定范围内
            if (!transactionDate.isBefore(startDate) && !transactionDate.isAfter(endDate)) {
                String formattedDate = transactionDate.format(DATE_FORMATTER); // 统一格式

                double amount = transaction.getAmount();
                String type = transaction.getType();

                if ("支出".equals(type)) {
                    tempExpenditure.put(formattedDate, tempExpenditure.getOrDefault(formattedDate, 0.0) + amount);
                } else if ("收入".equals(type)) { // 假设只有“收入”和“支出”两种类型
                    tempIncome.put(formattedDate, tempIncome.getOrDefault(formattedDate, 0.0) + amount);
                }
            }
        }

        // 步骤2: 填充最终的 income, expenditure, dailySurplus Map，并计算最大最小值
        // 遍历选定范围内的每一天，确保即使某天没有交易，其值也为 0
        LocalDate currentDay = startDate;
        while (!currentDay.isAfter(endDate)) {
            String formattedDay = currentDay.format(DATE_FORMATTER);

            double dailyInc = tempIncome.getOrDefault(formattedDay, 0.0);
            double dailyExp = tempExpenditure.getOrDefault(formattedDay, 0.0);
            double dailySur = dailyInc - dailyExp;

            this.income.put(formattedDay, dailyInc);
            this.expenditure.put(formattedDay, dailyExp);
            this.dailySurplus.put(formattedDay, dailySur); // 存储每日净余额

            // 更新最大最小值
            maxOverallValue = Math.max(maxOverallValue, dailyInc);
            maxOverallValue = Math.max(maxOverallValue, dailyExp);
            maxOverallValue = Math.max(maxOverallValue, dailySur); // 正盈余
            minOverallValue = Math.min(minOverallValue, dailySur); // 负盈余

            currentDay = currentDay.plusDays(1);
        }
    }

    // get方法
    public Map<String, Double> getIncome() {
        return income;
    }

    public Map<String, Double> getExpenditure() {
        return expenditure;
    }

    // 提供一个按日期排序的每日盈余 Map
    public Map<String, Double> getDailySurplusMap() {
        return dailySurplus;
    }

    // 以前的 getSurplus() 返回 List<Double>，现在我们按日期提供了 Map
    // 如果 GraphController 还是需要 List<Double>，可以这样转换：
    public List<Double> getSurplus() {
        return new ArrayList<>(dailySurplus.values());
    }

    // 获取选定日期范围内的最大金额或盈余值
    public double getMax() {
        return maxOverallValue;
    }

    // 获取选定日期范围内的最小金额或盈余值
    public double getMin() {
        return minOverallValue;
    }
}