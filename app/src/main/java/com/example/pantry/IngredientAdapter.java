package com.example.pantry;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;
//In this class:
//this converts the arraylist from storage into a recyclerview format
//There is also date checking at the bottom
public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ExampleViewHolder> {
    public ArrayList<IngredientItem> mIngredientList;
    public class ExampleViewHolder extends RecyclerView.ViewHolder {
        public TextView productName;
        public TextView  productCategory;
        public TextView productBestByDate;
        public TextView expired;
        final public Button deleteButton;
        public ExampleViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            productCategory = itemView.findViewById(R.id.product_category);
            productBestByDate=itemView.findViewById(R.id.product_bestby);
            expired=itemView.findViewById(R.id.expired);
            deleteButton = itemView.findViewById(R.id.deleteButton);//delete button needs to go here as it removes the arraylist item and the recycler item made from the arraylist
            deleteButton.setOnClickListener(new View.OnClickListener(){
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View view) {
                    removeIngredient(getAdapterPosition());//Remove ingredient on button click for location of current recycler thing
                }
            });
        }
    }

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public void removeIngredient(int position){
        //User spam tapping "delete" button causes Array out of bounds exception and crashes app. try/catch fixes it
            try {
                mIngredientList.remove(position);

                notifyItemRemoved(position);
                notifyItemChanged(position);

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("TAG", "caught in removeIngredient func");
            }


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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(ExampleViewHolder holder, int position) {
       IngredientItem currentItem = mIngredientList.get(position);
        holder.productName.setText(currentItem.getName());
        holder.productCategory.setText(currentItem.getCategory());
        holder.productBestByDate.setText(currentItem.getBestByDate());
        holder.expired.setVisibility(View.INVISIBLE);//Recyclerviews automatically set everything to visible, manually set each expiry warning to invisible
        checkExpiry(currentItem.getBestByDate(), position,holder);//Send each date to check if its expired when page loads
    }

    @Override
    public int getItemCount() {
        return mIngredientList.size();
    }

    //TODO signal more intricate date warning systems here. Different colours for different levels of gone off-ness.
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void checkExpiry(String mBestbyDate, int position, ExampleViewHolder holder){//Get the best by date and positions from each item, also pass holder so later on in expiredItem() we can call it to access textview
        Log.d("TAG", "Best by date in IngredientAdapter is: "+mBestbyDate);

        //Create a fresh calendar with today's date on it to compare to the best by
        Calendar freshCalendar= Calendar.getInstance();//make new calendar and set its date to now
        SimpleDateFormat newDateFormat= new SimpleDateFormat("dd-MM-yyyy");
        String thisDate = newDateFormat.format(freshCalendar.getTime());//turn it into string so it can be converted from SimpleDateFormat to a Date
        try {//This try takes both date strings and converts them into Dates so they can be checked against each other
            Date currentDate = new SimpleDateFormat("dd-MM-yyyy").parse(thisDate);//Create a current date from thisDate
            Date datetoCompare = new SimpleDateFormat("dd-MM-yyyy").parse(mBestbyDate);//Create a date from product best by
            Log.d("TAG", "Date to compare "+datetoCompare);
            Log.d("TAG", "Current date "+currentDate);

            assert datetoCompare != null;
            if (datetoCompare.before(currentDate)){//See if the date has passed
                Log.d("TAG", "Date has passed");
                expiredItem(position,holder);//Call the expired func with positions affected, it will then flag them to user
            }else{
                Log.d("TAG", "Date has not passed");
            }
/*//TODO make date comparissons to make more detailed show to user of how expired smt is


            freshCalendar.add(Calendar.DAY_OF_YEAR,3);
            String threeDaysTime=newDateFormat.format(freshCalendar.getTime());
            datetoCompare=new SimpleDateFormat("dd-MM-yyyy").parse(threeDaysTime);



            DateTimeFormatter dtf= DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDateTime date1 = LocalDateTime.parse(threeDaysTime,dtf);
            LocalDateTime date2 = LocalDateTime.parse(thisDate,dtf);


            long daysBetween= ChronoUnit.DAYS.between(date1,date2);
            Log.d("TAG", "Days between"+daysBetween);
*/
        } catch (ParseException e) {
       //     e.printStackTrace();
            Log.d("TAG", "Ingredient Adapter: Inside catch on Date checking"+e);
        }

    }

    public void expiredItem(int position,ExampleViewHolder holder){
        Log.d("TAG", "Expired item at position: "+position);
            holder.expired.setVisibility(View.VISIBLE);//Show user an expired item warning
    }
}