package com.example.pantry;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ExampleViewHolder> {
    public ArrayList<IngredientItem> mIngredientList;
    public class ExampleViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextViewLine1;
        public TextView mTextViewLine2;
        final public Button deleteButton;
        public ExampleViewHolder(View itemView) {
            super(itemView);
            mTextViewLine1 = itemView.findViewById(R.id.textview_line1);
            mTextViewLine2 = itemView.findViewById(R.id.textview_line_2);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            deleteButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    removeIngredient(getAdapterPosition());//Remove ingredient on button click for location of current recycler thing

                }
            });
        }
    }
public void removeIngredient(int position){
        mIngredientList.remove(position);
        notifyItemRemoved(position);
        notifyItemChanged(position);


        //TODO initiate list save here
}


    public IngredientAdapter(ArrayList<IngredientItem> ingredientList) {
        mIngredientList = ingredientList;
    }
    @Override
    public ExampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ingredient_item, parent, false);
        ExampleViewHolder evh = new ExampleViewHolder(v);
        return evh;
    }
    @Override
    public void onBindViewHolder(ExampleViewHolder holder, int position) {
       IngredientItem currentItem = mIngredientList.get(position);
        holder.mTextViewLine1.setText(currentItem.getName());
        holder.mTextViewLine2.setText(currentItem.getCategory());
    }
    @Override
    public int getItemCount() {
        return mIngredientList.size();
    }
}