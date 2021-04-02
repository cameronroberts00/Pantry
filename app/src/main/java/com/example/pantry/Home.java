package com.example.pantry;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;

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

import java.util.ArrayList;

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
    int count=0;//used to count the tips and telll the adapter how many tips to display
    String category;//category of the tip (for sorting)

    private RecyclerView mRecyclerView,mRecyclerView2;
    private TipAdapter mAdapter,mAdapter2;
    private RecyclerView.LayoutManager mLayoutManager, mLayoutManager2;

    private ImageView featuredImage;
    private CardView featured;
    private ScrollView content;
    private ConstraintLayout loading;

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
        content=view.findViewById(R.id.content);
        content.setVisibility(View.INVISIBLE);

        //todo get rid of this shit
        Glide.with(getActivity()).load("https://upload.wikimedia.org/wikipedia/commons/a/a4/Anatomy_of_a_Sunset-2.jpg").into(featuredImage);
        mQueue = Volley.newRequestQueue(getActivity());
        loadContent();

        return view;
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
                               content.setVisibility(View.VISIBLE);//hide content while its just empty
                               JSONArray jsonArray = response.getJSONArray("tips");//get the items array from the returned object
                               Log.d("TAG", "onResponse: Got reponse"+jsonArray.toString());//Show full reply in console


                               featureRandom(jsonArray);//Calls a function that removes a random tip from the array/recyclers so it be displayed bigger to encourage user to click and read

                               //this for loop iterates the array and accesses all the attributes of each individual item
                                for(int i =0; jsonArray.length()>i;i++){
                                    JSONObject childObject = jsonArray.getJSONObject(i);
                                    name = childObject.getString("name");
                                    body = childObject.getString("body");
                                    imageUrl = childObject.getString("image");
                                    category=childObject.getString("category");
                                    Log.d("TAG", "JSON tip "+i+" acquired as "+name+" "+body+" "+imageUrl+" "+category);

                                    //split the tips into 2 recycler views based on categories in the json
                                    sortCategories(category);

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

    String mFeaturedName;
    String mFeaturedBody;
    String mFeaturedUrl;
       @RequiresApi(api = Build.VERSION_CODES.KITKAT)
       public void featureRandom(JSONArray jsonArray){
           //todo get random number from array length, when the for loop gets this item, send it to the featured holder
           try {
               int random = (int) ((Math.random() * ((jsonArray.length()) + 1)));//get random number in array's length
               Log.d("TAG", "String caught for removal" + jsonArray.getString(random));

               //take the entries attributes and store them in seperate fields
                   JSONObject childObject = jsonArray.getJSONObject(random);
                   mFeaturedName = childObject.getString("name");
                   mFeaturedBody = childObject.getString("body");
                   mFeaturedUrl = childObject.getString("image");

               Log.d("TAG", "Featured tip is: " + mFeaturedName+mFeaturedBody);

               featured.setOnClickListener(listener);


               //remove the item thats going into the featured tip holder to stop it appearing with the others as well
               jsonArray.remove(random);
           }catch (Exception e){
               e.printStackTrace();
           }
       }

       View.OnClickListener listener = new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               switch(view.getId()){
                   case R.id.featured_holder:
                       Log.d("TAG", "Tapped featured holder\n"+mFeaturedName);
                       //todo deal with featured holder taps
                       //todo add items to bundle, send this with new fragment instannce of openedtip.java
                       break;
               }
           }
       };

       public void sortCategories(String category){
        if(category.equals("waste")){//If first category, chuck it in the first recycler
            Log.d("TAG", "Category 1 "+category);
            mTipList.add(new TipBlogItem(name, category,body,imageUrl));
            buildRecycler1();//Add item and send to recycler
        }else if(category.equals("tip")){
            mTipList2.add(new TipBlogItem(name, category,body,imageUrl));
          //  mTipList2.add(new TipBlogItem(name, category,body,imageUrl));
            buildRecycler2();//Add item and send to recycler
            Log.d("TAG", "Category 2 "+category);
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