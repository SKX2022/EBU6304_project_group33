package com.finance.model;


import com.finance.controller.CategoryManager;
import com.finance.controller.TransactionManager;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelImporter {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static List<String> importTransactions(User user, String filePath) {
        List<String> errorLogs = new ArrayList<>();
        TransactionManager transactionManager = new TransactionManager(user);
        CategoryManager categoryManager = new CategoryManager(user);

        try (FileInputStream file = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(file)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // 跳过标题行
            if (rows.hasNext()) rows.next();

            while (rows.hasNext()) {
                Row currentRow = rows.next();
                try {
                    // 解析单元格数据
                    String type = getStringCellValue(currentRow, 0);
                    String category = getStringCellValue(currentRow, 1);
                    double amount = getNumericCellValue(currentRow, 2);
                    String date = getDateCellValue(currentRow, 3);

                    // 数据验证
                    validateTransactionData(type, amount, date);

                    // 处理分类
                    handleCategory(categoryManager, category);

                    // 添加交易记录
                    transactionManager.addTransaction(type, category, amount, date);
                } catch (Exception e) {
                    errorLogs.add("Row " + (currentRow.getRowNum() + 1) +
                            " Error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            errorLogs.add("File Error: " + e.getMessage());
        }
        return errorLogs;
    }

    private static String getStringCellValue(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        return cell.getStringCellValue().trim();
    }

    private static double getNumericCellValue(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            throw new IllegalArgumentException("Amount cannot be empty");
        }
        return cell.getNumericCellValue();
    }

    private static String getDateCellValue(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        if (cell == null) {
            throw new IllegalArgumentException("Date cannot be empty");
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getLocalDateTimeCellValue().format(DATE_FORMATTER);
        } else {
            String dateString = cell.getStringCellValue().trim();
            // 验证日期格式
            LocalDateTime.parse(dateString, DATE_FORMATTER);
            return dateString;
        }
    }

    private static void validateTransactionData(String type, double amount, String date) {
        if (!"收入".equals(type) && !"支出".equals(type)) {
            throw new IllegalArgumentException("Invalid transaction type");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    private static void handleCategory(CategoryManager categoryManager, String category) {
        boolean exists = categoryManager.getCategories().stream()
                .anyMatch(c -> c.getName().equals(category));
        if (!exists) {
            categoryManager.addCategory(category);
        }
    }
}