package com.finance.service;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.finance.session.Session;
import com.finance.model.User;
import com.finance.manager.TransactionManager;
import com.finance.model.Transaction;
//本类用于筛选某一段日期内，每日的支出、收入和余额数据
public class UserfulDataPicker {
    double surplus = 0.0;
    List<Double> Surplus = new ArrayList<>();
    Map<String, Double> Income = new HashMap<>();
    Map<String, Double> Expenditure = new HashMap<>();
    //最大最小值
    double max = 0.0;
    double min = 0.0;
    public UserfulDataPicker(String Date1, String Date2) {
        //获取当前用户的交易记录
        User currentUser = Session.getCurrentUser();
        List<Transaction> transactions;
        TransactionManager transactionManager = new TransactionManager(currentUser);
        transactions = transactionManager.getAllTransactions();
        //TODO:先比较日期是否在范围中，再分为收入和支出，最后计算余额。
        for (Transaction transaction : transactions) {
            String date = transaction.getDate().substring(0);
            String type = transaction.getType();
            double amount = transaction.getAmount();
            surplus += amount * checkType(type);
            //判断日期是否在范围内
            if (date.compareTo(Date1) >= 0 && date.compareTo(Date2) <= 0) {
                Surplus.add(surplus);
                //在范围内,准备开始分类
                if (type.equals("支出")) {
                    Expenditure.put(date,amount);
                } else {
                    Income.put(date,amount);
                }
            }
            //计算最大值和最小值
            max = Math.max(max, surplus);
            max = Math.max(max, amount);
            min = Math.min(min, surplus);
            min = Math.min(min, amount);
        }
    }
    private int checkType(String type) {
        if (type.equals("支出")) {
            return -1;
        } else{
            return 1;
        }
    }

    //get方法
    public List<Double> getSurplus() {
        return Surplus;
    }
    public Map<String, Double> getIncome() {
        return Income;
    }
    public Map<String, Double> getExpenditure() {
        return Expenditure;
    }
    //获取最大值和最小值
    public double getMax() {
        return max;
    }
    public double getMin() {
        return min;
    }
}