package com.finance.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataPersistence {

    private static final String USERS_FILE = "users.json";

    // 加载用户数据
    public static List<User> loadUsers() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File(USERS_FILE);

            // 如果文件不存在或为空，返回一个空的用户列表
            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();
            }

            // 读取文件内容并将其转换为 User 列表
            return objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, User.class));
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();  // 发生异常时返回空的用户列表
        }
    }

    // 保存用户数据
    public static void saveUsers(List<User> users) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(new File(USERS_FILE), users);  // 保存为 JSON 文件
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}