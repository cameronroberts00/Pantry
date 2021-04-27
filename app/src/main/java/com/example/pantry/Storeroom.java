package com.example.pantry;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class Storeroom extends Fragment {

    View view;
    View emptyContainer;
    ArrayList<IngredientItem> mIngredientList;
    private RecyclerView mRecyclerView;
    private IngredientAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_storeroom, container, false);
        emptyContainer = view.findViewById(R.id.emptyContainer);
        loadData();
        buildRecyclerView();
        // setInsertButton();
       /* Button buttonSave = view.findViewById(R.id.button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });*/
        return view;
    }


    void saveData() {//To add recipe
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mIngredientList);
        editor.putString("ingredient list", json);
        editor.apply();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("ingredient list", null);
        Type type = new TypeToken<ArrayList<IngredientItem>>() {
        }.getType();
        mIngredientList = gson.fromJson(json, type);
        if (mIngredientList == null) {
            mIngredientList = new ArrayList<>();
        }
        isEmpty();//check to see if storeroom is empty on open, if sso, give them a hint to add items
    }

    private void isEmpty() {
        if (mIngredientList.size() <= 0) {

            Log.d("TAG", "Nowt here");
            //TODO load graphic that shows its empty
            emptyContainer.setVisibility(View.VISIBLE);
        }
    }

    private void sortExpireFirst(){
        Collections.sort(mIngredientList, new DateSort());

        new CountDownTimer(3000, 100) {
            public void onTick(long millisUntilFinished) {
                //necessary method in countdown timer thats called each interval
            }
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onFinish() {
                sortExpireLast();
            }
        }.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sortExpireLast(){
        Collections.sort(mIngredientList, new DateSort().reversed());
        mAdapter.notifyDataSetChanged();
    }

static class DateSort implements Comparator<IngredientItem>{
    @Override
    public int compare(IngredientItem ingredientItem, IngredientItem t1) {
SimpleDateFormat original = new SimpleDateFormat("dd-MM-yyyy");//original European looking date
SimpleDateFormat reversed = new SimpleDateFormat("yyyy-MM-dd");//International date format (needed to sort dates properly)
Date dateIngredient1 = null;//Holds date of first item being compared
Date dateIngredient2=null;//Holds date of seccond item being compared

try {
    dateIngredient1=original.parse(ingredientItem.getBestByDate());//Place the original dates in these Date variables
    dateIngredient2=original.parse(t1.getBestByDate());
}catch (ParseException e){
    e.printStackTrace();
}
        assert dateIngredient1 != null;
        assert dateIngredient2 != null;
        //The dates are reformatted to the reversed SimpleDateFormat before they go back
        return reversed.format(dateIngredient1).compareTo(reversed.format(dateIngredient2));
    }
}
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void buildRecyclerView() {
        sortExpireFirst();//By default, show user what expires first
        mRecyclerView = view.findViewById(R.id.recyclerview);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new IngredientAdapter(mIngredientList, getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        //    testy();
    }

    /*private void setInsertButton() {
       Button buttonInsert = view.findViewById(R.id.button_insert);
        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText name = view.findViewById(R.id.edittext_line_1);
                EditText category = view.findViewById(R.id.edittext_line_2);
                insertItem(name.getText().toString(), category.getText().toString());
            }
        });
    }*/
    //Going to add item added
    /*
    private void insertItem(String name, String category) {
        mIngredientList.add(new IngredientItem(name, category));
        mAdapter.notifyItemInserted(mIngredientList.size());
    }*/


    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }
}