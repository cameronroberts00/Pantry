package com.example.pantry;

import android.util.Log;

//
//Class not used, see category sorter in AddRecipe
//
//
//public class CategorySorter {
//int bestby;
//String category;
//    public String  getCategory( String categoryUppercased){
//        Log.d("TAG", "Received category of: "+categoryUppercased);
//        //This is a category sorter. when called in for loop it goes thru all product categories and finds most identifiable one. For example, If a product has: "Milk, Vegan, Dairy" as categories, obviously milk is the main identifiable one for a human.
//        switch (categoryUppercased){
//            case "MILK":
//                bestby=-7;
//                category=categoryUppercased;//swap categoryuppercased into category. For example, this puts value of "milk"  into category. Because saying categoryUppercased after switch will show the first category on the product, not the main identifying one which is milk. itd show "Vegan" etc. this is because we are going through a for loop, and 1st item might not be correct category
//                break;
//            default:
//                //dont put owt here as it is called for every category in the category array (the api provides many categories)
//                break;
//        }
//
//return category;
//    }
//
//}
