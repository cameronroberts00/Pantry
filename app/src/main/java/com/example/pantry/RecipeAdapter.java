package com.example.pantry;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ExampleViewHolder> {
    public ArrayList<RecipeItem>mRecipeList;
    private Context mContext;
    public CardView recipeContainer;
    public Button addToShopping;
    private RequestQueue mQueue,mQueue2;
    public ArrayList<ShoppingListItem> mShoppingList;
    public String image;//(this is image address sent as string to the shopping list for missing items)
    public class ExampleViewHolder extends RecyclerView.ViewHolder{
        public TextView recipeTitle;
        public TextView missedIngredients;
        public TextView usedIngredients;
        public ImageView recipeImage;



        public ExampleViewHolder(View itemView){
            super(itemView);
            //todo declare textviews and container here
            recipeTitle=itemView.findViewById(R.id.title);
            recipeImage=itemView.findViewById(R.id.imageView);
            missedIngredients=itemView.findViewById(R.id.missingText);
            usedIngredients=itemView.findViewById(R.id.usedText);
            addToShopping=itemView.findViewById(R.id.add_to_shopping);
recipeContainer=itemView.findViewById(R.id.recipe_container);
            mQueue = Volley.newRequestQueue(mContext);
            mQueue2 = Volley.newRequestQueue(mContext);


        }
    }

    public RecipeAdapter(ArrayList<RecipeItem> RecipeList, Context context){
        mContext=context;
        mRecipeList=RecipeList;
        loadData();
    }

    @Override
    public RecipeAdapter.ExampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item, parent, false);
        RecipeAdapter.ExampleViewHolder evh = new RecipeAdapter.ExampleViewHolder(v);


        return evh;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(final RecipeAdapter.ExampleViewHolder holder, final int position) {
        final RecipeItem currentItem = mRecipeList.get(position);


        holder.recipeTitle.setText(currentItem.getTitle());
        //holder.recipeImage.setText(currentItem.getBody());
        Glide.with(mContext).load(currentItem.getImage()).into(holder.recipeImage);


//display the ingredients missiing
        if (mRecipeList.get(position).getMissing().size() != 0) {
            holder.missedIngredients.setText("");
            for (int i = 0; i < mRecipeList.get(position).getMissing().size(); i++) {

                holder.missedIngredients.append(mRecipeList.get(position).getMissing().get(i));
                if (i != mRecipeList.get(position).getMissing().size() - 1) {
                    holder.missedIngredients.append(", ");
                }
                Log.d("TAG", mRecipeList.get(position).getTitle() + " is missing " + mRecipeList.get(position).getMissing().get(i));
            }
        } else {
            holder.missedIngredients.setText("None!");
        }
        Log.d("TAG", mRecipeList.get(position).getUsing().toString());
        //display the ingredients getting used
        if (mRecipeList.get(position).getUsing().size() != 0) {
            holder.usedIngredients.setText("");
            for (int i = 0; i < mRecipeList.get(position).getUsing().size(); i++) {
                holder.usedIngredients.append(mRecipeList.get(position).getUsing().get(i));
                if (i != mRecipeList.get(position).getUsing().size() - 1) {
                    holder.usedIngredients.append(", ");//Only add a comma to items if it isnt the last one. Stops lists going like: Item, item, item,
                }


                Log.d("TAG", mRecipeList.get(position).getTitle() + " is missing " + mRecipeList.get(position).getUsing().get(i));
            }
        } else {
            holder.usedIngredients.setText("None!");
        }


        // holder.usedIngredients.setText();
        // Log.d("TAG", "Loading image: "+currentItem.getmImageUrl());
        // if(holder.tipBody.getText().length()>=50){
        //      clipBody(holder);//clip the text body if its too long
        // }

        //There's two onclick listeners here instead of one just because it really was not having a fun time with 1 listener and a switch statement
        recipeContainer.setOnClickListener(new View.OnClickListener() {//if a tip is clicked, open it
            @Override
            public void onClick(View view) {
                Log.d("TAG", "clicked " + holder.getAdapterPosition() + " " + currentItem.getTitle() + " " + currentItem.getId());
                getWebUrl(currentItem.getId());
                //  openTip(position,holder,view);

            }
        });

        addToShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TAG", "Adding to shopping list: "+currentItem.getMissing());
                addMissingToShopping(currentItem);
            }
        });
        // holder.productBestByDate.setText(currentItem.getBestByDate());
        // holder.expired.setVisibility(View.INVISIBLE);//Recyclerviews automatically set everything to visible, manually set each expiry warning to invisible
        // Log.d("TAG", "Tip name"+currentItem.getName());

    }

    private void addMissingToShopping(RecipeItem currentItem){
        //This grabs all the missing items off the current item (as user clicked it), then takes them apart so theyre individual items and adds them to their shopping list
        currentItem.getMissing();
        String[] splitMissing=currentItem.getMissing().toString().replace("[","").replace("]","").split(", ");
        for(int i=0;i<splitMissing.length;i++){
            getImage(splitMissing[i]);
        }

        save();//save the shopping list
    }


    private void getImage(String i){
        final String name=i;

        String urlStart="https://api.spoonacular.com/food/ingredients/search?query=";
        String urlEnd="&apiKey=c3fd51aacc404bf4b88e83bdca4c5f11";
        String url;
        url=urlStart+name+urlEnd;
        final String imageLocation="https://spoonacular.com/cdn/ingredients_100x100/";
        Log.d("TAG", "Requesting missing items as: "+i+"\n"+url);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        //  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                //  loading.setVisibility(View.INVISIBLE);
                                // loaded = true;
                                //     content.setVisibility(View.VISIBLE);//hide content while its just empty

                                JSONArray jsonArray = response.getJSONArray("results");//get the items array from the returned object
                                Log.d("TAG", "onResponse: Got reponse" + jsonArray.toString());//Show full reply in console
                                if(jsonArray.toString().equals("[]")){
                                    Log.d("TAG", "Nothing found for item");
                                    image=" ";//this image=null is received in adapter to set a default image
                                }else {
                                    JSONObject childObject = jsonArray.getJSONObject(0);//Just grab the first child as this will be what matches user's search best
                                    image = imageLocation + childObject.getString("image");
                                    Log.d("TAG", "Item logged as\nName: " + name + " " + "\nImage: " + image);
                                }
                                Log.d("TAG", "User input added as "+name+" "+image);
                              //  addItem(userInput, image);
                                mShoppingList.add(new ShoppingListItem(name,image));
                                save();
                            } catch (JSONException ex) {//for some reason, the try failed.
                                ex.printStackTrace();

                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {//This will be triggered by API not finding product
                    Log.d("TAG", "onErrorResponse: " + error);
                }
            });
            mQueue2.add(request);//add the call to the volley queue

    }










    private void save(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mShoppingList);
        editor.putString("shopping list", json);
        editor.apply();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("shopping list", null);
        Type type = new TypeToken<ArrayList<ShoppingListItem>>() {}.getType();
        mShoppingList = gson.fromJson(json, type);
        if (mShoppingList == null) {
            mShoppingList = new ArrayList<>();
        }
        // sharedPreferences.edit().remove("shopping list").commit();
    }

    @Override
    public int getItemCount() {
        return mRecipeList.size();

    }

    public void getWebUrl(String itemId){
        Log.d("TAG", "Searching for product URL with item id: "+itemId);
        String urlStart="https://api.spoonacular.com/recipes/";
        String urlEnd="/information?includeNutrition=false";
        String key="&apiKey=c3fd51aacc404bf4b88e83bdca4c5f11";
        String url=urlStart+itemId+urlEnd+key;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", "Sending request");
                       // loaded=true;
                        try{
                                String recipeUrl = response.getString("sourceUrl");
                                Log.d("TAG", "Loading recipe url: "+recipeUrl);

                                //once web url is gotten, open new intent into web browser
                                Intent j = new Intent(Intent.ACTION_VIEW);
                                j.setData(Uri.parse(recipeUrl));
                                mContext.startActivity(j);
                        }catch (Exception e){//couldn't take recipe's attributes
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        error.printStackTrace();
                    }
                }
        );
        mQueue.add(jsonObjectRequest);
    }
}
