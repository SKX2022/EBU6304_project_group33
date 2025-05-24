package com.finance.controller;

import com.finance.manager.CategoryManager;
import com.finance.model.Category;
import com.finance.model.User;
import com.finance.service.LlmService;
import com.finance.session.Session;
import com.finance.utils.SceneSwitcher;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AddIncomeController {

    @FXML private TextField categoryField;
    @FXML private Label errorLabel;
    @FXML private ListView<String> similarCategoriesListView;
    @FXML private HBox confirmationBox;

    private CategoryManager categoryManager;
    private String suggestedCategory;
    private List<String> similarCategories;

    @FXML
    public void initialize() {
        User user = Session.getCurrentUser();
        if (user == null) {
            SceneSwitcher.switchScene("/view/Login.fxml");
            return;
        }
        categoryManager = new CategoryManager(user);

        // 初始化时隐藏相似分类列表和确认框
        similarCategoriesListView.setVisible(false);
        confirmationBox.setVisible(false);
    }

    @FXML
    private void handleSave() {
        String name = categoryField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setText("分类名称不能为空");
            errorLabel.setVisible(true);
            return;
        }
        boolean ok = categoryManager.addCategory("收入", name);
        if (!ok) {
            errorLabel.setText("该分类已存在");
            errorLabel.setVisible(true);
            return;
        }

        // 显示保存成功提示
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("成功");
        alert.setHeaderText(null);
        alert.setContentText("收入分类 \"" + name + "\" 已成功添加！");
        alert.showAndWait();

        // 关闭当前窗口
        Stage stage = (Stage) categoryField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleAiSuggest() {
        String userInput = categoryField.getText().trim();
        errorLabel.setText("正在使用AI智能生成分类建议，请稍候...");
        errorLabel.setVisible(true);

        CompletableFuture.runAsync(() -> {
            try {
                String prompt;
                if (!userInput.isEmpty()) {
                    prompt = "用户想创建一个关于 ‘" + userInput + "’ 的收入分类，请基于此内容并结合常见的分类标准，推荐一个合适的标准分类名称。如果用户输入已足够好或无法识别为标准分类，可以直接返回用户输入的内容。请仅回复分类名称，不要有任何其他解释。";
                } else {
                    prompt = "你是一个财务分类助手，请为用户推荐一个常见的收入分类。请只回复分类名称，不要有任何其他解释。例如：工资收入";
                }

                LlmService llmService = new LlmService(prompt);
                llmService.callLlmApi();
                suggestedCategory = llmService.getAnswer().trim();

                similarCategories = findSimilarCategories(suggestedCategory);

                Platform.runLater(() -> {
                    if (similarCategories.isEmpty()) {
                        categoryField.setText(suggestedCategory);
                        errorLabel.setText("AI已生成建议分类: " + suggestedCategory);
                        errorLabel.setVisible(true);
                        confirmationBox.setVisible(false);
                        similarCategoriesListView.setVisible(false);
                    } else {
                        categoryField.setText(suggestedCategory);
                        errorLabel.setText("发现类似的分类，请选择使用现有分类或创建新分类:");
                        errorLabel.setVisible(true);

                        ObservableList<String> items = FXCollections.observableArrayList(similarCategories);
                        similarCategoriesListView.setItems(items);
                        similarCategoriesListView.setVisible(true);
                        confirmationBox.setVisible(true);
                    }
                });
            } catch (IOException | InterruptedException | LlmService.LlmServiceException e) {
                Platform.runLater(() -> {
                    errorLabel.setText("AI服务调用失败: " + e.getMessage());
                    errorLabel.setVisible(true);
                });
            }
        });
    }

    @FXML
    private void useExistingCategory() {
        // 用户选择使用现有分类
        String selectedCategory = similarCategoriesListView.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            categoryField.setText(selectedCategory);
            similarCategoriesListView.setVisible(false);
            confirmationBox.setVisible(false);
            errorLabel.setText("您选择了现有分类: " + selectedCategory);
        } else {
            errorLabel.setText("请先从列表中选择一个分类");
        }
    }

    @FXML
    private void createNewCategory() {
        // 用户选择创建新分类
        similarCategoriesListView.setVisible(false);
        confirmationBox.setVisible(false);
        errorLabel.setText("您将创建新分类: " + suggestedCategory + "，点击保存以确认");
    }

    // 查找与建议分类相似的现有分类
    private List<String> findSimilarCategories(String suggestedName) {
        List<Category> allCategories = categoryManager.getCategories();
        List<String> similar = new ArrayList<>();

        // 筛选收入类型的分类
        for (Category category : allCategories) {
            if ("收入".equals(category.getType())) {
                String existingName = category.getName().toLowerCase();
                String suggestedLower = suggestedName.toLowerCase();

                // 简单的相似度判断: 包含关系或编辑距离小
                if (existingName.contains(suggestedLower) ||
                    suggestedLower.contains(existingName) ||
                    calculateLevenshteinDistance(existingName, suggestedLower) <= 3) {
                    similar.add(category.getName());
                }
            }
        }

        return similar;
    }

    // 计算两个字符串的编辑距离，用于判断相似度
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }

    @FXML
    private void handleCancel() {
        // 关闭当前窗口
        Stage stage = (Stage) categoryField.getScene().getWindow();
        stage.close();
    }
}

