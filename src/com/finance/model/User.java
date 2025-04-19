package com.finance.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private List<Transaction> transactions;
    private List<Category> categories;

    // 无参构造函数，确保 Jackson 能够正确反序列化 User 对象
    public User() {
        // 初始化 List 避免空指针异常
        this.transactions = new ArrayList<>();
        this.categories = new ArrayList<>();
    }

    // 带参数的构造函数
    @JsonCreator
    public User(@JsonProperty("username") String username, @JsonProperty("password") String password) {
        this.username = username;
        this.password = password;
        this.transactions = new ArrayList<>();
        this.categories = new ArrayList<>();
    }

    // getter 和 setter 方法
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
}