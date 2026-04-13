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

public class LoginActivity extends AppCompatActivity {

    // تعريف المتغيرات
    private EditText code1, code2, code3, code4;
    private ImageView btnBack;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        initViews();
        setupOtpLogic();

        // سهم العودة
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        code1 = findViewById(R.id.code1);
        code2 = findViewById(R.id.code2);
        code3 = findViewById(R.id.code3);
        code4 = findViewById(R.id.code4);
        btnBack = findViewById(R.id.imageView10);
    }

    private void setupOtpLogic() {
        // إضافة مستمع لكل حقل لينتقل المؤشر تلقائياً
        code1.addTextChangedListener(new OtpTextWatcher(code1, code2));
        code2.addTextChangedListener(new OtpTextWatcher(code2, code3));
        code3.addTextChangedListener(new OtpTextWatcher(code3, code4));

        // الحقل الأخير يقوم بعملية التحقق فور الكتابة فيه
        code4.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    verifyPasscode();
                }
            }
        });
    }

    private void verifyPasscode() {
        // تجميع الأرقام الأربعة في رمز الدخول السريع
        String enteredCode = code1.getText().toString() + code2.getText().toString() +
                code3.getText().toString() + code4.getText().toString();

        // جلب الكود الصحيح المحفوظ عند التسجيل
        String savedCode = preferences.getString("user_passcode", "");

        if (enteredCode.equals(savedCode)) {
            // إذا كان الكود صحيحاً، انتقل للشاشة الرئيسية للمصاريف
            Toast.makeText(this, "Welcome Back!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        } else {
            // إذا كان الكود خاطئاً، امسح الحقول وأخبر المستخدم
            Toast.makeText(this, "Wrong Passcode! Try again", Toast.LENGTH_SHORT).show();
            clearFields();
        }
    }

    private void clearFields() {
        code1.setText("");
        code2.setText("");
        code3.setText("");
        code4.setText("");
        code1.requestFocus(); // إعادة المؤشر للمربع الأول
    }

    // كلاس داخلي لتسهيل التنقل بين المربعات
    private class OtpTextWatcher implements TextWatcher {
        private EditText current, next;

        public OtpTextWatcher(EditText current, EditText next) {
            this.current = current;
            this.next = next;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 1 && next != null) {
                next.requestFocus();
            }
        }
    }
}