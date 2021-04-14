package com.example.pantry;

public class ShoppingListItem {
    private String mName;
   // private String mAisle;
    private String mImageUrl;
    public ShoppingListItem(String name,/*String aisle,*/ String imageUrl) {
        mName = name;
        mImageUrl=imageUrl;
      //  mAisle=aisle;
    }
    public String getName() {
        return mName;
    }
    public String getImage(){
        return  mImageUrl;
    }
//    public String getAisle(){return mAisle;}

}
