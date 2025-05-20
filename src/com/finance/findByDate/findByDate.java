package com.finance.findByDate;
import com.finance.model.Transaction;
import com.finance.model.User;
import com.finance.manager.TransactionManager;
import java.util.List;
import java.util.ArrayList;

public class findByDate {
    User user;
    String date;
    TransactionManager transactionManager;
    List<Transaction> result = new ArrayList<>();
    public findByDate(User user,String date){
        transactionManager = new TransactionManager(user);
        List<Transaction> transactions = transactionManager.getAllTransactions();
        this.date = date;
        for(Transaction transaction : transactions){
            if(transaction.getDate().substring(0,10).equals(date)){
                result.add(transaction);
            }
        }
    }
    public List<Transaction> getResult(){
        return result;
    }


}
