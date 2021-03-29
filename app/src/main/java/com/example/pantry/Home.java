package com.example.pantry;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Home extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*Trigger the updateProgress function in the progress bar (Add a point)*/
       FragmentManager fm = getFragmentManager();
        ProgressBar frag = (ProgressBar) fm.findFragmentById(R.id.progressFrame);
        if (frag != null) {
            frag.updateProgress();
        }

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

        return inflater.inflate(R.layout.fragment_first, container, false);
    }


}