// File path: src/com/finance/controller/ThresholdManager.java
package com.finance.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.model.UserThreshold;

import java.io.File;
import java.io.IOException;

public class ThresholdManager {
    private static final String THRESHOLD_FILE_PREFIX = "user_threshold_"; // File name format: user_threshold_{username}.json

    // Save the user threshold configuration
    public static void saveThreshold(UserThreshold threshold) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File(THRESHOLD_FILE_PREFIX + threshold.getUsername() + ".json");
            mapper.writeValue(file, threshold);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load the user threshold configuration
    public static UserThreshold loadThreshold(String username) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File(THRESHOLD_FILE_PREFIX + username + ".json");

            if (!file.exists()) return new UserThreshold(username); // If the file does not exist, an empty configuration is returned
            return mapper.readValue(file, UserThreshold.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new UserThreshold(username);
        }
    }
}