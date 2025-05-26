package com.finance.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataPersistence {

    private static final String USERS_FILE = "users.json";

    // Load user data
    public static List<User> loadUsers() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File(USERS_FILE);

            // If the file does not exist or is empty, an empty list of users is returned
            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();
            }

            // Read the contents of the file and convert it to a list of Users
            return objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, User.class));
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();  // An empty list of users is returned when an exception occurs
        }
    }

    //Save user data
    public static void saveUsers(List<User> users) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(new File(USERS_FILE), users);  // Save as a JSON file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}