package com.hindi.wazinhasad;

import androidx.room.Entity; // جدول في قاعدة البيانات
import androidx.room.PrimaryKey;  // المفتاح الرئيسي

@Entity(tableName = "categories") // جدول categories
public class Category {

    @PrimaryKey(autoGenerate = true)  // المفتاح الفريد id
    private int id;

    private String name;  // اسم الفئة
    private String iconPath; //مسار الصورة كنص

    // Constructor
    public Category(String name, String iconPath) {
        this.name = name;
        this.iconPath = iconPath;
    }

    // Getters & Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIconPath() { return iconPath; }
    public void setIconPath(String iconPath) { this.iconPath = iconPath; }
}