package com.hindi.wazinhasad;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity {

    // تعريف المتغيرات
    private TextView tvTotalBalance, tvTotalIncome, tvTotalExpense, tvCurrencySymbol;
    private TextView tvLabelBalance, tvLabelIncome, tvLabelExpense, tvStatsTitle;
    private View layoutRoot, cardBalanceInner, cardIncome, cardExpense, cardPeriodSelector;
    private Spinner spinnerPeriod;
    private BottomNavigationView bottomNavigationView;
    private WazinDatabase db;  // قاعدة البيانات
    private SharedPreferences preferences;  // البيانات المخزنة
    private SharedPreferences userPreferences; // بيانات المستخدم الحالي

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // الحصول على اعدادات التطبيق ( الوضع , العملة , بداية الاسبوع )
        preferences = getSharedPreferences("app_settings", MODE_PRIVATE);
        userPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        applyAppTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);  // ربط التصميم بالجافا

        db = WazinDatabase.getDatabase(this); // فتح قاعدة البيانات

        initViews(); // العمليات داخل الواجهة

        setupPeriodSpinner();  // قائمة Spinner

        setupNavigation();  // شريط السفلي

        applyManualIconColors();  // التحكم بألوان الأيقونات برمجياً عند تغيير الوضع
    }

    // التفاعل بين الواجهات عند اغلاق وفتح اي منها
    @Override
    protected void onResume() {
        super.onResume();
        applyAppTheme();  // التأكد من الوضع المحدد عندما يتحرك المستخدم بين الواجهات

        if (spinnerPeriod != null && spinnerPeriod.getSelectedItem() != null) { // التأكد من الفترة المحددة
            updateDataByPeriod(spinnerPeriod.getSelectedItem().toString());
        }
        applyManualIconColors();  // تلوين الايقونات حسب الوضع المحدد
    }

    // ربط الجافا بالتصميم لعرض العناصر داخلها والعناصر التي سنتحكم بلونها
    private void initViews() {
        tvTotalBalance = findViewById(R.id.tv_total_balance);
        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalExpense = findViewById(R.id.tv_total_expense);
        tvCurrencySymbol = findViewById(R.id.tv_currency_symbol);
        tvLabelBalance = findViewById(R.id.tv_label_balance);
        tvLabelIncome = findViewById(R.id.tv_label_income);
        tvLabelExpense = findViewById(R.id.tv_label_expense);
        tvStatsTitle = findViewById(R.id.tv_stats_title);
        spinnerPeriod = findViewById(R.id.spinner_period);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        layoutRoot = findViewById(R.id.main_layout);
        cardBalanceInner = findViewById(R.id.layout_balance_inner);
        cardIncome = findViewById(R.id.layout_income_card);
        cardExpense = findViewById(R.id.layout_expense_card);
        cardPeriodSelector = findViewById(R.id.card_period_selector);
    }

    // شكل الواجهة ( applyAppTheme )
    // يحدث عند فتح الواجهة فوراً
    private void applyAppTheme() {

        boolean isDarkMode = preferences.getBoolean("dark_mode", false);  // الوضع المحدد
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        // تغيير لون الواجهة حسب الوضع
        if (layoutRoot != null) {
            layoutRoot.setBackgroundColor(isDarkMode ? Color.parseColor("#121212") : Color.parseColor("#FEFEFE"));
        }
        updateVisuals(isDarkMode);  // دالة لتغيير باقي العناصر في الوضع الداكن
    }

    // التحكم بشكل العناصر عند تغيير الوضع
    private void updateVisuals(boolean isDark) {

        if (cardIncome == null || cardBalanceInner == null) return;  // هل العناصر تم تحميلها في الواجهة

        // الالوان المستخدمة
        int white = Color.WHITE;
        int spinnerBgLight = Color.parseColor("#18184E");
        int spinnerBgDark = Color.parseColor("#1E1E1E");

        // الوضع الداكن
        if (isDark) {

            getWindow().setStatusBarColor(Color.BLACK);  // اظهار شريط الحالة باللون الأسود

            int darkCardColor = Color.parseColor("#1E1E1E");
            int darkNavColor = Color.parseColor("#121212");

            cardBalanceInner.setBackgroundColor(darkCardColor); // بطافة التوتل
            cardIncome.setBackgroundColor(darkCardColor); // بطاقة الدخل
            cardExpense.setBackgroundColor(darkCardColor);  // بطاقة المصاريف

            if (cardPeriodSelector != null) { // قائمة spinner
                cardPeriodSelector.setBackgroundTintList(ColorStateList.valueOf(darkCardColor));
            }

            spinnerPeriod.setBackgroundTintList(ColorStateList.valueOf(white));
            bottomNavigationView.setBackgroundColor(darkNavColor);  // الشريط السفلي
            bottomNavigationView.setElevation(25f); // اظهار الشريط فوق الشاشة

            // النصوص
            tvLabelBalance.setTextColor(white);
            tvTotalBalance.setTextColor(white);
            tvLabelIncome.setTextColor(white);
            tvTotalIncome.setTextColor(white);
            tvLabelExpense.setTextColor(white);
            tvTotalExpense.setTextColor(white);
            tvStatsTitle.setTextColor(white);
            if(tvCurrencySymbol != null) tvCurrencySymbol.setTextColor(white);

        } else {  // العودة للوضع الافتراضي الفاتح

            cardBalanceInner.setBackgroundColor(spinnerBgLight);
            cardIncome.setBackground(ContextCompat.getDrawable(this, R.drawable.blue_gradient_bg));
            cardExpense.setBackground(ContextCompat.getDrawable(this, R.drawable.blue_gradient_bg));

            if (cardPeriodSelector != null) {
                cardPeriodSelector.setBackgroundTintList(ColorStateList.valueOf(spinnerBgLight));
            }
            spinnerPeriod.setBackgroundTintList(ColorStateList.valueOf(white));

            int darkText = Color.parseColor("#261739");
            tvLabelIncome.setTextColor(darkText);
            tvLabelExpense.setTextColor(darkText);
            tvStatsTitle.setTextColor(darkText);
            tvTotalBalance.setTextColor(white);
            tvLabelBalance.setTextColor(white);
            if(tvCurrencySymbol != null) tvCurrencySymbol.setTextColor(white);

            bottomNavigationView.setBackgroundColor(white);
            bottomNavigationView.setElevation(10f);
        }

        spinnerPeriod.post(() -> {
            View view = spinnerPeriod.getSelectedView();
            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                tv.setBackgroundColor(isDark ? spinnerBgDark : spinnerBgLight);
                tv.setTextColor(Color.WHITE);
            }
        });

        applyManualIconColors();
    }

    // شريط التنقل السفلي
    private void applyManualIconColors() {

        if (bottomNavigationView != null) { // هل تم تحميله ؟
            bottomNavigationView.setItemIconTintList(null); // تلوين الايقونات برمجيا يدويا

            Menu menu = bottomNavigationView.getMenu(); // فتح ملف تصميم الايقونات والشريط
            int skyBlue = Color.parseColor("#00BFFF"); // لون الايقونة النشطة

            // تلوين الايقونات الاخرة حسب الوضع المحدد داكن ام افتراضي
            int defaultIconColor = preferences.getBoolean("dark_mode", false) ? Color.parseColor("#E0E0E0") : Color.parseColor("#01687C");

            // اعطاء الالوان لكل ايقونة
            tintMenuItem(menu.findItem(R.id.home), skyBlue);
            tintMenuItem(menu.findItem(R.id.transactions), defaultIconColor);
            tintMenuItem(menu.findItem(R.id.add_transactions), defaultIconColor);
            tintMenuItem(menu.findItem(R.id.report), defaultIconColor);
            tintMenuItem(menu.findItem(R.id.settings), defaultIconColor);
        }
    }

    // دالة التلوين المستخدمة في دالة applyManualIconColors
    private void tintMenuItem(MenuItem item, int color) {
        if (item != null && item.getIcon() != null) {
            item.getIcon().setTint(color);
        }
    }

    // دالة عرض الأرقام والحسابات
    private void updateDataByPeriod(String period) {

        String[] dates = getStartAndEndDates(period); // دالة لتحديد الفترة التي ستعرض ارقامها

        // جلب رقم هوية المستخدم الحالي المسجل لفلترة البيانات الخاصة به فقط
        String currentUserId = userPreferences.getString("user_national_id", "");

        // عرض رمز العملة المحدد في الاعدادات
        String currencySetting = preferences.getString("currency", "USD ($)");
        String symbol = "$"; // القيمة الافتراضية

        // معاملات التحويل الثابتة
        double displayRate = 1.0;

        // تحديد التحويل ورمز العملة بناءً على اختيار المستخدم
        if (currencySetting.contains("ILS")) {
            symbol = "₪";
            displayRate = 3.70;
        } else if (currencySetting.contains("EUR")) {
            symbol = "€";
            displayRate = 0.92;
        } else if (currencySetting.contains("JOD")) {
            symbol = "د.أ";
            displayRate = 0.71;
        } else {
            symbol = "$";
            displayRate = 1.0;
        }

        final String finalSymbol = symbol;
        final double currentDisplayRate = displayRate;

        // العمل في الخلفية لحساب الأرقام والمبالغ
        Executors.newSingleThreadExecutor().execute(() -> {
            //  القائمة المفلترة للمستخدم والفترة المحددة
            List<Transaction> transactions = db.transactionDao().getFilteredTransactions(currentUserId, "", null, 0, dates[0], dates[1]);

            double totalIncomeConverted = 0;
            double totalExpenseConverted = 0;

            for (Transaction t : transactions) {
                //  تحويل مبلغ المعاملة من عملتها الأصلية إلى دولار
                double amountInUSD = t.getCurrency().equals("₪") ? t.getAmount() / 3.70 : t.getAmount();

                //  تحويل المبلغ من دولار إلى عملة العرض الحالية المختارة في الرئيسية
                double finalAmount = amountInUSD * currentDisplayRate;

                if (t.getType().equalsIgnoreCase("income")) {
                    totalIncomeConverted += finalAmount;
                } else {
                    totalExpenseConverted += finalAmount;
                }
            }

            float income = (float) totalIncomeConverted;
            float expense = (float) totalExpenseConverted;
            float balance = income - expense;

            // العودة للواجهة
            runOnUiThread(() -> {
                // تحديث الرمز في الواجهة
                if (tvCurrencySymbol != null) {
                    tvCurrencySymbol.setText(finalSymbol);
                }

                // عرض المبالغ المحولة في البطاقات
                tvTotalIncome.setText("+ " + String.format("%.2f", income)); // تنسيق الدخل المحول
                tvTotalExpense.setText("- " + String.format("%.2f", expense)); // تنسيق المصاريف المحولة
                tvTotalBalance.setText(String.format("%.2f", balance));  // تنسيق التوتل المحول

                updateCharts(income, expense, balance);  // تحديث الرسوم البيانية حسب المبالغ الجديدة المحولة
            });
        });
    }

    // تحويل الارقام لرسوم بيانية
    private void updateCharts(float income, float expense, float balance) {

        // شكل الرسوم البيانية حسب الوضع المحدد
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);
        int[] chartColors = isDarkMode ?
                new int[]{Color.WHITE, Color.parseColor("#757575")} :
                new int[]{Color.parseColor("#3498DB"), Color.parseColor("#85C1E9")};

        // المخطط الأول
        FrameLayout container1 = findViewById(R.id.chart_container);
        PieChart chart1 = new PieChart(this);
        ArrayList<PieEntry> entries = new ArrayList<>();
        if(income > 0 || expense > 0) {
            entries.add(new PieEntry(income, "income"));
            entries.add(new PieEntry(expense, "expense"));
        }

        // عنوان كل جزء من الرسوم واحجامها والوانها
        PieDataSet set1 = new PieDataSet(entries, "");
        set1.setColors(chartColors);
        set1.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        set1.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        int textColor = isDarkMode ? Color.WHITE : Color.BLACK;
        set1.setValueLineColor(textColor); // لون الخطوط الخارجية
        set1.setValueTextColor(textColor); // لون النسب
        set1.setValueTextSize(12f); // حجم النص
        chart1.setData(new PieData(set1));  // ربط البيانات بالرسم البياني
        chart1.setEntryLabelColor(textColor); // لون اسماء القئات
        chart1.setEntryLabelTextSize(12f); // حجم الاسماء
        chart1.setExtraOffsets(25, 10, 25, 10); // ازاحة
        styleChart(chart1);
        container1.removeAllViews(); // حذف اي رسم قديم
        container1.addView(chart1); // وضع الرسم الجديد


        // المخطط الثاني
        FrameLayout container2 = findViewById(R.id.chart_container_2);
        PieChart chart2 = new PieChart(this);
        ArrayList<PieEntry> entries2 = new ArrayList<>();
        entries2.add(new PieEntry(100f, ""));  // قطعة كاملة
        PieDataSet set2 = new PieDataSet(entries2, "");
        set2.setColor(isDarkMode ? Color.parseColor("#424242") : Color.parseColor("#AED6F1"));
        set2.setDrawValues(false);
        chart2.setData(new PieData(set2));
        chart2.setCenterText(String.format("%.2f", balance) + "\nBalance");
        chart2.setCenterTextColor(isDarkMode ? Color.WHITE : Color.parseColor("#2E86C1"));
        chart2.setHoleRadius(85f);
        styleChart(chart2);
        container2.removeAllViews();
        container2.addView(chart2);
    }

    // اخفاء الاعدادات الافتراضية التي تضعها المكتبة وعمل animate
    private void styleChart(PieChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.animateY(1000);  // تحريك الرسم بشكل دائري
        chart.setHoleColor(Color.TRANSPARENT);  // الجزء الداخلي من الرسوم شفافة
    }

    // دالة قائمة Spinner
    private void setupPeriodSpinner() {

        String[] periods = {"This Month", "Last Month", "This Year", "All Time"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, periods);

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spinnerPeriod.setAdapter(adapter);
        spinnerPeriod.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {

            // حدث اختيار فترة
            @Override
            public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                updateDataByPeriod(periods[pos]);
                updateVisuals(preferences.getBoolean("dark_mode", false));
            }
            // دالة افتراضية مع الكلاس السبنر
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
    }

    // حساب الفترات
    // حساب الفترات بشكل دقيق
    private String[] getStartAndEndDates(String period) {

        Calendar cal = Calendar.getInstance(); // الوقت الحالي
        // تم التعديل هنا لضمان وجود الأصفار (yyyy-MM-dd) لتتطابق مع قاعدة البيانات
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String startDate;
        String endDate = sdf.format(cal.getTime()); // نهاية الفترات هي اليوم

        switch (period) {
            case "This Month":
                cal.set(Calendar.DAY_OF_MONTH, 1);  // بداية الشهر الحالي
                startDate = sdf.format(cal.getTime());
                break;
            case "Last Month":
                cal.add(Calendar.MONTH, -1); // العودة شهر للخلف
                cal.set(Calendar.DAY_OF_MONTH, 1); // أول يوم في الشهر الماضي
                startDate = sdf.format(cal.getTime());
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH)); // آخر يوم في الشهر الماضي
                endDate = sdf.format(cal.getTime());
                break;
            case "This Year":
                cal.set(Calendar.DAY_OF_YEAR, 1); // أول يوم في السنة الحالية
                startDate = sdf.format(cal.getTime());
                break;
            default:
                startDate = "1970-01-01";
                endDate = "2099-12-31";
                break;
        }
        return new String[]{startDate, endDate};
    }
    // اعدادات الشريط السفلي
    private void setupNavigation() {
        bottomNavigationView.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_UNLABELED); // اخفاء النص الشارح
        bottomNavigationView.setItemActiveIndicatorEnabled(false); // اخفاء الوميض الخلفي
        bottomNavigationView.setItemRippleColor(ColorStateList.valueOf(Color.TRANSPARENT)); // اخفاء التأثير

        // مستمع النقر والتنقل بين الواجهات
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home) return false;
            else if (id == R.id.transactions) startActivity(new Intent(this, TransactionListActivity.class));
            else if (id == R.id.add_transactions) startActivity(new Intent(this, TransactionActivity.class));
            else if (id == R.id.report) startActivity(new Intent(this, ReportsActivity.class));
            else if (id == R.id.settings) startActivity(new Intent(this, SettingsActivity.class));
            return false;
        });
    }
}