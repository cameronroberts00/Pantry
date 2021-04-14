package com.example.pantry;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class Storeroom extends Fragment {

    View view;
    View emptyContainer;
    ArrayList<IngredientItem> mIngredientList;
    private RecyclerView mRecyclerView;
    private IngredientAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_storeroom, container, false);
        emptyContainer=view.findViewById(R.id.emptyContainer);


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
        Type type = new TypeToken<ArrayList<IngredientItem>>() {}.getType();
        mIngredientList = gson.fromJson(json, type);
        if (mIngredientList == null) {
            mIngredientList = new ArrayList<>();
        }
       isEmpty();//check to see if storeroom is empty on open, if sso, give them a hint to add items
    }

    private void isEmpty(){
        if (mIngredientList.size()<=0) {

            Log.d("TAG", "Nowt here");
            //TODO load graphic that shows its empty
            emptyContainer.setVisibility(View.VISIBLE);
        }
    }
    private void buildRecyclerView() {
        mRecyclerView = view.findViewById(R.id.recyclerview);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new IngredientAdapter(mIngredientList,getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    //    testy();
    }

  //  private void testy(){
   //     Log.d("TAG", "testy: "+mRecyclerView.getLayoutManager().findViewByPosition(0));
   // }

    //thid added
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