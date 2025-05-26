package com.finance.model;

import com.finance.utils.DataPersistence;

import java.util.List;

public class Register {
    private List<User> users;

    public Register() {
        users = DataPersistence.loadUsers();  // Load an existing user from a file
    }

    public List<User> getUsers() {
        return users;
    }

    public boolean registerUser(String username, String password) {
        // Check if the username already exists
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return false;  // The username already exists
            }
        }

        // If the username does not exist, create a new user and save it
        User newUser = new User(username, password);
        users.add(newUser);
        DataPersistence.saveUsers(users);  // Save the user list to a file
        return true;  //Registration is successful
    }
}