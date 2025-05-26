package com.finance.model;

import java.util.List;

public class Login {
    private List<User> users;

    // Constructor, which accepts a list of users as a parameter
    public Login(List<User> users) {
        this.users = users;
    }

    // Login verification method
    public boolean loginUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return true; // Login successful
            }
        }
        return false; // Login failed
    }
}