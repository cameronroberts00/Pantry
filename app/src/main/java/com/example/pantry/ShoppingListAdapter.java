package com.example.pantry;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;

public class ShoppingListAdapter  extends RecyclerView.Adapter<ShoppingListAdapter.ExampleViewHolder>{
    public ArrayList<ShoppingListItem> mShoppingList;
    public Context mContext;

    public class ExampleViewHolder extends RecyclerView.ViewHolder {
       public TextView productName;
       public ImageView productPhoto;
      //  public TextView  productCategory;
       // public TextView productBestByDate;
      //  public TextView expired;
        final public Button deleteButton;
        public CardView parentCard;
       // final public Button savedItemButton;
       public Context mContext;
       public ExampleViewHolder(View itemView) {
           super(itemView);
           productPhoto=itemView.findViewById(R.id.productPhoto);
           productName=itemView.findViewById(R.id.product_name);
           deleteButton=itemView.findViewById(R.id.deleteButton);
           parentCard=itemView.findViewById(R.id.parentCard);
           parentCard.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   Log.d("TAG", "Clicked "+mShoppingList.get(getAdapterPosition()).getImage()+"\n"+mShoppingList.get(getAdapterPosition()).getName());
               }
           });
           deleteButton.setOnClickListener(listener);
       }
        View.OnClickListener listener = new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.deleteButton:
                        //called if user binned an item
                        removeItem(getAdapterPosition());
                        break;
                }
            }
        };
    }



    public ShoppingListAdapter(ArrayList<ShoppingListItem> shoppingList, Context context) {
        mContext=context;
        mShoppingList = shoppingList;

    }

    @Override
    public ShoppingListAdapter.ExampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_list_item, parent, false);
       ShoppingListAdapter.ExampleViewHolder evh = new ExampleViewHolder(v);
        return evh;
    }
ShoppingListAdapter.ExampleViewHolder mHolder;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBindViewHolder(ShoppingListAdapter.ExampleViewHolder holder, int position) {

        ShoppingListItem currentItem = mShoppingList.get(position);
mHolder=holder;
        holder.productName.setText(currentItem.getName());

        Log.d("TAG", "onBindViewHolder: "+mShoppingList.get(position).getImage());
        try{

        if(currentItem.getImage()!=null){//if we have an image for it, show it
            holder.productPhoto.setImageDrawable(null);
            Glide.with(mContext).load(currentItem.getImage()).into(holder.productPhoto);
        }else{//if theres no image found for product, show locally sourced image (in case of no internet)
            Log.d("TAG", "No photo received for "+mShoppingList.get(position).getName()+", loading default");
            //holder.productPhoto.setBackgroundResource(R.drawable.can_background);
            holder.productPhoto.setImageDrawable(null);//Not setting image to null before loading another can make photos blend together
            Glide.with(mContext).load(R.mipmap.shopping_foreground).into(holder.productPhoto);
           // notifyItemInserted(position);
        }
        }
        catch (Exception e){
            e.printStackTrace();
        }
       // holder.productCategory.setText(currentItem.getCategory());
       // holder.productBestByDate.setText(currentItem.getBestByDate());
       // holder.expired.setVisibility(View.INVISIBLE);//Recyclerviews automatically set everything to visible, manually set each expiry warning to invisible
       // checkExpiry(currentItem.getBestByDate(), position,holder);//Send each date to check if its expired when page loads
    }

    public void removeItem(int position){
        //User spam tapping "delete" button causes Array out of bounds exception and crashes app. try/catch fixes it
        try {
            mShoppingList.remove(position);
            notifyItemRemoved(position);
            notifyItemChanged(position);

            save();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("TAG", "Couldnt remove ingredient");
        }

    }



    private void save(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mShoppingList);
        editor.putString("shopping list", json);
        editor.apply();
    }

@Override
    public int getItemCount() {
        return mShoppingList.size();
    }

}
