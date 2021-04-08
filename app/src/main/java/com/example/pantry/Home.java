package com.example.pantry;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class Home extends Fragment {
    ImageView image;
    View view;
    private RequestQueue mQueue;
    String url="https://pantry-be356-default-rtdb.europe-west1.firebasedatabase.app/.json";//Firebase database for tips

    //These are filled with json from firebase database
    String name;//tip name
    String imageUrl;//tip image
    String body;//tip body
    ArrayList<TipBlogItem> mTipList,mTipList2;
    //int count=0;//used to count the tips and telll the adapter how many tips to display
    String category;//category of the tip (for sorting)

    //These are used to hold featured tip info
    String mFeaturedName;
    String mFeaturedBody;
    String mFeaturedUrl;
    int random;//this is 0 by default but set between 0 and jsonaray length to determine featured tip.
    private TextView featuredName;

    private RecyclerView mRecyclerView,mRecyclerView2;
    private TipAdapter mAdapter,mAdapter2;
    private RecyclerView.LayoutManager mLayoutManager, mLayoutManager2;

    private ImageView featuredImage;
    private CardView featured;
    private ScrollView content;
    private ConstraintLayout loading;
    private ConstraintLayout timeout;
    private Button refresh;
    private boolean loaded=false;//this sets to true once jsonarray with content comes thru. if it hasnt set to true in 10 seconds, tell user to check internet/reload etc.

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        /*Trigger the updateProgress function in the progress bar (Add a point)*/
       FragmentManager fm = getFragmentManager();
        ProgressBar frag = (ProgressBar) fm.findFragmentById(R.id.progressFrame);
        if (frag != null) {
            frag.updateProgress();
        }


        buildRecycler1();
        buildRecycler2();
        featuredImage=view.findViewById(R.id.featured_image);
        featured=view.findViewById(R.id.featured_holder);
        loading=view.findViewById(R.id.loading);
        timeout=view.findViewById(R.id.timeout);
        content=view.findViewById(R.id.content);
        content.setVisibility(View.INVISIBLE);
        timeout.setVisibility(View.INVISIBLE);
        refresh=view.findViewById(R.id.refresh);
        refresh.setOnClickListener(listener);
        featuredName=view.findViewById(R.id.featured_name);

        mQueue = Volley.newRequestQueue(getActivity());
        checkTimeout();
        loadContent();



        return view;
    }
    private void checkTimeout(){
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
             if(loaded==false){
                 //if content hasnt loading in 10 seconds, tell user to refresh
                 Log.d("TAG", "Hasn't loaded in 10 seconds, offer refresh.");
                 loading.setVisibility(View.INVISIBLE);
                 timeout.setVisibility(View.VISIBLE);
             }
            }
        }, 10000);
    }
    private void buildRecycler1(){//build the first recycler
        if (mTipList == null) {
            mTipList = new ArrayList<>();
        }
            mRecyclerView = view.findViewById(R.id.recyclerview);
            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
            mAdapter = new TipAdapter(mTipList,getContext());
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mAdapter);
    }
    private void buildRecycler2(){//initialise and build the second recycler
        if (mTipList2 == null) {
            mTipList2 = new ArrayList<>();
        }
        mRecyclerView2 = view.findViewById(R.id.recyclerview2);
        mRecyclerView2.setHasFixedSize(true);
        mLayoutManager2 = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        mAdapter2 = new TipAdapter(mTipList2,getContext());
        mRecyclerView2.setLayoutManager(mLayoutManager2);
        mRecyclerView2.setAdapter(mAdapter2);
    }

   private void loadContent(){
       loading.setVisibility(View.VISIBLE);
           JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                   new Response.Listener<JSONObject>() {
                       //  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                       @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                       @Override
                       public void onResponse(JSONObject response) {
                           try {
                               loading.setVisibility(View.INVISIBLE);
                               loaded=true;
                               content.setVisibility(View.VISIBLE);//hide content while its just empty
                               JSONArray jsonArray = response.getJSONArray("tips");//get the items array from the returned object
                               Log.d("TAG", "onResponse: Got reponse"+jsonArray.toString());//Show full reply in console

                                //changes the featured tip once a day
                               Calendar calendar = Calendar.getInstance();
                               int currentDay=calendar.get(Calendar.DAY_OF_MONTH);
                               SharedPreferences settings = getContext().getSharedPreferences("PREFS", 0);
                               int lastDay = settings.getInt("day",0);
                               if(lastDay!=currentDay){
                                   SharedPreferences.Editor editor = settings.edit();
                                   editor.putInt("day",currentDay);
                                   editor.commit();

                                   featureRandom(jsonArray,random);//Calls a function that removes a random tip from the array/recyclers so it be displayed bigger to encourage user to click and read. send int random with  value of 0

                               }else{//if function has already run today, the featured tip has already been chosen, just grab it from shared preferences
                                   //TODO figure a way to stop it only showing once a day, maybe by saving which one got featured to shared prefs and pulling it here

                                   SharedPreferences randomStorer = getContext().getSharedPreferences("random", 0);
                                   random = randomStorer.getInt("random",0);
                                   //random=//from saved prefs;
                                   Log.d("TAG", "Function already called today, getting random feature from storage "+random);
                                   featureRandom(jsonArray,random);
                               }

                               //this for loop iterates the array and accesses all the attributes of each individual item
                                for(int i =0; jsonArray.length()>i;i++){
                                    JSONObject childObject = jsonArray.getJSONObject(i);
                                    name = childObject.getString("name");
                                    body = childObject.getString("body");
                                    imageUrl = childObject.getString("image");
                                    category=childObject.getString("category");
                                    Log.d("TAG", "JSON tip "+i+" acquired as "+name+" "+body+" "+imageUrl+" "+category);

                                    //split the tips into 2 recycler views based on categories in the json
                                    try {
                                        sortCategories(category);//chuck it in a try as spamming home button causes nullpointer exception and a crash when adding stuff to tiplists, if caught it doesnt affect performance
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }


                   }
                           } catch (JSONException ex) {//for some reason, the try failed.
                               ex.printStackTrace();

                           }
                       }
                   }, new Response.ErrorListener() {
               @Override
               public void onErrorResponse(VolleyError error) {//This will be triggered by API not finding product
                   Log.d("TAG", "onErrorResponse: "+error);
               }
           });
           mQueue.add(request);//add the call to the volley queue
       }


       @RequiresApi(api = Build.VERSION_CODES.KITKAT)
       public void featureRandom(JSONArray jsonArray, int random){
           //todo get random number from array length, when the for loop gets this item, send it to the featured holder
           try {
               if(random==0) {
                   Log.d("TAG", "Random feature wasnt initialised, getting random number and saving it "+random);
                   //if random isnt initaliased, then this was called from the function that runs once per day, get a random number and save it.
                   random = (int) ((Math.random() * ((jsonArray.length()))));//get random number in array's length

                   SharedPreferences randomStorer = getContext().getSharedPreferences("random", 0);
                   SharedPreferences.Editor editor = randomStorer.edit();
                   editor.putInt("random",random);
                   editor.commit();

               }
               Log.d("TAG", "String caught for removal" + jsonArray.getString(random));

               //take the entries attributes and store them in seperate fields
                   JSONObject childObject = jsonArray.getJSONObject(random);
                   mFeaturedName = childObject.getString("name");
                   mFeaturedBody = childObject.getString("body");
                   mFeaturedUrl = childObject.getString("image");

                   featuredName.setText(mFeaturedName);
               Glide.with(getActivity()).load(mFeaturedUrl).into(featuredImage);
               Log.d("TAG", "Featured tip is: " + mFeaturedName+mFeaturedBody);

               featured.setOnClickListener(listener);


               //remove the item thats going into the featured tip holder to stop it appearing with the others as well
               jsonArray.remove(random);
           }catch (Exception e){
               e.printStackTrace();
               Log.d("TAG", "In catch on featureRandom");
           }
       }

       View.OnClickListener listener = new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               switch(view.getId()){
                   case R.id.featured_holder:
                       Log.d("TAG", "Tapped featured holder\n"+mFeaturedName);

                       Fragment openedTip = new OpenedTip();
                       AppCompatActivity activity = (AppCompatActivity) view.getContext();
                       Bundle bundle = new Bundle();
                       bundle.putString("name", mFeaturedName);
                       bundle.putString("body", mFeaturedBody);
                       bundle.putString("image",mFeaturedUrl);

                       openedTip.setArguments(bundle);


                       activity.getSupportFragmentManager().beginTransaction().replace(R.id.frame, openedTip).addToBackStack(null).commit();
                       //todo deal with featured holder taps
                       //todo add items to bundle, send this with new fragment instannce of openedtip.java
                       break;
                   case R.id.refresh://user timed out, this reloads frag
                       Fragment home = new Home();
                       AppCompatActivity reload = (AppCompatActivity) view.getContext();
                       reload.getSupportFragmentManager().beginTransaction().replace(R.id.frame, home).addToBackStack(null).commit();
                       break;
               }
           }
       };

       public void sortCategories(String category){
        if(category.equals("waste")){//If first category, chuck it in the first recycler
         //   Log.d("TAG", "Category 1 "+category);
            mTipList.add(new TipBlogItem(name, category,body,imageUrl));
            buildRecycler1();//Add item and send to recycler
        }else if(category.equals("tip")){
            mTipList2.add(new TipBlogItem(name, category,body,imageUrl));
          //  mTipList2.add(new TipBlogItem(name, category,body,imageUrl));
            buildRecycler2();//Add item and send to recycler
        //    Log.d("TAG", "Category 2 "+category);
        }else{
            Log.d("TAG", "Entry with obscure category: "+name+category);
        }
       }

    @Override
    public void onPause() {
        //when activity is minimised, reset arraylists.
        /*when OpenedTip.java fragment is launched from TipAdapter.java, these arraylists stay intact, so navigating back using Android's
        inbuilt back arrow brings user back to the pre-loaded fragment they left off. This then re-calls the json and infinitely stacks the tip cards.
        This solves it.
         */
        super.onPause();
        mTipList2=null;
        mTipList=null;
        Log.d("TAG", "onPause: ArrayLists set to null");
    }
}












































/*
        int bestbydays=5;
        Calendar calendar = Calendar.getInstance();//initiate calendar
        calendar.add(Calendar.DAY_OF_YEAR,bestbydays);//adds how many days product lasts for onto the date
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd-MM-yyyy");//format UK style date
        String bestbyDate=simpleDateFormat.format(calendar.getTime());//save this new date as a string
        //Save string  to array as string here

        Log.d("TAG", "String that would be saved: "+bestbyDate);
        //(Everytime the storeroom is loaded, go thru each data and check it)
    //Imaginary end



        //start of pulling string back into a date and comparing it
Calendar freshCalendar= Calendar.getInstance();//make new calendar and set its date to now
SimpleDateFormat newDateFormat= new SimpleDateFormat("dd-MM-yyyy");//do all this fuckery to get a date then turn to string
String again = newDateFormat.format(freshCalendar.getTime());

//TODO integrate dates into the recycler view
        try {
            Date currentDate = new SimpleDateFormat("dd-MM-yyyy").parse(again);//get current date

            Date datetoCompare = new SimpleDateFormat("dd-MM-yyyy").parse(bestbyDate);//grab the string thats stored and turn it into a date
            Log.d("TAG", "Date to compare "+datetoCompare);
            Log.d("TAG", "Current date "+currentDate);

            assert datetoCompare != null;
            if (datetoCompare.before(currentDate)){//See if the date has passed
                Log.d("TAG", "Date has passed");
            }else{
                Log.d("TAG", "Date has not passed");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

*/
// Log.d("TAG", "Calendar "+bestbyDate);

/*End of add a point*/



//Food goes out of date today.
// if(simpleDateFormat.format(calendar.getTime()).equals(dateOutput)){
//   Log.d("TAG", "if date is equal");
//}
/*
        TinyDB tinyDB = new TinyDB(getContext());
        ArrayList<Object> productObjArrayList = new ArrayList<>();

        //Fake scanning loop, simulate multiple scans
        for (int i=0;i<5;i++) {
            name = "Example Item Number "+i;
            category = "Example Category"+i;
            bestby = "3"+i;


            ProductObj productObj = new ProductObj();
            productObj.addName(name);
            productObj.addCategory(category);
            productObj.addBestBy(bestby);


            productObjArrayList.add(i,productObj);

            //This is just a tester to show that stuff can be pulled out the array
          ProductObj tester = new ProductObj();
            tester= (ProductObj) productObjArrayList.get(i);
            Log.d("TAG", "These are things in the array before passing into storage: "+tester.getName());

            tinyDB.putListObject(String.valueOf(i),productObjArrayList);
        }

        //Example of adding an individual item outside of for loop//todo make it work
name="johnny";
        category="person";;
        bestby="3";
        ProductObj productObj = new ProductObj();
        productObj.addName(name);
        productObj.addCategory(category);
        productObj.addBestBy(bestby);
        productObjArrayList.add(productObjArrayList.size(),productObj);
       tinyDB.putListObject(String.valueOf(productObjArrayList.size()),productObjArrayList);








        //--------------- fake end------------------------

        ArrayList<Object> receivedProductObjArrayList = new ArrayList<>();
        Log.d("TAG", "onCreateView: "+receivedProductObjArrayList.size());


for(int i=0;i<tinyDB.getListObject(String.valueOf(i),ProductObj.class).size();i++) {//this loops the list object and gets the size at the same time to extract each object
        receivedProductObjArrayList = tinyDB.getListObject(String.valueOf(i), ProductObj.class);

        ProductObj receivedObj = new ProductObj();

        receivedObj = (ProductObj) receivedProductObjArrayList.get(i);


        //dis de final step
        Log.d("TAG", "Objects received on the other side: " + receivedObj.getName() +" "+ receivedObj.getBestBy() + " "+ receivedObj.getCategory());



}

//Example of how to remove an object
    ProductObj nudder = new ProductObj();//make a new object just for removing
nudder = (ProductObj)receivedProductObjArrayList.get(2);//2 here would be the object to remove
     Log.d("TAG", "onCreateView: " + nudder.getName()+nudder.getCategory());

*/