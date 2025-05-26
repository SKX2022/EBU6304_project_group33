package com.finance.controller;

import com.finance.manager.CategoryManager;
import com.finance.manager.TransactionManager;
import com.finance.model.Category;
import com.finance.model.ExcelImporter;
import com.finance.model.Transaction;
import com.finance.model.User;
import com.finance.service.LlmService; // addTheImportOfLlmService
import com.finance.session.Session;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FlowController {
    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private ComboBox<Category> categoryComboBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Button deleteButton;

    @FXML
    private TableView<Transaction> transactionsTable;

    @FXML
    private TableColumn<Transaction, String> typeColumn;

    @FXML
    private TableColumn<Transaction, String> projectColumn;

    @FXML
    private TableColumn<Transaction, LocalDate> timeColumn;

    @FXML
    private TableColumn<Transaction, String> categoryColumn;

    @FXML
    private TableColumn<Transaction, Double> amountColumn;

    @FXML
    private Button aiSmartAddButton;

    private final ObservableList<Transaction> transactionsData = FXCollections.observableArrayList();
    private ComboBox<Category> dialogCategoryField;
    private DatePicker dialogDatePickerField;

    @FXML
    public void initialize() {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) return;

        CategoryManager categoryManager = new CategoryManager(currentUser);
        transactionsTable.setItems(transactionsData);
        categoryComboBox.setOnAction(event -> applyFilters(new ActionEvent()));
        filterComboBox.setOnAction(this::applyFilters);
        startDatePicker.setOnAction(this::applyFilters);
        endDatePicker.setOnAction(this::applyFilters);

        filterComboBox.getItems().addAll("All", "Income", "Expenditure");
        filterComboBox.getSelectionModel().select(0);
        refreshTransactions();
        List<Category> categories = categoryManager.getCategories();
        categories.add(0, new Category("All", "ALL"));
        ObservableList<Category> categoryList = FXCollections.observableArrayList(categories);

        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters(new ActionEvent()));

        transactionsData.addListener((Observable observable) -> {});

        categoryComboBox.setItems(categoryList);

        categoryComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Category category) {
                return category != null ? category.getName() : "";
            }

            @Override
            public Category fromString(String string) {
                return categoryList.stream()
                        .filter(c -> c.getName().equalsIgnoreCase(string.trim()))
                        .findFirst()
                        .orElse(null);
            }
        });

        categoryComboBox.getSelectionModel().selectFirst();

        startDatePicker.setConverter(new StringConverter<>() {
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });

        endDatePicker.setConverter(new StringConverter<>() {
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });

        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        projectColumn.setCellValueFactory(new PropertyValueFactory<>("project"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        refreshTransactions();
        transactionsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> deleteButton.setDisable(newSelection == null));

        aiSmartAddButton.setOnAction(event -> handleAiSmartAddTransaction());
    }

    @FXML
    private void refreshTransactions() {
        TransactionManager transactionManager = new TransactionManager(Session.getCurrentUser());
        List<Transaction> filteredTransactions = transactionManager.getAllTransactions();

        transactionsData.setAll(filteredTransactions);
        transactionsTable.refresh();
        applyFilters(new ActionEvent());
    }

    @FXML
    private void importTransactions(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Excel transactions");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel file", "*.xlsx")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                List<String> errorLogs = ExcelImporter.importTransactions(
                        Session.getCurrentUser(),
                        selectedFile.getAbsolutePath()
                );

                if (!errorLogs.isEmpty()) {
                    showErrorDialog("Import error", String.join("\n", errorLogs));
                } else {
                    showInfoDialog("The import was successful", "Successfully imported " + selectedFile.getName());
                    refreshTransactions();
                }
            } catch (Exception e) {
                showErrorDialog("System error", "An unexpected exception occurred during the import process:\n" + e);
            }
        }
    }

    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        try {
            alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
            alert.getDialogPane().getStyleClass().add("login-card");
        } catch (NullPointerException e) {
            System.err.println("Unable to load CSS styles: " + e.getMessage());
        }

        ButtonType okButton = new ButtonType("determine", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        Button okButtonNode = (Button) alert.getDialogPane().lookupButton(okButton);
        okButtonNode.getStyleClass().add("button-blue");

        alert.showAndWait();
    }

    @FXML
    private void applyFilters(ActionEvent event) {
        TransactionManager tm = new TransactionManager(Session.getCurrentUser());
        CategoryManager cm = new CategoryManager(Session.getCurrentUser());
        List<Transaction> rawData = tm.getAllTransactions();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        Category selectedCategoryObj = categoryComboBox.getSelectionModel().getSelectedItem();

        transactionsData.setAll(rawData.stream()
                .filter(t -> {
                    LocalDate tradeDate = parseDate(t.getDate());
                    if (tradeDate == null) return false;

                    boolean typeMatch = switch (filterComboBox.getValue()) {
                        case "All" -> true;
                        case "Income" -> "收入".equals(t.getType());
                        case "Expenditure" -> "支出".equals(t.getType());
                        default -> false;
                    };

                    boolean dateMatch = true;
                    if (startDate != null) {
                        dateMatch = !tradeDate.isBefore(startDate);
                    }
                    if (endDate != null) {
                        dateMatch = dateMatch && !tradeDate.isAfter(endDate);
                    }

                    boolean categoryMatch = true;
                    if (selectedCategoryObj != null && !"ALL".equals(selectedCategoryObj.getName())) {
                        categoryMatch = selectedCategoryObj.getName().equals(t.getCategory());
                    }
                    return typeMatch && dateMatch && categoryMatch;
                })
                .collect(Collectors.toList()));
        transactionsTable.refresh();
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException e) {
            System.err.println("Date parsing error: " + dateStr + " - " + e.getMessage());
            return null;
        }
    }

    private boolean showConfirmDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        try {
            alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
            alert.getDialogPane().getStyleClass().add("login-card");
        } catch (NullPointerException e) {
            System.err.println("Unable to load CSS styles: " + e.getMessage());
        }

        ButtonType yesButton = new ButtonType("determine", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, cancelButton);

        Button yesButtonNode = (Button) alert.getDialogPane().lookupButton(yesButton);
        Button cancelButtonNode = (Button) alert.getDialogPane().lookupButton(cancelButton);

        yesButtonNode.getStyleClass().add("button-blue");
        cancelButtonNode.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155;");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yesButton;
    }

    @FXML
    private void handleAddTransaction(ActionEvent event) {
        try {
            TextInputDialog dialog = createNewDialog();
            configureDialogContent(dialog);
            dialog.showAndWait();
        } catch (Exception e) {
            showErrorDialog("System error", "The pop-up window failed to initialize：" + e.getMessage());
        }
    }

    private TextInputDialog createNewDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add transaction records");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setContent(createDialogContent());

        try {
            dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
            dialog.getDialogPane().getStyleClass().add("login-card");
        } catch (NullPointerException e) {
            System.err.println("Unable to load CSS styles: " + e.getMessage());
        }
        return dialog;
    }

    private Parent createDialogContent() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.getStyleClass().add("classification-box");

        ComboBox<String> typeComboBoxLocal = new ComboBox<>();
        typeComboBoxLocal.getItems().addAll("Income", "Expenditure");
        typeComboBoxLocal.setPromptText("selectAType");
        typeComboBoxLocal.setId("comboBox_type");
        typeComboBoxLocal.getStyleClass().add("input-field");
        typeComboBoxLocal.setPrefWidth(250);

        dialogCategoryField = new ComboBox<>();
        CategoryManager categoryManager = new CategoryManager(Session.getCurrentUser());
        List<Category> categories = categoryManager.getCategories();
        ObservableList<Category> categoryList = FXCollections.observableArrayList(
                categories.stream()
                        .filter(c -> !"ALL".equals(c.getType()))
                        .collect(Collectors.toList())
        );
        dialogCategoryField.setItems(categoryList);
        dialogCategoryField.setPromptText("Select a category");
        dialogCategoryField.getStyleClass().add("input-field");
        dialogCategoryField.setPrefWidth(250);
        dialogCategoryField.setConverter(new StringConverter<>() {
            @Override
            public String toString(Category category) {
                return category != null ? category.getName() : "";
            }

            @Override
            public Category fromString(String string) {
                return categoryList.stream()
                        .filter(c -> c.getName().equalsIgnoreCase(string.trim()))
                        .findFirst()
                        .orElse(null);
            }
        });

        TextField amountField = new TextField();
        amountField.setPromptText("Please enter an amount");
        amountField.setId("textField_amount");
        amountField.getStyleClass().add("input-field");
        amountField.setPrefWidth(250);

        dialogDatePickerField = new DatePicker(LocalDate.now());
        dialogDatePickerField.setPromptText("Select a date");
        dialogDatePickerField.setId("datePicker_date");
        dialogDatePickerField.getStyleClass().add("input-field");
        dialogDatePickerField.setPrefWidth(250);
        dialogDatePickerField.setConverter(new StringConverter<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(LocalDate date) {
                return date != null ? formatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty()
                        ? LocalDate.parse(string, formatter)
                        : null;
            }
        });

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Please enter a note description");
        descriptionField.setId("textField_description");
        descriptionField.getStyleClass().add("input-field");
        descriptionField.setPrefWidth(250);

        Label typeLabel = new Label("The type of transaction:");
        Label categoryLabel = new Label("classification:");
        Label amountLabel = new Label("amount:");
        Label dateLabel = new Label("date:");
        Label descriptionLabel = new Label("Remarks:");

        typeLabel.getStyleClass().add("label-section");
        categoryLabel.getStyleClass().add("label-section");
        amountLabel.getStyleClass().add("label-section");
        dateLabel.getStyleClass().add("label-section");
        descriptionLabel.getStyleClass().add("label-section");

        typeLabel.setStyle("-fx-font-size: 14px;");
        categoryLabel.setStyle("-fx-font-size: 14px;");
        amountLabel.setStyle("-fx-font-size: 14px;");
        dateLabel.setStyle("-fx-font-size: 14px;");
        descriptionLabel.setStyle("-fx-font-size: 14px;");

        grid.add(typeLabel, 0, 0);
        grid.add(typeComboBoxLocal, 1, 0);

        grid.add(categoryLabel, 0, 1);
        grid.add(dialogCategoryField, 1, 1);

        grid.add(amountLabel, 0, 2);
        grid.add(amountField, 1, 2);

        grid.add(dateLabel, 0, 3);
        grid.add(dialogDatePickerField, 1, 3);

        grid.add(descriptionLabel, 0, 4);
        grid.add(descriptionField, 1, 4);

        return grid;
    }

    private void configureDialogContent(TextInputDialog dialog) {
        ButtonType confirmButtonType = createButtonType("determine", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = createButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(confirmButtonType, cancelButtonType);

        Button confirmButton = (Button) dialog.getDialogPane().lookupButton(confirmButtonType);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);

        confirmButton.getStyleClass().add("button-blue");
        cancelButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == null) return null;

            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                try {
                    return processValidInput(dialog).toString();
                } catch (Exception e) {
                    showErrorDialog("Typing error", e.getMessage());
                    return null;
                }
            }
            return null;
        });
    }

    private ButtonType createButtonType(String text, ButtonBar.ButtonData data) {
        return new ButtonType(text, data);
    }

    private Object processValidInput(TextInputDialog dialog) {
        GridPane grid = (GridPane) dialog.getDialogPane().getContent();
        ComboBox<String> typeComboBoxLocal = (ComboBox<String>) grid.lookup("#comboBox_type");
        TextField amountField = (TextField) grid.lookup("#textField_amount");
        TextField descriptionField = (TextField) grid.lookup("#textField_description");

        ComboBox<Category> categoryField = dialogCategoryField;
        DatePicker datePicker = dialogDatePickerField;

        String type = typeComboBoxLocal.getValue();
        if (type == null || !List.of("Income", "Expenditure").contains(type)) {
            throw new IllegalArgumentException("Please select a valid transaction type (Income/Expense)");
        }

        Category selectedCategory = categoryField.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            throw new IllegalArgumentException("Please select a valid category");
        }
        String category = selectedCategory.getName();

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountField.getText().trim())
                    .setScale(2, RoundingMode.HALF_UP);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("The amount must be greater than zero");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a valid amount figure");
        }

        LocalDate selectedDate = datePicker.getValue();
        LocalDateTime dateTime;
        if (selectedDate == null) {
            dateTime = LocalDateTime.now();
        } else {
            dateTime = LocalDateTime.of(selectedDate, LocalDateTime.now().toLocalTime());
        }
        String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String description = descriptionField.getText().trim();

        TransactionManager transactionManager = new TransactionManager(Session.getCurrentUser());
        boolean success = transactionManager.addTransaction(
                type,
                category,
                amount.doubleValue(),
                formattedDate,
                description);

        if (!success) {
            throw new IllegalStateException("Failed to add transactions");
        }

        Transaction newTransaction = new Transaction(
                type,
                category,
                amount.doubleValue(),
                formattedDate,
                Session.getCurrentUser(),
                description);

        updateTransactionList(newTransaction);

        Platform.runLater(() -> showInfoDialog("THE OPERATION WAS SUCCESSFUL", "The transaction was added successfully"));

        return newTransaction;
    }

    private void updateTransactionList(Transaction transaction) {
        Platform.runLater(() -> {
            transactionsData.add(transaction);
            transactionsTable.scrollTo(transaction);
            clearFormFields();
        });
    }

    private void clearFormFields() {
        // Clear the field logic
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        try {
            alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
            alert.getDialogPane().getStyleClass().add("login-card");
        } catch (NullPointerException e) {
            System.err.println("Unable to load CSS styles: " + e.getMessage());
        }

        ButtonType okButton = new ButtonType("determine", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        Button okButtonNode = (Button) alert.getDialogPane().lookupButton(okButton);
        okButtonNode.getStyleClass().add("button-blue");

        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.initOwner(null);
        alert.showAndWait();
    }

    @FXML
    private void handleDeleteTransaction(ActionEvent event) {
        Transaction selectedTransaction = transactionsTable.getSelectionModel().getSelectedItem();
        if (selectedTransaction == null) {
            showErrorDialog("tips", "Start by selecting the transactions that you want to delete");
            return;
        }

        boolean confirmed = showConfirmDialog("Confirm the deletion", "Are you sure you want to delete the selected transactions? This action cannot be undone.");
        if (confirmed) {
            TransactionManager transactionManager = new TransactionManager(Session.getCurrentUser());
            boolean success = transactionManager.deleteTransaction(selectedTransaction);

            if (success) {
                transactionsData.remove(selectedTransaction);
                transactionsTable.refresh();
                showInfoDialog("The operation was successful", "The transaction was successfully deleted");
            } else {
                showErrorDialog("The operation failed", "An error occurred while deleting the transaction");
            }
        }
    }

    private void handleAiSmartAddTransaction() {
        try {
            TextInputDialog dialog = createAiInputDialog();
            dialog.showAndWait();
        } catch (Exception e) {
            showErrorDialog("System error", "The initialization of AI intelligent input failed：" + e.getMessage());
        }
    }

    private TextInputDialog createAiInputDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("AI intelligently enters transactions");
        dialog.setHeaderText(null);
        dialog.setContentText("Please describe your transaction (e.g. $88 spent on a restaurant yesterday)：");

        try {
            dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
            dialog.getDialogPane().getStyleClass().add("login-card");
        } catch (NullPointerException e) {
            System.err.println("Unable to load CSS styles:" + e.getMessage());
        }

        ButtonType confirmButtonType = new ButtonType("Identify and add", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(confirmButtonType, cancelButtonType);

        Button confirmButton = (Button) dialog.getDialogPane().lookupButton(confirmButtonType);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);

        confirmButton.getStyleClass().add("button-blue");
        cancelButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == null) return null;
            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                String userInput = dialog.getEditor().getText().trim();
                if (userInput.isEmpty()) {
                    showErrorDialog("Typing error", "Please enter a description of the transaction");
                    return null;
                }
                processAiTransaction(userInput);
                return userInput;
            }
            return null;
        });

        return dialog;
    }

    private void processAiTransaction(String userDescription) {
        showInfoDialog("Processing", "AI is being used to analyze your trades, please wait...");

        CompletableFuture.runAsync(() -> {
            try {
                String prompt = "你是一个财务分析助手。请从用户输入中提取以下信息，格式为JSON：\n" +
                        "1. 交易类型(type)：'收入'或'支出'\n" +
                        "2. 分类(category)：例如'餐饮'、'工资'等\n" +
                        "3. 金额(amount)：数字\n" +
                        "4. 日期(date)：如果有明确日期，则提取；如果是'昨天'、‘明天’、‘前天’、‘大前天’、‘后天’，转换为具体日期（例如，如果今天是2024-05-24，‘昨天’是2024-05-23，‘前天’是2024-05-22，‘大前天’是2024-05-21，‘明天’是2024-05-25，‘后天’是2024-05-26）；如果没有提及日期，使用当天\n" +
                        "5. 备注(note)：交易的其他描述信息\n\n" +
                        "用户输入: \"" + userDescription + "\"\n" +
                        "只返回JSON格式，不要有其他解释。";

                LlmService llmService = new LlmService(prompt);
                llmService.callLlmApi();
                String result = llmService.getAnswer().trim();

                Platform.runLater(() -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode rootNode = mapper.readTree(result);

                        String type = rootNode.path("type").asText();
                        String category = rootNode.path("category").asText();
                        double amount = rootNode.path("amount").asDouble();
                        String dateStr = rootNode.path("date").asText();
                        String note = rootNode.path("note").asText();

                        LocalDateTime transactionDateTime;
                        try {
                            transactionDateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        } catch (Exception e) {
                            try {
                                LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                transactionDateTime = LocalDateTime.of(date, LocalDateTime.now().toLocalTime());
                            } catch (Exception e2) {
                                transactionDateTime = LocalDateTime.now();
                            }
                        }
                        String formattedDate = transactionDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                        CategoryManager categoryManager = new CategoryManager(Session.getCurrentUser());
                        boolean categoryExists = categoryManager.getCategories().stream()
                                                .anyMatch(c -> c.getType().equals(type) && c.getName().equals(category));

                        if (!categoryExists) {
                            boolean createCategory = showConfirmDialog(
                                "Create a new taxonomy",
                                "No classification found \"" + category + "\"，Whether to create this taxonomy？"
                            );

                            if (createCategory) {
                                categoryManager.addCategory(type, category);
                                showInfoDialog("THE CATEGORY IS CREATED SUCCESSFULLY", "A NEW TAXONOMY HAS BEEN CREATED：" + category);
                            } else {
                                showCategorySelectionDialog(type, category, amount, formattedDate, note);
                                return;
                            }
                        }

                        TransactionManager transactionManager = new TransactionManager(Session.getCurrentUser());
                        boolean success = transactionManager.addTransaction(type, category, amount, formattedDate, note);

                        if (success) {
                            Transaction newTransaction = new Transaction(
                                type, category, amount, formattedDate, Session.getCurrentUser(), note
                            );
                            updateTransactionList(newTransaction);
                            showInfoDialog("THE TRANSACTION WAS ADDED SUCCESSFULLY",
                                          "ADDED" + type + "record:\n" +
                                          "classification: " + category + "\n" +
                                          "amount: " + amount + "\n" +
                                          "date: " + formattedDate.substring(0, 10) + "\n" +
                                          "remarks: " + note);
                            refreshTransactions();
                        } else {
                            showErrorDialog("Transaction added failed", "Transactions can't be added, try again");
                        }

                    } catch (Exception e) {
                        showErrorDialog("Parsing error", "The results returned by the AI cannot be parsed: " + e.getMessage() + "\nOriginal return: " + result);
                    }
                });
            } catch (IOException | InterruptedException | LlmService.LlmServiceException e) {
                Platform.runLater(() -> showErrorDialog("AI service error", "Failed to call the AI service: " + e.getMessage()));
            }
        });
    }

    private void showCategorySelectionDialog(String type, String suggestedCategory, double amount, String date, String note) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Select a category");
        dialog.setHeaderText("Please for you" + type + "Trade by selecting a category");

        CategoryManager categoryManager = new CategoryManager(Session.getCurrentUser());
        List<Category> categories = categoryManager.getCategories().stream()
                                    .filter(c -> c.getType().equals(type))
                                    .toList();

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(
            categories.stream().map(Category::getName).toList()
        );

        if (!categoryCombo.getItems().isEmpty()) {
            categoryCombo.getSelectionModel().select(0);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Select a category:"), 0, 0);
        grid.add(categoryCombo, 1, 0);

        dialog.getDialogPane().setContent(grid);

        ButtonType confirmButtonType = new ButtonType("confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return categoryCombo.getValue();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selectedCategory -> {
            TransactionManager transactionManager = new TransactionManager(Session.getCurrentUser());
            boolean success = transactionManager.addTransaction(type, selectedCategory, amount, date, note);

            if (success) {
                Transaction newTransaction = new Transaction(
                    type, selectedCategory, amount, date, Session.getCurrentUser(), note
                );
                updateTransactionList(newTransaction);
                showInfoDialog("The transaction was added successfully",
                              "added" + type + "record：\n" +
                              "classification: " + selectedCategory + "\n" +
                              "amount: " + amount + "\n" +
                              "date: " + date.substring(0, 10) + "\n" +
                              "remarks: " + note);
                refreshTransactions();
            } else {
                showErrorDialog("Transaction added failed", "Transactions can't be added, try again");
            }
        });
    }
}
