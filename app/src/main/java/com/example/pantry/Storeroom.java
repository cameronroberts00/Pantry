package com.example.pantry;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class Storeroom extends Fragment {

    View view;

    ArrayList<IngredientItem> mIngredientList;
    private RecyclerView mRecyclerView;
    private IngredientAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_storeroom, container, false);



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





    void saveData() {//To add recipe
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mIngredientList);
        editor.putString("task list", json);
        editor.apply();
    }
    private void loadData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("task list", null);
        Type type = new TypeToken<ArrayList<IngredientItem>>() {}.getType();
        mIngredientList = gson.fromJson(json, type);
        if (mIngredientList == null) {
            mIngredientList = new ArrayList<>();
        }
    }
    private void buildRecyclerView() {
        mRecyclerView = view.findViewById(R.id.recyclerview);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new IngredientAdapter(mIngredientList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    //thid added
    private void setInsertButton() {
        Button buttonInsert = view.findViewById(R.id.button_insert);
        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText name = view.findViewById(R.id.edittext_line_1);
                EditText category = view.findViewById(R.id.edittext_line_2);
                insertItem(name.getText().toString(), category.getText().toString());
            }
        });
    }
    //Going to add item added
    private void insertItem(String name, String category) {
        mIngredientList.add(new IngredientItem(name, category));
        mAdapter.notifyItemInserted(mIngredientList.size());
    }


    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }
}