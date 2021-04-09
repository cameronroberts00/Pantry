package com.example.pantry;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;


public class Recipes extends Fragment {
//todo add no internet error handling
    //todo add missed and used items to the recipes
    View view;
    private RequestQueue mQueue;
    private RecyclerView recipeRecycler;
    ArrayList<IngredientItem> mIngredientList;
    ArrayList<RecipeItem> mRecipeList;
    private RecyclerView mRecyclerView;
    private RecipeAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    boolean loaded=false;
    private LinearLayout empty;
    private LinearLayout noResults;
    private LinearLayout volleyError;
    private TextView volleyErrorText;
    private ConstraintLayout timeout;
    private ConstraintLayout loading;
    private Button refresh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_recipes, container, false);
        mQueue = Volley.newRequestQueue(getActivity());
        empty=view.findViewById(R.id.emptyContainer);
        noResults=view.findViewById(R.id.noResults);
        timeout=view.findViewById(R.id.timeout);
        refresh=view.findViewById(R.id.refresh);
        loading=view.findViewById(R.id.loading);
        volleyError=view.findViewById(R.id.volleyError);
        volleyErrorText=view.findViewById(R.id.volleyErrorText);
        refresh.setOnClickListener(listener);
        mRecipeList = new ArrayList<>();
        checkTimeout();
            loadData();


        if(mIngredientList.size()!=0){//check user has items in their storage before api request
            String formatedIngredients=getIngredientString();//Gets a ",+" formatted + concatenated string from user's storage items

            String url=getUrl(formatedIngredients);
            getRecipes(url);
            empty.setVisibility(View.INVISIBLE);
        }else{//No items in storage

            loading.setVisibility(view.INVISIBLE);
            empty.setVisibility(View.VISIBLE);
            Log.d("TAG", "No items found!");
        }

      // Log.d("TAG", "Formatted url is: " +url);


        return view;
        }
    private void checkTimeout(){
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(loaded==false){
                    //if content hasnt loading in 10 seconds, tell user to refresh
                    Log.d("TAG", "Hasn't loaded in 10 seconds, offer refresh.");
                    loading.setVisibility(View.INVISIBLE);
                    timeout.setVisibility(View.VISIBLE);
                }
            }
        }, 10000);
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.refresh://user timed out, this reloads frag
                    Fragment recipes = new Recipes();
                    AppCompatActivity reload = (AppCompatActivity) view.getContext();
                    reload.getSupportFragmentManager().beginTransaction().replace(R.id.frame, recipes).addToBackStack(null).commit();
                    break;
            }
        }
    };

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


    private void buildRecycler(){
        Log.d("TAG", "in recycler builder ");
        if (mRecipeList == null) {
            mRecipeList = new ArrayList<>();
        }
        mRecyclerView=view.findViewById(R.id.recipe_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        mAdapter = new RecipeAdapter(mRecipeList,getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    private String getIngredientString(){
        String ingredientName="";//This holds all the ingredients concatenated
        for(int i=0;i<mIngredientList.size();i++) {
            ingredientName=ingredientName+mIngredientList.get(i).getName()+",+";
            Log.d("TAG", "Ingredient: "+ingredientName);
        }
        //This string holds the concatenated string minus not needed ending
        String formatedIngredients =  ingredientName.substring(0, ingredientName.length() - 2);//Trim off the last ",+"

        Log.d("TAG", "Final ingredient string: "+formatedIngredients);
        return formatedIngredients;
    }

    private String getUrl(String formatedIngredients){
        String urlStart=" https://api.spoonacular.com/recipes/findByIngredients?number=";

        //Controls results returned
       int resultNumber=25;

       //Controls how many used ingredients are returned
        String ranking="&ranking=";
        int rankingNumber=2;//1 = maximise used ingredients. 2= minimise ingredients that are missing

        String ingredientsAre="&ingredients=";
    //    formatedIngredients;
String apiKey="&apiKey=c3fd51aacc404bf4b88e83bdca4c5f11";


        return urlStart + resultNumber + ranking + rankingNumber + ingredientsAre +formatedIngredients + apiKey;
    }

   private void getRecipes(String url){
       JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
               Request.Method.GET,
              url,
               null,
               new Response.Listener<JSONArray>() {
                   @Override
                   public void onResponse(JSONArray response) {
                       Log.d("TAG", response.toString());
                  loaded=true;
                  loading.setVisibility(View.INVISIBLE);
                  if(response.toString().equals("[]")){   //show user feedback if no results come back. (this can happen if theres only a few obscure or gibberish items in storage)
                     noResults.setVisibility(View.VISIBLE);
                      Log.d("TAG", "It's likely there were no results");
                  }else {//if the response is more than empty brackets, proceed to get stuff out of it
                      noResults.setVisibility(View.INVISIBLE);
                      try {
                          for (int i = 0; i < response.length(); i++) {
                              JSONObject currentRecipe = response.getJSONObject(i);
                              String id = currentRecipe.getString("id");
                              String title = currentRecipe.getString("title");
                              String image = currentRecipe.getString("image");
                              ArrayList<String> missing = new ArrayList<>();
                              ArrayList<String> used = new ArrayList<>();

                              Log.d("TAG", id + " " + title + " " + image + "\n");


                              //find matching ingredients
                              JSONArray usedArray = currentRecipe.getJSONArray("usedIngredients");
                              for (int j = 0; j < usedArray.length(); j++) {
                                  JSONObject childObject = usedArray.getJSONObject(j);
                                  String name = childObject.getString("name");
                                  Log.d("TAG", "Uses ingredients:" + name);
                                  used.add(name);
                              }
                              //find missing ingredients
                              JSONArray missedArray = currentRecipe.getJSONArray("missedIngredients");
                              for (int l = 0; l < missedArray.length(); l++) {
                                  JSONObject childObject = missedArray.getJSONObject(l);
                                  String name = childObject.getString("name");
                                  Log.d("TAG", "Misses ingredients:" + name);
                                  missing.add(name);
                              }

                              mRecipeList.add(new RecipeItem(id, title, image, missing, used));


                              //How to extract missing items:
/*
                             //  i=position
                               for(int p=0;p<mRecipeList.get(i).getMissing().size();p++){
                                   Log.d("TAG", "onResponse: "+mRecipeList.get(i).getMissing().get(p));
                               }
*/
                              buildRecycler();
                          }
                      } catch (JSONException e) {//couldn't take recipe's attributes
                          e.printStackTrace();

                      }
                  }
                   }
               },
               new Response.ErrorListener(){
                   @Override
                   public void onErrorResponse(VolleyError error){
error.printStackTrace();
try {//Check for known errors (freemium api limit reached etc)
    String responseBody = new String(error.networkResponse.data, "utf-8");
    JSONObject data = new JSONObject(responseBody);
    String errorCode = data.getString("code");
    Log.d("TAG", errorCode);
    if(errorCode.equals("402")){
        Log.d("TAG", "error 402, api limit reached");
    }

    loaded=true;
    loading.setVisibility(View.INVISIBLE);
    volleyError.setVisibility(View.VISIBLE);
    volleyErrorText.setText("Oh no, please try the action again.\n" +
            "If the problem persists drop me an email at \ncjr555@york.ac.uk\nPlease include this error code: "+errorCode);

}catch (Exception e){
e.printStackTrace();
}
                      Log.d("TAG", "It's likely there were no results!");
                   }
               }
       );
      mQueue.add(jsonArrayRequest);

   }

    @Override
    public void onPause() {
        super.onPause();
    //    saveData();
    }
}








/*
    //  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onResponse(JSONObject response) {
        try {
            //  Log.d("TAG", "onResponse: "+response);
            //      loading.setVisibility(View.INVISIBLE);
            loaded=true;
            //     content.setVisibility(View.VISIBLE);//hide content while its just empty
            //  JSONArray jsonArray = response.getJSONArray("recipes");//get the items array from the returned object
            //   Log.d("TAG", "onResponse: Got reponse"+jsonArray.toString());//Show full reply in console

            Log.d("TAG", "response object: "+response);
/*
                            //this for loop iterates the array and accesses all the attributes of each individual item
                            for(int i =0; jsonArray.length()>i;i++){
                                JSONObject childObject = jsonArray.getJSONObject(i);
                              String  id = childObject.getString("id");
                                String title = childObject.getString("title");
                             String imageUrl = childObject.getString("image");
                              //  category=childObject.getString("category");
                               // Log.d("TAG", "JSON tip "+i+" acquired as "+name+" "+body+" "+imageUrl+" "+category);

                            }


        } catch (Exception ex) {//for some reason, the try failed.
            ex.printStackTrace();

        }
    }
}, new Response.ErrorListener() {
@Override
public void onErrorResponse(VolleyError error) {//This will be triggered by API not finding product
        Log.d("TAG", "onErrorResponse: "+error);
        }
        });
        */





    /*
    private void buildRecyclerView() {
        mRecyclerView = view.findViewById(R.id.recyclerview);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new IngredientAdapter(mIngredientList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }*/
 /*   private void setInsertButton() {
        Button buttonInsert = view.findViewById(R.id.button_insert);
        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText line1 = view.findViewById(R.id.edittext_line_1);
                EditText line2 = view.findViewById(R.id.edittext_line_2);
                insertItem(line1.getText().toString(), line2.getText().toString());
            }
        });
    }*/
//Going to add item
   /* private void insertItem(String line1, String line2, String line3) {
        mIngredientList.add(new IngredientItem(line1, line2,line3));
        mAdapter.notifyItemInserted(mIngredientList.size());
    }*/
