package com.example.pantry;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

public class AddManually extends Fragment {
View view;
private static final String TAG="MainActivity";
    Button submitDate;
    Button back;
    EditText nameText;
    EditText categoryText;
    TextView error1;
    TextView error2;
    TextView saveText;
    String bestByDate;
   String name;
    String category;
    FragmentManager fm;
    ArrayList<IngredientItem> mIngredientList;
    public static final int REQUEST_CODE = 11; // Used to identify the result to do wit date picker thing

    private OnFragmentInteractionListener mListener;


    public AddManually() {
            // Required empty public constructor
        }

        public static AddManually newInstance() {
            AddManually fragment = new AddManually();
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_add_manually, container, false);
            submitDate = view.findViewById(R.id.chooseDate);
            nameText = view.findViewById(R.id.name);
            categoryText = view.findViewById(R.id.category);
            error1=view.findViewById(R.id.error1);
            error2=view.findViewById(R.id.error2);
            saveText=view.findViewById(R.id.saveText);
            back=view.findViewById(R.id.back);
            // get fragment manager so we can launch from fragment
           fm = ((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportFragmentManager();
            // Using an onclick listener on the editText to show the datePicker
            submitDate.setOnClickListener(listener);
            back.setOnClickListener(listener);

            loadData();//load array before adding owt
            return view;
        }

    final View.OnClickListener listener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void onClick(final View view) {
            switch(view.getId()) {
                case R.id.chooseDate:
                        grabTexts();//Get the text fields
                    break;
                case R.id.back:
                    //return back to prev activ
                    AddRecipe addRecipe = new AddRecipe();
                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame,addRecipe).addToBackStack(null).commit();
                    break;
            }
        }
    };
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            // check for the results
            if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                // get date from string
                bestByDate = data.getStringExtra("selectedDate");
                Log.d(TAG, "Manual input item recorded as:\nProduct Name: "+name+"\nCategory: "+category+"\nBest by date:"+bestByDate);
                //TODO call to save function
                //TODO SORT SAVING
             //   String bestByDate=selectedDate;

                insertItem(name,category,bestByDate);//this starts the save process by adding items to array list
                showSave();

            }
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            if (context instanceof OnFragmentInteractionListener) {
                mListener = (OnFragmentInteractionListener) context;
            }
        }

        @Override
        public void onDetach() {
            super.onDetach();
            mListener = null;

        }

        public interface OnFragmentInteractionListener {
            // TODO: Update argument type and name
            void onFragmentInteraction(Uri uri);

        }


    private void loadData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("ingredient list", null);
        Type type = new TypeToken<ArrayList<IngredientItem>>() {}.getType();
        mIngredientList = gson.fromJson(json, type);
        if (mIngredientList == null) {
            mIngredientList = new ArrayList<>();
        }
    }

    private void insertItem(String name, String category, String bestByDate){

        mIngredientList.add(new IngredientItem(name, category,bestByDate));
        //mAdapter.notifyItemInserted(mIngredientList.size());
        saveData();
    }
    void saveData() {//To add recipe
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mIngredientList);
        editor.putString("ingredient list", json);
        editor.apply();
    }

        public void showSave()  {
                    saveText.setVisibility(view.VISIBLE);
                    nameText.setText(null);
                    categoryText.setText(null);

                  //only show the save text for 1 sec
            new CountDownTimer(1000,100){
                public void onTick(long millisUntilFinished) {
                    //necessary method in countdown timer thats called each interval
                }
                @Override
                public void onFinish() {
                    saveText.setVisibility(view.INVISIBLE);
                }
            }.start();
        }


        public void grabTexts()  {
           name = nameText.getText().toString().trim();//the values saved are trimmed Strings from input fields. (Trimming stops users breaking the items by holding enter for whitespace)
           category = categoryText.getText().toString().trim();
           if (category.isEmpty()){//if user didnt write a category, just set it to this
               category="no category";
           }

            //Check user has entered other fields before launching date picker
            //(uncomment "error 2" and add ||category.isEmpty to force category input)
            if(name.isEmpty()){
                error1.setVisibility(View.VISIBLE);
                //error2.setVisibility(View.VISIBLE);
            }else{//if user has got content in the fields, load date picker
                error1.setVisibility(View.INVISIBLE);
               // error2.setVisibility(View.INVISIBLE);
                AppCompatDialogFragment newFragment = new DatePickerFragment();
                // set the targetFragment to receive the results, specifying the request code
                newFragment.setTargetFragment(AddManually.this, REQUEST_CODE);
                // show the datePicker
                newFragment.show(fm, "datePicker");
            }

        }
    }


























/*
    final Calendar calendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
           calendar.set(Calendar.YEAR, year);
           calendar.set(Calendar.MONTH, month);
           calendar.set(Calendar.DAY_OF_MONTH, day);
            updateLabel();
        }
    };

    private void updateLabel() {
        String myFormat = "dd-MM-yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);

        //edittext.setText(sdf.format(myCalendar.getTime()));
        Log.d("TAG", "updateLabel: "+sdf.format(calendar.getTime()));
    }*/
