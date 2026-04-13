package com.hindi.wazinhasad;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;  // قائمة الفئات من قاعدة البيانات
    private OnCategoryClickListener listener;

    // الضغط على العنصر
    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    // Constructor
    public CategoryAdapter(List<Category> categoryList, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    public void updateList(List<Category> newList) {  // دالة تحديث القائمة
        this.categoryList = newList;
        notifyDataSetChanged(); // جملة التحديث واعادة العرض
    }

    // ربط xml بالجافا
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    // تعبئة العناصر بالبيانات

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.tvName.setText(category.getName());

        // عرض الصورة في العنصر

        String path = category.getIconPath(); // استدعاء مسار الصورة
        if (path != null && !path.equals("default")) {  // هل المسار موجود
            File imgFile = new File(path);  // انشاء ملف لحفظ الصورة فيه
            if (imgFile.exists()) { // هل الصورة موجودة

                // تحميل الملف وضبط الصورة لتناسب العرض ( centerCrop ) ثم عرض الصورة في  ( imgIcon )
                Glide.with(holder.itemView.getContext()).load(imgFile).centerCrop().into(holder.imgIcon);
            }
            // اذا فشل تحميل الصورة بتم عرض صورة جاهزة حسب دالة اسم الفئة ( getDefaultIcon )
        } else {
            holder.imgIcon.setImageResource(getDefaultIcon(category.getName()));
        }

        // تفعيل النقرة على العنصر
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
    }

    // دالة عرض ايقونة الفئة حسب اسمها
    private int getDefaultIcon(String categoryName) {
        switch (categoryName) {
            case "Salary": return R.drawable.salary;
            case "Food": return R.drawable.food;
            case "Transport": return R.drawable.transport;
            case "Health": return R.drawable.health;
            case "Other": return R.drawable.other;
            default: return R.drawable.wallet3;
        }
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }  // حجم القائمة ( عدد العناصر )

    // حفظ العناصر هنا عند عرضها للمرة الأولى
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView tvName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.img_category_icon);
            tvName = itemView.findViewById(R.id.tv_category_name);
        }
    }
}