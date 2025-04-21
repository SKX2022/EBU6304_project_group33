// 文件路径: src/com/finance/controller/ThresholdManager.java
package com.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.model.UserThreshold;
import java.io.File;
import java.io.IOException;

public class ThresholdManager {
    private static final String THRESHOLD_FILE_PREFIX = "user_threshold_"; // 文件名格式: user_threshold_{username}.json

    // 保存用户阈值配置
    public static void saveThreshold(UserThreshold threshold) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File(THRESHOLD_FILE_PREFIX + threshold.getUsername() + ".json");
            mapper.writeValue(file, threshold);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 加载用户阈值配置
    public static UserThreshold loadThreshold(String username) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File(THRESHOLD_FILE_PREFIX + username + ".json");
            if (!file.exists()) return new UserThreshold(username); // 文件不存在时返回空配置
            return mapper.readValue(file, UserThreshold.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new UserThreshold(username);
        }
    }
}