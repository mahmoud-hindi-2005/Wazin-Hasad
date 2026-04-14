package com.hindi.wazinhasad;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    // تعريف المتغيرات
    private ImageView btnBack, btnGoToCategories; // تم إضافة btnGoToCategories
    private View layoutCurrency, layoutFirstDay;
    private TextView tvCurrentCurrency, tvFirstDay, tvTitle, tvUserName, tvLogoutConfirm; // تم إضافة tvUserName و tvLogoutConfirm
    private View layoutLogout; // تم إضافة حاوية تسجيل الخروج
    private SwitchMaterial switchTheme, switchFingerprint; // تم إضافة switchFingerprint
    private Button btnReset;
    private SharedPreferences preferences;
    private SharedPreferences userPreferences; // إضافة تفضيلات المستخدم لجلب الاسم
    private WazinDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        db = WazinDatabase.getDatabase(this); // فتح قاعدة البيانات
        preferences = getSharedPreferences("app_settings", MODE_PRIVATE); // ملف الاعدادات
        userPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE); // ملف بيانات المستخدم الحالي

        initViews(); // العمليات
        loadSavedSettings(); // الحصول على القيم المحفوظة
        setupListeners(); // عمليات الازرار
    }

    private void initViews() {

        // ربط العناصر بالكود
        btnBack = findViewById(R.id.imageView10);
        btnGoToCategories = findViewById(R.id.imageView3); // ربط الأيقونة المطلوبة
        layoutCurrency = findViewById(R.id.layout_currency);
        layoutFirstDay = findViewById(R.id.layout_first_day);
        tvCurrentCurrency = findViewById(R.id.tv_current_currency);
        tvFirstDay = findViewById(R.id.tv_first_day);
        switchTheme = findViewById(R.id.switch_theme);
        switchFingerprint = findViewById(R.id.switch_Fingerprint); // ربط مفتاح البصمة
        btnReset = findViewById(R.id.btn_reset);

        tvUserName = findViewById(R.id.tv_name); // ربط نص اسم المستخدم
        layoutLogout = findViewById(R.id.layout_Log_out); // ربط حاوية تسجيل الخروج
        tvLogoutConfirm = findViewById(R.id.tv_Log_out); // ربط نص تأكيد الخروج داخل الحاوية

        ConstraintLayout mainLayout = findViewById(R.id.main_layout_settings); // هل الوضع داكن ؟
        tvTitle = findViewById(R.id.tv_settings_title);

        // إعدادات التلوين للوضع الداكن
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            int darkBg = Color.parseColor("#121212");
            getWindow().setStatusBarColor(Color.BLACK);  // اظهار شريط الحالة باللون الأسود


            // تغيير لون الواجهة
            if (mainLayout != null) {
                mainLayout.setBackground(null);
                mainLayout.setBackgroundColor(darkBg);
            }

            // تلوين الايقونات والعنوان
            if (btnBack != null) btnBack.setColorFilter(Color.WHITE);
            if (btnGoToCategories != null) btnGoToCategories.setColorFilter(Color.WHITE); // تلوين أيقونة الفئات
            if (tvTitle != null) tvTitle.setTextColor(Color.WHITE);
            tvCurrentCurrency.setTextColor(Color.LTGRAY);
            tvFirstDay.setTextColor(Color.LTGRAY);

            // تلوين نصوص البصمة في الوضع الداكن
            TextView tvFingerprint = findViewById(R.id.tv_Fingerprint);
            if (tvFingerprint != null) tvFingerprint.setTextColor(Color.WHITE);

            // تلوين اسم المستخدم وتأكيد الخروج في الوضع الداكن
            if (tvUserName != null) tvUserName.setTextColor(Color.WHITE);
            if (tvLogoutConfirm != null) tvLogoutConfirm.setTextColor(Color.parseColor("#FFD1D1"));
        }
    }

    // القيم المحفوظة
    private void loadSavedSettings() {

        String currency = preferences.getString("currency", "USD ($)"); // العملة
        tvCurrentCurrency.setText(currency);

        String firstDay = preferences.getString("first_day", "Sunday"); // اول يوم في الاسبوع
        tvFirstDay.setText(firstDay);

        boolean isDarkMode = preferences.getBoolean("dark_mode", false); // الوضع داكن ام افتراضي فاتح
        switchTheme.setChecked(isDarkMode);

        boolean isFingerprintEnabled = preferences.getBoolean("fingerprint_enabled", false); // حالة البصمة المحفوظة
        switchFingerprint.setChecked(isFingerprintEnabled);

        // جلب اسم المستخدم من التفضيلات وعرضه
        String userName = userPreferences.getString("user_name", "User Name");
        tvUserName.setText(userName);
    }

    private void setupListeners() {
        // زر العودة
        btnBack.setOnClickListener(v -> finish());

        // فتح واجهة الفئات
        btnGoToCategories.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, CategoryManagementActivity.class);
            startActivity(intent);
        });

        layoutCurrency.setOnClickListener(v -> showCurrencyDialog()); // العملات المتاحة

        layoutFirstDay.setOnClickListener(v -> showFirstDayDialog()); // الايام المتاحة

        // مفتاح تغيير الوضع
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("dark_mode", isChecked).apply(); // الحفظ في القاعدة
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); // داكن
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // فاتح
            }
            recreate(); // اعادة تشكيل الواجهة
        });

        // مفتاح تفعيل/تعطيل البصمة
        switchFingerprint.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("fingerprint_enabled", isChecked).apply(); // حفظ خيار البصمة
            if (isChecked) {
                Toast.makeText(this, "Fingerprint lock enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Fingerprint lock disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // زر تسجيل الخروج
        layoutLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        // زر اعادة الضبط الكلي للتطبيق
        btnReset.setOnClickListener(v -> showResetConfirmationDialog());
    }

    // اختيار العملة
    private void showCurrencyDialog() {
        String[] currencies = {"USD ($)", "EUR (€)", "ILS (₪)", "JOD (د.أ)"};
        new AlertDialog.Builder(this)
                .setTitle("Select Currency")
                .setItems(currencies, (dialog, which) -> {
                    String selected = currencies[which];
                    tvCurrentCurrency.setText(selected);
                    preferences.edit().putString("currency", selected).apply();
                }).show();
    }

    // اختيار اول يوم في الاسبوع
    private void showFirstDayDialog() {
        String[] days = {"Saturday", "Sunday", "Monday"};
        new AlertDialog.Builder(this)
                .setTitle("First Day of Week")
                .setItems(days, (dialog, which) -> {
                    String selected = days[which];
                    tvFirstDay.setText(selected);
                    preferences.edit().putString("first_day", selected).apply();
                }).show();
    }

    // تأكيد تسجيل الخروج
    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    // تنفيذ عملية تسجيل الخروج
    // تنفيذ عملية تسجيل الخروج (بدون حذف الباسكود)
    private void performLogout() {
        // بدلاً من استخدام .clear()، نقوم بمسح بيانات الجلسة فقط
        userPreferences.edit()
                .putBoolean("is_logged_in", false) // تغيير حالة الدخول إلى false
                .apply();

        // نترك "passcode" كما هو، لا نستخدم .remove("passcode") هنا

        Intent intent = new Intent(SettingsActivity.this, SignUpActivity.class);
        // لمنع العودة للخلف
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // تحذير وتأكيد الحذف
    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Reset All Data?")
                .setMessage("This action will delete all your transactions and settings. This cannot be undone.")
                .setPositiveButton("Reset", (dialog, which) -> resetData())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // دالة اعادة الضبط الكلي
    private void resetData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.clearAllTables();
            preferences.edit().clear().apply();

            runOnUiThread(() -> {
                Toast.makeText(this, "All data has been reset", Toast.LENGTH_SHORT).show();
                // العودة لشاشة الرئيسية بعد المسح الشامل
                Intent intent = new Intent(SettingsActivity.this, DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }
}
