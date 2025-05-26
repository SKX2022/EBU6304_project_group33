package com.finance.findByDate;

import com.finance.model.Transaction;
import com.finance.model.User;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.IntStream;

public class findByDateUI extends Application {
    private User user;
    private Stage stage; // Stage object of the current window

    public findByDateUI(User user) {
        // CONSTRUCTOR
        this.user = user;
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage; //Save the Stage of the current window

        // Create a scrolling display box
        TextArea displayArea = new TextArea();
        // Make the TextArea scrollable and non-editable
        displayArea.setEditable(false);

        // Create a year drop-down box
        ComboBox<Integer> yearDropdown = new ComboBox<>();
        // Fill year options, from 1900 to 2100
        IntStream.range(1900, 2101).forEach(yearDropdown.getItems()::add);

        // Create a Month drop-down box
        ComboBox<Integer> monthDropdown = new ComboBox<>();
        //Fill month options, from 1 to 12
        IntStream.range(1, 13).forEach(monthDropdown.getItems()::add);

        // Create Date drop-down box
        ComboBox<Integer> dayDropdown = new ComboBox<>();
        // Fill date options, from 1 to 31
        IntStream.range(1, 32).forEach(dayDropdown.getItems()::add);


        // When the user selects a year or month, the date option is updated
        monthDropdown.setOnAction(e -> updateDays(yearDropdown, monthDropdown, dayDropdown));
        yearDropdown.setOnAction(e -> updateDays(yearDropdown, monthDropdown, dayDropdown));

        // Create a confirmation button
        Button confirmButton = new Button("确认");
        confirmButton.setOnAction(e -> {
            // Get the year, month, and day selected by the user
            Integer year = yearDropdown.getValue();
            Integer month = monthDropdown.getValue();
            Integer day = dayDropdown.getValue();

            // Check that all fields are selected
            if (year != null && month != null && day != null) {
                // Format the date as "YYYY-MM-DD" and pass it to the findByDate class
                String selectedDate = String.format("%04d-%02d-%02d", year, month, day);
                findByDate finder = new findByDate(user, selectedDate); // Pass in the user object and date
                List<Transaction> transactions = finder.getResult();

                // Clear the display area
                displayArea.clear();

                // Print the contents of each transaction
                if (transactions.isEmpty()) {
                    displayArea.appendText("No transactions were found.\n");
                } else {
                    for (Transaction transaction : transactions) {
                        String transactionString = transactionToString(transaction);
                        displayArea.appendText(transactionString + "\n");
                    }
                }
            } else {
                displayArea.appendText("Please select all fields (YYYY, MM, DD)。\n");
            }
        });

        //Create an exit button
        Button exitButton = new Button("PULLED OUT");
        exitButton.setOnAction(e -> {
            // Call the exit method
            exitToMain();
        });

        // Layout: The horizontal layout is used to place the drop-down boxes, and the vertical layout places all the components
        HBox dropdowns = new HBox(10, yearDropdown, monthDropdown, dayDropdown);
        HBox buttons = new HBox(10, confirmButton, exitButton); // The confirm and exit buttons are placed in a horizontal layout
        VBox layout = new VBox(10, dropdowns, buttons, displayArea);

        // Set up the scene
        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Date picker");
        primaryStage.show();
    }

   /**
     * A reserved method for converting a Transaction object to a string
     * @param transaction object
     * @return Converted string
     */
    private String transactionToString(Transaction transaction) {
        // TODO:Implement the logic of converting a Transaction object to a string
        StringBuilder sb = new StringBuilder();
        sb.append(transaction.getType());
        sb.append(" | ");
        sb.append(transaction.getCategory());
        sb.append(" | ");
        sb.append(String.valueOf(transaction.getAmount()));
        sb.append(" | ");
        sb.append(transaction.getDate());
        sb.append(" | ");
        sb.append(transaction.getUser().getUsername()); // 假设Transaction类有getUser()方法
        String s = "Transaction History:" + sb.toString();
        return s;
    }

    /**
     * Update the date drop-down box based on the year and month selected
     * @param yearDropdown year drop-down box
     * @param monthDropdown month dropdown box
     * @param dayDropdown date drop-down box
     */
    private void updateDays(ComboBox<Integer> yearDropdown, ComboBox<Integer> monthDropdown, ComboBox<Integer> dayDropdown) {
        Integer year = yearDropdown.getValue();
        Integer month = monthDropdown.getValue();

        if (year != null && month != null) {
            int daysInMonth = getDaysInMonth(year, month);
            dayDropdown.getItems().clear();
            IntStream.range(1, daysInMonth + 1).forEach(dayDropdown.getItems()::add);
        }
    }

    /**
     * Returns the number of days in the specified year and month
     * @param year
     * @param month
     * @return The number of days in the current month
     */
    private int getDaysInMonth(int year, int month) {
        switch (month) {
            case 2:
                return (isLeapYear(year)) ? 29 : 28;
            case 4: case 6: case 9: case 11:
                return 30;
            default:
                return 31;
        }
    }

    /**
     * Determine whether it is a leap year
     * @param year
     * @return Returns true if it is a leap year, false otherwise
     */
    private boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    /**
     * Return to the main interface and close the current window
     */
    private void exitToMain() {
        // Close the current window
        stage.close();

        // Return to the main interface logic (implemented by the main interface control logic)
        System.out.println("Go back to the main interface"); // This is where the main interface display logic can be triggered
    }
}