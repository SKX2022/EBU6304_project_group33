package com.finance.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Tax calculation services
 * Realize the calculation of individual income tax, social security and provident fund in China
 */
public class TaxCalculatorService {
    
    // Individual income tax threshold
    private static final double TAX_THRESHOLD = 5000.0;
    
    // Social Security Rates (Individual)
    private static final double PENSION_RATE = 0.08;        // Pension insurance 8%
    private static final double MEDICAL_RATE = 0.02;        // HEALTH INSURANCE 2%
    private static final double UNEMPLOYMENT_RATE = 0.005;  // UNEMPLOYMENT INSURANCE 0.5%
    
    // City configuration information
    private static final Map<String, CityConfig> CITY_CONFIGS = new HashMap<>();
    
    static {
        // Initialize the city configuration
        CITY_CONFIGS.put("BEJING", new CityConfig(0.12, 28221, 31884));
        CITY_CONFIGS.put("SHANGHAI", new CityConfig(0.07, 26004, 31014));
        CITY_CONFIGS.put("CANTON", new CityConfig(0.12, 22275, 26154));
        CITY_CONFIGS.put("SHENZHEN", new CityConfig(0.10, 22275, 26154));
        CITY_CONFIGS.put("HANGZHOU", new CityConfig(0.12, 20516, 21330));
        CITY_CONFIGS.put("NANKING", new CityConfig(0.10, 20016, 20016));
        CITY_CONFIGS.put("CHENGDU", new CityConfig(0.12, 19007, 19007));
        CITY_CONFIGS.put("WUHAN", new CityConfig(0.12, 18699, 18699));
        CITY_CONFIGS.put("Other cities", new CityConfig(0.08, 18000, 18000));
    }
    
    /**
     * Tax calculation result class
     */
    public static class TaxCalculationResult {

        private double annualIncome;           // Annual income入
        private double monthlyIncome;          // Monthly income
        private double incomeTax;              // Personal income tax
        private double socialSecurity;         // Social Security Expenses
        private double housingFund;            // CPF Expenses
        private double totalDeduction;         // Total deductions
        private double netAnnualIncome;        // Annual net income
        private double netMonthlyIncome;       // Net monthly income
        private double taxRate;                // Tax rate
        
        // Getters and Setters
        public double getAnnualIncome() { return annualIncome; }
        public void setAnnualIncome(double annualIncome) { this.annualIncome = annualIncome; }
        
        public double getMonthlyIncome() { return monthlyIncome; }
        public void setMonthlyIncome(double monthlyIncome) { this.monthlyIncome = monthlyIncome; }
        
        public double getIncomeTax() { return incomeTax; }
        public void setIncomeTax(double incomeTax) { this.incomeTax = incomeTax; }
        
        public double getSocialSecurity() { return socialSecurity; }
        public void setSocialSecurity(double socialSecurity) { this.socialSecurity = socialSecurity; }
        
        public double getHousingFund() { return housingFund; }
        public void setHousingFund(double housingFund) { this.housingFund = housingFund; }
        
        public double getTotalDeduction() { return totalDeduction; }
        public void setTotalDeduction(double totalDeduction) { this.totalDeduction = totalDeduction; }
        
        public double getNetAnnualIncome() { return netAnnualIncome; }
        public void setNetAnnualIncome(double netAnnualIncome) { this.netAnnualIncome = netAnnualIncome; }
        
        public double getNetMonthlyIncome() { return netMonthlyIncome; }
        public void setNetMonthlyIncome(double netMonthlyIncome) { this.netMonthlyIncome = netMonthlyIncome; }
        
        public double getTaxRate() { return taxRate; }
        public void setTaxRate(double taxRate) { this.taxRate = taxRate; }
    }
    
    /**
     * City configuration class
     */
    private static class CityConfig {
        double housingFundRate;     //CPF rates
        double socialSecurityBase;  // Upper limit of the social security base
        double housingFundBase;     // Upper limit on the provident fund base
        
        CityConfig(double housingFundRate, double socialSecurityBase, double housingFundBase) {
            this.housingFundRate = housingFundRate;
            this.socialSecurityBase = socialSecurityBase;
            this.housingFundBase = housingFundBase;
        }
    }
    
    /**
     * Calculate taxes
     * @param annualIncome annual income
     * @param city working city
     * @param socialSecurityBase (if 0, based on income)
     * @param housingFundBase (if 0, it is calculated based on income)
     * @return Tax calculation results
     */
    public TaxCalculationResult calculateTax(double annualIncome, String city, 
                                           double socialSecurityBase, double housingFundBase) {
        
        TaxCalculationResult result = new TaxCalculationResult();
        
        // Basic Income Information
        double monthlyIncome = annualIncome / 12.0;
        result.setAnnualIncome(annualIncome);
        result.setMonthlyIncome(monthlyIncome);
        
        // Get the city configuration
        CityConfig cityConfig = CITY_CONFIGS.getOrDefault(city, CITY_CONFIGS.get("Other cities"));
        
        // Calculate the social security base
        if (socialSecurityBase <= 0) {
            socialSecurityBase = Math.min(monthlyIncome, cityConfig.socialSecurityBase);
        }
        
        // Calculate the CPF base
        if (housingFundBase <= 0) {
            housingFundBase = Math.min(monthlyIncome, cityConfig.housingFundBase);
        }


        // Calculate Social Security Contributions (Individual Component, Annual)
        double monthlySocialSecurity = socialSecurityBase * (PENSION_RATE + MEDICAL_RATE + UNEMPLOYMENT_RATE);
        double annualSocialSecurity = monthlySocialSecurity * 12;
        result.setSocialSecurity(round(annualSocialSecurity));


        // Calculation of CPF Expenses (Personal Component, Annual)
        double monthlyHousingFund = housingFundBase * cityConfig.housingFundRate;
        double annualHousingFund = monthlyHousingFund * 12;
        result.setHousingFund(round(annualHousingFund));
        
        //Calculate personal income tax
        double annualTaxableIncome = annualIncome - annualSocialSecurity - annualHousingFund - (TAX_THRESHOLD * 12);
        double annualIncomeTax = calculateIncomeTax(annualTaxableIncome);
        result.setIncomeTax(round(annualIncomeTax));
        
        // Calculate gross deductions and net income
        double totalDeduction = annualIncomeTax + annualSocialSecurity + annualHousingFund;
        result.setTotalDeduction(round(totalDeduction));
        
        double netAnnualIncome = annualIncome - totalDeduction;
        result.setNetAnnualIncome(round(netAnnualIncome));
        result.setNetMonthlyIncome(round(netAnnualIncome / 12.0));
        
        // Calculate the tax rate
        double taxRate = (totalDeduction / annualIncome) * 100;
        result.setTaxRate(round(taxRate));
        
        return result;
    }
    
   /**
     * Calculation of personal income tax (annual)
     *Based on seven progressive tax rates after 2019
     */
    private double calculateIncomeTax(double taxableIncome) {
        if (taxableIncome <= 0) {
            return 0;
        }
        
        double tax = 0;
        
        // 七级累进税率
        double[][] taxBrackets = {
            {36000, 0.03},      // Less than 30,000，3%
            {144000, 0.10},     // 30,000-144,000，10%
            {300000, 0.20},     // 144,000-300,000，20%
            {420000, 0.25},     // 300,000-420,000，25%
            {660000, 0.30},     // 420,000-660,000，30%
            {960000, 0.35},     // 660,000-960,000，35%
            {Double.MAX_VALUE, 0.45}  // More than 960,000，45%
        };
        
        double remainingIncome = taxableIncome;
        double previousBracket = 0;
        
        for (double[] bracket : taxBrackets) {
            double bracketLimit = bracket[0];
            double rate = bracket[1];
            
            if (remainingIncome <= 0) {
                break;
            }
            
            double taxableAtThisBracket = Math.min(remainingIncome, bracketLimit - previousBracket);
            tax += taxableAtThisBracket * rate;
            
            remainingIncome -= taxableAtThisBracket;
            previousBracket = bracketLimit;
        }
        
        return tax;
    }
    
  /**
     * Get a list of supported cities
     */
    public String[] getSupportedCities() {
        return CITY_CONFIGS.keySet().toArray(new String[0]);
    }
    
    /**
     *
     * Values are rounded to 2 decimal places
     */
    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
} 