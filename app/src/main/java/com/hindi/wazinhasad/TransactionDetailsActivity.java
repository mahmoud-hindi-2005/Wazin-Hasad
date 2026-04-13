package com.hindi.wazinhasad;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.concurrent.Executors;

public class TransactionDetailsActivity extends AppCompatActivity {

    // تعريف المتغيرات
    private TextView tvAmount, tvType, tvCategory, tvDate, tvNote, tvTitle;
    private Button btnEdit, btnDelete;
    private ImageView btnBack;
    private Transaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_details);

        initViews();  // العمليات داخل الواجهة

        // استقبال البيانات
        transaction = (Transaction) getIntent().getSerializableExtra("TRANSACTION_DATA");

        if (transaction != null) {
            displayData();
        }

        // زر العودة
        btnBack.setOnClickListener(v -> finish());

        // زر التعديل
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(TransactionDetailsActivity.this, TransactionActivity.class);
            intent.putExtra("TRANSACTION_ID", transaction.getId());
            startActivity(intent);
            finish();
        });


        // زر الحذف
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Yes, Delete", (dialog, which) -> deleteTransaction())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    // ربط العناصر وتشغيل الواجهة والعمليات
    private void initViews() {

        tvAmount = findViewById(R.id.tv_amount);
        tvType = findViewById(R.id.tv_Type_det);
        tvCategory = findViewById(R.id.tv_Category_det);
        tvDate = findViewById(R.id.tv_Date_det);
        tvNote = findViewById(R.id.tv_Note_det);
        tvTitle = findViewById(R.id.tv_title);
        btnEdit = findViewById(R.id.btn_Edit);
        btnDelete = findViewById(R.id.btn_Delete);
        btnBack = findViewById(R.id.imageView10);
        ConstraintLayout mainLayout = findViewById(R.id.main_layout);

        // ما الوضع المفعل
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            int darkColor = Color.parseColor("#121212");

            getWindow().setStatusBarColor(Color.BLACK);  // اظهار شريط الحالة باللون الأسود

            // تغيير لون الخلفية
            if (mainLayout != null) mainLayout.setBackgroundColor(darkColor);

            // تغيير العنوان وسهم العودة
            if (tvTitle != null) tvTitle.setTextColor(Color.WHITE);
            if (btnBack != null) btnBack.setColorFilter(Color.WHITE);

            // النصوص داخل الأشكال
            tvType.setTextColor(Color.BLACK);
            tvCategory.setTextColor(Color.BLACK);
            tvDate.setTextColor(Color.BLACK);
            tvNote.setTextColor(Color.BLACK);

            // نص زر الحذف
            if (btnDelete != null) btnDelete.setTextColor(Color.WHITE);

            // تغيير الوان زر التعديل
            if (btnEdit != null) {
                // تلوين خلفية الزر بالأزرق الفاتح
                btnEdit.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#80D8FF")));
                // تلوين نص الزر بالأسود
                btnEdit.setTextColor(Color.BLACK);
            }
        }
    }

    // عرض البيانات
    private void displayData() {
        // عرض العملة المخزنة
        String currency = transaction.getCurrency() != null ? transaction.getCurrency() : "$";
        tvAmount.setText(currency + " " + transaction.getAmount());

        // نعبئة النصوص
        tvType.setText(transaction.getType());
        tvCategory.setText(transaction.getCategory());
        tvDate.setText(transaction.getDate());
        tvNote.setText(transaction.getNote());

        // لون المبلغ يتأثر بالوضع الداكن لضمان الوضوح
        if ("income".equalsIgnoreCase(transaction.getType())) {
            tvAmount.setTextColor(Color.parseColor("#2ECC71"));
        } else {
            tvAmount.setTextColor(Color.parseColor("#E74C3C"));
        }
    }

    // حذف المعاملة
    private void deleteTransaction() {
        Executors.newSingleThreadExecutor().execute(() -> {
            WazinDatabase.getDatabase(this).transactionDao().delete(transaction);
            runOnUiThread(() -> {
                Toast.makeText(this, "Successfully deleted", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}