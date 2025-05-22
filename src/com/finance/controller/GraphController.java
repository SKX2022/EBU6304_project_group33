package com.finance.controller;

import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable; // 导入 Initializable 接口
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URL;
import java.time.LocalDate; // 用于获取当前年份
import java.time.YearMonth; // 用于计算某月的天数
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// 让 GraphController 实现 Initializable 接口
public class GraphController implements Initializable {
    // 注入 FXML 中定义的 6 个下拉框
    @FXML private ComboBox<Integer> Year0;
    @FXML private ComboBox<Integer> Month0;
    @FXML private ComboBox<Integer> Day0;
    @FXML private ComboBox<Integer> Year1;
    @FXML private ComboBox<Integer> Month1;
    @FXML private ComboBox<Integer> Day1;

    // 将 initialize 方法的签名改为正确的形式，并添加 @Override 注解
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 先填充年和月，因为日期的填充依赖于它们
        fillYear0();
        fillYear1();
        fillMonth0();
        fillMonth1();

        // 为年和月添加监听器，以便在它们的值改变时更新日期的下拉框
        Year0.valueProperty().addListener((obs, oldVal, newVal) -> updateDayComboBox(0));
        Month0.valueProperty().addListener((obs, oldVal, newVal) -> updateDayComboBox(0));

        Year1.valueProperty().addListener((obs, oldVal, newVal) -> updateDayComboBox(1));
        Month1.valueProperty().addListener((obs, oldVal, newVal) -> updateDayComboBox(1));

        // 设置默认选择，并首次更新日期的下拉框
        LocalDate today = LocalDate.now();
        Year0.getSelectionModel().select(Integer.valueOf(today.getYear())); // 默认选择当前年份
        Month0.getSelectionModel().select(Integer.valueOf(today.getMonthValue())); // 默认选择当前月份
        // 第一次调用 updateDayComboBox 来填充 Day0
        updateDayComboBox(0);
        Day0.getSelectionModel().select(Integer.valueOf(today.getDayOfMonth())); // 默认选择当前日期

        Year1.getSelectionModel().select(Integer.valueOf(today.getYear())); // 默认选择当前年份
        Month1.getSelectionModel().select(Integer.valueOf(today.getMonthValue())); // 默认选择当前月份
        // 第一次调用 updateDayComboBox 来填充 Day1
        updateDayComboBox(1);
        Day1.getSelectionModel().select(Integer.valueOf(today.getDayOfMonth())); // 默认选择当前日期

        // 您可能还需要在这里调用一个方法来根据默认选中的日期更新图表
        // updateChartBasedOnDates();
    }


    public void analyzeAndSuggest(ActionEvent actionEvent) {
        // 您的 AI 分析逻辑
    }

    public void handleDateRangeSelection(MouseEvent mouseEvent) {
        // 这个方法在您的 FXML 中是 onMouseClicked="handleDateRangeSelection"，
        // 但现在日期选择逻辑由 ComboBox 的监听器处理，这个方法可能不再需要。
        // 如果您希望点击 HBox 还能触发其他行为，可以保留并添加代码。
    }

    // 填充下拉框
    // 填充起始年下拉框
    private void fillYear0(){
        int currentYear = LocalDate.now().getYear();
        // 建议增加年份范围，例如从 2000 年到当前年份 + 5 年
        ObservableList<Integer> years = IntStream.rangeClosed(currentYear - 10, currentYear + 5)
                .boxed()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        Year0.setItems(years);
        // Year0.getSelectionModel().selectFirst(); // 不再在这里设置默认值，统一在 initialize 里设置
    }
    // 填充结束年份下拉框
    private void fillYear1(){
        int currentYear = LocalDate.now().getYear();
        ObservableList<Integer> years = IntStream.rangeClosed(currentYear - 10, currentYear + 5)
                .boxed()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        Year1.setItems(years);
        // Year1.getSelectionModel().selectFirst(); // 不再在这里设置默认值
    }
    // 填充初始月份下拉框
    private void fillMonth0(){
        ObservableList<Integer> months = IntStream.rangeClosed(1, 12)
                .boxed()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        Month0.setItems(months);
    }
    // 填充结束月份下拉框
    private void fillMonth1(){
        ObservableList<Integer> months = IntStream.rangeClosed(1, 12)
                .boxed()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        Month1.setItems(months);
    }

    // 统一的更新日期下拉框的方法
    private void updateDayComboBox(int index) {
        ComboBox<Integer> yearComboBox = (index == 0) ? Year0 : Year1;
        ComboBox<Integer> monthComboBox = (index == 0) ? Month0 : Month1;
        ComboBox<Integer> dayComboBox = (index == 0) ? Day0 : Day1;

        Integer selectedYear = yearComboBox.getValue(); // 使用 getValue() 获取当前选择的值
        Integer selectedMonth = monthComboBox.getValue(); // 使用 getValue() 获取当前选择的值

        if (selectedYear != null && selectedMonth != null) {
            try {
                int daysInMonth = YearMonth.of(selectedYear, selectedMonth).lengthOfMonth();
                ObservableList<Integer> days = IntStream.rangeClosed(1, daysInMonth)
                        .boxed()
                        .collect(Collectors.toCollection(FXCollections::observableArrayList));

                Integer currentSelectedDay = dayComboBox.getValue(); // 记住当前选择的日期
                dayComboBox.setItems(days);

                // 尝试重新选择用户之前选中的日期
                if (currentSelectedDay != null && days.contains(currentSelectedDay)) {
                    dayComboBox.getSelectionModel().select(currentSelectedDay);
                } else {
                    // 如果旧日期超出新列表范围，则选择最后一个有效日期
                    dayComboBox.getSelectionModel().select(Integer.valueOf(Math.min(currentSelectedDay != null ? currentSelectedDay : 1, daysInMonth)));
                }
            } catch (java.time.DateTimeException e) {
                // 如果年份或月份组合导致无效日期（理论上不太会发生，除非 ComboBox 值是 null），这里可以处理
                System.err.println("无效的年份或月份组合: " + selectedYear + "-" + selectedMonth + " - " + e.getMessage());
                dayComboBox.setItems(FXCollections.emptyObservableList()); // 清空日期列表
            }
        } else {
            // 如果年份或月份未选择，则清空日期下拉框
            dayComboBox.setItems(FXCollections.emptyObservableList());
        }

        // 每次日期组合改变时，尝试更新图表数据
        updateChartWithSelectedDates();
    }

    // 填充初始日期下拉框 (废弃，使用 updateDayComboBox 替代)
    // private void fillDay0(){ ... }
    // 填充结束日期下拉框 (废弃，使用 updateDayComboBox 替代)
    // private void fillDay1(){ ... }

    // 获取选定日期并更新图表（这是您需要根据实际业务逻辑完成的部分）
    private void updateChartWithSelectedDates() {
        Integer startYear = Year0.getValue();
        Integer startMonth = Month0.getValue();
        Integer startDay = Day0.getValue();

        Integer endYear = Year1.getValue();
        Integer endMonth = Month1.getValue();
        Integer endDay = Day1.getValue();

        // 检查所有日期部件是否都已选择
        if (startYear != null && startMonth != null && startDay != null &&
                endYear != null && endMonth != null && endDay != null) {
            try {
                LocalDate startDate = LocalDate.of(startYear, startMonth, startDay);
                LocalDate endDate = LocalDate.of(endYear, endMonth, endDay);

                // 验证日期范围
                if (endDate.isBefore(startDate)) {
                    System.err.println("结束日期不能在起始日期之前！");
                    // 可以添加 UI 提示，例如 Alert 对话框
                    return;
                }

                System.out.println("选定的日期范围：从 " + startDate + " 到 " + endDate);
                // ****** 在这里调用您的数据加载和图表更新逻辑 ******
                // 例如：loadFinanceDataForChart(startDate, endDate);
                // financeLineChart.getData().clear();
                // Series<String, Number> series = new Series<>();
                // series.getData().add(new XYChart.Data<>("数据点1", 100));
                // financeLineChart.getData().add(series);

            } catch (java.time.DateTimeException e) {
                System.err.println("选定的日期无效：" + e.getMessage());
            }
        } else {
            System.out.println("请选择完整的起始和结束日期。");
        }
    }
}