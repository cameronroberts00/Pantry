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
import android.widget.ImageView;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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
  //  private Button priority;
    private ImageView priorityButton;

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
       // priority=view.findViewById(R.id.priority);
        loading=view.findViewById(R.id.loading);
        recipeRecycler=view.findViewById(R.id.recipe_recycler);
        volleyError=view.findViewById(R.id.volleyError);
        volleyErrorText=view.findViewById(R.id.volleyErrorText);
        priorityButton=view.findViewById(R.id.priority_button);
        priorityButton.setBackgroundResource(R.mipmap.tickboxunticked_foreground);

        refresh.setOnClickListener(listener);
      //  priority.setOnClickListener(listener);
        priorityButton.setOnClickListener(listener);
        mRecipeList = new ArrayList<>();
        checkTimeout();//count to 10 seconds and if content isnt loaded offer user a refresh button and tell em to turn internet on
            loadData();//get data from shared prefs
            prepareData();//get everything formatted right and stuff



      // Log.d("TAG", "Formatted url is: " +url);


        return view;
        }

        private void prepareData(){
            if(mIngredientList.size()!=0){//check user has items in their storage before api request
                String formatedIngredients=getIngredientString();//Gets a ",+" formatted + concatenated string from user's storage items
                String url=getUrl(formatedIngredients);
                getRecipes(url);
                empty.setVisibility(View.INVISIBLE);
            }else{//No items in storage
                loading.setVisibility(View.INVISIBLE);
                empty.setVisibility(View.VISIBLE);
                Log.d("TAG", "No items found!");
            }

        }

    private void checkTimeout(){
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!loaded&&mIngredientList.size()!=0){
                    //if content hasnt loading in 10 seconds, tell user to refresh, dont show if nowt in ingredient list anyway as refresh wouldnt fix that
                    Log.d("TAG", "Hasn't loaded in 10 seconds, offer refresh.");
                    loading.setVisibility(View.INVISIBLE);
                    noResults.setVisibility(View.INVISIBLE);
                    timeout.setVisibility(View.VISIBLE);
                }
            }
        }, 10000);
    }


public boolean prioritise=false;
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.refresh://user timed out, this reloads frag
                    Fragment recipes = new Recipes();
                    AppCompatActivity reload = (AppCompatActivity) view.getContext();
                    reload.getSupportFragmentManager().beginTransaction().replace(R.id.frame, recipes).addToBackStack(null).commit();
                    break;
                case R.id.priority_button:
                    recipeRecycler.setVisibility(View.INVISIBLE);
                    loading.setVisibility(View.VISIBLE);
                    noResults.setVisibility(View.INVISIBLE);
                    prioritise=!prioritise;//if user wants to prioritise stuff that expires first
                    if(prioritise){
                        priorityButton.setBackgroundResource(R.mipmap.tickboxticked_foreground);
                    }else{
                        priorityButton.setBackgroundResource(R.mipmap.tickboxunticked_foreground);
                    }
                    mRecipeList = new ArrayList<>();
                    loadData();
                    prepareData();
                    //prioritising happens in getIngredientString

                    //get
                    Log.d("TAG", "Prioritising almost expired stuff: "+prioritise);
                    break;
            }
        }
    };

    private void loadData() {


        SharedPreferences sharedPreferences = getContext().getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("ingredient list", null);
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

    //This method handles whether user has priority mode on or off too.
    private String getIngredientString(){
        String ingredientName="";//This holds all the ingredients concatenated
        String formatedIngredients="";
        if(prioritise){//if user wants to prioritise results on items expiring soon, send the data off to get checked
           // formatedIngredients ="";   //todo remove this boy
            try {
                //Get the current date
                Calendar calendar= Calendar.getInstance();//make new calendar and set its date to now
                SimpleDateFormat newDateFormat= new SimpleDateFormat("dd-MM-yyyy");
                String thisDate = newDateFormat.format(calendar.getTime());
                Date currentDate = new SimpleDateFormat("dd-MM-yyyy").parse(thisDate);
                Log.d("TAG", "Day"+String.valueOf(currentDate));

                //Get a date in 3 days time
                calendar.add(Calendar.DATE, 3);//add 3 days to range
                String endRangeString=newDateFormat.format(calendar.getTime());//this is the end range of dates we're chcking
                Date endRange = new SimpleDateFormat("dd-MM-yyyy").parse(endRangeString);

                for (int i=0;i<mIngredientList.size();i++){//Go through each ingredient
                    Log.d("TAG", "Going thru ingredients");
                    Date bestBy=new SimpleDateFormat("dd-MM-yyyy").parse(mIngredientList.get(i).getBestByDate());//get best by for each product
                    if(bestBy.before(currentDate)||bestBy.equals(currentDate)||bestBy.after(currentDate)&&bestBy.before(endRange)){  //if best by has passed or it is in less than 3 days time, get the product
                        ingredientName=ingredientName+mIngredientList.get(i).getName()+",+";//chuck the product on the big ol string that gets sent to the api
                        formatedIngredients = ingredientName.substring(0, ingredientName.length() - 2);//Trim off the last ",+"
                        Log.d("TAG", "Priority Ingredient: "+ingredientName);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                Log.d("TAG", "getIngredientString: Couldn't parse priority dates!");
                //If the user ends up in this catch, they will see the default "no items found!" screen until they disable priority mode.
            }
        }else{//if user isnt prioritising items that expire soon, just send their entire storeroom off to recipe getter thing (this is useful to not flood recipes with stuff that uses long shelf life items)
            for(int i=0;i<mIngredientList.size();i++) {
                ingredientName=ingredientName+mIngredientList.get(i).getName()+",+";
                Log.d("TAG", "Non priority Ingredient: "+ingredientName);

                formatedIngredients = ingredientName.substring(0, ingredientName.length() - 2);//Trim off the last ",+"

            }
        }
        Log.d("TAG", "formated ingredients"+ingredientName);
     //   try {//Sometimes, if loads of gibberish or loads of 1 letter items are in the storeroom, getting to here crashes the app. Used normally there shouldnt be a load of 1 letter products, but just in case, this "catch" will automatically just show a "No recipes found!" screen

            Log.d("TAG", "Final ingredient string: "+formatedIngredients);
     //   }catch (Exception e){
        //    e.printStackTrace();
     //   }


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
                  recipeRecycler.setVisibility(View.VISIBLE);
                  if(response.toString().equals("[]")){   //show user feedback if no results come back. (this can happen if theres only a few obscure or gibberish items in storage)
                     noResults.setVisibility(View.VISIBLE);
                      recipeRecycler.setVisibility(View.INVISIBLE);
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
//todo when submitting remove my name here
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
