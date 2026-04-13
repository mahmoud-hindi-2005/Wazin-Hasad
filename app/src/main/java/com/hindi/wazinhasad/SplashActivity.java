package com.hindi.wazinhasad;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // شريط التحميل
        ProgressBar progressBar = findViewById(R.id.progress_bar);

        // حركة التحميل بشكل كامل
        ObjectAnimator.ofInt(progressBar, "progress", 0, 100)
                .setDuration(4000)
                .start();

        // انتظار انتهاء تحميل الخط للانتقال للواجهة التالية
        new Handler().postDelayed(() -> {
            // جلب حالة البصمة من الإعدادات
            SharedPreferences preferences = getSharedPreferences("app_settings", MODE_PRIVATE);
            boolean isFingerprintEnabled = preferences.getBoolean("fingerprint_enabled", false);

            if (isFingerprintEnabled) {
                checkDeviceCanAuthenticate();
            } else {
                navigateToNext();
            }
        }, 4000);
    }

    private void checkDeviceCanAuthenticate() {
        BiometricManager biometricManager = BiometricManager.from(this);

        // فحص هل الجهاز يدعم البصمة وهل هي مفعلة في الإعدادات
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                // الجهاز جاهز تماماً
                showBiometricPrompt();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "This device does not have a fingerprint sensor", Toast.LENGTH_SHORT).show();
                navigateToNext();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "Sensor is currently unavailable", Toast.LENGTH_SHORT).show();
                navigateToNext();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "You must register a fingerprint in the phone settings first", Toast.LENGTH_LONG).show();
                navigateToNext();
                break;
            default:
                navigateToNext();
                break;
        }
    }

    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(SplashActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                navigateToNext();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                // إذا ضغط المستخدم إلغاء أو حدث خطأ
                Toast.makeText(SplashActivity.this, "Error " + errString, Toast.LENGTH_SHORT).show();
            }
        });

        // نافذة البصمة
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Privacy Lock")
                .setSubtitle("Use your fingerprint to access Wazin Hasad")
                .setNegativeButtonText("Cancel")
                .build();
        biometricPrompt.authenticate(promptInfo);
    }

    private void navigateToNext() {
        // فحص حالة التسجيل من ملف SharedPreferences
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isRegistered = userPrefs.getBoolean("is_registered", false);

        Intent intent;
        if (isRegistered) {
            // إذا سجل مسبقاً، ننتقل لواجهة تسجيل الدخول (Passcode)
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        } else {
            // إذا لم يسجل، ننتقل لواجهة Onboarding أو التسجيل
            intent = new Intent(SplashActivity.this, OnboardingActivity.class);
        }

        startActivity(intent);
        finish();
    }
}