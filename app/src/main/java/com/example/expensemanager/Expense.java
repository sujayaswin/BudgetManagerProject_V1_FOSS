package com.example.expensemanager;

public class Expense {
    public long id;
    public long timestamp; // millis
    public String date; // yyyy-MM-dd
    public String type; // INCOME or EXPENSE
    public String category;
    public double amount;
    public String note;

    public Expense() {}

    public Expense(long timestamp, String date, String type, String category, double amount, String note) {
        this.timestamp = timestamp;
        this.date = date;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.note = note;
    }
}
