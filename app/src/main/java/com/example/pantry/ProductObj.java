package com.example.pantry;

import java.util.ArrayList;

//Class not used
public class ProductObj {
    private ArrayList<String> product = new ArrayList<>();

    private String nameHere;
    private String categoryHere;
    private String bestbyHere;


    public void addName(String name) {
        nameHere = name;//Make the value of name the value of name in the Object.
        product.add(nameHere);
        saveProduct();
    }

    public void addCategory(String category) {
        categoryHere = category;
        product.add(categoryHere);
        saveProduct();
    }

    public void addBestBy(String bestby) {
        bestbyHere = bestby;
        product.add(bestbyHere);
        saveProduct();
    }

    public String getName() {
        return nameHere;
    }

    public String getCategory() {

        return categoryHere;
    }

    public String getBestBy() {
        return bestbyHere;
    }

    public void saveProduct() {

    }

}
