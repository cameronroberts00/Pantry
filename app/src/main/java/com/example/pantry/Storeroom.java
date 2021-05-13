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
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Text;

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
    boolean recyclerIsBuilt = false;
    View view;
    View emptyContainer;
    TextView newestButton;
    TextView oldestButton;
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
        newestButton = view.findViewById(R.id.newestButton);
        oldestButton = view.findViewById(R.id.oldestButton);
        newestButton.setOnClickListener(listener);
        oldestButton.setOnClickListener(listener);
        loadData();
        buildRecyclerView();
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
            Log.d("TAG", "Nothing in storeroom!");

            emptyContainer.setVisibility(View.VISIBLE);
        }
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.newestButton:
                    sortExpireLast();
                    break;
                case R.id.oldestButton:
                    sortExpireFirst();
                    break;
            }
        }
    };

    //Push expiring items to top
    private void sortExpireFirst() {
        Collections.sort(mIngredientList, new DateSort());
        if (recyclerIsBuilt) {//if recycler is built, then show the changes (stops calling notifyDataSetChanged during recycler build process, causing crash)
            mAdapter.notifyDataSetChanged();
            Toast.makeText(getContext(),"Sorted by: Expired items first", Toast.LENGTH_SHORT).show();
        }
    }

    //Push freshers items to top
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sortExpireLast() {
        Collections.sort(mIngredientList, new DateSort().reversed());
        if (recyclerIsBuilt) {
            //Only notify data change + show user feedback if this isnt the first call and recycler is already built
            mAdapter.notifyDataSetChanged();
            Toast.makeText(getContext(),"Sorted by: Expired items last", Toast.LENGTH_SHORT).show();
        }
    }

    //Organise items by date
    static class DateSort implements Comparator<IngredientItem> {
        @Override
        public int compare(IngredientItem ingredientItem, IngredientItem t1) {
            SimpleDateFormat original = new SimpleDateFormat("dd-MM-yyyy");//original European looking date
            SimpleDateFormat reversed = new SimpleDateFormat("yyyy-MM-dd");//International date format (needed to sort dates properly)
            Date dateIngredient1 = null;//Holds date of first item being compared
            Date dateIngredient2 = null;//Holds date of seccond item being compared
            try {
                dateIngredient1 = original.parse(ingredientItem.getBestByDate());//Place the original dates in these Date variables
                dateIngredient2 = original.parse(t1.getBestByDate());
            } catch (ParseException e) {
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
        //When recycler has been built, just set a bool to true so we can use notifyDataSetChanged in sorting funcs
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        recyclerIsBuilt = true;
                        mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
        );
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