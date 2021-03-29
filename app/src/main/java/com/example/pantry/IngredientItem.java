package com.example.pantry;

public class IngredientItem {
    private String mName;
    private String mCategory;
    private String mBestbyDate;
    public IngredientItem(String name, String category, String bestby) {
        mName = name;
        mCategory = category;
        mBestbyDate=bestby;
    }
    public String getName() {
        return mName;
    }
    public String getCategory() {
        return mCategory;
    }
    public String getBestByDate(){
        return mBestbyDate;
    }


}
