package com.example.pantry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
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

//This class has navigation components and frag loading
public class MainActivity extends AppCompatActivity {
    //private;
    FrameLayout frameLayout;
    FrameLayout progressLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameLayout = (FrameLayout) findViewById(R.id.frame);
        progressLayout = (FrameLayout) findViewById(R.id.progressFrame);
        loadFrag(new Home());//load home frag
        loadProgress(new ProgressBar());//Load progressbar into top frame

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
        //send bundle in fragment
        // Bundle bundle = new Bundle();//start new bundle to send
        //String value="";//value that is sent
        // String key="2";//how we identify the value
        // bundle.putString(key, value);//put in envelope.
        // fragment.setArguments(bundle);//ka-ciao, off they go
        transaction.commit();//do it, equiv of start intent
    }

    private void loadProgress(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.progressFrame, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {//catch on back presses and do nothing. stops fragments stacking infinitely if user keeps going home>add item>back>home>add item>back
       // super.onBackPressed();
        Log.d("TAG", "onBackPressed:");
    }
}