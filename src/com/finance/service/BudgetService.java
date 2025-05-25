package com.finance.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.manager.TransactionManager;
import com.finance.model.Transaction;
import com.finance.model.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BudgetService {

    private User user;
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final String BUDGET_SETTINGS_FILE_PREFIX = "budget_settings_";
    private static final String MONTHLY_BUDGET_PREFIX = "monthlyBudget_";

    public BudgetService(User user) {
        this.user = user;
    }

    // 保存预算设置
    public void saveBudgetSettings(Map<String, Double> settings) throws IOException {
        File settingsFile = getSettingsFile();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(settingsFile, settings);
    }

    // 加载预算设置
    public Map<String, Double> loadBudgetSettings() {
        File settingsFile = getSettingsFile();
        if (settingsFile.exists() && settingsFile.length() > 0) {
            try {
                return objectMapper.readValue(settingsFile, new TypeReference<Map<String, Double>>() {});
            } catch (IOException e) {
                System.err.println("加载预算设置失败: " + e.getMessage());
                return new HashMap<>(); // 返回空设置或默认设置
            }
        }
        return new HashMap<>(); // 如果文件不存在或为空，返回空设置
    }

    // 保存特定月份预算
    public void saveSpecificMonthlyBudget(String monthName, double budget) throws IOException {
        Map<String, Double> settings = loadBudgetSettings();
        settings.put(MONTHLY_BUDGET_PREFIX + monthName, budget);
        saveBudgetSettings(settings);
    }

    // 获取所有月份预���
    public Map<String, Double> getAllMonthlyBudgets() {
        Map<String, Double> allSettings = loadBudgetSettings();
        Map<String, Double> monthlyBudgets = new HashMap<>();
        for (Map.Entry<String, Double> entry : allSettings.entrySet()) {
            if (entry.getKey().startsWith(MONTHLY_BUDGET_PREFIX)) {
                String monthName = entry.getKey().substring(MONTHLY_BUDGET_PREFIX.length());
                monthlyBudgets.put(monthName, entry.getValue());
            }
        }
        return monthlyBudgets;
    }

    // 获取特定月份预算
    public Double getSpecificMonthlyBudget(String monthName) {
        Map<String, Double> settings = loadBudgetSettings();
        return settings.get(MONTHLY_BUDGET_PREFIX + monthName); // 如果未找到，返回null
    }

    // 获取预算设置文件
    private File getSettingsFile() {
        String fileName = BUDGET_SETTINGS_FILE_PREFIX + user.getUsername() + ".json";
        return Paths.get(fileName).toFile();
    }

    // 解析AI返回的预算建议 (JSON格式)
    public Map<String, Double> parseAiBudgetSuggestion(String jsonSuggestion) throws IOException {
        if (jsonSuggestion == null || jsonSuggestion.trim().isEmpty()) {
            return new HashMap<>();
        }
        return objectMapper.readValue(jsonSuggestion, new TypeReference<Map<String, Double>>() {});
    }

    // 获取预算分析数据
    public Map<String, Object> getBudgetAnalysis(TransactionManager transactionManager) {
        Map<String, Object> analysisResult = new HashMap<>();
        Map<String, Double> budgetSettings = loadBudgetSettings();

        double yearlyBudget = budgetSettings.getOrDefault("yearlyBudget", 0.0);
        double springFestivalBudgetSet = budgetSettings.getOrDefault("springFestivalBudget", 0.0);
        double otherFestivalBudgetSet = budgetSettings.getOrDefault("otherFestivalBudget", 0.0);

        // 确定当前月份的预算
        Month currentMonthEnum = LocalDate.now().getMonth();
        String currentMonthName = currentMonthEnum.getDisplayName(java.time.format.TextStyle.FULL_STANDALONE, new Locale("zh", "CN"));
        Double specificCurrentMonthBudget = getSpecificMonthlyBudget(currentMonthName);

        double monthlyBudget;
        if (specificCurrentMonthBudget != null) {
            monthlyBudget = specificCurrentMonthBudget;
        } else if (yearlyBudget > 0) {
            monthlyBudget = yearlyBudget / 12;
        } else {
            monthlyBudget = 0.0;
        }

        analysisResult.put("monthlyBudget", monthlyBudget);
        analysisResult.put("yearlyBudget", yearlyBudget);

        List<Transaction> transactions = transactionManager.getAllTransactions();
        LocalDate today = LocalDate.now();
        YearMonth currentYearMonth = YearMonth.from(today);

        // 计算当月支出
        double currentMonthSpending = transactions.stream()
                .filter(t -> "支出".equals(t.getType()) && YearMonth.from(parseTransactionDate(t.getDate())).equals(currentYearMonth))
                .mapToDouble(Transaction::getAmount)
                .sum();
        analysisResult.put("currentMonthSpending", currentMonthSpending);

        // 计算当年支出
        double currentYearSpending = transactions.stream()
                .filter(t -> "支出".equals(t.getType()) && parseTransactionDate(t.getDate()).getYear() == today.getYear())
                .mapToDouble(Transaction::getAmount)
                .sum();
        analysisResult.put("currentYearSpending", currentYearSpending);

        // 节假日预算分析
        double springFestivalSpending = calculateFestivalSpending(transactions, today.getYear(), Month.JANUARY, 15, Month.FEBRUARY, 28, List.of("春节", "年货", "红包", "过年", "拜年"));

        double totalFestivalBudgetSet = springFestivalBudgetSet + otherFestivalBudgetSet;
        double totalFestivalSpending = springFestivalSpending;

        double remainingFestivalBudget = totalFestivalBudgetSet - totalFestivalSpending;
        analysisResult.put("remainingFestivalBudget", remainingFestivalBudget);
        analysisResult.put("springFestivalSpending", springFestivalSpending);

        // 预算健康状况评估
        String budgetHealthStatus = "健康";
        if (yearlyBudget > 0 && currentYearSpending > yearlyBudget) {
            budgetHealthStatus = "超出年度预算";
        } else if (monthlyBudget > 0 && currentMonthSpending > monthlyBudget * 1.1) { // 月度超支10%警告
            budgetHealthStatus = "月度预算紧张";
        } else if (totalFestivalBudgetSet > 0 && remainingFestivalBudget < 0) {
            budgetHealthStatus = "节日预算超支";
        }
        analysisResult.put("budgetHealthStatus", budgetHealthStatus);

        return analysisResult;
    }

    // 辅助方法：解析交易日期字符串为LocalDate
    private LocalDate parseTransactionDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            System.err.println("无法解析交易日期: " + dateStr + " - " + e.getMessage());
            return LocalDate.MIN;
        }
    }

    // 辅助方法：计算指定节日期间的支出
    private double calculateFestivalSpending(List<Transaction> transactions, int year,
                                             Month startMonth, int startDay,
                                             Month endMonth, int endDay,
                                             List<String> keywords) {
        LocalDate startDate = LocalDate.of(year, startMonth, startDay);
        LocalDate endDate = LocalDate.of(year, endMonth, endDay);

        return transactions.stream()
            .filter(t -> "支出".equals(t.getType()))
            .filter(t -> {
                LocalDate transactionDate = parseTransactionDate(t.getDate());
                return !transactionDate.isBefore(startDate) && !transactionDate.isAfter(endDate);
            })
            .filter(t -> keywords.stream().anyMatch(keyword ->
                            t.getCategory().toLowerCase().contains(keyword.toLowerCase()) ||
                            (t.getProject() != null && t.getProject().toLowerCase().contains(keyword.toLowerCase())) ))
            .mapToDouble(Transaction::getAmount)
            .sum();
    }
}
