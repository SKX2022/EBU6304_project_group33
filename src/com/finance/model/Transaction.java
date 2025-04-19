package com.finance.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Transaction {
    private String type;
    private String category;
    private double amount;
    private String date;
    private User user;  // 关联的用户对象

    // 使用 @JsonCreator 和 @JsonProperty 注解
    @JsonCreator
    public Transaction(@JsonProperty("type") String type,
                       @JsonProperty("category") String category,
                       @JsonProperty("amount") double amount,
                       @JsonProperty("date") String date,
                       @JsonProperty("user") User user) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.user = user;
    }

    // Getter 和 Setter 方法
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}