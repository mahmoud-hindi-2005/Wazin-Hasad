package com.hindi.wazinhasad;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class SignUpActivity extends AppCompatActivity {

    private EditText etName, etNationalID, etPassword;
    private EditText code1, code2, code3, code4;
    private MaterialButton btnCreate;
    private ImageView btnBack;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        initViews();
        setupOtpListeners();

        // سهم الرجوع
        btnBack.setOnClickListener(v -> finish());

        // زر إنشاء الحساب
        btnCreate.setOnClickListener(v -> {
            if (validateInputs()) {
                saveUserData();
            }
        });


    }

    private void initViews() {
        etName = findViewById(R.id.et_Name);
        etNationalID = findViewById(R.id.et_National_ID);
        etPassword = findViewById(R.id.et_Password);

        code1 = findViewById(R.id.code1);
        code2 = findViewById(R.id.code2);
        code3 = findViewById(R.id.code3);
        code4 = findViewById(R.id.code4);

        btnCreate = findViewById(R.id.but_Create);
        btnBack = findViewById(R.id.imageView10);
    }

    private void setupOtpListeners() {
        code1.addTextChangedListener(new OtpTextWatcher(code1, code2));
        code2.addTextChangedListener(new OtpTextWatcher(code2, code3));
        code3.addTextChangedListener(new OtpTextWatcher(code3, code4));
        code4.addTextChangedListener(new OtpTextWatcher(code4, null));
    }

    private boolean validateInputs() {
        // جلب القيم من الحقول
        String name = etName.getText().toString().trim();
        String id = etNationalID.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        // جلب قيم مربعات الـ OTP الأربعة
        String c1 = code1.getText().toString().trim();
        String c2 = code2.getText().toString().trim();
        String c3 = code3.getText().toString().trim();
        String c4 = code4.getText().toString().trim();

        // 1. فحص حقل الاسم
        if (name.isEmpty()) {
            etName.setError("الاسم مطلوب");
            etName.requestFocus();
            return false;
        }

        // 2. فحص حقل الهوية
        if (id.isEmpty()) {
            etNationalID.setError("رقم الهوية مطلوب");
            etNationalID.requestFocus();
            return false;
        }

        // 3. فحص حقل كلمة المرور (إذا كان مستخدم في تصميمك)
        if (pass.isEmpty()) {
            etPassword.setError("كلمة المرور مطلوبة");
            etPassword.requestFocus();
            return false;
        }

        // 4. فحص إكمال كود الدخول (OTP)
        if (c1.isEmpty() || c2.isEmpty() || c3.isEmpty() || c4.isEmpty()) {
            Toast.makeText(this, "الرجاء إكمال كود الدخول المكون من 4 أرقام", Toast.LENGTH_SHORT).show();
            // توجيه المؤشر لأول مربع فارغ في الكود
            if (c1.isEmpty()) code1.requestFocus();
            else if (c2.isEmpty()) code2.requestFocus();
            else if (c3.isEmpty()) code3.requestFocus();
            else code4.requestFocus();
            return false;
        }

        return true;
    }

    private void saveUserData() {
        // استخراج البيانات النهائية للحفظ
        String name = etName.getText().toString().trim();
        String id = etNationalID.getText().toString().trim();
        String otp = code1.getText().toString().trim() +
                code2.getText().toString().trim() +
                code3.getText().toString().trim() +
                code4.getText().toString().trim();

        // الحفظ في SharedPreferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_name", name);
        editor.putString("user_national_id", id);
        editor.putString("user_passcode", otp);
        editor.putBoolean("is_registered", true);
        editor.apply();

        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();

        // التوجه للداشبورد
        Intent intent = new Intent(SignUpActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    private class OtpTextWatcher implements TextWatcher {
        private EditText currentView, nextView;

        public OtpTextWatcher(EditText currentView, EditText nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            // إذا كتب المستخدم رقماً واحداً، انتقل للمربع التالي تلقائياً
            if (s.length() == 1 && nextView != null) {
                nextView.requestFocus();
            }
        }
    }
}
