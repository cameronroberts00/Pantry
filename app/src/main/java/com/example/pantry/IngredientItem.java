package com.example.pantry;

public class IngredientItem {
    private String mName;
    private String mCategory;
    public IngredientItem(String name, String category) {
        mName = name;
        mCategory = category;
    }
    public String getName() {
        return mName;
    }
    public String getCategory() {
        return mCategory;
    }



}
