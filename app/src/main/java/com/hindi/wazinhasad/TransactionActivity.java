package com.hindi.wazinhasad;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

public class TransactionActivity extends AppCompatActivity {

    private TextInputEditText etAmount, etDate, etNote;
    private AutoCompleteTextView categoryDropdown;
    private MaterialButtonToggleGroup toggleGroup;
    private MaterialButton btnExpense, btnIncome;
    private Button btnSave, btnCancel;
    private ImageView btnBack;
    private WazinDatabase db;
    private String selectedType = "expense";
    private int editTransactionId = -1;
    private SharedPreferences preferences;
    private SharedPreferences userPreferences;

    private final double EXCHANGE_RATE = 3.7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = getSharedPreferences("app_settings", MODE_PRIVATE);
        userPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        applyAppTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        db = WazinDatabase.getDatabase(this);
        initViews();
        setupCategoryDropdown();
        setupDatePicker();
        setupToggleLogic();
        checkEditMode();

        btnSave.setOnClickListener(v -> saveTransaction());
        btnCancel.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());
    }

    private void applyAppTheme() {
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void initViews() {
        etAmount = findViewById(R.id.et_Amount);
        etDate = findViewById(R.id.et_Date);
        etNote = findViewById(R.id.et_Note);
        categoryDropdown = findViewById(R.id.category_Dropdown);
        toggleGroup = findViewById(R.id.toggleGroup);
        btnExpense = findViewById(R.id.btnExpense);
        btnIncome = findViewById(R.id.btnIncome);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.imageView8);
        TextView tvTitle = findViewById(R.id.tv_title);
        ConstraintLayout mainLayout = findViewById(R.id.main_layout);

        TextInputLayout tilAmount = findViewById(R.id.tilAmount);
        TextInputLayout tilCategory = findViewById(R.id.tilCategory);
        TextInputLayout tilDate = findViewById(R.id.tilDate);
        TextInputLayout tilNote = findViewById(R.id.tilNote);

        boolean isDark = preferences.getBoolean("dark_mode", false);

        if (isDark) {
            mainLayout.setBackgroundColor(Color.parseColor("#121212"));
            if (tvTitle != null) tvTitle.setTextColor(Color.WHITE);
            btnBack.setColorFilter(Color.WHITE);
            getWindow().setStatusBarColor(Color.BLACK);

            int customBgColor = ContextCompat.getColor(this, R.color.my_custom_bg);
            etAmount.setTextColor(Color.BLACK);
            etDate.setTextColor(Color.BLACK);
            etNote.setTextColor(Color.BLACK);
            categoryDropdown.setTextColor(Color.BLACK);

            tilAmount.setBoxBackgroundColor(customBgColor);
            tilCategory.setBoxBackgroundColor(customBgColor);
            tilDate.setBoxBackgroundColor(customBgColor);
            tilNote.setBoxBackgroundColor(customBgColor);

            btnSave.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#757575")));
            btnSave.setTextColor(Color.WHITE);
            btnCancel.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
            btnCancel.setTextColor(Color.BLACK);
        }
    }

    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("TRANSACTION_ID")) {
            editTransactionId = intent.getIntExtra("TRANSACTION_ID", -1);
            btnSave.setText("Update");

            Executors.newSingleThreadExecutor().execute(() -> {
                Transaction t = db.transactionDao().getTransactionById(editTransactionId);
                if (t != null) {
                    runOnUiThread(() -> {
                        etAmount.setText(String.valueOf(t.getAmount()));
                        etDate.setText(t.getDate());
                        etNote.setText(t.getNote());
                        categoryDropdown.setText(t.getCategory(), false);

                        if ("income".equalsIgnoreCase(t.getType())) {
                            toggleGroup.check(R.id.btnIncome);
                            updateToggleColors(R.id.btnIncome);
                            selectedType = "income";
                        } else {
                            toggleGroup.check(R.id.btnExpense);
                            updateToggleColors(R.id.btnExpense);
                            selectedType = "expense";
                        }
                    });
                }
            });
        }
    }

    private void saveTransaction() {
        String amountStr = etAmount.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String categoryName = categoryDropdown.getText().toString();
        String note = etNote.getText().toString();

        if (amountStr.isEmpty() || date.isEmpty() || categoryName.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double enteredAmount = Double.parseDouble(amountStr);
        String currentUserId = userPreferences.getString("user_national_id", "");
        String fullCurrency = preferences.getString("currency", "USD ($)");
        String symbolOnly = "$";
        if (fullCurrency.contains("(") && fullCurrency.contains(")")) {
            symbolOnly = fullCurrency.substring(fullCurrency.indexOf("(") + 1, fullCurrency.indexOf(")"));
        }

        final String finalSymbol = symbolOnly;

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Transaction> allTransactions = db.transactionDao().getAllTransactionsByUser(currentUserId);

            double totalIncomeUSD = 0;
            double totalExpenseUSD = 0;

            for (Transaction t : allTransactions) {
                double valUSD = t.getCurrency().equals("₪") ? t.getAmount() / EXCHANGE_RATE : t.getAmount();
                if (t.getType().equalsIgnoreCase("income")) {
                    totalIncomeUSD += valUSD;
                } else {
                    totalExpenseUSD += valUSD;
                }
            }

            double balanceUSD = totalIncomeUSD - totalExpenseUSD;
            double availableInCurrentCurrency = finalSymbol.equals("₪") ? balanceUSD * EXCHANGE_RATE : balanceUSD;

            if (selectedType.equalsIgnoreCase("expense")) {
                if (editTransactionId != -1) {
                    Transaction oldT = db.transactionDao().getTransactionById(editTransactionId);
                    if (oldT != null && oldT.getType().equalsIgnoreCase("expense")) {
                        double oldValInCurrent = oldT.getCurrency().equals(finalSymbol) ? oldT.getAmount() :
                                (oldT.getCurrency().equals("$") ? oldT.getAmount() * EXCHANGE_RATE : oldT.getAmount() / EXCHANGE_RATE);
                        availableInCurrentCurrency += oldValInCurrent;
                    }
                }

                final double finalAvailable = availableInCurrentCurrency;
                if (enteredAmount > finalAvailable) {
                    runOnUiThread(() -> Toast.makeText(this, "Budget exceeded! Available: " + String.format("%.2f", finalAvailable) + " " + finalSymbol, Toast.LENGTH_LONG).show());
                    return;
                }
            }

            Transaction transaction = new Transaction(enteredAmount, selectedType, 1, date, note, categoryName, finalSymbol, currentUserId);
            if (editTransactionId == -1) {
                db.transactionDao().insert(transaction);
            } else {
                transaction.setId(editTransactionId);
                db.transactionDao().update(transaction);
            }

            runOnUiThread(() -> finish());
        });
    }

    private void setupToggleLogic() {
        updateToggleColors(toggleGroup.getCheckedButtonId());
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                selectedType = (checkedId == R.id.btnExpense) ? "expense" : "income";
                updateToggleColors(checkedId);
            }
        });
    }

    private void updateToggleColors(int checkedId) {
        boolean isDark = preferences.getBoolean("dark_mode", false);
        int unselectedBg = isDark ? Color.parseColor("#2C2C2C") : Color.parseColor("#F5F5F5");
        int unselectedText = isDark ? Color.WHITE : Color.BLACK;

        if (checkedId == R.id.btnExpense) {
            btnExpense.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C0392B")));
            btnExpense.setTextColor(Color.WHITE);
            btnIncome.setBackgroundTintList(ColorStateList.valueOf(unselectedBg));
            btnIncome.setTextColor(unselectedText);
        } else if (checkedId == R.id.btnIncome) {
            btnIncome.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2E7D32")));
            btnIncome.setTextColor(Color.WHITE);
            btnExpense.setBackgroundTintList(ColorStateList.valueOf(unselectedBg));
            btnExpense.setTextColor(unselectedText);
        }
    }

    private void setupCategoryDropdown() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Category> dbCategories = db.categoryDao().getAllCategories();
            List<String> finalCategories = new ArrayList<>();
            finalCategories.add("Salary");
            finalCategories.add("Food");
            finalCategories.add("Transport");
            finalCategories.add("Health");
            finalCategories.add("Other");

            for (Category cat : dbCategories) {
                if (!finalCategories.contains(cat.getName())) {
                    finalCategories.add(cat.getName());
                }
            }

            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, finalCategories);
                categoryDropdown.setAdapter(adapter);
            });
        });
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                String selectedDate = year + "-" + String.format("%02d", (month + 1)) + "-" + String.format("%02d", day);
                etDate.setText(selectedDate);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupCategoryDropdown();
    }
}