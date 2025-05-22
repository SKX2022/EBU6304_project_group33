package com.finance.controller;

import com.finance.service.UserfulDataPicker; // 导入 UserfulDataPicker 类

import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable; // 导入 Initializable 接口
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert; // 导入 Alert 类用于显示提示信息

import java.net.URL;
import java.time.LocalDate; // 用于获取当前年份
import java.time.YearMonth; // 用于计算某月的天数
import java.util.List;
import java.util.Map;
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
        // 注意：这里移除了对 DayX 的监听，因为 submitDateRange 按钮会统一处理
        Year0.valueProperty().addListener((obs, oldVal, newVal) -> updateDayComboBox(0));
        Month0.valueProperty().addListener((obs, oldVal, newVal) -> updateDayComboBox(0));

        Year1.valueProperty().addListener((obs, oldVal, newVal) -> updateDayComboBox(1));
        Month1.valueProperty().addListener((obs, oldVal, newVal) -> updateDayComboBox(1));

        // 设置默认选择，并首次更新日期的下拉框
        LocalDate today = LocalDate.now();
        Year0.getSelectionModel().select(Integer.valueOf(today.getYear())); // 默认选择当前年份
        Month0.getSelectionModel().select(Integer.valueOf(today.getMonthValue())); // 默认选择当前月份
        updateDayComboBox(0); // 第一次调用 updateDayComboBox 来填充 Day0
        Day0.getSelectionModel().select(Integer.valueOf(today.getDayOfMonth())); // 默认选择当前日期

        Year1.getSelectionModel().select(Integer.valueOf(today.getYear())); // 默认选择当前年份
        Month1.getSelectionModel().select(Integer.valueOf(today.getMonthValue())); // 默认选择当前月份
        updateDayComboBox(1); // 第一次调用 updateDayComboBox 来填充 Day1
        Day1.getSelectionModel().select(Integer.valueOf(today.getDayOfMonth())); // 默认选择当前日期

        // 默认情况下，initialize 不再立即调用 updateChartWithSelectedDates()，
        // 而是由确认按钮 (submitDateRange) 来触发。
        // 如果您希望应用启动时就显示默认日期的数据，可以保留在这里调用一次 updateChartWithSelectedDates();
        // 但如果您的用户预期是先选日期再点确认，则不需要在此处调用。


    }

    @FXML
    public void analyzeAndSuggest(ActionEvent actionEvent) {
        // AI 分析逻辑，保持不变
    }

    @FXML
    public void submitDateRange(ActionEvent actionEvent) {
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

        // --- 日期范围封装位置 ---
        // 在这里，您可以将 startDate 和 endDate 封装到您希望的任何对象中
        // 例如，您可以创建一个自定义的 DateRange 类，或者直接传递这两个 LocalDate 对象
        UserfulDataPicker dataPicker = new UserfulDataPicker(startDate.toString(), endDate.toString());
        // 这里可以使用 dataPicker 对象来获取所需的数据
        //最大值
        double max = dataPicker.getMax();
        //最小值
        double min = dataPicker.getMin();
        // 每日的余额
        List<Double> surplus = dataPicker.getSurplus();
        // 收入
        Map<String,Double> income = dataPicker.getIncome();
        //支出
        Map<String,Double> expenditure = dataPicker.getExpenditure();

        // ****** 在这里调用您的数据加载和图表更新逻辑 ******
        // 这就是您需要根据选定的 startDate 和 endDate 来查询数据库或服务，
        // 并更新 LineChart 的地方。
        // financeLineChart.getData().clear(); // 示例：清空现有数据
        // Series<String, Number> expenditureSeries = new Series<>();
        // ... 根据 startDate 和 endDate 从您的数据源获取数据 ...
        // financeLineChart.getData().add(expenditureSeries);

        // 提示用户操作成功
        //showAlert("日期范围已提交", "图表将更新显示 " + startDate + " 至 " + endDate + " 的数据。", Alert.AlertType.INFORMATION);
    }

    // 填充起始年下拉框
    private void fillYear0(){
        int currentYear = LocalDate.now().getYear();
        // 年份范围从当前年份前15年到当前年份后5年
        ObservableList<Integer> years = IntStream.rangeClosed(currentYear - 15, currentYear + 5)
                .boxed()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        Year0.setItems(years);
    }
    // 填充结束年份下拉框
    private void fillYear1(){
        int currentYear = LocalDate.now().getYear();
        ObservableList<Integer> years = IntStream.rangeClosed(currentYear - 15, currentYear + 5)
                .boxed()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        Year1.setItems(years);
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
                System.err.println("无效的年份或月份组合: " + selectedYear + "-" + selectedMonth + " - " + e.getMessage());
                dayComboBox.setItems(FXCollections.emptyObservableList()); // 清空日期列表
            }
        } else {
            // 如果年份或月份未选择，则清空日期下拉框
            dayComboBox.setItems(FXCollections.emptyObservableList());
        }

        // 注意：这里不再自动调用 updateChartWithSelectedDates()，而是由确认按钮触发。
        // 如果您希望每次日期下拉框改变时都更新图表（即不需要确认按钮），则取消注释下面这行。
        // updateChartWithSelectedDates();
    }

    /**
     * 辅助方法：从年、月、日 ComboBoxes 中获取 LocalDate 对象。
     * @param yearCb 年份 ComboBox
     * @param monthCb 月份 ComboBox
     * @param dayCb 日期 ComboBox
     * @return 组合的 LocalDate 对象，如果任何一个 ComboBox 没有选择，或日期组合无效，则返回 null。
     */
    private LocalDate getSelectedDate(ComboBox<Integer> yearCb, ComboBox<Integer> monthCb, ComboBox<Integer> dayCb) {
        Integer year = yearCb.getValue();
        Integer month = monthCb.getValue();
        Integer day = dayCb.getValue();

        if (year != null && month != null && day != null) {
            try {
                return LocalDate.of(year, month, day);
            } catch (java.time.DateTimeException e) {
                System.err.println("无效的日期组合: " + year + "-" + month + "-" + day + " - " + e.getMessage());
                return null; // 返回 null 表示日期无效
            }
        }
        return null; // 如果任何一个 ComboBox 没有选择
    }

    /**
     * 显示一个简单的提示框。
     * @param title 提示框标题
     * @param message 提示信息
     * @param type 提示框类型 (例如 Alert.AlertType.INFORMATION, Alert.AlertType.ERROR)
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // 不显示头部文本
        alert.setContentText(message);
        alert.showAndWait();
    }
}