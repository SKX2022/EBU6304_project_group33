package com.finance.controller;

import com.finance.service.LlmService;
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
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    // 定义用于存储图表数据的数据系列
    private XYChart.Series<String, Number> expenditureSeries;
    private XYChart.Series<String, Number> incomeSeries;
    private XYChart.Series<String, Number> surplusSeries;

    // 日期格式化器，用于将 LocalDate 转换为 "yyyy-MM-dd" 字符串，以匹配 UserfulDataPicker 的 Map 键
    private static final DateTimeFormatter DATE_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

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
        expenditureSeries.setName("支出");

        incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("收入");

        surplusSeries = new XYChart.Series<>();
        surplusSeries.setName("盈余");

        // 添加系列的顺序很重要，它决定了在CSS中它们对应的索引
        financeLineChart.getData().addAll(expenditureSeries, incomeSeries, surplusSeries);

        // 配置轴的初始标签
        xAxis.setLabel("日期");
        yAxis.setLabel("金额");

        // 添加图表整体悬停交互效果
        financeLineChart.setOnMouseMoved(event -> handleChartMouseMoved(event));
        financeLineChart.setOnMouseExited(event -> resetChartStyles());

        // 应用启动时默认加载数据
        updateChartWithSelectedDates();
    }

    /**
     * 处理鼠标在图表上移动的事件
     */
    private void handleChartMouseMoved(MouseEvent event) {
        boolean seriesHighlighted = false;

        // 首先将所有系列设置为淡出状态
        for (XYChart.Series<String, Number> s : financeLineChart.getData()) {
            if (s.getNode() != null) {
                s.getNode().setStyle("-fx-opacity: 0.5;");
            }

            // 数据点也淡出
            for (XYChart.Data<String, Number> d : s.getData()) {
                if (d.getNode() != null) {
                    d.getNode().setStyle("-fx-opacity: 0.5;");
                }
            }
        }

        // 然后检查鼠标是否在某个系列的数据点附近
        for (XYChart.Series<String, Number> s : financeLineChart.getData()) {
            for (XYChart.Data<String, Number> d : s.getData()) {
                if (d.getNode() != null && d.getNode().getBoundsInParent().contains(
                        d.getNode().sceneToLocal(event.getSceneX(), event.getSceneY()))) {
                    // 高亮当前系列
                    if (s.getNode() != null) {
                        s.getNode().setStyle("-fx-opacity: 1.0;");
                    }

                    // 高亮该系列的所有数据点
                    for (XYChart.Data<String, Number> pd : s.getData()) {
                        if (pd.getNode() != null) {
                            pd.getNode().setStyle("-fx-opacity: 1.0;");
                        }
                    }

                    seriesHighlighted = true;
                    break;
                }
            }
        }

        // 如果没有找到特定的系列，则恢复所有系列的正常显示
        if (!seriesHighlighted) {
            resetChartStyles();
        }
    }

    /**
     * 重置所有图表样式到默认状态
     */
    private void resetChartStyles() {
        for (XYChart.Series<String, Number> s : financeLineChart.getData()) {
            if (s.getNode() != null) {
                s.getNode().setStyle("-fx-opacity: 1.0;");
            }

            for (XYChart.Data<String, Number> d : s.getData()) {
                if (d.getNode() != null) {
                    d.getNode().setStyle("-fx-opacity: 1.0;");
                }
            }
        }
    }

    @FXML
    public void analyzeAndSuggest(ActionEvent actionEvent) {
        System.out.println("GraphController: AI 分析收支情况并给出储蓄建议等 - 按钮被点击");

        // 1. 获取选定的日期范围，用于 UserfulDataPicker
        LocalDate startDate = getSelectedDate(Year0, Month0, Day0);
        LocalDate endDate = getSelectedDate(Year1, Month1, Day1);

        if (startDate == null || endDate == null) {
            showAlert("日期选择错误", "请确保已选择完整的起始和结束日期。", Alert.AlertType.ERROR);
            System.err.println("GraphController: 日期选择不完整，无法进行AI分析。");
            return;
        }
        if (endDate.isBefore(startDate)) {
            showAlert("日期范围错误", "结束日期不能早于起始日期！", Alert.AlertType.ERROR);
            System.err.println("GraphController: 结束日期早于起始日期，无法进行AI分析。");
            return;
        }

        // 2. 使用 UserfulDataPicker 获取原始交易明细列表
        System.out.println("GraphController: 正在通过 UserfulDataPicker 获取交易明细...");
        UserfulDataPicker dataPicker = new UserfulDataPicker(
                startDate.format(DATE_KEY_FORMATTER),
                endDate.format(DATE_KEY_FORMATTER)
        );
        List<String> transactionDetailsList = dataPicker.getTransactionDetails();
        System.out.println("GraphController: 获取到的原始交易明细数量: " + transactionDetailsList.size());

        // 添加打印日志，查看UserfulDataPicker返回的原始交易明细内容
        System.out.println("GraphController: --- UserfulDataPicker 获取到的原始交易明细内容 ---");
        if (transactionDetailsList.isEmpty()) {
            System.out.println("  (交易明细列表为空)");
        } else {
            for (String detail : transactionDetailsList) {
                System.out.println("  - " + detail);
            }
        }
        System.out.println("GraphController: ------------------------------------------------");


        // 3. 将 List<String> 拼接成一个大字符串作为 LLM 的 prompt
        String details = mergeString(transactionDetailsList);
        String fullPrompt = details + "请分析以上收支情况，并给出详细的储蓄和支出优化建议：";

        System.out.println("GraphController: --- 准备发送给 AI 的完整 Prompt ---");
        System.out.println(fullPrompt); // **打印完整的 Prompt，确认其内容和格式**
        System.out.println("GraphController: ------------------------------------");

        try {
            // 4. 创建 LlmService 实例
            LlmService llmService = new LlmService(fullPrompt);

            // ****** 核心修复：调用 callLlmApi() 方法，实际执行 API 请求 ******
            System.out.println("GraphController: 正在调用 LlmService.callLlmApi()，请求 AI API...");
            llmService.callLlmApi(); // 这是关键，确保API被调用
            System.out.println("GraphController: LlmService.callLlmApi() 调用完成。");
            // ******************************************************************

            // 5. 获取 AI 的回答
            String aiResponse = llmService.getAnswer(); // 现在应该能获取到数据了
            System.out.println("GraphController: --- 从 LlmService 获取到的 AI 回答 ---");
            System.out.println(aiResponse != null && !aiResponse.isEmpty() ? aiResponse : "[回答为空或无效]"); // 打印获取到的回答，或提示为空
            System.out.println("GraphController: -------------------------------------");

            // 6. 弹出窗口显示结果
            if (aiResponse != null && !aiResponse.isEmpty()) {
                showAlert("AI 分析结果", aiResponse, Alert.AlertType.INFORMATION);
            } else {
                showAlert("AI 分析失败", "未能获取到AI的分析结果。请检查API返回或数据是否为空。", Alert.AlertType.WARNING);
            }

        } catch (IOException e) {
            System.err.println("GraphController 错误：API 调用网络或I/O异常: " + e.getMessage());
            e.printStackTrace(); // 打印完整的堆栈跟踪
            showAlert("错误", "AI 分析服务网络连接失败：" + e.getMessage(), Alert.AlertType.ERROR);
        } catch (InterruptedException e) {
            System.err.println("GraphController 错误：API 调用被中断: " + e.getMessage());
            e.printStackTrace(); // 打印完整的堆栈跟踪
            showAlert("错误", "AI 分析服务中断，请稍后再试。", Alert.AlertType.ERROR);
            Thread.currentThread().interrupt(); // 重新设置中断状态
        } catch (LlmService.LlmServiceException e) {
            System.err.println("GraphController 错误：AI 服务内部异常: " + e.getMessage());
            e.printStackTrace(); // 打印完整的堆栈跟踪
            showAlert("错误", "AI 分析服务出错：" + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) { // 捕获所有其他未知异常
            System.err.println("GraphController 错误：发生未知错误: " + e.getMessage());
            e.printStackTrace(); // 打印完整的堆栈跟踪
            showAlert("错误", "发生未知错误：" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    //字符串拼接
    private String mergeString(List<String> strings) {
        StringBuilder sb = new StringBuilder();
        if (strings != null && !strings.isEmpty()) {
            for (String str : strings) {
                sb.append(str).append(";\n"); // 每个明细后加分号和换行符，更清晰
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 2); // 移除最后一个 ";\n"
            }
        } else {
            System.out.println("GraphController: mergeString 收到空列表，返回空字符串。"); // 添加日志
        }
        return sb.toString();
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

        // 为数据点添加交互效果
        addDataPointInteractions();
    }

    /**
     * 为图表中的所有数据点添加交互效果
     */
    private void addDataPointInteractions() {
        // 为每个系列的每个数据点添加交互效果
        for (XYChart.Series<String, Number> series : financeLineChart.getData()) {
            String seriesName = series.getName();
            String seriesColor = getColorForSeries(seriesName);

            for (XYChart.Data<String, Number> dataPoint : series.getData()) {
                // 数据点可能尚未被渲染，需要等待节点实际可用
                if (dataPoint.getNode() != null) {
                    setupDataPointNode(dataPoint, seriesName, seriesColor);
                } else {
                    // 如果节点尚未准备好，添加监听器等待节点变为可用
                    dataPoint.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            setupDataPointNode(dataPoint, seriesName, seriesColor);
                        }
                    });
                }
            }
        }
    }

    /**
     * 为单个数据点节点设置交互效果
     */
    private void setupDataPointNode(XYChart.Data<String, Number> dataPoint, String seriesName, String seriesColor) {
        Node node = dataPoint.getNode();
        if (node == null) return;

        // 获取数据值和日期标签
        String dateStr = dataPoint.getXValue();
        Number value = dataPoint.getYValue();

        // 创建工具提示
        Tooltip tooltip = new Tooltip(
            String.format("%s\n日期：%s\n金额：%.2f 元",
                seriesName, dateStr, value.doubleValue())
        );
        tooltip.setStyle("-fx-font-size: 14px; -fx-background-color: rgba(50,50,50,0.8); -fx-text-fill: white;");

        // 安装工具提示
        Tooltip.install(node, tooltip);

        // 添加鼠标进入事件
        node.setOnMouseEntered(event -> {
            // 高亮显示该数据点
            node.setStyle(
                "-fx-background-color: " + seriesColor + ", white; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 8px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 0); " +
                "-fx-scale-x: 1.5; " +
                "-fx-scale-y: 1.5;"
            );

            // 高亮当前系列，淡化其他系列
            highlightSeries(seriesName);

            // 阻止事件继续传播
            event.consume();
        });

        // 添加鼠标离开事件
        node.setOnMouseExited(event -> {
            // 恢复默认样式
            node.setStyle("");

            // 恢复所有系列的正常显示
            resetChartStyles();

            // 阻止事件继续传播
            event.consume();
        });

        // 添加点击事件
        node.setOnMouseClicked(event -> {
            // 获取完整日期（假设我们可以从标签解析出日期）
            // 这里简化处理，实际可能需要从数据源获取完整信息
            showDataPointDetails(seriesName, dateStr, value.doubleValue());
            event.consume();
        });
    }

    /**
     * 高亮特定系列，淡化其他系列
     */
    private void highlightSeries(String seriesName) {
        for (XYChart.Series<String, Number> s : financeLineChart.getData()) {
            if (s.getName().equals(seriesName)) {
                // 高亮当前系列
                if (s.getNode() != null) {
                    s.getNode().setStyle("-fx-opacity: 1.0;");
                }

                // 高亮该系列的所有数据点
                for (XYChart.Data<String, Number> d : s.getData()) {
                    if (d.getNode() != null && !d.getNode().isHover()) {
                        d.getNode().setStyle("-fx-opacity: 1.0;");
                    }
                }
            } else {
                // 淡化其他系列
                if (s.getNode() != null) {
                    s.getNode().setStyle("-fx-opacity: 0.3;");
                }

                // 淡化其他系列的所有数据点
                for (XYChart.Data<String, Number> d : s.getData()) {
                    if (d.getNode() != null) {
                        d.getNode().setStyle("-fx-opacity: 0.3;");
                    }
                }
            }
        }
    }

    /**
     * 显示数据点详细信息
     */
    private void showDataPointDetails(String seriesName, String dateStr, double value) {
        String formattedValue = String.format("%.2f", value);
        String title = seriesName + "详情";
        String message = String.format("日期：%s\n%s：%s 元", dateStr, seriesName, formattedValue);

        // 如果是盈余，可以添加额外信息
        if (seriesName.equals("盈余")) {
            if (value > 0) {
                message += "\n\n该日有盈余，财务状况良好！";
            } else if (value < 0) {
                message += "\n\n该日支出超过收入，建议控制支出。";
            } else {
                message += "\n\n该日收支平衡。";
            }
        }

        showAlert(title, message, Alert.AlertType.INFORMATION);
    }

    /**
     * 根据系列名称返回对应的颜色代码
     */
    private String getColorForSeries(String seriesName) {
        switch(seriesName) {
            case "支出": return "#FF6B6B";
            case "收入": return "#4CAF50";
            case "盈余": return "#4A90E2";
            default: return "#555555";
        }
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
                System.err.println("GraphController: 无效的年份或月份组合: " + selectedYear + "-" + selectedMonth + " - " + e.getMessage());
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
                System.err.println("GraphController: 无效的日期组合: " + year + "-" + month + "-" + day + " - " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        // 如果message是null，给一个默认值防止NullPointerException
        alert.setContentText(message != null ? message : "[未获取到AI分析结果]");

        // 绑定到主窗口并尝试居中显示，如果你希望弹窗更显眼
        if (financeLineChart != null && financeLineChart.getScene() != null && financeLineChart.getScene().getWindow() != null) {
            alert.initOwner(financeLineChart.getScene().getWindow());
            // 尝试将弹窗居中
            alert.setX(financeLineChart.getScene().getWindow().getX() + financeLineChart.getScene().getWindow().getWidth() / 2 - alert.getDialogPane().getWidth() / 2);
            alert.setY(financeLineChart.getScene().getWindow().getY() + financeLineChart.getScene().getWindow().getHeight() / 2 - alert.getDialogPane().getHeight() / 2);
        }

        System.out.println("GraphController: 准备显示弹窗，标题: \"" + title + "\", 内容截取: \"" + (message != null ? message.substring(0, Math.min(message.length(), 100)) : "[内容为null或空]") + "...\"");
        alert.showAndWait(); // 显示弹窗并等待用户关闭
        System.out.println("GraphController: 弹窗已关闭。");
    }
}