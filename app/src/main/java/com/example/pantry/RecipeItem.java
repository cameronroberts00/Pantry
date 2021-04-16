package com.example.pantry;

import java.util.ArrayList;

public class RecipeItem {
    private String mId;
    private String mTitle;
    private String mImage;
    private ArrayList<String> mMissing;
    private ArrayList<String> mUsing;

    public RecipeItem(String id, String title, String image, ArrayList<String> missing, ArrayList<String> using) {
        mId = id;
        mTitle = title;
        mImage = image;
        mMissing = missing;
        mUsing = using;
    }

    public String getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getImage() {
        return mImage;
    }

    public ArrayList<String> getMissing() {
        return mMissing;
    }

    public ArrayList<String> getUsing() {
        return mUsing;
    }
}
