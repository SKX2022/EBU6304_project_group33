package com.finance.controller;

import com.finance.model.Transaction;
import com.finance.service.LlmService;
import com.finance.manager.TransactionManager;
import com.finance.session.Session;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import java.util.stream.Collectors;
import java.util.Map;
import com.finance.service.LlmService.LlmServiceException;
import java.io.IOException;
import java.util.List;


public class ExpenditureAnalysisController {

    @FXML private ListView<String> categoryTypeList;
    @FXML private ListView<Double> categoryAmountList;
    @FXML private Label totalExpenseLabel;
    @FXML private PieChart expenditurePieChart;
    @FXML private Label analysisResultLabel;

    private TransactionManager transactionManager;
    private List<Transaction> transactions; // 新增交易记录列表

    @FXML
    public void initialize() {
        // 初始化事务管理器
        transactionManager = new TransactionManager(Session.getCurrentUser());

        // 加载交易数据
        transactions = transactionManager.getAllTransactions();

        // 初始化UI组件
        loadExpenditureData();
    }

    private void loadExpenditureData() {
        // 构建支出分类统计
        Map<String, Double> categoryMap = transactions.stream()
                .filter(t -> "支出".equals(t.getType()))
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        // 更新总消费标签
        double total = categoryMap.values().stream().mapToDouble(Double::doubleValue).sum();
        totalExpenseLabel.setText(String.format("总支出: ¥%.2f", total));

        // 绑定分类数据到UI控件
        categoryTypeList.setItems(FXCollections.observableArrayList(
                categoryMap.keySet().stream().sorted().toList()
        ));
        categoryAmountList.setItems(FXCollections.observableArrayList(
                categoryMap.values().stream().sorted().toList()
        ));

        // 更新饼图数据
        updatePieChartData(categoryMap);
    }

    private void updatePieChartData(Map<String, Double> categoryMap) {
        expenditurePieChart.getData().clear();
        double total = categoryMap.values().stream().mapToDouble(Double::doubleValue).sum();

        categoryMap.forEach((category, amount) -> {
            String label = String.format("%s (%.1f%%)", category,
                    (amount / total) * 100);
            expenditurePieChart.getData().add(new PieChart.Data(label, amount));
        });
    }

    @FXML
    private void handleAnalyzeButtonClick() {
        try {
            // 初始化分类统计Map
            Map<String, Double> categoryAmounts = transactionManager.getAllTransactions().stream()
                    .filter(t -> "支出".equals(t.getType()))
                    .collect(Collectors.groupingBy(
                            Transaction::getCategory,
                            Collectors.summingDouble(Transaction::getAmount)
                    ));

            // 构建分类统计信息
            StringBuilder promptBuilder = new StringBuilder();
            categoryAmounts.forEach((category, amount) -> {
                promptBuilder.append(String.format("%s: ¥%.2f\n", category, amount));
            });

            // 获取交易明细
            List<Transaction> transactions = transactionManager.getAllTransactions();

            transactions.forEach(t -> {
                String datePart = t.getDate().substring(0, 10);
                promptBuilder.append(String.format(
                        "[%s] %s: ¥%.2f;\n",
                        datePart,
                        t.getType(),
                        t.getAmount())
                );
            });
            promptBuilder.append("请分析各类支出情况，并给出详细的支出优化建议：");

            // 调用AI服务
            LlmService llmService = new LlmService(promptBuilder.toString());
            llmService.callLlmApi();
            String aiResponse = llmService.getAnswer();

            // 显示结果弹窗
            showAlert(Alert.AlertType.INFORMATION, "分析结果", aiResponse);

        } catch (IOException | InterruptedException e) {
            showAlert(Alert.AlertType.ERROR, "系统错误", "请求AI服务时发生异常: " + e.getMessage());
        } catch (LlmServiceException e) {
            showAlert(Alert.AlertType.ERROR, "AI解析错误", e.getMessage());
        }
    }

    // 通用弹窗显示方法
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}