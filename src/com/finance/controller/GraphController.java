package com.finance.controller;

import com.finance.service.UserfulDataPicker;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
// 不再需要这些导入，因为我们不再从FMXL的Circle获取颜色或手动设置内联样式
// import javafx.scene.paint.Paint;
// import javafx.scene.shape.Circle;
// import javafx.application.Platform; // 如果不再Platform.runLater()，也可移除

import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.HashMap;
import java.time.format.DateTimeFormatter;

public class GraphController implements Initializable {
    // FXML 注入的 UI 元素
    @FXML private ComboBox<Integer> Year0;
    @FXML private ComboBox<Integer> Month0;
    @FXML private ComboBox<Integer> Day0;
    @FXML private ComboBox<Integer> Year1;
    @FXML private ComboBox<Integer> Month1;
    @FXML private ComboBox<Integer> Day1;

    // FXML 注入的折线图及其轴
    @FXML private LineChart<String, Number> financeLineChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    // 如果 FXML 中有 fx:id，这里仍然会注入，但我们不会使用它们来设置折线颜色。
    // 如果 FXML 中没有 fx:id，可以删除这三行。
    // @FXML private Circle expenditureColorCircle;
    // @FXML private Circle incomeColorCircle;
    // @FXML private Circle surplusColorCircle;

    // 定义用于存储图表数据的数据系列
    private XYChart.Series<String, Number> expenditureSeries;
    private XYChart.Series<String, Number> incomeSeries;
    private XYChart.Series<String, Number> surplusSeries;

    // 日期格式化器，用于将 LocalDate 转换为 "yyyy-MM-dd" 字符串，以匹配 UserfulDataPicker 的 Map 键
    private static final DateTimeFormatter DATE_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 初始化日期选择下拉框
        fillYear0();
        fillYear1();
        fillMonth0();
        fillMonth1();

        // 为年和月下拉框添加监听器，以便在值改变时更新日期下拉框
        Year0.valueProperty().addListener((obs, oldVal, newVal) -> updateDayComboBox(0));
        Month0.valueProperty().addListener((obs, oldVal, newVal) -> updateDayComboBox(0));
        Year1.valueProperty().addListener((obs, oldVal, newVal) -> updateDayComboBox(1));
        Month1.valueProperty().addListener((obs, oldVal, newVal) -> updateDayComboBox(1));

        // 设置默认选择为当前日期，并首次更新日期下拉框
        LocalDate today = LocalDate.now();
        Year0.getSelectionModel().select(Integer.valueOf(today.getYear()));
        Month0.getSelectionModel().select(Integer.valueOf(today.getMonthValue()));
        updateDayComboBox(0);
        Day0.getSelectionModel().select(Integer.valueOf(today.getDayOfMonth()));

        Year1.getSelectionModel().select(Integer.valueOf(today.getYear()));
        Month1.getSelectionModel().select(Integer.valueOf(today.getMonthValue()));
        updateDayComboBox(1);
        Day1.getSelectionModel().select(Integer.valueOf(today.getDayOfMonth()));

        // 初始化图表数据系列并添加到图表中
        expenditureSeries = new XYChart.Series<>();
        expenditureSeries.setName("支出"); // 确保图例显示中文

        incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("收入"); // 确保图例显示中文

        surplusSeries = new XYChart.Series<>();
        surplusSeries.setName("盈余"); // 确保图例显示中文

        // 添加系列的顺序很重要，它决定了在CSS中它们对应的索引（-fx-series-0, -fx-series-1, -fx-series-2）
        financeLineChart.getData().addAll(expenditureSeries, incomeSeries, surplusSeries);

        // *** 重要：移除所有手动设置折线颜色的代码 ***
        // 因为颜色将由 style.css 控制

        // 配置轴的初始标签
        xAxis.setLabel("日期");
        yAxis.setLabel("金额");

        // 应用启动时默认加载数据（可选，如果需要用户点击按钮才加载则注释此行）
        updateChartWithSelectedDates();
    }

    @FXML
    public void analyzeAndSuggest(ActionEvent actionEvent) {
        System.out.println("AI 分析收支情况并给出储蓄建议等");
        showAlert("AI 分析", "AI 分析功能待实现，敬请期待！", Alert.AlertType.INFORMATION);
    }

    @FXML
    public void submitDateRange(ActionEvent actionEvent) {
        // 触发图表根据选定日期更新
        updateChartWithSelectedDates();
    }

    /**
     * 根据用户选择的日期范围获取数据并更新图表。
     */
    private void updateChartWithSelectedDates() {
        // 获取选定的起始和结束日期
        LocalDate startDate = getSelectedDate(Year0, Month0, Day0);
        LocalDate endDate = getSelectedDate(Year1, Month1, Day1);

        // 检查日期是否完整且有效
        if (startDate == null || endDate == null) {
            showAlert("日期选择错误", "请确保已选择完整的起始和结束日期。", Alert.AlertType.ERROR);
            return;
        }

        // 验证日期范围：结束日期不能在起始日期之前
        if (endDate.isBefore(startDate)) {
            showAlert("日期范围错误", "结束日期不能早于起始日期！", Alert.AlertType.ERROR);
            return;
        }

        // 使用 UserfulDataPicker 获取数据
        UserfulDataPicker dataPicker = new UserfulDataPicker(
                startDate.format(DATE_KEY_FORMATTER),
                endDate.format(DATE_KEY_FORMATTER)
        );

        Map<String, Double> incomeDataMap = dataPicker.getIncome();
        Map<String, Double> expenditureDataMap = dataPicker.getExpenditure();

        // 调用更新图表的核心方法
        updateFinanceChart(incomeDataMap, expenditureDataMap, startDate, endDate);
        // *** 重要：这里也不再需要调用 applySeriesColors() ***
    }

    /**
     * 根据获取到的财务数据更新折线图。
     *
     * @param incomeDataMap 每日收入数据，键为 "yyyy-MM-dd" 格式的日期字符串
     * @param expenditureDataMap 每日支出数据，键为 "yyyy-MM-dd" 格式的日期字符串
     * @param startDate 数据范围的开始日期
     * @param endDate 数据范围的结束日期
     */
    private void updateFinanceChart(Map<String, Double> incomeDataMap,
                                    Map<String, Double> expenditureDataMap,
                                    LocalDate startDate, LocalDate endDate) {
        // 清除旧数据点
        expenditureSeries.getData().clear();
        incomeSeries.getData().clear();
        surplusSeries.getData().clear();

        double maxAllValue = 0;
        double minSurplusValue = 0;

        // 定义 X 轴日期标签的格式（例如 "05-23"）
        DateTimeFormatter xAxisLabelFormatter = DateTimeFormatter.ofPattern("MM-dd");

        // 遍历日期范围，添加数据点
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dateKey = currentDate.format(DATE_KEY_FORMATTER);

            Double dailyIncome = incomeDataMap.getOrDefault(dateKey, 0.0);
            Double dailyExpenditure = expenditureDataMap.getOrDefault(dateKey, 0.0);
            Double dailySurplus = dailyIncome - dailyExpenditure;

            String dateLabel = currentDate.format(xAxisLabelFormatter);
            expenditureSeries.getData().add(new XYChart.Data<>(dateLabel, dailyExpenditure));
            incomeSeries.getData().add(new XYChart.Data<>(dateLabel, dailyIncome));
            surplusSeries.getData().add(new XYChart.Data<>(dateLabel, dailySurplus));

            maxAllValue = Math.max(maxAllValue, dailyExpenditure);
            maxAllValue = Math.max(maxAllValue, dailyIncome);
            maxAllValue = Math.max(maxAllValue, Math.abs(dailySurplus));

            minSurplusValue = Math.min(minSurplusValue, dailySurplus);

            currentDate = currentDate.plusDays(1);
        }

        // --- 动态配置 Y 轴 ---
        double effectiveUpperBound = calculateUpperBound(maxAllValue);
        double effectiveLowerBound = 0;
        double effectiveTickUnit;

        if (minSurplusValue < 0) {
            effectiveLowerBound = Math.floor(minSurplusValue / 1000) * 1000;
            effectiveTickUnit = calculateTickUnit(effectiveUpperBound - effectiveLowerBound);
        } else {
            effectiveLowerBound = 0;
            effectiveTickUnit = calculateTickUnit(effectiveUpperBound);
        }

        yAxis.setLowerBound(effectiveLowerBound);
        yAxis.setUpperBound(effectiveUpperBound);
        yAxis.setTickUnit(effectiveTickUnit);
        yAxis.setMinorTickVisible(false);
    }

    private double calculateUpperBound(double maxValue) {
        if (maxValue <= 0) return 10000.0;
        if (maxValue < 1000) return Math.ceil(maxValue / 100.0) * 100.0;
        if (maxValue < 5000) return Math.ceil(maxValue / 500.0) * 500.0;
        if (maxValue < 10000) return Math.ceil(maxValue / 1000.0) * 1000.0;
        if (maxValue < 50000) return Math.ceil(maxValue / 5000.0) * 5000.0;
        if (maxValue < 100000) return Math.ceil(maxValue / 10000.0) * 10000.0;
        return Math.ceil(maxValue / 50000.0) * 50000.0;
    }

    private double calculateTickUnit(double range) {
        if (range <= 0) return 1000.0;
        double roughTickUnit = range / 5.0;

        if (roughTickUnit < 100) return Math.ceil(roughTickUnit / 10.0) * 10.0;
        if (roughTickUnit < 500) return Math.ceil(roughTickUnit / 50.0) * 50.0;
        if (roughTickUnit < 1000) return Math.ceil(roughTickUnit / 100.0) * 100.0;
        if (roughTickUnit < 5000) return Math.ceil(roughTickUnit / 500.0) * 500.0;
        if (roughTickUnit < 10000) return Math.ceil(roughTickUnit / 1000.0) * 1000.0;
        return Math.ceil(roughTickUnit / 5000.0) * 5000.0;
    }

    private void fillYear0(){
        int currentYear = LocalDate.now().getYear();
        ObservableList<Integer> years = IntStream.rangeClosed(currentYear - 15, currentYear + 5)
                .boxed()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        Year0.setItems(years);
    }

    private void fillYear1(){
        int currentYear = LocalDate.now().getYear();
        ObservableList<Integer> years = IntStream.rangeClosed(currentYear - 15, currentYear + 5)
                .boxed()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        Year1.setItems(years);
    }

    private void fillMonth0(){
        ObservableList<Integer> months = IntStream.rangeClosed(1, 12)
                .boxed()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        Month0.setItems(months);
    }

    private void fillMonth1(){
        ObservableList<Integer> months = IntStream.rangeClosed(1, 12)
                .boxed()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        Month1.setItems(months);
    }

    private void updateDayComboBox(int index) {
        ComboBox<Integer> yearComboBox = (index == 0) ? Year0 : Year1;
        ComboBox<Integer> monthComboBox = (index == 0) ? Month0 : Month1;
        ComboBox<Integer> dayComboBox = (index == 0) ? Day0 : Day1;

        Integer selectedYear = yearComboBox.getValue();
        Integer selectedMonth = monthComboBox.getValue();

        if (selectedYear != null && selectedMonth != null) {
            try {
                int daysInMonth = YearMonth.of(selectedYear, selectedMonth).lengthOfMonth();
                ObservableList<Integer> days = IntStream.rangeClosed(1, daysInMonth)
                        .boxed()
                        .collect(Collectors.toCollection(FXCollections::observableArrayList));

                Integer currentSelectedDay = dayComboBox.getValue();
                dayComboBox.setItems(days);

                if (currentSelectedDay != null && days.contains(currentSelectedDay)) {
                    dayComboBox.getSelectionModel().select(currentSelectedDay);
                } else {
                    dayComboBox.getSelectionModel().select(Integer.valueOf(Math.min(currentSelectedDay != null ? currentSelectedDay : 1, daysInMonth)));
                }
            } catch (java.time.DateTimeException e) {
                System.err.println("无效的年份或月份组合: " + selectedYear + "-" + selectedMonth + " - " + e.getMessage());
                dayComboBox.setItems(FXCollections.emptyObservableList());
            }
        } else {
            dayComboBox.setItems(FXCollections.emptyObservableList());
        }
    }

    private LocalDate getSelectedDate(ComboBox<Integer> yearCb, ComboBox<Integer> monthCb, ComboBox<Integer> dayCb) {
        Integer year = yearCb.getValue();
        Integer month = monthCb.getValue();
        Integer day = dayCb.getValue();

        if (year != null && month != null && day != null) {
            try {
                return LocalDate.of(year, month, day);
            } catch (java.time.DateTimeException e) {
                System.err.println("无效的日期组合: " + year + "-" + month + "-" + day + " - " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}