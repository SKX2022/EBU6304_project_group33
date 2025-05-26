package com.finance.controller;

import com.finance.service.TaxCalculatorService;
import com.finance.service.TaxCalculatorService.TaxCalculationResult;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

/**
* Revenue Analysis Controller
 * Handle income entry, tax calculations, and result display
 */
public class RevenueController implements Initializable {
    
    // Input controls
    @FXML private TextField annualIncomeField;
    @FXML private ComboBox<String> cityComboBox;
    @FXML private TextField socialSecurityBaseField;
    @FXML private TextField housingFundBaseField;
    @FXML private Button calculateButton;
    @FXML private Button resetButton;
    
    // Result display controls
    @FXML private Label yearlyIncomeLabel;
    @FXML private Label monthlyIncomeLabel;
    @FXML private Label incomeTaxLabel;
    @FXML private Label socialSecurityLabel;
    @FXML private Label housingFundLabel;
    @FXML private Label totalDeductionLabel;
    @FXML private Label netYearlyIncomeLabel;
    @FXML private Label netMonthlyIncomeLabel;
    @FXML private Label taxRateLabel;
    
    // SERVICES
    private TaxCalculatorService taxCalculatorService;
    private DecimalFormat currencyFormat;
    private DecimalFormat percentFormat;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the service class
        taxCalculatorService = new TaxCalculatorService();
        
        // Initialize the formatter
        currencyFormat = new DecimalFormat("#,##0.00");
        percentFormat = new DecimalFormat("#0.00");
        
        // Initialize the City drop-down box
        initializeCityComboBox();
        
        // Initialize the input field
        initializeInputFields();
        
        // Set the default value
        resetForm(null);
    }
    
    /**
    * Initialize the city drop-down box
     */
    private void initializeCityComboBox() {
        String[] cities = taxCalculatorService.getSupportedCities();
        cityComboBox.setItems(FXCollections.observableArrayList(cities));
        cityComboBox.setValue("BEJING"); // By default, Beijing is selected
    }
    
    /**
     * Initialize the input field
     */
    private void initializeInputFields() {
        // Only numbers are allowed
        annualIncomeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                annualIncomeField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            // Automatic calculation of social security and provident fund bases
            updateDefaultBases();
        });
        
        socialSecurityBaseField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                socialSecurityBaseField.setText(oldValue);
            }
        });
        
        housingFundBaseField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                housingFundBaseField.setText(oldValue);
            }
        });
        
        // The base is updated when the city changes
        cityComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateDefaultBases();
        });
    }
    
    /**
    * Automatically update the default social security and provident fund bases
     */
    private void updateDefaultBases() {
        String incomeText = annualIncomeField.getText();
        if (!incomeText.isEmpty()) {
            try {
                double annualIncome = Double.parseDouble(incomeText);
                double monthlyIncome = annualIncome / 12.0;
                
                // If the cardinality field is blank or the user has not manually modified it, it is set automatically
                if (socialSecurityBaseField.getText().isEmpty()) {
                    socialSecurityBaseField.setText(currencyFormat.format(monthlyIncome));
                }
                
                if (housingFundBaseField.getText().isEmpty()) {
                    housingFundBaseField.setText(currencyFormat.format(monthlyIncome));
                }
            } catch (NumberFormatException e) {
                // Ignore formatting errors
            }
        }
    }
    
    /**
     * Calculate taxes
     */
    @FXML
    private void calculateTax(ActionEvent event) {
        try {
            // Get the input values
            String incomeText = annualIncomeField.getText().trim();
            String city = cityComboBox.getValue();
            String socialSecurityText = socialSecurityBaseField.getText().trim().replace(",", "");
            String housingFundText = housingFundBaseField.getText().trim().replace(",", "");
            
            // Validate the input
            if (incomeText.isEmpty()) {
                showAlert("Input Error", "Please enter the amount of annual income!", Alert.AlertType.WARNING);
                return;
            }
            
            if (city == null || city.isEmpty()) {
                showAlert("Typing error", "Please select a city to work for!", Alert.AlertType.WARNING);
                return;
            }
            
            double annualIncome = Double.parseDouble(incomeText);
            double socialSecurityBase = socialSecurityText.isEmpty() ? 0 : Double.parseDouble(socialSecurityText);
            double housingFundBase = housingFundText.isEmpty() ? 0 : Double.parseDouble(housingFundText);
            
            // 验证收入范围
            if (annualIncome <= 0 || annualIncome > 10000000) {
                showAlert("Input error", "The annual income should be between 1 yuan and 10 million yuan!", Alert.AlertType.WARNING);
                return;
            }
            
            // 执行计算
            TaxCalculationResult result = taxCalculatorService.calculateTax(
                annualIncome, city, socialSecurityBase, housingFundBase);
            
            // 显示结果
            displayResults(result);
            
        } catch (NumberFormatException e) {
            showAlert("Typo Error", "Please enter a valid number!", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Calculation error", "Error occurred during tax calculation:" + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    /**
     * Displays the results of the calculation
     */
    private void displayResults(TaxCalculationResult result) {
        yearlyIncomeLabel.setText("¥ " + currencyFormat.format(result.getAnnualIncome()));
        monthlyIncomeLabel.setText("¥ " + currencyFormat.format(result.getMonthlyIncome()));
        
        incomeTaxLabel.setText("¥ " + currencyFormat.format(result.getIncomeTax()));
        socialSecurityLabel.setText("¥ " + currencyFormat.format(result.getSocialSecurity()));
        housingFundLabel.setText("¥ " + currencyFormat.format(result.getHousingFund()));
        totalDeductionLabel.setText("¥ " + currencyFormat.format(result.getTotalDeduction()));
        
        netYearlyIncomeLabel.setText("¥ " + currencyFormat.format(result.getNetAnnualIncome()));
        netMonthlyIncomeLabel.setText("¥ " + currencyFormat.format(result.getNetMonthlyIncome()));
        taxRateLabel.setText(percentFormat.format(result.getTaxRate()) + "%");
    }
    
    /**
     * Reset the form
     */
    @FXML
    private void resetForm(ActionEvent event) {
        // Clear the input fields
        annualIncomeField.clear();
        socialSecurityBaseField.clear();
        housingFundBaseField.clear();
        cityComboBox.setValue("BEJING");
        
        //The reset result is displayed
        yearlyIncomeLabel.setText("¥ 0.00");
        monthlyIncomeLabel.setText("¥ 0.00");
        incomeTaxLabel.setText("¥ 0.00");
        socialSecurityLabel.setText("¥ 0.00");
        housingFundLabel.setText("¥ 0.00");
        totalDeductionLabel.setText("¥ 0.00");
        netYearlyIncomeLabel.setText("¥ 0.00");
        netMonthlyIncomeLabel.setText("¥ 0.00");
        taxRateLabel.setText("0.00%");
        
        // Focus on the revenue input box
        annualIncomeField.requestFocus();
    }
    
    /**
     * A prompt dialog box appears
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 