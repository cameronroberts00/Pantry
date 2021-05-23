package com.example.pantry;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.transition.Explode;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
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
    int pointIncrement = 20;//how big of a jump is made when incrementing points
    int steps = 100 / pointIncrement;//how many "steps" to get to 100% completion. in this case. 100/20=5. steps is used to show user how many items to save before a levelup

    private int mProgressStatus;//=0;
    Button okay;
    TextView bigLevel;
    TextView longLevel;
    TextView hint;
    SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_progress_bar, container, false);
        mProgressBar = view.findViewById(R.id.progressbar);
        currentLevel = view.findViewById(R.id.currentLevel);
        nextLevel = view.findViewById(R.id.nextLevel);
        hint = view.findViewById(R.id.hint);

        /*Set the custom styles*/
        Drawable draw = getResources().getDrawable(R.drawable.custom_progress);
        mProgressBar.setProgressDrawable(draw);

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        //Retrieve user data
        currentInt=preferences.getInt("currentLevel",0);
        nextInt=preferences.getInt("nextLevel",0);
        mProgressStatus=preferences.getInt("status",0);

        //If the next achievable level comes back as 0 (default val), then this is app's first launch, set it to 1
        if (nextInt == 0) {
            nextInt = 1;
        }
        //Display current stats to user
        currentLevel.setText(String.valueOf(currentInt));
        nextLevel.setText(String.valueOf(nextInt));
        mProgressBar.setProgress(mProgressStatus);


        updateHint();
        return view;
    }

    private void updateHint() {
        int hintNumber = steps - mProgressStatus / pointIncrement;
        hint.setText("Save " + hintNumber + " more item");
        if (steps - mProgressStatus / pointIncrement != 1) {//if more than 1 item, add on an s
            hint.append("s");
        }
    }

    //When called, this func increments the leveller by a point and levels the user up if required
    public void updateProgress() {
        //This runs a thread seperate to where the UI is, to avoid crashing etc
        new Thread(new Runnable() {
            @Override
            public void run() {
                // if (mProgressStatus < 100) {//Uncomment to loop the levelling infinitely
                mProgressStatus = mProgressStatus + pointIncrement;
                SystemClock.sleep(50);//sleep a lil bit
                mHandler.post(new Runnable() {//Open another thread, this time it updates the progress bar
                    @Override
                    public void run() {
                        mProgressBar.setProgress(mProgressStatus);
                        updateHint();
                    }
                });
                //  }
                if (mProgressStatus >= 100) {//if user is at 100, let them level up.
                    mHandler.post(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
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
                            hint.setText("You did it!");
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

    public void save() {

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("status",mProgressStatus);
        editor.putInt("currentLevel",currentInt);
        editor.putInt("nextLevel",nextInt);
        editor.apply();

        Log.d("TAG",
                "Save status:\n" + "progress status: " + mProgressStatus + "\n" +
                        "current level: " + currentInt + "\n" +
                        "next level: " + nextInt);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void triggerPopup() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View popup = inflater.inflate(R.layout.level_up_popup, null);
        okay = popup.findViewById(R.id.okay);
        bigLevel = popup.findViewById(R.id.big_level);
        longLevel = popup.findViewById(R.id.long_level_text);
        bigLevel.setText(String.valueOf(currentInt));//fill in the textviews
        longLevel.setText("You are now level " + String.valueOf(currentInt));

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popup, width, height, true);
        popupWindow.setEnterTransition(new Slide());
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        //Entry animation for the big level textview
        bigLevel.setAlpha(0.0f);
        bigLevel.animate()
                .translationX(60)
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        bigLevel.animate()
                                .translationX(0)
                                .alpha(1.0f)
                                .setListener(null).setDuration(300);
                    }
                });

        // dismiss on click of "okay" with a nice lil exit
        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //animate an exit on the big level textview then close the popup
                bigLevel.animate()
                        .translationX(-60)
                        .alpha(0.0f).setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @RequiresApi(api = Build.VERSION_CODES.M)
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                popupWindow.setExitTransition(new Slide());
                                popupWindow.dismiss();
                                updateHint();
                            }
                        });

            }
        });
    }

}