package com.example.pantry;

public class TipBlogItem {
    private String mName;
    private String mCategory;
    private String mBody;
    private String mImageUrl;

    public TipBlogItem(String name, String category, String body, String imageUrl) {
        mName = name;
        mCategory = category;
        mBody=body;
        mImageUrl=imageUrl;
    }
    public String getName() {
        return mName;
    }
    public String getCategory() {
        return mCategory;
    }
    public String getBody(){
        return mBody;
    }
    public String getmImageUrl(){
        return  mImageUrl;
    }


}
