package com.example.pantry;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;


public class Recipes extends Fragment {

    View view;

    ArrayList<IngredientItem> mExampleList;
    private RecyclerView mRecyclerView;
    private IngredientAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_recipes, container, false);



            loadData();
            buildRecyclerView();
            setInsertButton();
            Button buttonSave = view.findViewById(R.id.button_save);
            buttonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveData();
                }
            });
        return view;
        }





    void saveData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mExampleList);
        editor.putString("task list", json);
        editor.apply();
    }
    private void loadData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("task list", null);
        Type type = new TypeToken<ArrayList<IngredientItem>>() {}.getType();
        mExampleList = gson.fromJson(json, type);
        if (mExampleList == null) {
            mExampleList = new ArrayList<>();
        }
    }
    private void buildRecyclerView() {
        mRecyclerView = view.findViewById(R.id.recyclerview);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new IngredientAdapter(mExampleList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }
    private void setInsertButton() {
        Button buttonInsert = view.findViewById(R.id.button_insert);
        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText line1 = view.findViewById(R.id.edittext_line_1);
                EditText line2 = view.findViewById(R.id.edittext_line_2);
                insertItem(line1.getText().toString(), line2.getText().toString());
            }
        });
    }
    //Going to add item
    private void insertItem(String line1, String line2) {
        mExampleList.add(new IngredientItem(line1, line2));
        mAdapter.notifyItemInserted(mExampleList.size());
    }


    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }
}