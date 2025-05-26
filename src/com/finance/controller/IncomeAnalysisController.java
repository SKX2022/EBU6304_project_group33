package com.finance.controller;

import com.finance.manager.TransactionManager;
import com.finance.model.Transaction;
import com.finance.model.User;
import com.finance.service.LlmService;
import com.finance.session.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class IncomeAnalysisController {
    @FXML private PieChart incomePieChart;
    @FXML private TableView<IncomeType> incomeTable;
    @FXML private TableColumn<IncomeType, String> typeColumn;
    @FXML private TableColumn<IncomeType, String> amountColumn;
    @FXML private Label totalIncomeLabel;
    @FXML private Button analyzeButton;

    @FXML
    public void initialize() {
        try {
            User currentUser = Session.getCurrentUser();
            if (currentUser == null) {
                showAlert("Error", "No user logged in", Alert.AlertType.ERROR);
                return;
            }

            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

            TransactionManager transactionManager = new TransactionManager(currentUser);
            loadIncomeData(transactionManager);
        } catch (Exception e) {
            showAlert("Initialization Error", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void loadIncomeData(TransactionManager transactionManager) {
        Map<String, Double> incomeByCategory = transactionManager.getAllTransactions().stream()
                .filter(t -> "Income".equals(t.getType()))
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        double totalIncome = incomeByCategory.values().stream().mapToDouble(Double::doubleValue).sum();
        totalIncomeLabel.setText(String.format("¥%.2f", totalIncome));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        ObservableList<IncomeType> tableData = FXCollections.observableArrayList();

        incomeByCategory.forEach((category, amount) -> {
            pieChartData.add(new PieChart.Data(category, amount));
            tableData.add(new IncomeType(category, String.format("¥%.2f", amount)));
        });

        incomePieChart.setData(pieChartData);
        incomeTable.setItems(tableData);

        String[] colors = {"#4CAF50", "#8BC34A", "#CDDC39", "#FFC107", "#FF9800"};
        for (int i = 0; i < pieChartData.size(); i++) {
            pieChartData.get(i).getNode().setStyle("-fx-pie-color: " + colors[i % colors.length] + ";");
        }
    }

    @FXML
    private void showIncomeAnalysisPopup() {
        try {
            // 1. Get data on all revenue transactions
            User currentUser = Session.getCurrentUser();
            if (currentUser == null) {
                showAlert("Error", "No user logged in", Alert.AlertType.ERROR);
                return;
            }

            TransactionManager transactionManager = new TransactionManager(currentUser);
            List<Transaction> incomeTransactions = transactionManager.getAllTransactions().stream()
                    .filter(t -> "Income".equals(t.getType()))
                    .collect(Collectors.toList());

            if (incomeTransactions.isEmpty()) {
                showAlert("No Data", "No income data available for analysis", Alert.AlertType.INFORMATION);
                return;
            }

            // 2. The concatenate transaction data is a string
            StringBuilder details = new StringBuilder("Here's a breakdown of the user's earnings：\n\n");
            incomeTransactions.forEach(t ->
                    details.append(String.format("date: %s, category: %s, amount: ¥%.2f\n",
                            t.getDate(), t.getCategory(), t.getAmount()))
            );

            String fullPrompt = details.toString() + "\nBased on the above revenue data, analyze the distribution of revenue sources, main income categories, and revenue trends, and provide optimization suggestions:";

            // 3. Invoke the AI service
            LlmService llmService = new LlmService(fullPrompt);
            llmService.callLlmApi();
            String aiResponse = llmService.getAnswer();

            // 4. Displays the results of the analysis
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("AI Income Analysis");

            TextArea analysisText = new TextArea(aiResponse);
            analysisText.setEditable(false);
            analysisText.setWrapText(true);
            analysisText.setStyle("-fx-font-size: 14px;");

            Button closeButton = new Button("Close");
            closeButton.setOnAction(e -> popupStage.close());

            VBox content = new VBox(15,
                    new Label("AI Income Analysis Report"),
                    analysisText,
                    closeButton
            );
            content.setAlignment(Pos.CENTER);
            content.setPadding(new Insets(20));

            popupStage.setScene(new Scene(content, 600, 400));
            popupStage.show();

        } catch (Exception e) {
            showAlert("Error", "Failed to generate analysis: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class IncomeType {
        private final String type;
        private final String amount;

        public IncomeType(String type, String amount) {
            this.type = type;
            this.amount = amount;
        }

        public String getType() { return type; }
        public String getAmount() { return amount; }
    }
}