package com.hindi.wazinhasad;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CategoryManagementActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private CategoryAdapter adapter;
    private ImageView btnBack;
    private TextView tvTitle;
    private WazinDatabase db;
    private SharedPreferences preferences;

    private ActivityResultLauncher<String> galleryLauncher;
    private ImageView tempDialogImageView;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // استدعاء الاعدادات المخزنة مسبقا للتطبيق
        preferences = getSharedPreferences("app_settings", MODE_PRIVATE);
        applyAppTheme();

        // ربط xml
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories_management);

        db = WazinDatabase.getDatabase(this); // فتح قاعدة البيانات

        initGalleryLauncher(); // فتح المعرض

        initViews(); // العمليات داخل الواجهة

        setupRecyclerView();  // عرض القائمة

        loadCategories();  // عرض البيانات في القائمة
    }

    // شكل الواجهة ( applyAppTheme )
    // يحدث عند فتح الواجهة فوراً
    private void applyAppTheme() {
        // هل الوضع الداكن مفعل ام لا ؟ false الوضع الافتراضي
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);
        // نعم مفعل
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); // التحويل للوضع الداكن
            getWindow().setStatusBarColor(Color.BLACK);  // اظهار شريط الحالة باللون الأسود
        // لا غير مفعل
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);  // البقاء على الوضع الإفتراضي
        }
    }


    // ربط العناصر وتشغيل الواجهة والعمليات
    private void initViews() {

        rvCategories = findViewById(R.id.rv_categories);  // قائمة الفئات
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_category); // زر اضافة فئة
        btnBack = findViewById(R.id.imageView6);  // سهم العودة
        tvTitle = findViewById(R.id.tv_title); // عنوان الواجهة

        // هل الوضع داكن ام لا ؟
        boolean isDark = preferences.getBoolean("dark_mode", false);
        View root = findViewById(android.R.id.content);  // root الواجهة الكلية

        // نعم الوضع داكن
        if (isDark) {
            root.setBackgroundColor(Color.BLACK); //  الواجهة سوداء
            btnBack.setColorFilter(Color.WHITE); // سهم العودة أبيض
            tvTitle.setTextColor(Color.WHITE);  // عنوان الواجهة أبيض

        }

        btnBack.setOnClickListener(v -> finish());  // عملية العودة
        fabAdd.setOnClickListener(v -> showAddCategoryDialog());  // عملية اضافة فئة
    }

    // ضبط القائمة
    private void setupRecyclerView() {
        rvCategories.setLayoutManager(new GridLayoutManager(this, 2)); // شبكي بعمودين
        adapter = new CategoryAdapter(new ArrayList<>(), this::showEditDeleteDialog); // عرض العناصر وعملية حدث الضغط
        rvCategories.setAdapter(adapter);
    }

    // نافذة التعديل أو الحذف
    private void showEditDeleteDialog(Category category) {

        // هل الفئة جاهزة مع التطبيق ؟ نعم . لا يمكن تعديل عليها أو حذفها
        if (category.getIconPath().equals("default")) {
            Toast.makeText(this, "Basic category cannot Edit or Deleted", Toast.LENGTH_SHORT).show();
            return;
        }

        // ربط النافذة بالكود
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_category, null);
        builder.setView(dialogView);

        // ربط العناصر بالكود
        ImageView imgEdit = dialogView.findViewById(R.id.edit_image);
        EditText etEditName = dialogView.findViewById(R.id.et_edit_category_name);
        Button btnSave = dialogView.findViewById(R.id.btn_save_category);
        Button btnDelete = dialogView.findViewById(R.id.btn_delete_category);

        etEditName.setText(category.getName()); // وضع الاسم بالحقل
        tempDialogImageView = imgEdit;  // وضع الصورة في النافذة
        selectedImageUri = null;

        // الملف موجود نستخدم مكتبة Glide لعرض الصورة
        File imgFile = new File(category.getIconPath());
        if (imgFile.exists()) {
            Glide.with(this).load(imgFile).centerCrop().into(imgEdit);
        }

        AlertDialog dialog = builder.create();

        // حدث الضغط على تغيير الصورة
        imgEdit.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        // حفظ التعديلات
        btnSave.setOnClickListener(v -> {
            String newName = etEditName.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "Please identify the name", Toast.LENGTH_SHORT).show();
                return;
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                category.setName(newName);  // تحديث الاسم
                if (selectedImageUri != null) {  // حفظ مسار الصورة الجديدة
                    category.setIconPath(copyFileToInternal(selectedImageUri));
                }
                db.categoryDao().update(category); // تحديث قاعدة البيانات
                runOnUiThread(() -> {  // العودة للواجهة
                    loadCategories();
                    dialog.dismiss();
                    Toast.makeText(this, "Edited successfully", Toast.LENGTH_SHORT).show();
                });
            });
        });

        // حذف الفئة
        btnDelete.setOnClickListener(v -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                db.categoryDao().delete(category); // حذف من القاعدة
                runOnUiThread(() -> { // العودة للواحهة
                    loadCategories();
                    dialog.dismiss();
                    Toast.makeText(this, "Successfully deleted", Toast.LENGTH_SHORT).show();
                });
            });
        });

        dialog.show();
    }

    // نافذة اضافة فئة
    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.et_category_name);  // اسم الفئة
        tempDialogImageView = dialogView.findViewById(R.id.img_select_icon);  // عرض الصورة

        selectedImageUri = null;
        tempDialogImageView.setOnClickListener(v -> galleryLauncher.launch("image/*")); // حدث الضغط على اضافة صورة

        // زر الحفظ
        builder.setPositiveButton("save", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) {
                saveCategoryProcess(name); // دالة الحفظ
            } else {
                Toast.makeText(this, "Enter nameً", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("cancel", null);
        builder.create().show();
    }

    // دالة الحفظ
    private void saveCategoryProcess(String name) {
        Executors.newSingleThreadExecutor().execute(() -> {  // العمل خارج التطبيق
            String finalPath = "default"; // مسار صورة افتراضي
            if (selectedImageUri != null) {
                finalPath = copyFileToInternal(selectedImageUri); // حفظ الصورة وعرضها
            }
            db.categoryDao().insert(new Category(name, finalPath));  // الحفظ في قاعدة البيانات
            runOnUiThread(this::loadCategories);
        });
    }

    // تخزين الصورة
    private String copyFileToInternal(Uri uri) {
        try {
            // ضمان وجود الصورة حتى لو حذفت من المعرض
            String fileName = "cat_" + System.currentTimeMillis() + ".jpg";
            File file = new File(getFilesDir(), fileName);
            // وضع الصورة في ملف لوحدها
            InputStream is = getContentResolver().openInputStream(uri);
            FileOutputStream os = new FileOutputStream(file);
            // نقل الصورة للملف
            byte[] buffer = new byte[4096];
            int read;
            while ((read = is.read(buffer)) != -1) os.write(buffer, 0, read);
            // اغلاق الملف
            os.flush(); os.close(); is.close();
            return file.getAbsolutePath();
            // غير ذلك نستخدم الايقونة الافتراضية
        } catch (Exception e) {
            return "default";
        }
    }

    // عرض الفئات اليدوية الجاهزة والمضافة
    private void loadCategories() {
        Executors.newSingleThreadExecutor().execute(() -> {  // العمل خارج التطبيق
            List<Category> userCategories = db.categoryDao().getAllCategories(); // تجهيز القائمة
            List<Category> allCategories = new ArrayList<>();

            // الفئات الجاهزة الاساسية
            allCategories.add(new Category("Salary", "default"));
            allCategories.add(new Category("Food", "default"));
            allCategories.add(new Category("Transport", "default"));
            allCategories.add(new Category("Health", "default"));
            allCategories.add(new Category("Other", "default"));

            // منع ظهور فئات بنفس الاسم
            for (Category userCat : userCategories) {
                boolean exists = false;
                for (Category defaultCat : allCategories) {
                    if (defaultCat.getName().equalsIgnoreCase(userCat.getName())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) allCategories.add(userCat);
            }

            runOnUiThread(() -> adapter.updateList(allCategories)); // تحديث القائمة
        });
    }

    // فتح المعرض والحصول على مسار الصورة فقط الان وليس كملف ( مرحلة الاولى )
    private void initGalleryLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        if (tempDialogImageView != null) {
                            Glide.with(this).load(uri).into(tempDialogImageView);
                        }
                    }
                }
        );
    }
}