package com.hindi.wazinhasad;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CategoryDao {  // قاعدة البيانات الخاصة ب Category

    // إضافة فئة جديدة
    // اضافة سجل في الجدول
    // عند اضافة فئة موحودة مسبقا يتم تجديد الفئة بدلا من تعطيل النتطبيق
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Category category);

    @Update
    void update(Category category);  // التعديل

    @Delete
    void delete(Category category);  // الحذف

    // جلب كافة الفئات

    @Query("SELECT * FROM categories ORDER BY name ASC")
    List<Category> getAllCategories();

    // جلب الفئات بواسطة الاسم
    @Query("SELECT * FROM categories WHERE name = :categoryName LIMIT 1")
    Category getCategoryByName(String categoryName);

    // جلب أيقونة الفئة
    @Query("SELECT iconPath FROM categories WHERE name = :categoryName")
    String getIconPathByName(String categoryName);

    // حذف بيانات الجدول
    @Query("DELETE FROM categories")
    void deleteAll();
}
