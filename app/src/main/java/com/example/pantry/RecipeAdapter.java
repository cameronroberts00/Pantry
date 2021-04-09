package com.example.pantry;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ExampleViewHolder> {
    public ArrayList<RecipeItem>mRecipeList;
    private Context mContext;
    public CardView recipeContainer;
    private RequestQueue mQueue;
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
recipeContainer=itemView.findViewById(R.id.recipe_container);
            mQueue = Volley.newRequestQueue(mContext);


        }
    }

    public RecipeAdapter(ArrayList<RecipeItem> RecipeList, Context context){
        mContext=context;
        mRecipeList=RecipeList;
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
        if (mRecipeList.get(position).getMissing().size()!=0) {
            holder.missedIngredients.setText("");
            for (int i = 0; i < mRecipeList.get(position).getMissing().size(); i++) {

                holder.missedIngredients.append(mRecipeList.get(position).getMissing().get(i));
                if(i!=mRecipeList.get(position).getMissing().size()-1){
                    holder.missedIngredients.append(", ");
                }
                Log.d("TAG", mRecipeList.get(position).getTitle()+" is missing " + mRecipeList.get(position).getMissing().get(i));
            }
        }else{
            holder.missedIngredients.setText("None!");
        }
        Log.d("TAG", mRecipeList.get(position).getUsing().toString());
        //display the ingredients getting used
        if (mRecipeList.get(position).getUsing().size()!=0) {
            holder.usedIngredients.setText("");
            for (int i = 0; i < mRecipeList.get(position).getUsing().size(); i++) {
                holder.usedIngredients.append(mRecipeList.get(position).getUsing().get(i) );
                if(i!=mRecipeList.get(position).getUsing().size()-1){
                    holder.usedIngredients.append(", ");//Only add a comma to items if it isnt the last one. Stops lists going like: Item, item, item,
                }


                Log.d("TAG", mRecipeList.get(position).getTitle()+" is missing " + mRecipeList.get(position).getUsing().get(i));
            }
        }else{
            holder.usedIngredients.setText("None!");
        }


        // holder.usedIngredients.setText();
        // Log.d("TAG", "Loading image: "+currentItem.getmImageUrl());
       // if(holder.tipBody.getText().length()>=50){
      //      clipBody(holder);//clip the text body if its too long
       // }

        recipeContainer.setOnClickListener(new View.OnClickListener() {//if a tip is clicked, open it
            @Override
            public void onClick(View view) {
                Log.d("TAG", "clicked "+holder.getAdapterPosition()+" "+currentItem.getTitle()+" "+currentItem.getId());
                getWebUrl(currentItem.getId());
              //  openTip(position,holder,view);
                //todo get url from id here then open it
            }
        });
        // holder.productBestByDate.setText(currentItem.getBestByDate());
        // holder.expired.setVisibility(View.INVISIBLE);//Recyclerviews automatically set everything to visible, manually set each expiry warning to invisible
        // Log.d("TAG", "Tip name"+currentItem.getName());
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
