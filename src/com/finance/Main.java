//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.finance;

import com.finance.model.ExcelImporter;
import com.finance.model.User;
import com.finance.ui.FinanceTrackerUI;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import javafx.application.Application;

public class Main {
    public Main() {
    }

    public static void main(String[] args) {
        User currentUser = new User("test", "0000");
        List<String> errors = ExcelImporter.importTransactions(currentUser, "EBU6304_project_group33/transactions.xlsx");
        if (!errors.isEmpty()) {
            System.out.println("导入完成，以下行存在错误：");
            PrintStream var10001 = System.out;
            Objects.requireNonNull(var10001);
            errors.forEach(var10001::println);
        } else {
            System.out.println("所有数据成功导入！");
        }

        Application.launch(FinanceTrackerUI.class, args);
    }
}
