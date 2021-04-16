package com.example.pantry;

import android.content.Context;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

public class TipAdapter extends RecyclerView.Adapter<TipAdapter.ExampleViewHolder> {
    public ArrayList<TipBlogItem> mTipList;
    private Context mContext;

    // public String mName;
    // public String mBody;
    // public String mImage;
    // public int mCount;
    public CardView tipContainer;

    public class ExampleViewHolder extends RecyclerView.ViewHolder {
        public TextView tipName;
        public TextView tipBody;
        public ImageView tipImage;

        public ExampleViewHolder(View itemView) {
            super(itemView);
            tipName = itemView.findViewById(R.id.tip_blog_name);
            tipBody = itemView.findViewById(R.id.tip_blog_body);
            tipImage = itemView.findViewById(R.id.tip_blog_image);
            tipContainer = itemView.findViewById(R.id.tip_container);


        }
    }

    public void openTip(int position, TipAdapter.ExampleViewHolder holder, View view) {
        Log.d("TAG", "Tip clicked: " + position + holder.tipName.getText());

        //Grab the clicked item's info and send it to a new fragment to view
        String name = mTipList.get(position).getName();
        String body = mTipList.get(position).getBody();
        String image = mTipList.get(position).getImageUrl();

        Fragment openedTip = new OpenedTip();
        AppCompatActivity activity = (AppCompatActivity) view.getContext();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("body", body);
        bundle.putString("image", image);
        openedTip.setArguments(bundle);

        activity.getSupportFragmentManager().beginTransaction().replace(R.id.frame, openedTip).addToBackStack(null).commit();
    }

    public TipAdapter(ArrayList<TipBlogItem> TipList, Context context/*Context context, String name, String body, String imageUrl, String category,int count*/) {
        mContext = context;//set context as context of layout's adapter so Glide image library can correctly put images in
        mTipList = TipList;
        // mName=name;
        // mBody=body;
        //mImage=imageUrl;
        // mCount=count;
        //Log.d("TAG", "TipAdapter: "+mName+mBody+mImage);
    }

    @Override
    public TipAdapter.ExampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tip_blog_item, parent, false);
        TipAdapter.ExampleViewHolder evh = new TipAdapter.ExampleViewHolder(v);
        return evh;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(final TipAdapter.ExampleViewHolder holder, final int position) {
        final TipBlogItem currentItem = mTipList.get(position);

        holder.tipName.setText(currentItem.getName());
        holder.tipBody.setText(currentItem.getBody());
        Glide.with(mContext).load(currentItem.getImageUrl()).into(holder.tipImage);
        // Log.d("TAG", "Loading image: "+currentItem.getmImageUrl());
        if (holder.tipBody.getText().length() >= 50) {
            clipBody(holder);//clip the text body if its too long
        }
        tipContainer.setOnClickListener(new View.OnClickListener() {//if a tip is clicked, open it
            @Override
            public void onClick(View view) {
                openTip(position, holder, view);
            }
        });
        // holder.productBestByDate.setText(currentItem.getBestByDate());
        // holder.expired.setVisibility(View.INVISIBLE);//Recyclerviews automatically set everything to visible, manually set each expiry warning to invisible
        // Log.d("TAG", "Tip name"+currentItem.getName());
    }

    @Override
    public int getItemCount() {
        return mTipList.size();
    }

    public void clipBody(TipAdapter.ExampleViewHolder holder) {
        //  Log.d("TAG", "SNIP ");
        String snipped = (String) holder.tipBody.getText();
        snipped = snipped.substring(0, 50) + "...".trim();//Truncated body text is a substring from char 0 to 50 with ... on the end and no whitespace
        holder.tipBody.setText(snipped);
    }
}
