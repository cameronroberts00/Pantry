package com.example.pantry;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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

public class Home extends Fragment {
    ImageView image;
    View view;
    private RequestQueue mQueue;
    String url="https://pantry-be356-default-rtdb.europe-west1.firebasedatabase.app/.json";//Firebase database for tips
    String name;
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

        //image= view.findViewById(R.id.image);

        //Glide.with(getActivity()).load("https://upload.wikimedia.org/wikipedia/commons/a/a4/Anatomy_of_a_Sunset-2.jpg").into(image);
        mQueue = Volley.newRequestQueue(getActivity());
        loadContent();

        return view;
    }

   private void loadContent(){
           JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                   new Response.Listener<JSONObject>() {
                       //  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                       @Override
                       public void onResponse(JSONObject response) {
                           try {

                               JSONArray jsonArray = response.getJSONArray("tip");//get the items array from the returned object
                               Log.d("TAG", "onResponse: Got reponse"+jsonArray.toString());//Show full reply in console
                               JSONObject childObject = jsonArray.getJSONObject(0);//Go into the array, access the child which has the attributes - as this is a barcode search, we only need the first result, as this is the barcode's (normally only) match.
                               name = childObject.getString("name");//Get name of the product out the child
                               // barcodeText.setText(name);//Show name of scanned product

                           } catch (JSONException ex) {//for some reason, the try failed.
                               ex.printStackTrace();

                           }
                       }
                   }, new Response.ErrorListener() {
               @Override
               public void onErrorResponse(VolleyError error) {//This will be triggered by API not finding product

               }
           });
           mQueue.add(request);//add the call to the volley queue
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