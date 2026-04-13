package com.hindi.wazinhasad;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {


    // البيانات و Constructor
    private List<Onboarding> onboarding;

    public OnboardingAdapter(List<Onboarding> onboarding) {
        this.onboarding = onboarding;
    }

    // تحويل التصميم لكود جافا
    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding_page, parent, false);
        return new OnboardingViewHolder(view);
    }

    // ربط البيانات بالعناصر
    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.bind(onboarding.get(position));
    }

    @Override
    public int getItemCount() {
        return onboarding.size();
    } // حجم وعدد العناصر

    // حفظ العناصر لاول مرة
    class OnboardingViewHolder extends RecyclerView.ViewHolder {
        private TextView textTitle;
        private ImageView imageOnboarding;

        OnboardingViewHolder(@NonNull View view) {
            super(view);
            textTitle = view.findViewById(R.id.tv_title);
            imageOnboarding = view.findViewById(R.id.img_onboarding);
        }

        // تعبئة البيانات
        void bind(Onboarding item) {
            textTitle.setText(item.getTitle());
            imageOnboarding.setImageResource(item.getImage());
        }
    }
}