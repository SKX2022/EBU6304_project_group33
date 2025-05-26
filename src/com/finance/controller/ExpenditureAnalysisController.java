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
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

public class ExpenditureAnalysisController {

    @FXML private ListView<String> categoryTypeList;
    @FXML private ListView<String> categoryAmountList;
    @FXML private Label totalExpenseLabel;
    @FXML private PieChart expenditurePieChart;  // Correctly declared (no generic parameters)
    @FXML private Label analysisResultLabel;

    private TransactionManager transactionManager;
    private List<Transaction> transactions;

    @FXML
    public void initialize() {
        transactionManager = new TransactionManager(Session.getCurrentUser());
        transactions = transactionManager.getAllTransactions();
        loadExpenditureData();
    }

    private void loadExpenditureData() {
        // calculateAccuratelyWithBigDecimal
        Map<String, BigDecimal> categoryMap = transactions.stream()
                .filter(t -> "Expenditure".equals(t.getType()))
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.mapping(Transaction::getAmount,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> list.stream()
                                                .map(BigDecimal::valueOf)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add)))));

        // convertToAMapWithTwoDecimalPlaces
        Map<String, BigDecimal> formattedMap = categoryMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().setScale(2, RoundingMode.HALF_UP)));

            // updateTheTotalSpendTab
        BigDecimal total = formattedMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalExpenseLabel.setText(String.format("Total spending: ¥%s",
                total.stripTrailingZeros().toPlainString()));

        // configureTheBigDecimalConverter
        StringConverter<BigDecimal> amountConverter = new StringConverter<>() {
            private final DecimalFormat df = new DecimalFormat("#0.00");

            @Override
            public String toString(BigDecimal object) {
                return df.format(object != null ? object : BigDecimal.ZERO);
            }

            @Override
            public BigDecimal fromString(String string) {
                return new BigDecimal(string);
            }
        };

        // bindDataToUIControls
        ObservableList<String> categories = FXCollections.observableArrayList(
                formattedMap.keySet().stream().sorted().toList());
        ObservableList<String> amounts = FXCollections.observableArrayList(
                formattedMap.values().stream()
                        .map(amountConverter::toString)
                        .toList());

        categoryTypeList.setItems(categories);
        categoryAmountList.setItems(amounts);

        // updateThePieChartData
        updatePieChartData(formattedMap);
    }

    private void updatePieChartData(Map<String, BigDecimal> categoryMap) {
        expenditurePieChart.getData().clear();
        BigDecimal total = categoryMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);



        categoryMap.forEach((category, amount) -> {
            // calculateUsingAnExactPercentage
            BigDecimal percentage = amount.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            //enforceTheRetentionOfTwoDecimalPlaces
            String formattedPercentage = percentage.setScale(2, RoundingMode.HALF_UP) + "%";
            String label = String.format("%s\n%s", category, formattedPercentage);

            // constructPieChartDataWithTheDoubleValue
            expenditurePieChart.getData().add(new PieChart.Data(label, amount.doubleValue()));
        });

        // Force a refresh of the chart layout
        expenditurePieChart.layout();
    }


    @FXML
    private void handleAnalyzeButtonClick() {
        try {
            Map<String, Double> categoryAmounts = transactions.stream()
                    .filter(t -> "Expenditure".equals(t.getType()))
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
            promptBuilder.append("Analyze your spend and make detailed recommendations for how to optimize your spend：");

            LlmService llmService = new LlmService(promptBuilder.toString());
            llmService.callLlmApi();
            showAlert(Alert.AlertType.INFORMATION, "Analyze the results", llmService.getAnswer());

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "error", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);

        // createAScrollingPanel
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        //createATextArea
        TextArea textArea = new TextArea(message);
        textArea.setStyle("-fx-font-size: 14px; -fx-wrap-text: true;");
        textArea.setEditable(false);
        scrollPane.setContent(textArea);

        // configureTheDialogBox
        alert.getDialogPane().setContent(scrollPane);
        alert.setWidth(600);
        alert.setHeight(400);
        alert.setResizable(false);

        // aPopUpWindowAppears
        alert.showAndWait();
    }
}