package com.finance.controller;

import com.finance.service.LlmService;
import com.finance.service.UserfulDataPicker;

import javafx.application.Platform;
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

public class
GraphController implements Initializable {
    // FXML-injected UI elements
    @FXML private ComboBox<Integer> Year0;
    @FXML private ComboBox<Integer> Month0;
    @FXML private ComboBox<Integer> Day0;
    @FXML private ComboBox<Integer> Year1;
    @FXML private ComboBox<Integer> Month1;
    @FXML private ComboBox<Integer> Day1;

    //An FXML-injected line chart and its axes
    @FXML private LineChart<String, Number> financeLineChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    // Define the data series that is used to store the chart data
    private XYChart.Series<String, Number> expenditureSeries;
    private XYChart.Series<String, Number> incomeSeries;
    private XYChart.Series<String, Number> surplusSeries;

    // A date formatter that converts LocalDate to a "yyyy-MM-dd" string to match the Map key of the UserfulDataPicker
    private static final DateTimeFormatter DATE_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize Date drop-down box
        fillYear0();
        fillYear1();
        fillMonth0();
        fillMonth1();

        // Add a listener to the Year and Month drop-down boxes to update the date drop-down box when the value changes
        Year0.valueProperty().addListener((obs, oldVal, newVal) -> updateDayComboBox(0));
        Month0.valueProperty().addListener((obs, oldVal, newVal) -> updateDayComboBox(0));
        Year1.valueProperty().addListener((obs, oldVal, newVal) -> updateDayComboBox(1));
        Month1.valueProperty().addListener((obs, oldVal, newVal) -> updateDayComboBox(1));

        // 添加选择事件监听器，确保选择后文本正确显示
        configureComboBox(Year0);
        configureComboBox(Month0);
        configureComboBox(Day0);
        configureComboBox(Year1);
        configureComboBox(Month1);
        configureComboBox(Day1);

            // Set the default setting to Current Date and update the Date for First Time drop-down box
        LocalDate today = LocalDate.now();
        Year0.getSelectionModel().select(Integer.valueOf(today.getYear()));
        Month0.getSelectionModel().select(Integer.valueOf(today.getMonthValue()));
        updateDayComboBox(0);
        Day0.getSelectionModel().select(Integer.valueOf(today.getDayOfMonth()));

        Year1.getSelectionModel().select(Integer.valueOf(today.getYear()));
        Month1.getSelectionModel().select(Integer.valueOf(today.getMonthValue()));
        updateDayComboBox(1);
        Day1.getSelectionModel().select(Integer.valueOf(today.getDayOfMonth()));

        //Initialize the chart data series and add it to the chart
        expenditureSeries = new XYChart.Series<>();
        expenditureSeries.setName("Expenditure");

        incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");

        surplusSeries = new XYChart.Series<>();
        surplusSeries.setName("Surplus");

        // The order in which the series are added is important, and it determines their corresponding index in CSS
        financeLineChart.getData().addAll(expenditureSeries, incomeSeries, surplusSeries);

        // Configure the initial label for the axis
        xAxis.setLabel("date");
        yAxis.setLabel("amount");

        // Add an overall chart hover interaction effect
        financeLineChart.setOnMouseMoved(event -> handleChartMouseMoved(event));
        financeLineChart.setOnMouseExited(event -> resetChartStyles());

        // Data is loaded by default when the app starts
        updateChartWithSelectedDates();
    }

    /**
     * Handles events where the mouse moves on the chart
     */
    private void handleChartMouseMoved(MouseEvent event) {
        boolean seriesHighlighted = false;

        // Start by setting all series to fade out
        for (XYChart.Series<String, Number> s : financeLineChart.getData()) {
            if (s.getNode() != null) {
                s.getNode().setStyle("-fx-opacity: 0.5;");
            }

            // Data points also fade out
            for (XYChart.Data<String, Number> d : s.getData()) {
                if (d.getNode() != null) {
                    d.getNode().setStyle("-fx-opacity: 0.5;");
                }
            }
        }

        // Then check if the mouse is near a series of data points
        for (XYChart.Series<String, Number> s : financeLineChart.getData()) {
            for (XYChart.Data<String, Number> d : s.getData()) {
                if (d.getNode() != null && d.getNode().getBoundsInParent().contains(
                        d.getNode().sceneToLocal(event.getSceneX(), event.getSceneY()))) {
                    // Highlight the current series
                    if (s.getNode() != null) {
                        s.getNode().setStyle("-fx-opacity: 1.0;");
                    }

                    // highlightAllDataPointsForTheSeries
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

            // If no specific series is found, the normal display of all series is restored
        if (!seriesHighlighted) {
            resetChartStyles();
        }
    }

    /**
     * Reset all chart styles to their default state
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
        System.out.println("GraphController: AI Analyze income and expenses and give savings advice, etc. - the button is clicked");

        // 1. Gets the selected date range，used UserfulDataPicker
        LocalDate startDate = getSelectedDate(Year0, Month0, Day0);
        LocalDate endDate = getSelectedDate(Year1, Month1, Day1);

        if (startDate == null || endDate == null) {
            showAlert("Date selected incorrectly", "Please make sure you have selected a full start and end date.", Alert.AlertType.ERROR);
            System.err.println("GraphController: Incomplete date selection for AI analysis.");
            return;
        }
        if (endDate.isBefore(startDate)) {
            showAlert("Date range error", "End date cannot be earlier than start date!", Alert.AlertType.ERROR);
            System.err.println("GraphController: End date is earlier than start date, AI analysis is not possible.");
            return;
        }

        // 2. Use UserfulDataPicker to get a detailed list of the original transactions
        System.out.println("GraphController: Fetching transaction details via UserfulDataPicker...");
        UserfulDataPicker dataPicker = new UserfulDataPicker(
                startDate.format(DATE_KEY_FORMATTER),
                endDate.format(DATE_KEY_FORMATTER)
        );
        List<String> transactionDetailsList = dataPicker.getTransactionDetails();
        System.out.println("GraphController: Number of raw transaction details obtained: " + transactionDetailsList.size());

        // Add a print log to view the original transaction details returned by UserfulDataPicker
        System.out.println("GraphController: --- UserfulDataPicker The original transaction details obtained ---");
        if (transactionDetailsList.isEmpty()) {
            System.out.println("  (The transaction details list is empty)");
        } else {
            for (String detail : transactionDetailsList) {
                System.out.println("  - " + detail);
            }
        }
        System.out.println("GraphController: ------------------------------------------------");


        // 3.<String> Stitch the List into a large string as the LLM's prompt
        String details = mergeString(transactionDetailsList);
        String fullPrompt = details + "Please analyze the above income and expenditure situation and give detailed savings and spending optimization suggestions:";

        System.out.println("GraphController: --- prepareAFullPromptToSendToTheAI ---");
        System.out.println(fullPrompt); // **Print the full Prompt to confirm its content and formatting**
        System.out.println("GraphController: ------------------------------------");

        try {
            // _4CreateAnLlmServiceInstance
            LlmService llmService = new LlmService(fullPrompt);

            //Core Fix: Call callLlmApi() method to actually execute API request ******
            System.out.println("GraphController: calling LlmService.callLlmApi()，");
            llmService.callLlmApi(); // thisIsTheKeyToEnsureThatTheAPIIsCalled
            System.out.println("GraphController: LlmService.callLlmApi() theCallIsComplete。");
            // ******************************************************************

            // 5. getAnswersFromAI
            String aiResponse = llmService.getAnswer(); // youShouldBeAbleToGetTheDataNow
            System.out.println("GraphController: ---aiAnswersObtainedFromTheLlmService ---");
            System.out.println(aiResponse != null && !aiResponse.isEmpty() ? aiResponse : "[theAnswerIsEmptyOrInvalid]"); // Print the answers you get, or leave the prompt blank
            System.out.println("GraphController: -------------------------------------");

            // 6. aPopUpWindowDisplaysTheResults
            if (aiResponse != null && !aiResponse.isEmpty()) {
                showAlert("AI analyzeTheResults", aiResponse, Alert.AlertType.INFORMATION);
            } else {
                showAlert("AI analysisFailed", "Unable to obtain the results of the AI analysis. Please check if the API return or data is empty。", Alert.AlertType.WARNING);
            }

        } catch (IOException e) {
            System.err.println("GraphController Error: API call network or I/O exception: " + e.getMessage());
            e.printStackTrace(); // Print the full stack trace
            showAlert("error", "AI The analysis service network connection failed：" + e.getMessage(), Alert.AlertType.ERROR);
        } catch (InterruptedException e) {
            System.err.println("GraphController Error: The API call was interrupted: " + e.getMessage());
            e.printStackTrace(); // Print the full stack trace
            showAlert("error", "AI Analyze the outage, please try again later", Alert.AlertType.ERROR);
            Thread.currentThread().interrupt(); // resetTheInterruptStatus
        } catch (LlmService.LlmServiceException e) {
            System.err.println("GraphController Error: AI service internal exception: " + e.getMessage());
            e.printStackTrace(); // Print the full stack trace
            showAlert("mistake", "AI There was an error in the analysis service：" + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) { // Catch all other unknown anomalies
            System.err.println("GraphController Error: An unknown error has occurred: " + e.getMessage());
            e.printStackTrace(); // Print the full stack trace
            showAlert("error", "An unknown error has occurred：" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    //String splicing
    private String mergeString(List<String> strings) {
        StringBuilder sb = new StringBuilder();
        if (strings != null && !strings.isEmpty()) {
            for (String str : strings) {
                sb.append(str).append(";\n"); // Add semicolons and line breaks after each detail for clarity
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 2); // Remove the last "; \n"
            }
        } else {
            System.out.println("GraphController: mergeString An empty list is received, and an empty string is returned。"); // 添加日志
        }
        return sb.toString();
    }


    @FXML
    public void submitDateRange(ActionEvent actionEvent) {
        // Trigger the chart to update based on the selected date
        updateChartWithSelectedDates();
    }

    /**
     * Get the data and update the chart based on the date range selected by the user.
     */
    private void updateChartWithSelectedDates() {
        // Gets the selected start and end dates
        LocalDate startDate = getSelectedDate(Year0, Month0, Day0);
        LocalDate endDate = getSelectedDate(Year1, Month1, Day1);

        // Check that the date is complete and valid
        if (startDate == null || endDate == null) {
            showAlert("The date was selected incorrectly", "Make sure you have selected a full start and end date。", Alert.AlertType.ERROR);
            return;
        }

        // Validation date range: The end date can't be before the start date
        if (endDate.isBefore(startDate)) {
            showAlert("The date range is incorrect", "The end date cannot be earlier than the start date！", Alert.AlertType.ERROR);
            return;
        }

        // Use the UserfulDataPicker to get the data
        UserfulDataPicker dataPicker = new UserfulDataPicker(
                startDate.format(DATE_KEY_FORMATTER),
                endDate.format(DATE_KEY_FORMATTER)
        );

        Map<String, Double> incomeDataMap = dataPicker.getIncome();
        Map<String, Double> expenditureDataMap = dataPicker.getExpenditure();

        // Call the core method of updating the chart
        updateFinanceChart(incomeDataMap, expenditureDataMap, startDate, endDate);
    }

    /**
     * The line chart is updated based on the financial data obtained.
     *
     * @param incomeDataMap Daily revenue data with a date string in the format "yyyy-MM-dd".
     * @param expenditureDataMap Daily spend data, key as a date string in the format "yyyy-MM-dd".
     * @param startDate The start date of the data range
     * @param endDate The end date of the data range
     */
    private void updateFinanceChart(Map<String, Double> incomeDataMap,
                                    Map<String, Double> expenditureDataMap,
                                    LocalDate startDate, LocalDate endDate) {
        // Purge old data points
        expenditureSeries.getData().clear();
        incomeSeries.getData().clear();
        surplusSeries.getData().clear();

        double maxAllValue = 0;
        double minSurplusValue = 0;

        // Define the format of the X-axis date label (e.g. "05-23")
        DateTimeFormatter xAxisLabelFormatter = DateTimeFormatter.ofPattern("MM-dd");

        // Iterate through the date range and add data points
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

        // --- Dynamically configure Y-axis ---
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

        // Add interactivity to your data points
        addDataPointInteractions();
    }

    /**
     * Add interactivity to all data points in the chart
     */
    private void addDataPointInteractions() {
        // Add interactivity to each data point for each series
        for (XYChart.Series<String, Number> series : financeLineChart.getData()) {
            String seriesName = series.getName();
            String seriesColor = getColorForSeries(seriesName);

            for (XYChart.Data<String, Number> dataPoint : series.getData()) {
                // The data point may not have been rendered yet and you need to wait for the node to actually be available
                if (dataPoint.getNode() != null) {
                    setupDataPointNode(dataPoint, seriesName, seriesColor);
                } else {
                    // If the node is not ready, add a listener and wait for the node to become available
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
     * Set up interactions for individual data point nodes
     */
    private void setupDataPointNode(XYChart.Data<String, Number> dataPoint, String seriesName, String seriesColor) {
        Node node = dataPoint.getNode();
        if (node == null) return;

        // Get data values and date labels
        String dateStr = dataPoint.getXValue();
        Number value = dataPoint.getYValue();

        // Create a tooltip
        Tooltip tooltip = new Tooltip(
            String.format("%s\nDate: %s\nAmount: %.2f yuan",
                seriesName, dateStr, value.doubleValue())
        );
        tooltip.setStyle("-fx-font-size: 14px; -fx-background-color: rgba(50,50,50,0.8); -fx-text-fill: white;");

        // Install tooltips
        Tooltip.install(node, tooltip);

        // Add a mouse entry event
        node.setOnMouseEntered(event -> {
            // Highlight the data point
            node.setStyle(
                "-fx-background-color: " + seriesColor + ", white; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 8px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 0); " +
                "-fx-scale-x: 1.5; " +
                "-fx-scale-y: 1.5;"
            );

            // Highlight the current series and fade the others
            highlightSeries(seriesName);

            // Stop the event from continuing to spread
            event.consume();
        });

        // Add a mouse away event
        node.setOnMouseExited(event -> {
            // Restore the default style
            node.setStyle("");

            // Restore normal display for all series
            resetChartStyles();

            // Stop the event from continuing to spread
            event.consume();
        });

        // Add a click event
        node.setOnMouseClicked(event -> {
            // Get the full date (assuming we can parse the date from the tag)
            // This is where the process is simplified, and it may actually be necessary to get the complete information from the data source
            showDataPointDetails(seriesName, dateStr, value.doubleValue());
            event.consume();
        });
    }

   /**
     * Highlight specific series and fade out others
     */
    private void highlightSeries(String seriesName) {
        for (XYChart.Series<String, Number> s : financeLineChart.getData()) {
            if (s.getName().equals(seriesName)) {
                // Highlight the current series
                if (s.getNode() != null) {
                    s.getNode().setStyle("-fx-opacity: 1.0;");
                }

                // Highlight all data points for the series
                for (XYChart.Data<String, Number> d : s.getData()) {
                    if (d.getNode() != null && !d.getNode().isHover()) {
                        d.getNode().setStyle("-fx-opacity: 1.0;");
                    }
                }
            } else {
                // Downplay other series
                if (s.getNode() != null) {
                    s.getNode().setStyle("-fx-opacity: 0.3;");
                }

                // Fade all data points for other series
                for (XYChart.Data<String, Number> d : s.getData()) {
                    if (d.getNode() != null) {
                        d.getNode().setStyle("-fx-opacity: 0.3;");
                    }
                }
            }
        }
    }

    /**
     * Displays data point details
     */
    private void showDataPointDetails(String seriesName, String dateStr, double value) {
        String formattedValue = String.format("%.2f", value);
        String title = seriesName + "details";
        String message = String.format("date：%s\n%s：%s ", dateStr, seriesName, formattedValue);

        // If it's a surplus, you can add additional information
        if (seriesName.equals("surplus")) {
            if (value > 0) {
                message += "\n\nThere was a surplus on the day and the financial position was good！";
            } else if (value < 0) {
                message += "\n\nSpending more than income on that day, it is recommended to control expenditure。";
            } else {
                message += "\n\nBreak even on that day。";
            }
        }

        showAlert(title, message, Alert.AlertType.INFORMATION);
    }

    /**
     * Returns the corresponding color code based on the series name
     */
    private String getColorForSeries(String seriesName) {
        switch(seriesName) {
            case "Expenditure": return "#FF6B6B";
            case "Income": return "#4CAF50";
            case "surplus": return "#4A90E2";
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
                System.err.println("GraphController: Invalid year or month combination: " + selectedYear + "-" + selectedMonth + " - " + e.getMessage());
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
                System.err.println("GraphController: Invalid date combinations: " + year + "-" + month + "-" + day + " - " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        // If the message is null, give a default value to prevent NullPointerException
        alert.setContentText(message != null ? message : "[The AI analysis results were not obtained]");

        // Bind to the main window and try to center it if you want the pop-up to be more prominent
        if (financeLineChart != null && financeLineChart.getScene() != null && financeLineChart.getScene().getWindow() != null) {
            alert.initOwner(financeLineChart.getScene().getWindow());
            // Try centering the pop-up
            alert.setX(financeLineChart.getScene().getWindow().getX() + financeLineChart.getScene().getWindow().getWidth() / 2 - alert.getDialogPane().getWidth() / 2);
            alert.setY(financeLineChart.getScene().getWindow().getY() + financeLineChart.getScene().getWindow().getHeight() / 2 - alert.getDialogPane().getHeight() / 2);
        }

        System.out.println("GraphController: Ready to display a pop-up window with the following title: \"" + title + "\", Content Capture: \"" + (message != null ? message.substring(0, Math.min(message.length(), 100)) : "[Content is null or empty]") + "...\"");
        alert.showAndWait(); // Display a pop-up window and wait for the user to close it
        System.out.println("GraphController: The pop-up window has closed。");
    }

    private void configureComboBox(ComboBox<Integer> comboBox) {
        // 使用StringConverter来确保正确转换和显示Integer值
        comboBox.setConverter(new javafx.util.StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return object == null ? "" : object.toString();
            }

            @Override
            public Integer fromString(String string) {
                try {
                    return string.isEmpty() ? null : Integer.parseInt(string);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });

        // 设置按钮单元格
        comboBox.setButtonCell(new javafx.scene.control.ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    // 确保完整显示数字（例如：2024）
                    setText(item.toString());
                }
            }
        });

        // 设置单元格工厂
        comboBox.setCellFactory(listView -> new javafx.scene.control.ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.toString());
                }
            }
        });

        // 添加选择监听器，确保在选择后更新显示
        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Platform.runLater(() -> {
                    // 强制更新ComboBox显示
                    comboBox.setButtonCell(new javafx.scene.control.ListCell<Integer>() {
                        @Override
                        protected void updateItem(Integer item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item == null) {
                                setText("");
                            } else {
                                setText(item.toString());
                            }
                        }
                    });
                });
            }
        });
    }
}
