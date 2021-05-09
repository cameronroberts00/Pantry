package com.example.pantry;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationMenu;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;
import java.util.concurrent.ThreadLocalRandom;

//This class has navigation components and frag loading
public class MainActivity extends AppCompatActivity {
    //private;
    FrameLayout frameLayout;
    FrameLayout progressLayout;

    //Notification alarm vars
    PendingIntent pendingIntent;
    AlarmManager alarmManager;
    BroadcastReceiver mReceiver;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameLayout = (FrameLayout) findViewById(R.id.frame);
        progressLayout = (FrameLayout) findViewById(R.id.progressFrame);
        loadFrag(new Home());//load home frag
        loadProgress(new ProgressBar());//Load progressbar into top frame

      //  startAlarm();
        //   getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final BottomNavigationView btmn = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        btmn.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        loadFrag(new Home());
                        break;
                    case R.id.shopping:
                        loadFrag(new Shopping());
                        break;
                    case R.id.recipes:
                        loadFrag(new Recipes());
                        break;
                    case R.id.storeroom:
                        loadFrag(new Storeroom());
                        break;
                    // default:
                    //  throw new IllegalStateException("Unexpected value: " + item.getItemId());
                }
                return true;
            }
        });
        //Check if the AddRecipe fab has been selected (it isnt in the btmn nav)
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFrag(new AddRecipe());
                btmn.getMenu().findItem(R.id.empty).setChecked(true);//Move the checked status onto the empty spacing-aid button to deselect the 4 "normal" menu items.
            }
        });

    }

    private void loadFrag(Fragment fragment) {//take fragment input and load appropriate classs associated with it
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, fragment);//replace frameview with content from fragment
        transaction.commit();//do it, equiv of start intent
    }

    private void loadProgress(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.progressFrame, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
       // super.onBackPressed();
        RegisterAlarmBroadcast();
        alarmManager.set( AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000 , pendingIntent );
        Log.d("TAG", "onBackPressed:");
    }

    //--- Notification timing/alarm stuff past this point ---
    //NotifyService class and alarm broadcasting in MainActivity with help from https://stackoverflow.com/questions/29058179/android-app-with-daily-notification
    private void RegisterAlarmBroadcast()
    {
        Log.d("TAG", "Registering alarm broadcast");
        mReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.d("TAG","Alarm time reached");
                PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0,
                        new Intent(getApplicationContext(), NotifyService.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);
                try {
                    pendingIntent.send(getApplicationContext(), 0, intent);
                    Log.d("TAG","NotifyService opened");
                } catch (PendingIntent.CanceledException e) {
                    Log.d("TAG","Couldn't load NotifyService!");
                    e.printStackTrace();
                }
           }
        };
        // register the alarm broadcast
        registerReceiver(mReceiver, new IntentFilter("com.myalarm.alarmexample") );
        pendingIntent = PendingIntent.getBroadcast( this, 0, new Intent("com.myalarm.alarmexample"),0 );
        alarmManager = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

}