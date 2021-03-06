package com.example.pantry;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import javax.xml.namespace.QName;

import static android.content.Context.MODE_PRIVATE;

public class Shopping extends Fragment {
    private RequestQueue mQueue;
    View view;
    EditText input;
    TextView showSaveText;
    TextView requiredText;
    Button saveButton;
    String userInput;
    String url;
    String imageLocation = "https://spoonacular.com/cdn/ingredients_100x100/";
    String image;
    ArrayList<ShoppingListItem> mShoppingList;
    private RecyclerView mRecyclerView;
    private ShoppingListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_shopping, container, false);
        input = view.findViewById(R.id.shoppingInput);
        saveButton = view.findViewById(R.id.addButton);
        saveButton.setOnClickListener(listener);
        showSaveText = view.findViewById(R.id.saveText);
        showSaveText.bringToFront();
        requiredText = view.findViewById(R.id.required);
        mQueue = Volley.newRequestQueue(getActivity());
        loadData();
        buildRecycler();
        return view;
    }

    private void buildRecycler() {//build the first recycler
        if (mShoppingList == null) {
            mShoppingList = new ArrayList<>();
        }
        mRecyclerView = view.findViewById(R.id.recyclerview);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mAdapter = new ShoppingListAdapter(mShoppingList, getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.addButton:
                    if (!input.getText().toString().trim().isEmpty()) {//if input isnt just space characters or is empty
                        getUrl();
                        Log.d("TAG", "onClick: " + url);
                        searchItem();//search for users item to find appropriate photo and aisle info for them
                        clearField();
                        requiredText.setVisibility(View.INVISIBLE);
                    } else {
                        requiredText.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    };
    boolean gotResponse = false;

    private void searchItem() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    //  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            gotResponse = true;

                            JSONArray jsonArray = response.getJSONArray("results");//get the items array from the returned object
                            Log.d("TAG", "onResponse: Got reponse" + jsonArray.toString());//Show full reply in console
                            if (jsonArray.toString().equals("[]")) {
                                Log.d("TAG", "Nothing found for item");
                                //image=" ";//this image=null is received in adapter to set a default image
                            } else {
                                JSONObject childObject = jsonArray.getJSONObject(0);//Just grab the first child as this will be what matches user's search best
                                image = imageLocation + childObject.getString("image");
                                Log.d("TAG", "Item logged as\nName: " + userInput + " " + "\nImage: " + image);
                            }

                        } catch (JSONException ex) {//for some reason, the try failed.
                            ex.printStackTrace();
//image=" ";
                        }
                        //    addItem(userInput, image);//these calls have to be duplicated as they cant go at the end because they need to only be called on a response
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {//This will be triggered by API not finding product
                Log.d("TAG", "onErrorResponse: " + error);
                //  image=" ";
                // addItem(userInput, image);
            }
        });

        mQueue.add(request);//add the call to the volley queue
        new CountDownTimer(500, 100) {//This gives half a second for the response to come back so we arent waiting ages to get an image (i.e. no internet ). if image goes thru as null (i.e. internet is down), a little trolley image will appear instead. This isnt in the error func above as timeout error comes a lot later than .5s
            public void onTick(long millisUntilFinished) {
                //necessary method in countdown timer thats called each interval
            }

            @Override
            public void onFinish() {
                addItem(userInput, image);
            }
        }.start();
        Log.d("TAG", "User input sent from seachItem() as " + userInput + " " + image);
    }

    private void getUrl() {
        String urlStart = "https://api.spoonacular.com/food/ingredients/search?query=";
        String urlEnd = "&apiKey=c3fd51aacc404bf4b88e83bdca4c5f11";
        userInput = input.getText().toString();
        Log.d("TAG", "User input is " + userInput);
        url = urlStart + userInput + urlEnd;
    }

    private void addItem(String name, String image) {
        // image="";//image is gotten in the adapter class, just pass an empty image for now
        mShoppingList.add(new ShoppingListItem(name, image));
        Log.d("TAG", "Item actually inserted as " + mShoppingList.get(mAdapter.getItemCount() - 1).getName() + mShoppingList.get(mAdapter.getItemCount() - 1).getImage());
        mAdapter.notifyItemInserted(mAdapter.getItemCount());
        save();//do save
    }

    private void save() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mShoppingList);
        editor.putString("shopping list", json);
        editor.apply();
        image = null;
        showSave();//show save text
    }

    private void clearField() {
        input.setText("");
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("shopping list", null);
        Type type = new TypeToken<ArrayList<ShoppingListItem>>() {
        }.getType();
        mShoppingList = gson.fromJson(json, type);
        if (mShoppingList == null) {
            mShoppingList = new ArrayList<>();
        }
        // sharedPreferences.edit().remove("shopping list").commit();
    }

    private void showSave() {
        showSaveText.setVisibility(View.VISIBLE);
        new CountDownTimer(1000, 100) {
            public void onTick(long millisUntilFinished) {
                //necessary method in countdown timer thats called each interval
            }

            @Override
            public void onFinish() {
                showSaveText.setVisibility(view.INVISIBLE);
            }
        }.start();
    }
}