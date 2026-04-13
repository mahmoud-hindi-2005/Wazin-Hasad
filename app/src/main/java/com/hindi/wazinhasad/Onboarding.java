package com.hindi.wazinhasad;

public class Onboarding {
    private int image;  // معرف الصورة
    private String title;  // العنوان

    // Constructor
    public Onboarding(int image, String title) {
        this.image = image;
        this.title = title;
    }

    // Getters
    public int getImage() { return image; }
    public String getTitle() { return title; }
}