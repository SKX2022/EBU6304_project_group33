package com.finance.controller;

import com.finance.manager.TransactionManager;
import com.finance.model.User;
import com.finance.service.BudgetService; // 新的预算服务类
import com.finance.service.LlmService;
import com.finance.session.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LocalFinanceSettingsController {

    @FXML private TextField yearlyBudgetField;
    @FXML private TextField springFestivalBudgetField;
    @FXML private TextField otherFestivalBudgetField;
    @FXML private TextField emergencyFundField;
    @FXML private CheckBox springFestivalCheck;
    @FXML private CheckBox midAutumnCheck;
    @FXML private CheckBox dragonBoatCheck;
    @FXML private CheckBox seasonalCheck;
    @FXML private CheckBox regionalCheck;
    @FXML private TextArea recommendationArea;
    @FXML private Label monthBudgetLabel;
    @FXML private Label yearBudgetLabel;
    @FXML private Label festivalBudgetLabel;
    @FXML private Label budgetHealthLabel;

    private User currentUser;
    private BudgetService budgetService; // 新的预算服务实例
    private Map<String, Double> aiSuggestedBudgets = new HashMap<>();

    @FXML
    public void initialize() {
        currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            // 处理用户未登录的情况，例如跳转到登录页
            System.out.println("用户未登录，无法加载本地财务设置。");
            return;
        }
        budgetService = new BudgetService(currentUser);
        loadUserSettings();
        updateBudgetAnalysisDisplay();
    }

    private void loadUserSettings() {
        Map<String, Double> settings = budgetService.loadBudgetSettings();
        yearlyBudgetField.setText(settings.getOrDefault("yearlyBudget", 0.0).toString());
        springFestivalBudgetField.setText(settings.getOrDefault("springFestivalBudget", 0.0).toString());
        otherFestivalBudgetField.setText(settings.getOrDefault("otherFestivalBudget", 0.0).toString());
        emergencyFundField.setText(settings.getOrDefault("emergencyFund", 0.0).toString());
        // AI 偏好设置可以后续添加加载逻辑
    }

    @FXML
    private void handleManualSave() {
        try {
            Map<String, Double> settings = new HashMap<>();
            settings.put("yearlyBudget", Double.parseDouble(yearlyBudgetField.getText()));
            settings.put("springFestivalBudget", Double.parseDouble(springFestivalBudgetField.getText()));
            settings.put("otherFestivalBudget", Double.parseDouble(otherFestivalBudgetField.getText()));
            settings.put("emergencyFund", Double.parseDouble(emergencyFundField.getText()));

            budgetService.saveBudgetSettings(settings);
            showAlert(Alert.AlertType.INFORMATION, "保存成功", "手动预算设置已保存。");
            updateBudgetAnalysisDisplay();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "输入错误", "请输入有效的数字金额。");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "保存失败", "保存设置时发生错误: " + e.getMessage());
        }
    }

    @FXML
    private void handleAiSuggest() {
        recommendationArea.setText("正在获取AI智能建议，请稍候...");
        CompletableFuture.runAsync(() -> {
            try {
                String prompt = buildAiPrompt();
                LlmService llmService = new LlmService(prompt);
                llmService.callLlmApi();
                String suggestion = llmService.getAnswer();

                // 解析AI建议 (这里假设AI返回的是JSON格式的预算建��)
                // 例如: {"yearlyBudget": 100000, "springFestivalBudget": 8000, ...}
                // 实际解析逻辑需要根据AI返回的具体格式调整
                aiSuggestedBudgets = budgetService.parseAiBudgetSuggestion(suggestion);

                Platform.runLater(() -> {
                    recommendationArea.setText("AI建议：\n" + formatMapToString(aiSuggestedBudgets));
                    showAlert(Alert.AlertType.INFORMATION, "AI建议", "已获取AI智能预算建议。");
                });
            } catch (IOException | InterruptedException | LlmService.LlmServiceException e) {
                Platform.runLater(() -> {
                    recommendationArea.setText("获取AI建议失败: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "AI服务错误", "获取AI建议时发生错误。");
                });
            }
        });
    }

    private String formatMapToString(Map<String, Double> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    private String buildAiPrompt() {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("你是一个专业的财务顾问，请根据以下用户偏好，为中国用户生成一份年度财务预算建议。返回一个JSON对象，包含以下键：'yearlyBudget', 'springFestivalBudget', 'otherFestivalBudget', 'emergencyFund'。金额为数字。\n");
        promptBuilder.append("用户偏好：\n");
        if (springFestivalCheck.isSelected()) {
            promptBuilder.append("- 重点考虑春节期间的高消费。\n");
        }
        if (midAutumnCheck.isSelected()) {
            promptBuilder.append("- 考虑中秋节的消费。\n");
        }
        if (dragonBoatCheck.isSelected()) {
            promptBuilder.append("- 考虑端午节的消费。\n");
        }
        if (seasonalCheck.isSelected()) {
            promptBuilder.append("- 进行季节性消费习惯分析。\n");
        }
        if (regionalCheck.isSelected()) {
            promptBuilder.append("- 考虑用户所在地区的消费差异（假设为中国普遍情况）。\n");
        }
        promptBuilder.append("请仅返回JSON格式的预算建议。例如：{\"yearlyBudget\": 120000, \"springFestivalBudget\": 10000, \"otherFestivalBudget\": 3000, \"emergencyFund\": 15000}");
        return promptBuilder.toString();
    }

    @FXML
    private void applyAiSuggestion() {
        if (aiSuggestedBudgets.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "无建议", "请先获取AI智能建议。");
            return;
        }
        yearlyBudgetField.setText(aiSuggestedBudgets.getOrDefault("yearlyBudget", 0.0).toString());
        springFestivalBudgetField.setText(aiSuggestedBudgets.getOrDefault("springFestivalBudget", 0.0).toString());
        otherFestivalBudgetField.setText(aiSuggestedBudgets.getOrDefault("otherFestivalBudget", 0.0).toString());
        emergencyFundField.setText(aiSuggestedBudgets.getOrDefault("emergencyFund", 0.0).toString());

        // 保存AI建议到用户设置
        handleManualSave();
        showAlert(Alert.AlertType.INFORMATION, "应用成功", "AI预算建议已应用并保存。");
    }

    @FXML
    private void resetSettings() {
        // 恢复默认或清空设置的逻辑
        yearlyBudgetField.setText("0.0");
        springFestivalBudgetField.setText("0.0");
        otherFestivalBudgetField.setText("0.0");
        emergencyFundField.setText("0.0");
        try {
            budgetService.saveBudgetSettings(new HashMap<>()); // 保存空设置或默认设置
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "保存失败", "重置预算设置时发生错误: " + e.getMessage());
        }
        updateBudgetAnalysisDisplay();
        showAlert(Alert.AlertType.INFORMATION, "重置成功", "预算设置已恢复默认。");
    }

    private void updateBudgetAnalysisDisplay() {
        TransactionManager transactionManager = new TransactionManager(currentUser);
        Map<String, Object> analysis = budgetService.getBudgetAnalysis(transactionManager);

        monthBudgetLabel.setText(String.format("%.2f / %.2f",
                                (Double) analysis.getOrDefault("currentMonthSpending", 0.0),
                                (Double) analysis.getOrDefault("monthlyBudget", 0.0)));
        yearBudgetLabel.setText(String.format("%.2f / %.2f",
                                (Double) analysis.getOrDefault("currentYearSpending", 0.0),
                                (Double) analysis.getOrDefault("yearlyBudget", 0.0)));
        festivalBudgetLabel.setText(String.format("%.2f", (Double) analysis.getOrDefault("remainingFestivalBudget", 0.0)));
        budgetHealthLabel.setText((String) analysis.getOrDefault("budgetHealthStatus", "N/A"));
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

