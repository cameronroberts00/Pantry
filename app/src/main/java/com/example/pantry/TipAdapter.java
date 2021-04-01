package com.example.pantry;

import android.media.Image;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class TipAdapter extends RecyclerView.Adapter<TipAdapter.ExampleViewHolder>{
    public ArrayList<TipBlogItem> mTipList;

    public class ExampleViewHolder extends RecyclerView.ViewHolder {
        public TextView tipName;
        public TextView tipBody;
        public ImageView tipImage;
        public CardView tipContainer;
        // final public Button deleteButton;

        public ExampleViewHolder(View itemView) {
            super(itemView);
            tipName = itemView.findViewById(R.id.tip_blog_name);
            tipBody = itemView.findViewById(R.id.tip_blog_body);
            tipImage=itemView.findViewById(R.id.tip_blog_image);
            tipContainer=itemView.findViewById(R.id.tip_container);

            tipContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   openTip(getAdapterPosition());
                }
            });

        }
    }

public void openTip(int position){
    Log.d("TAG", "Tip clicked"+position);
        /*TODO
        *  1. Create opened tip activity
        * 2. Send current tip data to this new fragment activity*/
}


    public TipAdapter(ArrayList<TipBlogItem> TipList) {
        mTipList = TipList;
    }

    @Override
    public TipAdapter.ExampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tip_blog_item, parent, false);
        TipAdapter.ExampleViewHolder evh = new TipAdapter.ExampleViewHolder(v);
        return evh;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(TipAdapter.ExampleViewHolder holder, int position) {
        TipBlogItem currentItem = mTipList.get(position);
     //   holder.productName.setText(currentItem.getName());
       // holder.productCategory.setText(currentItem.getCategory());
       // holder.productBestByDate.setText(currentItem.getBestByDate());
       // holder.expired.setVisibility(View.INVISIBLE);//Recyclerviews automatically set everything to visible, manually set each expiry warning to invisible
    }

    @Override
    public int getItemCount() {
        return mTipList.size();
    }


}
