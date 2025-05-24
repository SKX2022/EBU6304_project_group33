package com.finance.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.model.Category;
import com.finance.model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

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
        this.categories = loadCategoriesFromFile();  // 加载用户的分类数据
    }

    // 从文件加载用户的分类数据（私有方法）
    private List<Category> loadCategoriesFromFile() {
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

    // 加载用户的分类数据（公开方法 - 返回内存中的数据）
    public List<Category> loadCategories() {
        return new ArrayList<>(categories);  // 返回副本，防止外部修改
    }

    // 保存用户的分类数据到文件
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

    // 删除指定分类
    public boolean deleteCategory(String type, String categoryName) {
        boolean removed = false;
        Iterator<Category> iterator = categories.iterator();
        while (iterator.hasNext()) {
            Category category = iterator.next();
            if (category.getType().equals(type) && category.getName().equals(categoryName)) {
                iterator.remove();
                removed = true;
                break;
            }
        }

        if (removed) {
            saveCategories();  // 保存更新后的分类数据
            return true;
        }
        return false;
    }

    // 获取用户的所有分类
    public List<Category> getCategories() {
        return new ArrayList<>(categories);  // 返回副本，防止外部修改
    }

    // 重新从文件加载分类（如果需要强制刷新）
    public void reloadCategories() {
        this.categories = loadCategoriesFromFile();
    }
}

