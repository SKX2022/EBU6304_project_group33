package com.finance.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.manager.TransactionManager;
import com.finance.model.User;
import com.finance.service.BudgetService;
import com.finance.service.LlmService;
import com.finance.session.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LocalFinanceSettingsController {

    @FXML private TextField yearlyBudgetField;
    @FXML private TextField springFestivalBudgetField;
    @FXML private TextField otherFestivalBudgetField;
    @FXML private TextField emergencyFundField;

    // FXML IDs from LocalFinanceSettings.fxml for AI interaction
    @FXML private TextArea aiUserInputArea;
    @FXML private TextArea aiResponseArea;

    // FXML IDs for budget overview labels
    @FXML private Label monthBudgetLabel;
    @FXML private Label yearBudgetLabel;
    @FXML private Label springFestivalLabel;
    @FXML private Label otherFestivalLabel;
    @FXML private Label emergencyFundLabel;

    @FXML private ComboBox<String> monthComboBox;
    @FXML private TextField monthlyBudgetField;
    @FXML private Button updateSelectedMonthBudgetInMemoryButton;
    @FXML private Label aiSuggestionForSelectedMonthLabel;
    @FXML private Button applyAiToSelectedMonthButton;

    // Added FXML field for the new GridPane
    @FXML private GridPane monthlyBudgetGrid;
    private Map<String, Label> monthBudgetLabels = new HashMap<>();

    private User currentUser;
    private BudgetService budgetService;
    private Map<String, Double> aiSuggestedBudgets = new HashMap<>();
    private List<Map<String, Object>> aiCustomPeriodSuggestions = new ArrayList<>();
    private Map<String, Double> monthlyBudgets = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            System.out.println("The user isn't logged in and can't load the local financial settings.");
            showAlert(Alert.AlertType.ERROR, "Error", "User is not logged in, settings cannot be loaded.");
            disableUIComponents();
            return;
        }
        budgetService = new BudgetService(currentUser);
        populateMonthComboBox();
        monthComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldMonth, newMonth) -> {
            if (newMonth != null) {
                loadBudgetForSelectedMonth(newMonth);
                updateAiSuggestionForSelectedMonth(newMonth);
            }
        });
        loadUserSettings();
        updateBudgetAnalysisDisplay(); // This will also call updateMonthlyOverviewDisplay
    }

    private void disableUIComponents() {
        yearlyBudgetField.setDisable(true);
        emergencyFundField.setDisable(true);
        aiUserInputArea.setDisable(true);
    }

    private void populateMonthComboBox() {
        ObservableList<String> months = IntStream.rangeClosed(1, 12)
                .mapToObj(monthNum -> Month.of(monthNum).getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        monthComboBox.setItems(months);
    }

    private void loadUserSettings() {
        Map<String, Double> settings = budgetService.loadBudgetSettings();
        yearlyBudgetField.setText(settings.getOrDefault("yearlyBudget", 0.0).toString());
        emergencyFundField.setText(settings.getOrDefault("emergencyFund", 0.0).toString());

        monthlyBudgets.clear();
        ObservableList<String> months = monthComboBox.getItems();
        if (months != null) {
            for (String monthName : months) {
                Double budget = settings.getOrDefault("monthlyBudget_" + monthName, 0.0);
                monthlyBudgets.put(monthName, budget);
            }
        }
        String selectedMonth = monthComboBox.getSelectionModel().getSelectedItem();
        if (selectedMonth != null) {
            loadBudgetForSelectedMonth(selectedMonth);
        }

        updateOverviewLabels(settings);
        updateMonthlyOverviewDisplay(); // Added an immediate refresh display
    }

    private void loadBudgetForSelectedMonth(String monthName) {
        Double budget = monthlyBudgets.getOrDefault(monthName, 0.0);
        monthlyBudgetField.setText(String.format("%.2f", budget));
    }

    @FXML
    private void handleUpdateSelectedMonthBudgetInMemory() {
        String selectedMonth = monthComboBox.getSelectionModel().getSelectedItem();
        if (selectedMonth == null) {
            showAlert(Alert.AlertType.WARNING, "tips", "Please select a month first.");
            return;
        }
        try {
            double budgetAmount = Double.parseDouble(monthlyBudgetField.getText());
            if (budgetAmount < 0) {
                showAlert(Alert.AlertType.ERROR, "Typing error", "The budget amount can't be negative.");
                return;
            }
            monthlyBudgets.put(selectedMonth, budgetAmount);
            updateMonthlyOverviewDisplay(); // Added an immediate refresh display
            showAlert(Alert.AlertType.INFORMATION, "The update was successful", selectedMonth + " The budget has been updated in memory to: " + String.format("%.2f", budgetAmount) + "。\nRemember to click Save Manual Settings below to persistently make all changes.");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Typing error", "Please enter a valid numeric amount");
        }
    }

    private void updateAiSuggestionForSelectedMonth(String monthName) {
        boolean suggestionFound = false;
        for (Map<String, Object> suggestion : aiCustomPeriodSuggestions) {
            String periodName = (String) suggestion.get("periodName");
            if (periodName.contains(monthName) || monthName.contains(periodName)) {
                Double suggestedBudget = (Double) suggestion.get("suggestedBudget");
                aiSuggestionForSelectedMonthLabel.setText(String.format("%.2f ", suggestedBudget));
                applyAiToSelectedMonthButton.setDisable(false);
                suggestionFound = true;
                break;
            }
        }
        if (!suggestionFound) {
            aiSuggestionForSelectedMonthLabel.setText("-");
            applyAiToSelectedMonthButton.setDisable(true);
        }
    }

    @FXML
    private void handleApplyAiToSelectedMonth() {
        String selectedMonth = monthComboBox.getSelectionModel().getSelectedItem();
        if (selectedMonth == null) {
            showAlert(Alert.AlertType.WARNING, "Tip", "Please select a month first.");
            return;
        }

        boolean applied = false;
        for (Map<String, Object> suggestion : aiCustomPeriodSuggestions) {
            String periodName = (String) suggestion.get("periodName");
            if (periodName.contains(selectedMonth) || selectedMonth.contains(periodName)) {
                Double suggestedBudget = (Double) suggestion.get("suggestedBudget");
                monthlyBudgetField.setText(String.format("%.2f", suggestedBudget));
                monthlyBudgets.put(selectedMonth, suggestedBudget);
                updateMonthlyOverviewDisplay(); // Added an immediate refresh display
                showAlert(Alert.AlertType.INFORMATION, "AI recommendations have been applied", "AI对 " + selectedMonth + " BUDGET PROPOSALS" + String.format("%.2f", suggestedBudget) + " Populated into the input box and updated to memory. \nPlease remember to click Save Manual Settings below to persist all changes");
                applied = true;
                break;
            }
        }
        if (!applied) {
            showAlert(Alert.AlertType.WARNING, "No Match Suggestion", "Failed to find AI budget suggestion for "+selectedMonth+" to apply.");
        }
    }

    @FXML
    private void handleManualSave() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User is not logged in, cannot be saved.");
            return;
        }
        try {
            Map<String, Double> settings = new HashMap<>();
            settings.put("yearlyBudget", Double.parseDouble(yearlyBudgetField.getText()));
            settings.put("emergencyFund", Double.parseDouble(emergencyFundField.getText()));

            for (Map.Entry<String, Double> entry : monthlyBudgets.entrySet()) {
                settings.put("monthlyBudget_" + entry.getKey(), entry.getValue());
            }

            budgetService.saveBudgetSettings(settings);

            //Reload all settings and update the display
            loadUserSettings();
            updateBudgetAnalysisDisplay();
            updateMonthlyOverviewDisplay();

            showAlert(Alert.AlertType.INFORMATION, "The save was successful", "All budget settings, including monthly budgets, are saved.");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid numeric amount.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Save failed", "An error occurred while saving settings: "+ e.getMessage());
        }
    }

    @FXML
    private void handleAiConversation() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "The user is not logged in and cannot use the AI feature.");
            return;
        }
        String userInput = aiUserInputArea.getText();
        if (userInput == null || userInput.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Enter a prompt", "Please enter your budget adjustment needs or inquiries.");
            return;
        }

        aiResponseArea.setText("Getting AI smart suggestions, please wait...");
        CompletableFuture.runAsync(() -> {
            try {
                String prompt = buildDynamicAiPrompt(userInput);
                LlmService llmService = new LlmService(prompt);
                llmService.callLlmApi();
                String rawAiResponse = llmService.getAnswer();

                Platform.runLater(() -> {
                    aiResponseArea.setText(rawAiResponse != null ? rawAiResponse : "The AI failed to provide a response.");
                    if (rawAiResponse != null) {
                        tryToParseAndStoreAiSuggestion(rawAiResponse);
                        String selectedMonth = monthComboBox.getSelectionModel().getSelectedItem();
                        if (selectedMonth != null) {
                            updateAiSuggestionForSelectedMonth(selectedMonth);
                        }
                        // After AI suggestion, if it affects monthly budgets, refresh overview
                        updateMonthlyOverviewDisplay();
                    }
                });

            } catch (IOException | InterruptedException | LlmService.LlmServiceException e) {
                Platform.runLater(() -> {
                    aiResponseArea.setText("Failed to get AI suggestions:" + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "AI Service Error", "An error occurred while getting AI recommendations." + e.getMessage());
                    e.printStackTrace();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    aiResponseArea.setText("An unknown error occurred while calling AI: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "AI Unknown Error", "An Unknown Error Occurred While Invoking AI." + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    private String buildDynamicAiPrompt(String userInput) {
        String currentBudgetsJson = "{}";
        try {
            Map<String, Object> currentSettingsMap = new HashMap<>();
            currentSettingsMap.put("yearlyBudget", yearlyBudgetField.getText().isEmpty() ? 0.0 : Double.parseDouble(yearlyBudgetField.getText()));
            currentSettingsMap.put("emergencyFund", emergencyFundField.getText().isEmpty() ? 0.0 : Double.parseDouble(emergencyFundField.getText()));

            Map<String, Double> currentMonthlyBudgets = new HashMap<>();
            ObservableList<String> months = monthComboBox.getItems();
            if (months != null) {
                for (String monthName : months) {
                    currentMonthlyBudgets.put("monthlyBudget_" + monthName, this.monthlyBudgets.getOrDefault(monthName, 0.0));
                }
            }
            currentSettingsMap.putAll(currentMonthlyBudgets);
            currentBudgetsJson = objectMapper.writeValueAsString(currentSettingsMap);

        } catch (NumberFormatException e) {
            System.err.println("Error parsing current budget fields for AI prompt (NumberFormatException): " + e.getMessage());
            try {
                Map<String, Object> fallbackSettingsMap = new HashMap<>();
                currentBudgetsJson = objectMapper.writeValueAsString(fallbackSettingsMap);
            } catch (IOException ioe) {
                currentBudgetsJson = "{}";
            }
        } catch (IOException e) {
            System.err.println("Error serializing current budget for AI prompt (IOException): " + e.getMessage());
            currentBudgetsJson = "{}";
        }

        return String.format("\"\"\"\n" +
                "你是一个专业的财务顾问。请根据用户的以下需求，为中国用户生成一份年度财务预算建议。\n" +
                "用户的需求是：“%s”\n" +
                "用户当前的预算设置大致如下（如果用户是初次设置，这些可能是0或默认值，月度预算键为 monthlyBudget_月份名，例如 monthlyBudget_一月）：%s\n" +
                "\n" +
                "请在你的回复中分析用户的需求并给出建议。\n" +
                "1.  请识别用户输入中提到的任何特定时间段（例如月份如“一月”、“二月”，节假日如“春节”、“国庆节”、“双十一”等、或事件如“夏季旅游”）。请确保月份名称与中文习惯一致（例如 “一月”， “二月”， ... “十二月”）。\n" +
                "2.  你的回复应该包含两部分：\n" +
                "    a.  对用户整体需求的文本分析和建议。\n" +
                "    b.  一个JSON对象，包含以下内容：\n" +
                "        -   标准的预算建议，使用键：'yearlyBudget', 'springFestivalBudget', 'otherFestivalBudget', 'emergencyFund'。这些值应该是基于用户请求的整体年度或主要预算。\n" +
                "        -   如果识别出特定的时间段或事件，请在JSON中添加一个名为 'customPeriodBudgets' 的数组。数组中的每个对象应有 'periodName' (字符串，例如 “六月”, “双十一购物”, \"一月\") 和 'suggestedBudget' (数字)。\n" +
                "\n" +
                "例如，如果用户说：“我想为六月份和双十一购物做预算，并了解下全年的大概开销。”\n" +
                "你的JSON回复可能像这样：\n" +
                "{\n" +
                "    \"yearlyBudget\": 150000,\n" +
                "    \"springFestivalBudget\": 10000,\n" +
                "    \"otherFestivalBudget\": 5000,\n" +
                "    \"emergencyFund\": 20000,\n" +
                "    \"customPeriodBudgets\": [\n" +
                "        { \"periodName\": \"六月\", \"suggestedBudget\": 12000 },\n" +
                "        { \"periodName\": \"双十一购物\", \"suggestedBudget\": 8000 }\n" +
                "    ]\n" +
                "}\n" +
                "如果用户只问年度预算，则 'customPeriodBudgets' 可以省略或是空数组。\n" +
                "请确保返回的JSON对象是有效的，并且在文本建议之后提供。\n" +
                "\"\"\"", userInput, currentBudgetsJson);
    }

    private void tryToParseAndStoreAiSuggestion(String aiResponse) {
        int jsonStart = aiResponse.lastIndexOf("{\n");
        if (jsonStart == -1) {
            jsonStart = aiResponse.lastIndexOf("{");
        }

        if (jsonStart != -1) {
            String potentialJson = aiResponse.substring(jsonStart);
            try {
                int jsonEnd = potentialJson.lastIndexOf("}");
                if (jsonEnd != -1 && jsonEnd > potentialJson.indexOf("{")) {
                    potentialJson = potentialJson.substring(0, jsonEnd + 1);
                    if (potentialJson.trim().startsWith("{") && potentialJson.trim().endsWith("}")) {
                        Map<String, Object> rootJsonMap = objectMapper.readValue(potentialJson, new TypeReference<Map<String, Object>>() {});

                        aiSuggestedBudgets.clear();
                        aiCustomPeriodSuggestions.clear();

                        String[] standardKeys = {"yearlyBudget", "springFestivalBudget", "otherFestivalBudget", "emergencyFund"};
                        for (String key : standardKeys) {
                            if (rootJsonMap.containsKey(key) && rootJsonMap.get(key) instanceof Number) {
                                aiSuggestedBudgets.put(key, ((Number) rootJsonMap.get(key)).doubleValue());
                            }
                        }
                        System.out.println("Parsed AI standard budgets: " + aiSuggestedBudgets);

                        if (rootJsonMap.containsKey("customPeriodBudgets")) {
                            Object customPeriodsObj = rootJsonMap.get("customPeriodBudgets");
                            if (customPeriodsObj instanceof java.util.List) {
                                List<?> rawList = (List<?>) customPeriodsObj;
                                for (Object item : rawList) {
                                    if (item instanceof Map) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> suggestionMap = (Map<String, Object>) item;
                                        if (suggestionMap.containsKey("periodName") && suggestionMap.get("periodName") instanceof String &&
                                                suggestionMap.containsKey("suggestedBudget") && suggestionMap.get("suggestedBudget") instanceof Number) {

                                            Map<String, Object> validatedSuggestion = new HashMap<>();
                                            validatedSuggestion.put("periodName", suggestionMap.get("periodName"));
                                            validatedSuggestion.put("suggestedBudget", ((Number) suggestionMap.get("suggestedBudget")).doubleValue());
                                            aiCustomPeriodSuggestions.add(validatedSuggestion);
                                        }
                                    }
                                }
                            }
                        }

                        String alertMessage ="The budget proposal returned by the AI has been successfully resolved.";
                        if (!aiCustomPeriodSuggestions.isEmpty()) {
                            alertMessage += "\nThe AI recognized and suggested " + aiCustomPeriodSuggestions.size() + " Budgets for specific periods/events.";
                            System.out.println("Parsed AI custom period budgets: " + aiCustomPeriodSuggestions);
                        }
                        if (aiSuggestedBudgets.isEmpty() && aiCustomPeriodSuggestions.isEmpty()){
                            System.out.println("No structured budget data was found in the AI response.");
                        } else {
                            showAlert(Alert.AlertType.INFORMATION, "AI Suggestion Resolution Successful", alertMessage);
                        }

                    } else {
                        System.out.println("Extracted string is not a valid JSON object structure: " + potentialJson);
                    }
                } else {
                    System.out.println("Could not find a clear end '}' for the JSON object in: " + potentialJson);
                }
            } catch (IOException e) {
                System.err.println("Unable to parse budget JSON from AI response: " + e.getMessage() + " (JSON attempted: " + potentialJson + ")");
            }
        } else {
            System.out.println("No budget proposal found in JSON format in AI response.");
        }
    }

    @FXML
    private void applyAiSuggestion() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User is not logged in.");
            return;
        }
        if (aiSuggestedBudgets.isEmpty() && aiCustomPeriodSuggestions.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No recommendations", "There are no AI intelligent suggestions that can be applied, or the recommendations are not successfully resolved. Obtain and resolve AI suggestions first.");
            return;
        }

        java.util.function.Function<TextField, Double> getCurrentValueOrDefault = (field) -> {
            try {
                String text = field.getText();
                if (text == null || text.trim().isEmpty()) {
                    return 0.0;
                }
                return Double.parseDouble(text);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        };

        boolean standardApplied = false;
        if (!aiSuggestedBudgets.isEmpty()) {
            yearlyBudgetField.setText(aiSuggestedBudgets.getOrDefault("yearlyBudget", getCurrentValueOrDefault.apply(yearlyBudgetField)).toString());
            emergencyFundField.setText(aiSuggestedBudgets.getOrDefault("emergencyFund", getCurrentValueOrDefault.apply(emergencyFundField)).toString());
            standardApplied = true;
        }

        if (standardApplied) {
            handleManualSave();
        }

        String applyAlertMessage = standardApplied ? "The AI's main budget proposal has been applied to the corresponding text box and has triggered a save." : "AI doesn't provide budget recommendations that can be applied to the main text box.";

        if (!aiCustomPeriodSuggestions.isEmpty()) {
            applyAlertMessage += "\nAI also provides: " + aiCustomPeriodSuggestions.size() + " for a specific period/event. Look next to the month picker and apply separately, if applicable.";
        }
        showAlert(Alert.AlertType.INFORMATION, "Apply AI recommendations", applyAlertMessage);
    }

    @FXML
    private void resetSettings() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User is not logged in.");
            return;
        }

        yearlyBudgetField.setText("0.0");
        emergencyFundField.setText("0.0");

        monthlyBudgetField.setText("0.0");
        monthlyBudgets.clear();
        ObservableList<String> months = monthComboBox.getItems();
        if (months != null) {
            for (String monthName : months) {
                monthlyBudgets.put(monthName, 0.0);
            }
        }
        String selectedMonth = monthComboBox.getSelectionModel().getSelectedItem();
        if (selectedMonth != null) {
            loadBudgetForSelectedMonth(selectedMonth);
        }

        aiSuggestedBudgets.clear();
        aiCustomPeriodSuggestions.clear();
        aiResponseArea.setText("");
        aiUserInputArea.setText("");
        if (selectedMonth != null) {
            updateAiSuggestionForSelectedMonth(selectedMonth);
        } else {
            aiSuggestionForSelectedMonthLabel.setText("-");
            applyAiToSelectedMonthButton.setDisable(true);
        }

        try {
            Map<String, Double> defaultSettings = new HashMap<>();
            defaultSettings.put("yearlyBudget", 0.0);
            defaultSettings.put("springFestivalBudget", 0.0);
            defaultSettings.put("otherFestivalBudget", 0.0);
            defaultSettings.put("emergencyFund", 0.0);
            for (Map.Entry<String, Double> entry : monthlyBudgets.entrySet()) {
                defaultSettings.put("monthlyBudget_" + entry.getKey(), 0.0);
            }

            budgetService.saveBudgetSettings(defaultSettings);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Save failed", "An error occurred while resetting budget settings: "+ e.getMessage());
        }
        updateBudgetAnalysisDisplay();
        updateMonthlyOverviewDisplay(); // Refresh monthly overview after reset
        showAlert(Alert.AlertType.INFORMATION, "Reset successful", "All budget settings have been restored to their defaults and saved.");
    }

    private void updateBudgetAnalysisDisplay() {
        if (currentUser == null) return;

        Map<String, Double> currentSettings = budgetService.loadBudgetSettings();
        updateOverviewLabels(currentSettings);

        // Update monthly budget data
        monthlyBudgets.clear();
        ObservableList<String> months = monthComboBox.getItems();
        if (months != null) {
            for (String monthName : months) {
                Double budget = currentSettings.getOrDefault("monthlyBudget_" + monthName, 0.0);
                monthlyBudgets.put(monthName, budget);
            }
        }

        TransactionManager transactionManager = new TransactionManager(currentUser);
        Map<String, Object> analysis = budgetService.getBudgetAnalysis(transactionManager);

        monthBudgetLabel.setText(String.format("%.2f / %.2f",
                (Double) analysis.getOrDefault("currentMonthSpending", 0.0),
                (Double) analysis.getOrDefault("monthlyBudget", 0.0)));
        yearBudgetLabel.setText(String.format("%.2f / %.2f",
                (Double) analysis.getOrDefault("currentYearSpending", 0.0),
                (Double) analysis.getOrDefault("yearlyBudget", currentSettings.getOrDefault("yearlyBudget", 0.0))));

        // Update the monthly budget details display
        updateMonthlyOverviewDisplay();
    }

    private void updateOverviewLabels(Map<String, Double> settings) {
        emergencyFundLabel.setText(String.format("%.2f", settings.getOrDefault("emergencyFund", 0.0)));
    }

    private void updateMonthlyOverviewDisplay() {
        if (monthlyBudgetGrid == null) {
            System.out.println("monthlyBudgetGrid is null, cannot update.");
            return;
        }

        monthlyBudgetGrid.getChildren().clear();
        monthBudgetLabels.clear();

        ObservableList<String> monthNames = monthComboBox.getItems();
        if (monthNames == null || monthNames.isEmpty()) {
            monthNames = IntStream.rangeClosed(1, 12)
                    .mapToObj(monthNum -> Month.of(monthNum)
                            .getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
        }

        int col = 0;
        int row = 0;
        final int MAX_COLS = 2; // Each row shows 2 groups of months (4 columns, since each group is 2 columns)

        for (int i = 0; i < monthNames.size(); i++) {
            String monthName = monthNames.get(i);
            Double budgetValue = monthlyBudgets.getOrDefault(monthName, 0.0);

            Label monthLabel = new Label(monthName + "budget:");
            monthLabel.getStyleClass().add("label-info-sm");
            GridPane.setHalignment(monthLabel, javafx.geometry.HPos.RIGHT);

            Label valueLabel = new Label(String.format("¥%.2f", budgetValue));
            valueLabel.getStyleClass().add("label-value-sm");
            GridPane.setHalignment(valueLabel, javafx.geometry.HPos.LEFT);
            monthBudgetLabels.put(monthName, valueLabel);

            // Calculate the location in the GridPane
            int baseCol = (i % MAX_COLS) * 2;  // Each set of months occupies 2 columns
            row = i / MAX_COLS;

            monthlyBudgetGrid.add(monthLabel, baseCol, row);
            monthlyBudgetGrid.add(valueLabel, baseCol + 1, row);
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void exportSettings() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User is not logged in.");
            return;
        }
        System.out.println("Export settings action triggered.");
        showAlert(Alert.AlertType.INFORMATION, "Feature to be implemented", "Export financial settings feature is in development.");
    }

    @FXML
    private void importSettings() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User is not logged in.");
            return;
        }
        System.out.println("Import settings action triggered.");
        showAlert(Alert.AlertType.INFORMATION, "Functionality to be implemented", "Import financial settings feature is under development.");
    }
}