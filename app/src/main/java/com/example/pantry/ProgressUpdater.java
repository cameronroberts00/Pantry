package com.example.pantry;

import android.content.Context;
import android.preference.PreferenceManager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class ProgressUpdater extends FragmentActivity {
    //not used
//TODO remove this class
    public void addPoint() {
        /*Trigger the updateProgress function in the progress bar, which increments levels & points*/
        FragmentManager fm = getSupportFragmentManager();
        ProgressBar frag = (ProgressBar) fm.findFragmentById(R.id.progressFrame);
        if (frag != null) {
            frag.updateProgress();
        }
    }


}
