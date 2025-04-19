package com.finance.model;

public class Summary {
    private double totalIncome;
    private double totalExpenditure;
    private double balance;

    // 默认无参构造函数
    public Summary() {
        // Jackson 需要无参构造函数来进行反序列化
    }

    // 带参构造函数
    public Summary(double totalIncome, double totalExpenditure, double balance) {
        this.totalIncome = totalIncome;
        this.totalExpenditure = totalExpenditure;
        this.balance = balance;
    }

    // Getter 和 Setter 方法
    public double getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
    }

    public double getTotalExpenditure() {
        return totalExpenditure;
    }

    public void setTotalExpenditure(double totalExpenditure) {
        this.totalExpenditure = totalExpenditure;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}