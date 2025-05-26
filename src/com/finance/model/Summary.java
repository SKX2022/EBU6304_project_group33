package com.finance.model;

public class Summary {
    private double totalIncome;
    private double totalExpenditure;
    private double balance;


    //The default parameter constructor is not parameterized
    public Summary() {
        // Jackson requires a parameterless constructor for deserialization
    }

    // Constructor with parameters
    public Summary(double totalIncome, double totalExpenditure, double balance) {
        this.totalIncome = totalIncome;
        this.totalExpenditure = totalExpenditure;
        this.balance = balance;
    }

    // Getter and Setter methods
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