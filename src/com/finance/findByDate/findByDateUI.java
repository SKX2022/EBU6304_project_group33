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
    private Stage stage; // 当前窗口的Stage对象

    public findByDateUI(User user) {
        // 构造函数
        this.user = user;
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage; // 保存当前窗口的Stage

        // 创建滚动显示框
        TextArea displayArea = new TextArea();
        // 设置TextArea为可滚动且不可编辑
        displayArea.setEditable(false);

        // 创建年份下拉框
        ComboBox<Integer> yearDropdown = new ComboBox<>();
        // 填充年份选项，从1900到2100
        IntStream.range(1900, 2101).forEach(yearDropdown.getItems()::add);

        // 创建月份下拉框
        ComboBox<Integer> monthDropdown = new ComboBox<>();
        // 填充月份选项，从1到12
        IntStream.range(1, 13).forEach(monthDropdown.getItems()::add);

        // 创建日期下拉框
        ComboBox<Integer> dayDropdown = new ComboBox<>();
        // 填充日期选项，从1到31
        IntStream.range(1, 32).forEach(dayDropdown.getItems()::add);

        // 当用户选择年份或月份时，更新日期选项
        monthDropdown.setOnAction(e -> updateDays(yearDropdown, monthDropdown, dayDropdown));
        yearDropdown.setOnAction(e -> updateDays(yearDropdown, monthDropdown, dayDropdown));

        // 创建确认按钮
        Button confirmButton = new Button("确认");
        confirmButton.setOnAction(e -> {
            // 获取用户选择的年份、月份和日期
            Integer year = yearDropdown.getValue();
            Integer month = monthDropdown.getValue();
            Integer day = dayDropdown.getValue();

            // 检查是否所有字段都已选择
            if (year != null && month != null && day != null) {
                // 将日期格式化为"YYYY-MM-DD"并传入findByDate类
                String selectedDate = String.format("%04d-%02d-%02d", year, month, day);
                findByDate finder = new findByDate(user, selectedDate); // 传入用户对象和日期
                List<Transaction> transactions = finder.getResult();

                // 清空显示区域
                displayArea.clear();

                // 打印每个Transaction的内容
                if (transactions.isEmpty()) {
                    displayArea.appendText("没有找到任何交易记录。\n");
                } else {
                    for (Transaction transaction : transactions) {
                        String transactionString = transactionToString(transaction);
                        displayArea.appendText(transactionString + "\n");
                    }
                }
            } else {
                displayArea.appendText("请选择所有字段（年、月、日）。\n");
            }
        });

        // 创建退出按钮
        Button exitButton = new Button("退出");
        exitButton.setOnAction(e -> {
            // 调用退出方法
            exitToMain();
        });

        // 布局：水平布局用于放置下拉框，垂直布局放置所有组件
        HBox dropdowns = new HBox(10, yearDropdown, monthDropdown, dayDropdown);
        HBox buttons = new HBox(10, confirmButton, exitButton); // 确认和退出按钮放在水平布局中
        VBox layout = new VBox(10, dropdowns, buttons, displayArea);

        // 设置场景
        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("日期选择器");
        primaryStage.show();
    }

    /**
     * 预留方法，用于将Transaction对象转换为字符串
     * @param transaction 交易记录对象
     * @return 转换后的字符串
     */
    private String transactionToString(Transaction transaction) {
        // TODO: 实现将Transaction对象转换为字符串的逻辑
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
        String s = "交易记录：" + sb.toString();
        return s;
    }

    /**
     * 根据选择的年份和月份更新日期下拉框
     * @param yearDropdown 年份下拉框
     * @param monthDropdown 月份下拉框
     * @param dayDropdown 日期下拉框
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
     * 返回指定年份和月份的天数
     * @param year 年份
     * @param month 月份
     * @return 当前月份的天数
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
     * 判断是否为闰年
     * @param year 年份
     * @return 如果是闰年则返回true，否则返回false
     */
    private boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    /**
     * 返回主界面并关闭当前窗口
     */
    private void exitToMain() {
        // 关闭当前窗口
        stage.close();
        // 返回主界面逻辑（由主界面控制逻辑实现）
        System.out.println("返回主界面"); // 这里可以触发主界面显示逻辑
    }
}