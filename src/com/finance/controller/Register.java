package com.finance.controller;

import com.finance.model.User;
import com.finance.utils.DataPersistence;

import java.util.List;

public class Register {
    private List<User> users;

    public Register() {
        users = DataPersistence.loadUsers();  // 从文件中加载现有的用户
    }

    public List<User> getUsers() {
        return users;
    }

    public boolean registerUser(String username, String password) {
        // 检查用户名是否已存在
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return false;  // 用户名已存在
            }
        }

        // 如果用户名不存在，则创建新的用户并保存
        User newUser = new User(username, password);
        users.add(newUser);
        DataPersistence.saveUsers(users);  // 保存用户列表到文件
        return true;  // 注册成功
    }
}