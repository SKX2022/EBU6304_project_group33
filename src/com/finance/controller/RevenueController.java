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
 * 收入分析控制器
 * 处理收入输入、税收计算和结果显示
 */
public class RevenueController implements Initializable {
    
    // 输入控件
    @FXML private TextField annualIncomeField;
    @FXML private ComboBox<String> cityComboBox;
    @FXML private TextField socialSecurityBaseField;
    @FXML private TextField housingFundBaseField;
    @FXML private Button calculateButton;
    @FXML private Button resetButton;
    
    // 结果显示控件
    @FXML private Label yearlyIncomeLabel;
    @FXML private Label monthlyIncomeLabel;
    @FXML private Label incomeTaxLabel;
    @FXML private Label socialSecurityLabel;
    @FXML private Label housingFundLabel;
    @FXML private Label totalDeductionLabel;
    @FXML private Label netYearlyIncomeLabel;
    @FXML private Label netMonthlyIncomeLabel;
    @FXML private Label taxRateLabel;
    
    // 服务类
    private TaxCalculatorService taxCalculatorService;
    private DecimalFormat currencyFormat;
    private DecimalFormat percentFormat;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 初始化服务类
        taxCalculatorService = new TaxCalculatorService();
        
        // 初始化格式化器
        currencyFormat = new DecimalFormat("#,##0.00");
        percentFormat = new DecimalFormat("#0.00");
        
        // 初始化城市下拉框
        initializeCityComboBox();
        
        // 初始化输入字段
        initializeInputFields();
        
        // 设置默认值
        resetForm(null);
    }
    
    /**
     * 初始化城市下拉框
     */
    private void initializeCityComboBox() {
        String[] cities = taxCalculatorService.getSupportedCities();
        cityComboBox.setItems(FXCollections.observableArrayList(cities));
        cityComboBox.setValue("北京"); // 默认选择北京
    }
    
    /**
     * 初始化输入字段
     */
    private void initializeInputFields() {
        // 只允许输入数字
        annualIncomeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                annualIncomeField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            // 自动计算社保和公积金基数
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
        
        // 城市变化时更新基数
        cityComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateDefaultBases();
        });
    }
    
    /**
     * 自动更新默认的社保和公积金基数
     */
    private void updateDefaultBases() {
        String incomeText = annualIncomeField.getText();
        if (!incomeText.isEmpty()) {
            try {
                double annualIncome = Double.parseDouble(incomeText);
                double monthlyIncome = annualIncome / 12.0;
                
                // 如果基数字段为空或者用户没有手动修改，则自动设置
                if (socialSecurityBaseField.getText().isEmpty()) {
                    socialSecurityBaseField.setText(currencyFormat.format(monthlyIncome));
                }
                
                if (housingFundBaseField.getText().isEmpty()) {
                    housingFundBaseField.setText(currencyFormat.format(monthlyIncome));
                }
            } catch (NumberFormatException e) {
                // 忽略格式错误
            }
        }
    }
    
    /**
     * 计算税收
     */
    @FXML
    private void calculateTax(ActionEvent event) {
        try {
            // 获取输入值
            String incomeText = annualIncomeField.getText().trim();
            String city = cityComboBox.getValue();
            String socialSecurityText = socialSecurityBaseField.getText().trim().replace(",", "");
            String housingFundText = housingFundBaseField.getText().trim().replace(",", "");
            
            // 验证输入
            if (incomeText.isEmpty()) {
                showAlert("输入错误", "请输入年收入金额！", Alert.AlertType.WARNING);
                return;
            }
            
            if (city == null || city.isEmpty()) {
                showAlert("输入错误", "请选择工作城市！", Alert.AlertType.WARNING);
                return;
            }
            
            double annualIncome = Double.parseDouble(incomeText);
            double socialSecurityBase = socialSecurityText.isEmpty() ? 0 : Double.parseDouble(socialSecurityText);
            double housingFundBase = housingFundText.isEmpty() ? 0 : Double.parseDouble(housingFundText);
            
            // 验证收入范围
            if (annualIncome <= 0 || annualIncome > 10000000) {
                showAlert("输入错误", "年收入应在1元到1000万元之间！", Alert.AlertType.WARNING);
                return;
            }
            
            // 执行计算
            TaxCalculationResult result = taxCalculatorService.calculateTax(
                annualIncome, city, socialSecurityBase, housingFundBase);
            
            // 显示结果
            displayResults(result);
            
        } catch (NumberFormatException e) {
            showAlert("输入错误", "请输入有效的数字！", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("计算错误", "税收计算过程中发生错误：" + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    /**
     * 显示计算结果
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
     * 重置表单
     */
    @FXML
    private void resetForm(ActionEvent event) {
        // 清空输入字段
        annualIncomeField.clear();
        socialSecurityBaseField.clear();
        housingFundBaseField.clear();
        cityComboBox.setValue("北京");
        
        // 重置结果显示
        yearlyIncomeLabel.setText("¥ 0.00");
        monthlyIncomeLabel.setText("¥ 0.00");
        incomeTaxLabel.setText("¥ 0.00");
        socialSecurityLabel.setText("¥ 0.00");
        housingFundLabel.setText("¥ 0.00");
        totalDeductionLabel.setText("¥ 0.00");
        netYearlyIncomeLabel.setText("¥ 0.00");
        netMonthlyIncomeLabel.setText("¥ 0.00");
        taxRateLabel.setText("0.00%");
        
        // 聚焦到收入输入框
        annualIncomeField.requestFocus();
    }
    
    /**
     * 显示提示对话框
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 