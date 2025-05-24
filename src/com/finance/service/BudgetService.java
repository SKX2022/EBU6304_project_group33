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
import java.util.Map;

public class BudgetService {

    private User user;
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final String BUDGET_SETTINGS_FILE_PREFIX = "budget_settings_";

    public BudgetService(User user) {
        this.user = user;
    }

    // 保存预算设置
    public void saveBudgetSettings(Map<String, Double> settings) throws IOException {
        File settingsFile = getSettingsFile();
        objectMapper.writeValue(settingsFile, settings);
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

    // 获取预算设置文件
    private File getSettingsFile() {
        String fileName = BUDGET_SETTINGS_FILE_PREFIX + user.getUsername() + ".json";
        // 确保文件路径在项目工作目录下，例如 target/ 或者用户数据目录下
        // 这里简单起见，放在项目根目录，实际项目中应放在更合适的位置
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
        // double emergencyFund = budgetSettings.getOrDefault("emergencyFund", 0.0); // 紧急备用金暂不直接参与消耗分析显示

        double monthlyBudget = yearlyBudget > 0 ? yearlyBudget / 12 : 0.0;
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

        // 计算当年���出
        double currentYearSpending = transactions.stream()
                .filter(t -> "支出".equals(t.getType()) && parseTransactionDate(t.getDate()).getYear() == today.getYear())
                .mapToDouble(Transaction::getAmount)
                .sum();
        analysisResult.put("currentYearSpending", currentYearSpending);

        // 节假日预算分析
        double springFestivalSpending = calculateFestivalSpending(transactions, today.getYear(), Month.JANUARY, 15, Month.FEBRUARY, 28, List.of("春节", "年货", "红包", "过年", "拜年"));
        // 对于其他节日，可以类似地定义周期和关键词，或合并为一个总的“其他节日”支出
        // 这里简化处理，将 otherFestivalBudgetSet 视为一个总的其他节日预算，不单独计算其具体支出，而是从总节日预算中扣除

        double totalFestivalBudgetSet = springFestivalBudgetSet + otherFestivalBudgetSet;
        double totalFestivalSpending = springFestivalSpending; // 如果有其他节日支出计算，加在这里

        double remainingFestivalBudget = totalFestivalBudgetSet - totalFestivalSpending;
        analysisResult.put("remainingFestivalBudget", remainingFestivalBudget);
        analysisResult.put("springFestivalSpending", springFestivalSpending); // 可以选择性地返回具体节日支出

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
            // 假设日期格式为 "yyyy-MM-dd HH:mm:ss"
            return LocalDate.parse(dateStr.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            System.err.println("无法解析交易日期: " + dateStr + " - " + e.getMessage());
            return LocalDate.MIN; // 返回一个极小日期以避免NullPointer，并标记错误
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
