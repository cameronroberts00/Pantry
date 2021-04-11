package com.example.pantry;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;


public class ProgressBar extends Fragment {
    private android.widget.ProgressBar mProgressBar;
    private Handler mHandler = new Handler();
    View view;
    TextView currentLevel, nextLevel;

    //Ints + their default values (not initialised as that would overide user save on app open)
    int currentInt;//=0;
    int nextInt;//=1;
    private int mProgressStatus;//=0;

    TinyDB tinyDB;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_progress_bar, container, false);
        mProgressBar = view.findViewById(R.id.progressbar);
        currentLevel=view.findViewById(R.id.currentLevel);
        nextLevel=view.findViewById(R.id.nextLevel);

        /*Set the custom styles*/
        Drawable draw= getResources().getDrawable(R.drawable.custom_progress);
        mProgressBar.setProgressDrawable(draw);

        tinyDB= new TinyDB(getContext());

    //Retrieve user data
    currentInt = tinyDB.getInt("currentLevel");
    nextInt = tinyDB.getInt("nextLevel");
    mProgressStatus = tinyDB.getInt("status");
    //If the next achievable level comes back as 0 (default val), then this is app's first launch, set it to 1
        if(nextInt==0){
       nextInt=1;
       //TODO trigger a call to a tutorial here
    }
    //Display current stats to user
        currentLevel.setText(String.valueOf(currentInt));
        nextLevel.setText(String.valueOf(nextInt));
        mProgressBar.setProgress(mProgressStatus);

        return view;
    }

    //When called, this func increments the leveller by a point and levels the user up if required
    public void updateProgress(){
        //This runs a thread seperate to where the UI is, to avoid crashing etc
        new Thread(new Runnable() {
            @Override
            public void run() {
               // if (mProgressStatus < 100) {//Uncomment to loop the levelling infinitely
                    mProgressStatus=mProgressStatus+20;
                    SystemClock.sleep(50);//sleep a lil bit
                    mHandler.post(new Runnable() {//Open another thread, this time it updates the progress bar
                        @Override
                        public void run() {
                           mProgressBar.setProgress(mProgressStatus);
                        }
                    });
              //  }
                if(mProgressStatus>=100) {//if user is at 100, let them level up.
                    mHandler.post(new Runnable() {
                        @Override//this handles after the progress bar has updated
                        public void run() {
                            //increment the levels
                            currentInt++;
                            nextInt++;
                            currentLevel.setText(String.valueOf(currentInt));
                            nextLevel.setText(String.valueOf(nextInt));
                            SystemClock.sleep(100);//pause for a lil mo to show users the bar is completed
                            mProgressStatus = 0;
                            mProgressBar.setProgress(mProgressStatus);
                            triggerPopup();

                            save();//save progress on every levelup

                        }
                    });
                }//if statement end bracket
            }
        }).start();
    }

    @Override
    public void onStop() {//save if app minimises etc
        super.onStop();
     save();
    }

    public void save(){
        tinyDB.putInt("status",mProgressStatus);
        tinyDB.putInt("currentLevel",currentInt);
        tinyDB.putInt("nextLevel",nextInt);
        Log.d("TAG",
                  "Save status:\n"+"progress status: " +mProgressStatus+"\n"+
                        "current level: "+currentInt+"\n"+
                        "next level: "+nextInt);
    }

    public void triggerPopup(){
        LayoutInflater inflater=(LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popup=inflater.inflate(R.layout.level_up_popup,null);

       // int width = LinearLayout.LayoutParams.WRAP_CONTENT;
       // int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        //This converts pixels to dp
        final float scale = getContext().getResources().getDisplayMetrics().density;
        int width = (int) (350 * scale + 0.5f);
        int height =(int) (450 * scale + 0.5f);

        final PopupWindow popupWindow = new PopupWindow(popup,width, height,true);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);


        // dismiss the popup window when touched
        popup.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

}