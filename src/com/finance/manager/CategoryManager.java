package com.finance.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.model.Category;
import com.finance.model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryManager {

    private User user;
    private List<Category> categories = new ArrayList<>();
    private static final String CATEGORIES_FILE_PREFIX = "categories_";

    // 构造函数接收 User 参数
    public CategoryManager(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        this.user = user;
        this.categories = loadCategories();  // 加载用户的分类数据
    }

    // 加载用户的分类数据
    public List<Category> loadCategories() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File(CATEGORIES_FILE_PREFIX + user.getUsername() + ".json");
            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();  // 文件为空，返回空列表
            }
            return objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, Category.class));
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();  // 发生异常时返回空列表
        }
    }

    // 保存用户的分类数据
    private void saveCategories() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(new File(CATEGORIES_FILE_PREFIX + user.getUsername() + ".json"), categories);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 添加新的分类
    public boolean addCategory(String type, String categoryName) {
        if (categories.stream().anyMatch(c -> c.getName().equals(categoryName) && c.getType().equals(type))) {
            return false; // 分类已存在
        }
        Category category = new Category(type, categoryName);
        categories.add(category);
        saveCategories(); // 保存分类数据到文件
        return true;
    }
    // 获取用户的所有分类
    public List<Category> getCategories() {
        return categories;
    }
}