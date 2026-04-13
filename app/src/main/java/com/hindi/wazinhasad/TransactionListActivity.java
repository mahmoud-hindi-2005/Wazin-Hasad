package com.hindi.wazinhasad;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class TransactionListActivity extends AppCompatActivity {

    // تعريف المتغيرات
    private EditText etSearch;
    private ChipGroup chipGroupFilters;
    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private WazinDatabase db;
    private List<Transaction> allTransactions = new ArrayList<>();
    private ImageButton btnSort;
    private boolean isNewestFirst = true;
    private ImageView btnBack;
    private SharedPreferences preferences;
    private SharedPreferences userPreferences; // بيانات المستخدم الحالي

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        // استدعاء الاعدادات المخزنة مسبقا للتطبيق وللمستخدم
        preferences = getSharedPreferences("app_settings", MODE_PRIVATE);
        userPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // فتح قاعدة البيانات
        db = WazinDatabase.getDatabase(this);

        initViews(); // العمليات في الواجهة
        setupRecyclerView(); // قائمة المعاملات
        setupSearch();
        setupFilters();
        loadInitialData();
    }

    private void initViews() {

        // ربط العناصر
        etSearch = findViewById(R.id.et_search);
        chipGroupFilters = findViewById(R.id.chip_group_filters);
        rvTransactions = findViewById(R.id.rv_transactions);
        btnSort = findViewById(R.id.btn_sort);
        btnBack = findViewById(R.id.imageView5);

        View layoutHeader = findViewById(R.id.layout_header); // العنوان
        View scrollFilters = findViewById(R.id.scroll_filters); // الفلاتر

        // هل الوضع داكن ام عادي وتغيير الالوان

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            int bgColor = Color.parseColor("#121212");
            getWindow().setStatusBarColor(Color.BLACK);  // اظهار شريط الحالة باللون الأسود

            findViewById(android.R.id.content).setBackgroundColor(bgColor);
            rvTransactions.setBackgroundColor(bgColor);

            if (layoutHeader != null) layoutHeader.setBackgroundColor(bgColor);
            if (scrollFilters != null) scrollFilters.setBackgroundColor(bgColor);

            etSearch.setTextColor(Color.BLACK);
            etSearch.setHintTextColor(Color.DKGRAY);

            btnBack.setColorFilter(Color.WHITE);
            btnSort.setColorFilter(Color.WHITE);
        }

        // زر العودة
        btnBack.setOnClickListener(v -> finish());

        // زر الفرز
        btnSort.setOnClickListener(v -> {
            isNewestFirst = !isNewestFirst;
            btnSort.setRotation(isNewestFirst ? 0 : 180);
            applyFilters();
        });
    }

    // دالة تحديد بداية الاسبوع من المحفوظات
    private int getFirstDayFromPrefs() {
        String day = preferences.getString("first_day", "Sunday");
        switch (day) {
            case "Saturday": return Calendar.SATURDAY;
            case "Monday":   return Calendar.MONDAY;
            default:         return Calendar.SUNDAY;
        }
    }

    // ازرار الفلترة والالوان
    private void setupFilters() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDark = currentNightMode == Configuration.UI_MODE_NIGHT_YES;

        int activeColor = isDark ? Color.parseColor("#00897B") : Color.parseColor("#4CAF50");
        int unselectedColor = isDark ? Color.parseColor("#2C2C2C") : Color.parseColor("#BCDAFD");

        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            Chip chipIncome = findViewById(R.id.chip_income);
            Chip chipExpense = findViewById(R.id.chip_expense);
            Chip chipAll = findViewById(R.id.chip_all);
            Chip chipDateWeek = findViewById(R.id.chip_date_week);

            // منع اختيار دخل مع مصروف
            if (checkedIds.contains(R.id.chip_income) && checkedIds.contains(R.id.chip_expense)) {
                if (chipIncome.isPressed()) chipExpense.setChecked(false);
                else if (chipExpense.isPressed()) chipIncome.setChecked(false);
            }

            // منع اختيار الكل مع باقي الفلاتر
            if (checkedIds.contains(R.id.chip_all) && chipAll.isPressed()) {
                chipIncome.setChecked(false);
                chipExpense.setChecked(false);
                if (chipDateWeek != null) chipDateWeek.setChecked(false);
            }

            // الغاء الكل عند اختيار فلتر اخر
            if ((checkedIds.contains(R.id.chip_income) || checkedIds.contains(R.id.chip_expense) || checkedIds.contains(R.id.chip_date_week))
                    && chipAll.isChecked() && !chipAll.isPressed()) {
                chipAll.setChecked(false);
            }

            // اختيار افتراضي
            if (group.getCheckedChipIds().isEmpty()) {
                chipAll.setChecked(true);
            }

            // تحديث الوان الفلاتر المختارة
            for (int i = 0; i < group.getChildCount(); i++) {
                Chip chip = (Chip) group.getChildAt(i);
                chip.setChipBackgroundColor(ColorStateList.valueOf(chip.isChecked() ? activeColor : unselectedColor));
                chip.setTextColor(Color.WHITE);
            }
            applyFilters();
        });
    }

    // مربع البحث والفلترة
    private void applyFilters() {

        List<Transaction> filteredList = new ArrayList<>(); // قائمة لعرض النتائج
        String query = etSearch.getText().toString().toLowerCase().trim();
        List<Integer> checkedIds = chipGroupFilters.getCheckedChipIds();

        // تحديد بداية الاسبوع
        boolean filterByWeek = checkedIds.contains(R.id.chip_date_week);
        Calendar cal = Calendar.getInstance();
        int firstDay = getFirstDayFromPrefs();
        cal.setFirstDayOfWeek(firstDay);
        cal.set(Calendar.DAY_OF_WEEK, firstDay);

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startOfWeek = cal.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        boolean showAll = checkedIds.contains(R.id.chip_all) || checkedIds.isEmpty();

        for (Transaction t : allTransactions) {

            //   هل الاسم يحتوي على الحروف التي كتبها المستخدم ؟
            boolean matchesSearch = query.isEmpty() || (t.getCategory() != null && t.getCategory().toLowerCase().contains(query));

            // هل هي دخل أم مصرف أو عرض الكل
            boolean matchesType = showAll ||
                    (checkedIds.contains(R.id.chip_income) && "income".equalsIgnoreCase(t.getType())) ||
                    (checkedIds.contains(R.id.chip_expense) && "expense".equalsIgnoreCase(t.getType()));

            // تطبيق التاريخ وبداية الاسبوع
            boolean matchesDate = true;
            if (filterByWeek) {
                try {
                    Date transactionDate = sdf.parse(t.getDate());
                    matchesDate = transactionDate != null && !transactionDate.before(startOfWeek);
                } catch (Exception e) { matchesDate = false; }
            }

            // عرض النتائج الصحيحة

            if (matchesSearch && matchesType && matchesDate) filteredList.add(t);
        }

        // الاحدث او الاقدم
        Collections.sort(filteredList, (t1, t2) -> isNewestFirst ? t2.getDate().compareTo(t1.getDate()) : t1.getDate().compareTo(t2.getDate()));
        adapter.updateList(filteredList);
    }


    // تصميم القائمة
    private void setupRecyclerView() {
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(new ArrayList<>(), new TransactionAdapter.OnTransactionClickListener() {
            @Override public void onDeleteClick(Transaction t) {
                Executors.newSingleThreadExecutor().execute(() -> { db.transactionDao().delete(t); loadInitialData(); });
            }
            @Override public void onItemClick(Transaction t) {
                Intent i = new Intent(TransactionListActivity.this, TransactionDetailsActivity.class);
                i.putExtra("TRANSACTION_DATA", t);
                startActivity(i);
            }
        });
        rvTransactions.setAdapter(adapter);
    }

    // تحميل البيانات
    private void loadInitialData() {
        // جلب رقم هوية المستخدم الحالي المسجل لفلترة البيانات
        String currentUserId = userPreferences.getString("user_national_id", "");

        Executors.newSingleThreadExecutor().execute(() -> {
            // استدعاء الدالة المعدلة في DAO التي تأخذ ID المستخدم
            allTransactions = db.transactionDao().getFilteredTransactions(currentUserId, "", null, 0, "2000-01-01", "2100-01-01");
            runOnUiThread(this::applyFilters);
        });
    }

    // البحث اثناء الكتابة في المربع
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // التحديث المتواصل
    @Override
    protected void onResume() {
        super.onResume();
        loadInitialData();
    }
}