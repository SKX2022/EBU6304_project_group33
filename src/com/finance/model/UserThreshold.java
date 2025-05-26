// File path: src/com/finance/model/UserThreshold.java
package com.finance.model;

public class UserThreshold {
    private String username;
    private Double totalExpenseThreshold; //Total spend threshold
    private Double remainingThreshold;   // Remaining threshold

    public UserThreshold() {} // Jackson requires a parameterless constructor

    public UserThreshold(String username) {
        this.username = username;
    }

    // Getter 和 Setter
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Double getTotalExpenseThreshold() { return totalExpenseThreshold; }
    public void setTotalExpenseThreshold(Double totalExpenseThreshold) { this.totalExpenseThreshold = totalExpenseThreshold; }
    public Double getRemainingThreshold() { return remainingThreshold; }
    public void setRemainingThreshold(Double remainingThreshold) { this.remainingThreshold = remainingThreshold; }
}