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
import java.math.BigDecimal;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.text.DecimalFormat;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

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
            Map<String, Double> categoryAmounts = transactions.stream()
                    .filter(t -> "支出".equals(t.getType()))
                    .collect(Collectors.groupingBy(
                            Transaction::getCategory,
                            Collectors.summingDouble(Transaction::getAmount))
                    );

            StringBuilder promptBuilder = new StringBuilder();
            categoryAmounts.forEach((category, amount) ->
                    promptBuilder.append(String.format("%s: ¥%.2f\n", category, amount)));

            transactions.forEach(t -> {
                BigDecimal amount = BigDecimal.valueOf(t.getAmount())
                        .setScale(2, RoundingMode.HALF_UP);
                promptBuilder.append(String.format(
                        "[%s] %s: ¥%s;\n",
                        t.getDate().substring(0, 10),
                        t.getType(),
                        amount));
            });
            promptBuilder.append("请分析各类支出情况，并给出详细的支出优化建议：");

            LlmService llmService = new LlmService(promptBuilder.toString());
            llmService.callLlmApi();
            showAlert(Alert.AlertType.INFORMATION, "分析结果", llmService.getAnswer());

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "错误", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);

        // 创建滚动面板
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // 创建文本区域
        TextArea textArea = new TextArea(message);
        textArea.setStyle("-fx-font-size: 14px; -fx-wrap-text: true;");
        textArea.setEditable(false);
        scrollPane.setContent(textArea);

        // 配置对话框
        alert.getDialogPane().setContent(scrollPane);
        alert.setWidth(600);
        alert.setHeight(400);
        alert.setResizable(false);

        // 显示弹窗
        alert.showAndWait();
    }
}
