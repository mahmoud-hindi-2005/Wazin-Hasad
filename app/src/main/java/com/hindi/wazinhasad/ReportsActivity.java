package com.hindi.wazinhasad;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

public class ReportsActivity extends AppCompatActivity {

    // تعريف المتغيرات
    private PieChart pieChart;
    private TabLayout tabLayout;
    private Button btnMonth, btnYear;
    private ImageView btnBack;
    private WazinDatabase db;
    private String currentType = "all";
    private String currentPeriod = "month";
    private boolean isDarkMode = false;
    private SharedPreferences userPreferences; // بيانات المستخدم الحالي

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        // فتح قاعدة البيانات واستدعاء تفضيلات المستخدم
        db = WazinDatabase.getDatabase(this);
        userPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // شكل الواجهة حسب الوضع
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        isDarkMode = (currentNightMode == Configuration.UI_MODE_NIGHT_YES);

        initViews(); // العمليات في الواجهة
        setupChart(); // الرسم البياني
        updateFilterButtonsVisuals();
        loadDataAndRenderChart(); // الرسوم البيانية

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() { // مستمع النقر على الثلاث فلاتر
            @Override
            public void onTabSelected(TabLayout.Tab tab) { // دالة النقر
                switch (tab.getPosition()) {
                    case 0: currentType = "all"; break; // الخيار الاول
                    case 1: currentType = "expense"; break; // الخيار الثاني
                    case 2: currentType = "income"; break; // الخيار الثالث
                }
                loadDataAndRenderChart(); // اعادة تحميل الرسوم البيانية
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {} // ترك خيار واختيار خيار اخر
            @Override public void onTabReselected(TabLayout.Tab tab) {} // النقر على خيار مختار
        });

        // فلترة شهرية
        btnMonth.setOnClickListener(v -> {
            currentPeriod = "month";
            updateFilterButtonsVisuals();
            loadDataAndRenderChart();
        });

        // فلترة سنوية
        btnYear.setOnClickListener(v -> {
            currentPeriod = "year";
            updateFilterButtonsVisuals();
            loadDataAndRenderChart();
        });

        // العودة
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {

        // ربط العناصر للتحكم بها
        tabLayout = findViewById(R.id.tab_layout);
        btnMonth = findViewById(R.id.btn_filter_month);
        btnYear = findViewById(R.id.btn_filter_year);
        btnBack = findViewById(R.id.imageView10);
        FrameLayout chartContainer = findViewById(R.id.chart_container);
        ConstraintLayout mainLayout = findViewById(R.id.main_layout_reports);

        pieChart = new PieChart(this); // واجهة الرسم البياني
        chartContainer.removeAllViews(); // حذف رسوم قديمة
        chartContainer.addView(pieChart); // وضع الرسم الجديد

        // ضبط الوضع الداكن

        if (isDarkMode) {
            getWindow().setStatusBarColor(Color.BLACK);  // اظهار شريط الحالة باللون الأسود
            int darkBg = Color.parseColor("#121212");
            if (mainLayout != null) {
                mainLayout.setBackground(null);
                mainLayout.setBackgroundColor(darkBg);
            }
            if (tabLayout != null) {
                tabLayout.setBackgroundColor(Color.WHITE);
                tabLayout.setTabTextColors(Color.BLACK, Color.BLACK);
                tabLayout.setSelectedTabIndicatorColor(Color.BLACK);
            }
            if (btnBack != null) btnBack.setColorFilter(Color.WHITE);
        }
    }

    // الحصول على أول يوم في الأسبوع
    private int getFirstDayFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        String day = prefs.getString("first_day", "Sunday");
        switch (day) {
            case "Saturday": return Calendar.SATURDAY;
            case "Monday":   return Calendar.MONDAY;
            default:         return Calendar.SUNDAY; // يوم السبت
        }
    }

    // ضبط شكل الرسم البياني
    private void setupChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        if (isDarkMode) {
            pieChart.setHoleColor(Color.parseColor("#121212"));
            pieChart.setCenterTextColor(Color.WHITE);
            pieChart.getLegend().setTextColor(Color.WHITE);
            pieChart.setEntryLabelColor(Color.WHITE);
        } else {
            pieChart.setHoleColor(Color.WHITE);
            pieChart.setCenterTextColor(Color.BLACK);
            pieChart.setEntryLabelColor(Color.BLACK);
        }
        pieChart.setCenterText("Transactions");
        pieChart.setCenterTextSize(16f);
    }


    // ضبط ازرار الفلاتر
    private void updateFilterButtonsVisuals() {

        // ضبط الالوان والوضع الداكن ام لا ؟
        int activeBg = isDarkMode ? Color.parseColor("#80D8FF") : Color.parseColor("#BCDAFD");
        int activeText = Color.parseColor("#003366");
        int inactiveBg = Color.TRANSPARENT;
        int inactiveText = Color.WHITE;

        if (currentPeriod.equals("month")) { // الشهري
            btnMonth.setBackgroundTintList(ColorStateList.valueOf(activeBg));
            btnMonth.setTextColor(activeText);
            btnYear.setBackgroundTintList(ColorStateList.valueOf(inactiveBg));
            btnYear.setTextColor(inactiveText);

        } else { // السنوي
            btnYear.setBackgroundTintList(ColorStateList.valueOf(activeBg));
            btnYear.setTextColor(activeText);
            btnMonth.setBackgroundTintList(ColorStateList.valueOf(inactiveBg));
            btnMonth.setTextColor(inactiveText);
        }
    }


    // تحميل البيانات داخل الرسم البياني
    private void loadDataAndRenderChart() {

        // جلب رقم هوية المستخدم الحالي المسجل لفلترة الرسوم البيانية الخاصة به
        String currentUserId = userPreferences.getString("user_national_id", "");

        // الحساب في الخلفية
        Executors.newSingleThreadExecutor().execute(() -> {
            String[] dates = getStartAndEndDates(currentPeriod);
            List<Transaction> transactions;

            // تم تمرير currentUserId لضمان عدم اختلاط البيانات في التقارير
            if (currentType.equals("all")) {
                transactions = db.transactionDao().getFilteredTransactions(currentUserId, "", null, 0, dates[0], dates[1]);
            } else {
                transactions = db.transactionDao().getFilteredTransactions(currentUserId, "", currentType, 0, dates[0], dates[1]);
            }

            // الرسم البياني
            List<PieEntry> entries = new ArrayList<>();
            if (transactions != null) {
                for (Transaction t : transactions) {
                    entries.add(new PieEntry((float) t.getAmount(), t.getCategory()));
                }
            }

            runOnUiThread(() -> {  // العودة للواجهة

                // لا يوجدى بيانات
                if (entries.isEmpty()) {
                    pieChart.clear();
                    pieChart.setNoDataText("No data is currently available");
                    if (isDarkMode) pieChart.setNoDataTextColor(Color.WHITE);
                    pieChart.invalidate();

                    // رسم المخطط البياني
                } else {
                    PieDataSet dataSet = new PieDataSet(entries, ""); // جمع القطع
                    dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // الوان مختلفة
                    dataSet.setValueTextColor(isDarkMode ? Color.WHITE : Color.BLACK); // لون نص الارقام
                    PieData data = new PieData(dataSet);
                    pieChart.setData(data); // وضع البيانات
                    pieChart.animateY(1000); // حركة كاملة
                    pieChart.invalidate(); // تحديث واجهة الرسم البياني
                }
            });
        });
    }

    private String[] getStartAndEndDates(String period) {

        // الحصول على التاريخ الحالي وضبط التنسيق
        Calendar cal = Calendar.getInstance();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
        String endDate = sdf.format(cal.getTime());

        // فلتر الشهر
        if (period.equals("month")) {
            cal.set(Calendar.DAY_OF_MONTH, 1);

            // فلتر السنة
        } else {
            int firstDay = getFirstDayFromPrefs();
            cal.setFirstDayOfWeek(firstDay);
            cal.set(Calendar.DAY_OF_YEAR, 1);
        }
        return new String[]{sdf.format(cal.getTime()), endDate};
    }

    // تحديث الواجهة عند الخروج والدخول
    @Override
    protected void onResume() {
        super.onResume();
        loadDataAndRenderChart();
    }
}