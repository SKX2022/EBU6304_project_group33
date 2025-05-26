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

        // Hide the list of similar categories and the confirmation box during initialization
        similarCategoriesListView.setVisible(false);
        confirmationBox.setVisible(false);
    }

    @FXML
    private void handleSave() {
        String name = categoryField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setText("The category name cannot be empty");
            errorLabel.setVisible(true);
            return;
        }
        boolean ok = categoryManager.addCategory("income", name);
        if (!ok) {
            errorLabel.setText("The classification already exists");
            errorLabel.setVisible(true);
            return;
        }

        // A successful save prompt is displayed
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("success");
        alert.setHeaderText(null);
        alert.setContentText("Income classification \"" + name + "\" Successfully added！");
        alert.showAndWait();

        // Close the current window
        Stage stage = (Stage) categoryField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleAiSuggest() {
        String userInput = categoryField.getText().trim();
        errorLabel.setText("Classification suggestions are being generated using AI intelligence, please wait...");
        errorLabel.setVisible(true);

        CompletableFuture.runAsync(() -> {
            try {
                String prompt;
                if (!userInput.isEmpty()) {
                    prompt = "If a user wants to create a revenue category about '" + userInput + "', please recommend a suitable standard classification name based on this content and in combination with common classification criteria. If the user input is good enough or not recognized as a standard classification, you can return the content directly to the user type. Please reply with the category name only, without any other explanation.";
                } else {
                    prompt = "You are a financial categorization assistant, please recommend a common income categorization for users. Please reply only with the category name without any other explanation. For example: salary income,please answer only with English.";
                }

                LlmService llmService = new LlmService(prompt);
                llmService.callLlmApi();
                suggestedCategory = llmService.getAnswer().trim();

                similarCategories = findSimilarCategories(suggestedCategory);

                Platform.runLater(() -> {
                    if (similarCategories.isEmpty()) {
                        categoryField.setText(suggestedCategory);
                        errorLabel.setText("The AI has generated a recommendation classification: " + suggestedCategory);
                        errorLabel.setVisible(true);
                        confirmationBox.setVisible(false);
                        similarCategoriesListView.setVisible(false);
                    } else {
                        categoryField.setText(suggestedCategory);
                        errorLabel.setText("If you find a similar taxonomy, choose to use an existing taxonomy or create a new one:");
                        errorLabel.setVisible(true);

                        ObservableList<String> items = FXCollections.observableArrayList(similarCategories);
                        similarCategoriesListView.setItems(items);
                        similarCategoriesListView.setVisible(true);
                        confirmationBox.setVisible(true);
                    }
                });
            } catch (IOException | InterruptedException | LlmService.LlmServiceException e) {
                Platform.runLater(() -> {
                        errorLabel.setText("The AI service call failed: " + e.getMessage());
                    errorLabel.setVisible(true);
                });
            }
        });
    }

    @FXML
    private void useExistingCategory() {
        // The user chooses to use an existing taxonomy
        String selectedCategory = similarCategoriesListView.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            categoryField.setText(selectedCategory);
            similarCategoriesListView.setVisible(false);
            confirmationBox.setVisible(false);
            errorLabel.setText("You selected an existing category: " + selectedCategory);
        } else {
            errorLabel.setText("Please select a category from the list first");
        }
    }

    @FXML
    private void createNewCategory() {
        // The user chooses to create a new taxonomy
        similarCategoriesListView.setVisible(false);
        confirmationBox.setVisible(false);
        errorLabel.setText("You will create a new taxonomy: " + suggestedCategory + "，Click Save to confirm");
    }

    // Find existing classifications that are similar to the suggested classifications
    private List<String> findSimilarCategories(String suggestedName) {
        List<Category> allCategories = categoryManager.getCategories();
        List<String> similar = new ArrayList<>();

        // Filter the categorization of income types
        for (Category category : allCategories) {
            if ("收入".equals(category.getType())) {
                String existingName = category.getName().toLowerCase();
                String suggestedLower = suggestedName.toLowerCase();

                // Simple similarity judgment: The inclusion relationship or edit distance is small
                if (existingName.contains(suggestedLower) ||
                    suggestedLower.contains(existingName) ||
                    calculateLevenshteinDistance(existingName, suggestedLower) <= 3) {
                    similar.add(category.getName());
                }
            }
        }

        return similar;
    }

    // Calculate the editing distance between two strings, which is used to determine the similarity
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
        // Close the current window
        Stage stage = (Stage) categoryField.getScene().getWindow();
        stage.close();
    }
}

