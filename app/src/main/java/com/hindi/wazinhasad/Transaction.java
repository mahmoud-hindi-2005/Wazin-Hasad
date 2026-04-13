package com.hindi.wazinhasad;

import androidx.room.Entity; // جدول في قاعدة البيانات
import androidx.room.PrimaryKey; // المفتاح الرئيسي
import java.io.Serializable;

@Entity(tableName = "transactions") // جدول  Transactions
public class Transaction implements Serializable {
    @PrimaryKey(autoGenerate = true) // المفتاح الفريد id
    private int id;
    private double amount; // المبلغ
    private String type; // نوعه دخل ام مصروف
    private int categoryId; // معرف الفئة
    private String date; // التاريخ
    private String note; // الملاحظة
    private String category; // اسم الفئة
    private String currency; // رمز العملة
    private String userNationalId; // رقم هوية المستخدم صاحب العملية (للتفريق بين الحسابات)

    // Constructor

    public Transaction(double amount, String type, int categoryId, String date, String note, String category, String currency, String userNationalId) {
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
        this.date = date;
        this.note = note;
        this.category = category;
        this.currency = currency;
        this.userNationalId = userNationalId;
    }

    // Getters and Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public int getCategoryId() { return categoryId; }
    public String getDate() { return date; }
    public String getNote() { return note; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getUserNationalId() { return userNationalId; }
    public void setUserNationalId(String userNationalId) { this.userNationalId = userNationalId; }
}