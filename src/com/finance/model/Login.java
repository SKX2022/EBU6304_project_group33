package com.finance.model;

import java.util.List;

public class Login {
    private List<User> users;

    // 构造器，接受用户列表作为参数
    public Login(List<User> users) {
        this.users = users;
    }

    // 登录验证方法
    public boolean loginUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return true; // 登录成功
            }
        }
        return false; // 登录失败
    }
}