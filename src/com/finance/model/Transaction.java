package com.finance.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Transaction {
    private String type;
    private String category;
    private double amount;
    private String date;
    private User user;  // Associated user objects
    private String project; // Remarks information

    // Use @JsonCreator and @JsonProperty annotations
    @JsonCreator
    public Transaction(@JsonProperty("type") String type,
                       @JsonProperty("category") String category,
                       @JsonProperty("amount") double amount,
                       @JsonProperty("date") String date,
                       @JsonProperty("user") User user,
                       @JsonProperty("project") String project) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.user = user;
        this.project = project != null ? project : ""; // Make sure the comment is not null
    }


    // Compatible with older versions of constructors, without comment parameters
    public Transaction(String type, String category, double amount, String date, User user) {
        this(type, category, amount, date, user, "");
    }

    // Getter and Setter methods
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

    // Added getter and setter methods for notes
    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }
}

