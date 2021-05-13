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
import android.widget.Toast;

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

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
    public ArrayList<RecipeItem> mRecipeList;
    private Context mContext;
    public CardView recipeContainer;
    public Button addToShopping;
    private ImageView heart;
    boolean isHearted=false;
    private RequestQueue mQueue, mQueue2;
    public ArrayList<ShoppingListItem> mShoppingList;
    public String image;//(this is image address sent as string to the shopping list for missing items)

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView recipeTitle;
        public TextView missedIngredients;
        public TextView usedIngredients;
        public ImageView recipeImage;

        public ViewHolder(View itemView) {
            super(itemView);
            //todo declare textviews and container here
            recipeTitle = itemView.findViewById(R.id.title);
            recipeImage = itemView.findViewById(R.id.imageView);
            missedIngredients = itemView.findViewById(R.id.missingText);
            usedIngredients = itemView.findViewById(R.id.usedText);
            addToShopping = itemView.findViewById(R.id.add_to_shopping);
            recipeContainer = itemView.findViewById(R.id.recipe_container);
            heart=itemView.findViewById(R.id.heart);
            heart.setVisibility(View.INVISIBLE);//Not using heart feature currently, set view invisible instead of deleting just so its easier to reimplement
            mQueue = Volley.newRequestQueue(mContext);
            mQueue2 = Volley.newRequestQueue(mContext);
        }
    }

    public RecipeAdapter(ArrayList<RecipeItem> RecipeList, Context context) {
        mContext = context;
        mRecipeList = RecipeList;
        loadData();
    }

    @Override
    public RecipeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item, parent, false);
        RecipeAdapter.ViewHolder evh = new RecipeAdapter.ViewHolder(v);


        return evh;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(final RecipeAdapter.ViewHolder holder, final int position) {
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

         if(holder.recipeTitle.getText().length()>=50){
             clipTitle(holder);//clip the text body if its too long
        }

        //There's 2 onclick listeners here instead of 1 just because it really was not having a fun time with 1 listener and a switch statement, it might be because its in a recyclerview, i dont know
        recipeContainer.setOnClickListener(new View.OnClickListener() {//if a tip is clicked, open it
            @Override
            public void onClick(View view) {
                Log.d("TAG", "clicked " + holder.getAdapterPosition() + " " + mRecipeList.get(holder.getAdapterPosition()).getTitle() + " " + mRecipeList.get(holder.getAdapterPosition()).getId());
               // getWebUrl(currentItem.getId());
               // getWebUrl(currentItem.getId());

                getWebUrl(mRecipeList.get(holder.getAdapterPosition()).getId());
                Log.d("TAG", "loading url for "+mRecipeList.get(holder.getAdapterPosition()).getTitle());
            }
        });
        addToShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TAG", "Adding to shopping list: " + mRecipeList.get(holder.getAdapterPosition()).getMissing());
                addMissingToShopping(mRecipeList.get(holder.getAdapterPosition()));
            }
        });

    }

    private void clipTitle(ViewHolder holder){
        String snipped = (String) holder.recipeTitle.getText();
        snipped = snipped.substring(0, 50) + "...".trim();//Truncated body text is a substring from char 0 to 50 with ... on the end and no whitespace
        holder.recipeTitle.setText(snipped);
    }

    private void addMissingToShopping(RecipeItem currentItem) {
        //This grabs all the missing items off the current item (as user clicked it), then takes them apart so theyre individual items and adds them to their shopping list
      //  currentItem.getMissing();
        if(currentItem.getMissing().size()!=0) {
            String[] splitMissing = currentItem.getMissing().toString().replace("[", "").replace("]", "").split(", ");
            for (int i = 0; i < splitMissing.length; i++) {
                getImage(splitMissing[i]);
                Log.d("TAG", "Missing saved: "+splitMissing[i]);
            }
            Toast.makeText(mContext, "Items added to your shopping list!", Toast.LENGTH_SHORT).show();
            save();//save the shopping list
        }else{
            Toast.makeText(mContext, "No items missing!", Toast.LENGTH_SHORT).show();
        }
    }

    private void getImage(String i) {
        final String name = i;
        String urlStart = "https://api.spoonacular.com/food/ingredients/search?query=";
        String urlEnd = "&apiKey=c3fd51aacc404bf4b88e83bdca4c5f11";
        String url;
        url = urlStart + name + urlEnd;
        final String imageLocation = "https://spoonacular.com/cdn/ingredients_100x100/";
        Log.d("TAG", "Requesting missing items as: " + i + "\n" + url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    //  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("results");//get the items array from the returned object
                            Log.d("TAG", "onResponse: Got reponse" + jsonArray.toString());//Show full reply in console
                            if (jsonArray.toString().equals("[]")) {
                                Log.d("TAG", "Nothing found for item");
                                image = " ";//this image=null is received in adapter to set a default image
                            } else {
                                JSONObject childObject = jsonArray.getJSONObject(0);//Just grab the first child as this will be what matches user's search best
                                image = imageLocation + childObject.getString("image");
                                Log.d("TAG", "Item logged as\nName: " + name + " " + "\nImage: " + image);
                            }
                            Log.d("TAG", "User input added as " + name + " " + image);
                            //  addItem(userInput, image);
                            mShoppingList.add(new ShoppingListItem(name, image));
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

    private void save() {
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
        Type type = new TypeToken<ArrayList<ShoppingListItem>>() {
        }.getType();
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

    public void getWebUrl(String itemId) {
        Log.d("TAG", "Searching for product URL with item id: " + itemId);
        String urlStart = "https://api.spoonacular.com/recipes/";
        String urlEnd = "/information?includeNutrition=false";
        String key = "&apiKey=c3fd51aacc404bf4b88e83bdca4c5f11";
        String url = urlStart + itemId + urlEnd + key;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", "Sending request");
                        // loaded=true;
                        try {
                            String recipeUrl = response.getString("sourceUrl");
                            Log.d("TAG", "Loading recipe url: " + recipeUrl);

                            //once web url is gotten, open new intent into web browser
                            Intent j = new Intent(Intent.ACTION_VIEW);
                            j.setData(Uri.parse(recipeUrl));
                            mContext.startActivity(j);
                        } catch (Exception e) {//couldn't take recipe's attributes
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );
        mQueue.add(jsonObjectRequest);
    }
}





       /* This heart feature just didnt really work well, when removing items from the list it would sometimes get out of bounds errors or end up in the catch from doing a Toast.makeText (??) so got removed
        heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //  Log.d("TAG", "Item hearted: "+currentItem.getTitle());
           //     heart.setBackgroundResource(R.mipmap.heartcoloured_foreground);
            //   Glide.with(mContext).load(R.mipmap.heartcoloured_foreground).into(heart);
           //     Toast.makeText(mContext,"Added "+currentItem.getTitle()+" to favourites", Toast.LENGTH_SHORT).show();
               // notifyItemRemoved(currentItem);
                try{
                   // Toast.makeText(mContext,"Added "+mRecipeList.get(holder.getAdapterPosition()).getTitle()+" to your favourites!", Toast.LENGTH_SHORT).show();
                    mRecipeList.remove(mRecipeList.get(holder.getLayoutPosition()));
                    notifyItemChanged(holder.getLayoutPosition());
                    notifyItemRemoved(holder.getLayoutPosition());
                  //  Log.d("TAG", "Item added to favourites "+mRecipeList.get(holder.getAdapterPosition()).getTitle());
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(mContext,"FAILED to add "+mRecipeList.get(holder.getAdapterPosition()).getTitle(), Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                }
            }
        });*/
// holder.productBestByDate.setText(currentItem.getBestByDate());
// holder.expired.setVisibility(View.INVISIBLE);//Recyclerviews automatically set everything to visible, manually set each expiry warning to invisible
// Log.d("TAG", "Tip name"+currentItem.getName());