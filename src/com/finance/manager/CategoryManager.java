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

    // The constructor receives the User parameter
    public CategoryManager(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        this.user = user;
        this.categories = loadCategoriesFromFile();  // Load the user's categorical data
    }


    // Load the user's categorical data from a file (private method)
    private List<Category> loadCategoriesFromFile() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File(CATEGORIES_FILE_PREFIX + user.getUsername() + ".json");
            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();  // If the file is empty, an empty list is returned
            }
            return objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, Category.class));
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();  // An empty list is returned when an exception occurs
        }
    }


    // Load the user's categorical data (expose method - return data in memory)
    public List<Category> loadCategories() {
        return new ArrayList<>(categories);  // Returns a copy to prevent external modifications
    }

    // Save the user's classification data to a file
    private void saveCategories() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(new File(CATEGORIES_FILE_PREFIX + user.getUsername() + ".json"), categories);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Add a new taxonomy
    public boolean addCategory(String type, String categoryName) {
        if (categories.stream().anyMatch(c -> c.getName().equals(categoryName) && c.getType().equals(type))) {
            return false; // The classification already exists
        }
        Category category = new Category(type, categoryName);
        categories.add(category);
        saveCategories(); // Save the classification data to a file
        return true;
    }

    // Deletes the specified category
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
            saveCategories();  // Save the updated categorical data
            return true;
        }
        return false;
    }

    // Get all classifications of users
    public List<Category> getCategories() {
        return new ArrayList<>(categories);  // Returns a copy to prevent external modifications
    }


    // Reload classification from file (if a forced refresh is required)
    public void reloadCategories() {
        this.categories = loadCategoriesFromFile();
    }
}

