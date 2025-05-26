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

    // No parameter constructor to ensure that Jackson can deserialize the User object correctly
    public User() {
        // Initialize the List to avoid null pointer exceptions
        this.transactions = new ArrayList<>();
        this.categories = new ArrayList<>();
    }

    // Constructor with parameters
    @JsonCreator
    public User(@JsonProperty("username") String username, @JsonProperty("password") String password) {
        this.username = username;
        this.password = password;
        this.transactions = new ArrayList<>();
        this.categories = new ArrayList<>();
    }

    // GETTER AND SETTER METHODS
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