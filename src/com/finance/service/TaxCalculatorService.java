package com.finance.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 税收计算服务类
 * 实现中国个人所得税、社保、公积金的计算
 */
public class TaxCalculatorService {
    
    // 个人所得税起征点
    private static final double TAX_THRESHOLD = 5000.0;
    
    // 社保费率（个人承担部分）
    private static final double PENSION_RATE = 0.08;        // 养老保险 8%
    private static final double MEDICAL_RATE = 0.02;        // 医疗保险 2%
    private static final double UNEMPLOYMENT_RATE = 0.005;  // 失业保险 0.5%
    
    // 城市配置信息
    private static final Map<String, CityConfig> CITY_CONFIGS = new HashMap<>();
    
    static {
        // 初始化城市配置
        CITY_CONFIGS.put("北京", new CityConfig(0.12, 28221, 31884));
        CITY_CONFIGS.put("上海", new CityConfig(0.07, 26004, 31014));
        CITY_CONFIGS.put("广州", new CityConfig(0.12, 22275, 26154));
        CITY_CONFIGS.put("深圳", new CityConfig(0.10, 22275, 26154));
        CITY_CONFIGS.put("杭州", new CityConfig(0.12, 20516, 21330));
        CITY_CONFIGS.put("南京", new CityConfig(0.10, 20016, 20016));
        CITY_CONFIGS.put("成都", new CityConfig(0.12, 19007, 19007));
        CITY_CONFIGS.put("武汉", new CityConfig(0.12, 18699, 18699));
        CITY_CONFIGS.put("其他城市", new CityConfig(0.08, 18000, 18000));
    }
    
    /**
     * 税收计算结果类
     */
    public static class TaxCalculationResult {
        private double annualIncome;           // 年收入
        private double monthlyIncome;          // 月收入
        private double incomeTax;              // 个人所得税
        private double socialSecurity;         // 社保费用
        private double housingFund;            // 公积金费用
        private double totalDeduction;         // 总扣除
        private double netAnnualIncome;        // 年净收入
        private double netMonthlyIncome;       // 月净收入
        private double taxRate;                // 税负率
        
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
     * 城市配置类
     */
    private static class CityConfig {
        double housingFundRate;     // 公积金费率
        double socialSecurityBase;  // 社保基数上限
        double housingFundBase;     // 公积金基数上限
        
        CityConfig(double housingFundRate, double socialSecurityBase, double housingFundBase) {
            this.housingFundRate = housingFundRate;
            this.socialSecurityBase = socialSecurityBase;
            this.housingFundBase = housingFundBase;
        }
    }
    
    /**
     * 计算税收
     * @param annualIncome 年收入
     * @param city 工作城市
     * @param socialSecurityBase 社保基数（如果为0则按收入计算）
     * @param housingFundBase 公积金基数（如果为0则按收入计算）
     * @return 税收计算结果
     */
    public TaxCalculationResult calculateTax(double annualIncome, String city, 
                                           double socialSecurityBase, double housingFundBase) {
        
        TaxCalculationResult result = new TaxCalculationResult();
        
        // 基本收入信息
        double monthlyIncome = annualIncome / 12.0;
        result.setAnnualIncome(annualIncome);
        result.setMonthlyIncome(monthlyIncome);
        
        // 获取城市配置
        CityConfig cityConfig = CITY_CONFIGS.getOrDefault(city, CITY_CONFIGS.get("其他城市"));
        
        // 计算社保基数
        if (socialSecurityBase <= 0) {
            socialSecurityBase = Math.min(monthlyIncome, cityConfig.socialSecurityBase);
        }
        
        // 计算公积金基数
        if (housingFundBase <= 0) {
            housingFundBase = Math.min(monthlyIncome, cityConfig.housingFundBase);
        }
        
        // 计算社保费用（个人部分，年度）
        double monthlySocialSecurity = socialSecurityBase * (PENSION_RATE + MEDICAL_RATE + UNEMPLOYMENT_RATE);
        double annualSocialSecurity = monthlySocialSecurity * 12;
        result.setSocialSecurity(round(annualSocialSecurity));
        
        // 计算公积金费用（个人部分，年度）
        double monthlyHousingFund = housingFundBase * cityConfig.housingFundRate;
        double annualHousingFund = monthlyHousingFund * 12;
        result.setHousingFund(round(annualHousingFund));
        
        // 计算个人所得税
        double annualTaxableIncome = annualIncome - annualSocialSecurity - annualHousingFund - (TAX_THRESHOLD * 12);
        double annualIncomeTax = calculateIncomeTax(annualTaxableIncome);
        result.setIncomeTax(round(annualIncomeTax));
        
        // 计算总扣除和净收入
        double totalDeduction = annualIncomeTax + annualSocialSecurity + annualHousingFund;
        result.setTotalDeduction(round(totalDeduction));
        
        double netAnnualIncome = annualIncome - totalDeduction;
        result.setNetAnnualIncome(round(netAnnualIncome));
        result.setNetMonthlyIncome(round(netAnnualIncome / 12.0));
        
        // 计算税负率
        double taxRate = (totalDeduction / annualIncome) * 100;
        result.setTaxRate(round(taxRate));
        
        return result;
    }
    
    /**
     * 计算个人所得税（年度）
     * 按照2019年后的七级累进税率
     */
    private double calculateIncomeTax(double taxableIncome) {
        if (taxableIncome <= 0) {
            return 0;
        }
        
        double tax = 0;
        
        // 七级累进税率
        double[][] taxBrackets = {
            {36000, 0.03},      // 3万以下，3%
            {144000, 0.10},     // 3万-14.4万，10%
            {300000, 0.20},     // 14.4万-30万，20%
            {420000, 0.25},     // 30万-42万，25%
            {660000, 0.30},     // 42万-66万，30%
            {960000, 0.35},     // 66万-96万，35%
            {Double.MAX_VALUE, 0.45}  // 96万以上，45%
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
     * 获取支持的城市列表
     */
    public String[] getSupportedCities() {
        return CITY_CONFIGS.keySet().toArray(new String[0]);
    }
    
    /**
     * 数值四舍五入到2位小数
     */
    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
} 