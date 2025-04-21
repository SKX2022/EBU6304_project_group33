// 文件路径: src/com/finance/model/UserThreshold.java
package com.finance.model;

public class UserThreshold {
    private String username;
    private Double totalExpenseThreshold; // 总支出阈值
    private Double remainingThreshold;   // 剩余阈值

    public UserThreshold() {} // Jackson 需要无参构造器

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