package com.finance.controller;

import com.finance.manager.CategoryManager;
import com.finance.manager.SummaryManager;
import com.finance.manager.TransactionManager;
import com.finance.model.Category;
import com.finance.model.User;
import com.finance.session.Session;
import com.finance.utils.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.event.EventHandler;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.text.DecimalFormat;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class HomeController {

    @FXML private Label monthlyIncomeLabel;
    @FXML private Label monthlyExpenseLabel;
    @FXML private Label monthlySurplusLabel;
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpenseLabel;
    @FXML private Label totalSurplusLabel;

    @FXML private ProgressBar incomeProgressBar;
    @FXML private ProgressBar expenseProgressBar;
    @FXML private ProgressBar surplusProgressBar;

    @FXML private ListView<String> incomeCategoryList;
    @FXML private ListView<String> expenseCategoryList;

    private CategoryManager categoryManager;
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = Session.getCurrentUser();
        if (currentUser == null) return;

        categoryManager = new CategoryManager(currentUser);
        TransactionManager transactionManager = new TransactionManager(currentUser);
        SummaryManager summaryManager = new SummaryManager(transactionManager, categoryManager);

        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        // Monthly income and expenditure
        double incomeMonth = transactionManager.getMonthlyIncome();
        double expenseMonth = transactionManager.getMonthlyExpenditure();

        // Use BigDecimal to calculate the surplus with precision
        BigDecimal surplusMonth = BigDecimal.valueOf(incomeMonth)
                .subtract(BigDecimal.valueOf(expenseMonth))
                .setScale(2, RoundingMode.HALF_UP);

        // Format the display
        DecimalFormat df = new DecimalFormat("#,##0.00");

        monthlyIncomeLabel.setText("Month Income：¥ " + df.format(incomeMonth));
        monthlyExpenseLabel.setText("Month Expenditure：¥ " + df.format(expenseMonth));
        monthlySurplusLabel.setText("Month Surplus：¥ " + df.format(surplusMonth));

        // Total income and expenditure
        double incomeTotal = summaryManager.getTotalIncome();
        double expenseTotal = summaryManager.getTotalExpenditure();
        
        // Use BigDecimal to calculate the surplus
        BigDecimal surplusTotal = BigDecimal.valueOf(incomeTotal)
                .subtract(BigDecimal.valueOf(expenseTotal))
                .setScale(2, RoundingMode.HALF_UP);

        totalIncomeLabel.setText("Total Income：¥ " + df.format(incomeTotal));
        totalExpenseLabel.setText("Total Expenditure：¥ " + df.format(expenseTotal));
        totalSurplusLabel.setText("Total Surplus：¥ " + df.format(surplusTotal));

        //Categorical display
        updateCategoryLists();

        // Set the progress bar (based on the maximum value)
        BigDecimal bdIncomeTotal = BigDecimal.valueOf(incomeTotal);
        BigDecimal bdExpenseTotal = BigDecimal.valueOf(expenseTotal);
        BigDecimal maxValue = bdIncomeTotal.max(bdExpenseTotal).max(BigDecimal.ONE);

        // Calculate the scale and convert to double for the progress bar
        incomeProgressBar.setProgress(bdIncomeTotal.divide(maxValue, 10, RoundingMode.HALF_UP).doubleValue());
        expenseProgressBar.setProgress(bdExpenseTotal.divide(maxValue, 10, RoundingMode.HALF_UP).doubleValue());

        // The surplus progress bar is processed to ensure that it is not negative
        BigDecimal surplusRatio = surplusTotal.divide(maxValue, 10, RoundingMode.HALF_UP);
        double surplusProgress = Math.max(surplusRatio.doubleValue(), 0);
        surplusProgressBar.setProgress(surplusProgress);
    }

    // Update the classification list method
    private void updateCategoryLists() {
        // Clear the existing list
        incomeCategoryList.getItems().clear();
        expenseCategoryList.getItems().clear();

        
        // Create a new CategoryManager instance to ensure the most up-to-date data is available
        categoryManager = new CategoryManager(currentUser);

        // Get the most up-to-date list of categories
        List<Category> allCategories = categoryManager.getCategories();
        for (Category c : allCategories) {
            if ("Income".equals(c.getType())) {
                incomeCategoryList.getItems().add(c.getName());
            } else if ("Expenditure".equals(c.getType())) {
                expenseCategoryList.getItems().add(c.getName());
            }
        }
    }

    // Create a window close event listener that refreshes the list of categories after the Add Categories window closes
    private EventHandler<WindowEvent> createWindowCloseHandler() {
        return event -> updateCategoryLists();
    }

    @FXML
    private void goIncomeAnalysis(ActionEvent event) {
        SceneSwitcher.switchScene("/view/IncomeAnalysis.fxml");
    }

    @FXML
    private void goExpenditureAnalysis(ActionEvent event) {
        SceneSwitcher.switchScene("/view/ExpenditureAnalysis.fxml");
    }

    @FXML
    private void deleteExpenseCategory() {
        String selectedCategory = expenseCategoryList.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            showAlert("warning", "Start by selecting an expense category that you want to delete", Alert.AlertType.WARNING);
            return;
        }

        // 弹出确认对话框
        boolean confirmed = showConfirmationDialog("Delete a category",
                "Decide that you want to remove the expense classification \"" + selectedCategory + "\" \nAfter deletion, the data cannot be recovered.");

        if (confirmed) {
            boolean deleted = categoryManager.deleteCategory("Expenditure", selectedCategory);
            if (deleted) {
                updateCategoryLists(); // Update the list display
                showAlert("success", "the ExpenseCategory Has Been Removed", Alert.AlertType.INFORMATION);
            } else {
                showAlert("error", "Failed to delete the classification", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void deleteIncomeCategory() {
        String selectedCategory = incomeCategoryList.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            showAlert("warning", "Start by selecting a revenue category that you want to delete", Alert.AlertType.WARNING);
            return;
        }

        // 弹出确认对话框
        boolean confirmed = showConfirmationDialog("Delete a category",

                "Decide that you want to delete the revenue classification \"" + selectedCategory + "\"\nAfter deletion, the data cannot be recovered.");

        if (confirmed) {
            boolean deleted = categoryManager.deleteCategory("Income", selectedCategory);
            if (deleted) {
                updateCategoryLists(); // Update the list display
                showAlert("Success", "Income classification removed", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to delete classification", Alert.AlertType.ERROR);
            }
        }
    }

    // A confirmation dialog box is displayed
    private boolean showConfirmationDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // A prompt message is displayed
    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goAddExpense(ActionEvent event) {
        Window owner = ((Node) event.getSource()).getScene().getWindow();
        showAddDialog("/view/AddExpense.fxml", "Add Expense Category", owner);
    }

    @FXML
    private void goAddIncome(ActionEvent event) {
        Window owner = ((Node) event.getSource()).getScene().getWindow();
        showAddDialog("/view/AddIncome.fxml", "Add Income Category", owner);
    }

    private void showAddDialog(String fxmlPath, String title, Window owner) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));

            Stage dialog = new Stage();
            dialog.setTitle(title);
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initOwner(owner);      // The main window passed in by the caller
            dialog.setResizable(false);

            // Add a window close event listener to refresh the category list when the window closes
            dialog.setOnHidden(createWindowCloseHandler());

            dialog.show();                // non-modal; If you want to block the main window, use showAndWait()

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
