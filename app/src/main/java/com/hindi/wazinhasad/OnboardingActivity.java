package com.hindi.wazinhasad;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    // تعريف المتغيرات
    private ViewPager2 viewPager2;
    private OnboardingAdapter onboardingAdapter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // هل نعرض واجهة الترحيب ام لا حسب المستخدم هل فتح الواجهة ام لا ؟
        sharedPreferences = getSharedPreferences("WazinHasadPrefs", Context.MODE_PRIVATE);
        boolean open_First_time = sharedPreferences.getBoolean("open_First_time", true);

        if (!open_First_time) {
            navigateToDashboard();
            return;
        }

        setContentView(R.layout.activity_onboarding); // ملف تصميم الواجهة

        viewPager2 = findViewById(R.id.view_pager_onboarding); // مساحة العرض
        ImageView btnNext = findViewById(R.id.iv_Return);
        TextView btnSkip = findViewById(R.id.tV_Back);

        // القائمة التي ستعرض التصميمات
        setupOnboardingItems();
        viewPager2.setAdapter(onboardingAdapter); // ربط viewPager2 بالتصميمات

        // زر التالي لعرض جميع التصميمات بالتتابع
        btnNext.setOnClickListener(v -> {
            if (viewPager2.getCurrentItem() + 1 < onboardingAdapter.getItemCount()) {
                viewPager2.setCurrentItem(viewPager2.getCurrentItem() + 1);
            } else {
                markOnboardingFinished(); // اخر تصميم
                navigateToDashboard(); // اغلاق الواجهة
            }
        });

        // زر التخطي للوصول للواجهة الرئيسية فوراً
        btnSkip.setOnClickListener(v -> {
            markOnboardingFinished();
            navigateToDashboard();
        });
    }

    // التصميمات التي ستظهر
    private void setupOnboardingItems() {
        List<Onboarding> items = new ArrayList<>();
        items.add(new Onboarding(R.drawable.spending, "Track Your Spending"));
        items.add(new Onboarding(R.drawable.cost, "Manage Your Budget"));
        items.add(new Onboarding(R.drawable.bookkeeping, "Monitor Your Finances"));
        onboardingAdapter = new OnboardingAdapter(items);
    }

    // تخزين ان المستخدم شاهد الترحيبات عند فتح التطبيق لاول مرة فلا تظهر مرة اخرى
    private void markOnboardingFinished() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("open_First_time", false);
        editor.apply();
    }

    // فتح الواجهة الرئيسية بعد الانتهاء من الترحيب
    private void navigateToDashboard() {
        startActivity(new Intent(this, SignUpActivity.class));
        finish();
    }
}