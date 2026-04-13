package com.hindi.wazinhasad;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface TransactionPro {

    // إضافة معاملة جديدة إلى قاعدة البيانات
    @Insert
    void insert(Transaction transaction);

    // تحديث بيانات معاملة موجودة مسبقاً
    @Update
    void update(Transaction transaction);

    // حذف معاملة معينة من قاعدة البيانات
    @Delete
    void delete(Transaction transaction);

    // جلب بيانات معاملة واحدة باستخدام الرقم المعرف (ID)
    @Query("SELECT * FROM transactions WHERE id = :id")
    Transaction getTransactionById(int id);

    // جلب قائمة بجميع المعاملات الخاصة بمستخدم محدد
    @Query("SELECT * FROM transactions WHERE userNationalId = :userId")
    List<Transaction> getAllTransactionsByUser(String userId);

    // جلب قائمة معاملات مفلترة بناءً على نص البحث، النوع، الفئة، والتاريخ
    @Query("SELECT * FROM transactions WHERE userNationalId = :userId AND (note LIKE '%' || :search || '%') AND (:type IS NULL OR type = :type) AND (:catId == 0 OR categoryId = :catId) AND date BETWEEN :startDate AND :endDate ORDER BY date DESC, id DESC")
    List<Transaction> getFilteredTransactions(String userId, String search, String type, int catId, String startDate, String endDate);

    // حساب مجموع مبالغ المعاملات لنوع معين (دخل/صرف) خلال فترة زمنية محددة
    @Query("SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE userNationalId = :userId AND type = :type AND date BETWEEN :startDate AND :endDate")
    float getSumByType(String userId, String type, String startDate, String endDate);

    // حساب إجمالي المبالغ المسجلة لنوع معين (دخل/صرف) بشكل كلي للمستخدم
    @Query("SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE userNationalId = :userId AND type = :type")
    double getTotalByType(String userId, String type);

    // حساب الرصيد الصافي المتبقي للمستخدم (إجمالي الدخل مطروحاً منه إجمالي المصاريف)
    @Query("SELECT (SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE userNationalId = :userId AND type = 'income') - (SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE userNationalId = :userId AND type = 'expense')")
    double getBalance(String userId);
}