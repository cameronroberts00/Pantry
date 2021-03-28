package com.example.pantry;

import android.Manifest;
import android.content.ClipData;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class AddRecipe extends Fragment {
    View view;

    private SurfaceView surfaceView;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    //This class provides methods to play DTMF tones
    private ToneGenerator toneGen1;
    private TextView barcodeText;
    private TextView bestByText;
    private TableLayout productInfo;
    private Button manualButton;
    private TableLayout actionButtons;
    private Button saveItem;
    private Button cancelItem;
    private Button closeError;
    private TextView tip;
    private TextView errorText;
    private TextView categoryText;
    private String barcodeData;
    private String name;

    //These hold the product data individually
    private String category;
    private int bestby;

JSONObject product;
JSONArray products;
TinyDB tinyDB;

    private RequestQueue mQueue;
    String url;//This is the full url concatenated from the below
    String urlStart="https://chompthis.com/api/v2/food/branded/barcode.php?api_key=";
    String apiKey="Azq8PVSRvs3Ht2c12";
    String urlBarcode;// 9780140157376 <- example of barcode appearance
    String urlEnd="&code=";

    public boolean scanOnce=false;//just a lil bool to stop continous scanning

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_add_recipe, container, false);//TODO put this in all fraggies
        /*TODO
        *  Make cancel button hide the product text. Make add product manually fragment. Work on the product detection and date setting.  */

        //TODO Save products as java objects NOT json and save that to an ArrayList of products.
        toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC,     100);
        surfaceView = view.findViewById(R.id.surface_view);
        barcodeText = view.findViewById(R.id.barcode_text);
        tip=view.findViewById(R.id.tip);
        errorText=view.findViewById(R.id.error);
        categoryText=view.findViewById(R.id.category_text);
        manualButton=view.findViewById(R.id.manual_button);
        closeError=view.findViewById(R.id.closeError);
        saveItem=view.findViewById(R.id.save);
        cancelItem=view.findViewById(R.id.cancel);

        productInfo = view.findViewById(R.id.product_table);
    //    actionButtons=view.findViewById(R.id.action_buttons);
        bestByText=view.findViewById(R.id.bestby_text);

        mQueue = Volley.newRequestQueue(getActivity());
        //product=new JSONObject();
       // products=new JSONArray();
        tinyDB=new TinyDB(getContext());

        productInfo.setVisibility(View.GONE);//hide, this will appear when product is actually found
        initialiseDetectorsAndSources();


        loadData();

        Log.d("Warning", "This fragment will only work properly on a real life device or with an emulated camera");
        return view;
    }

private void getCategory( String categoryUppercased){
        //TODO add categories

        //This is a category sorter. when called in for loop it goes thru all product categories and finds most identifiable one. For example, If a product has: "Milk, Vegan, Dairy" as categories, obviously milk is the main identifiable one for a human.
        switch (categoryUppercased){
            case "MILK":
                bestby=7;
                category=categoryUppercased;//swap categoryuppercased into category. For example, this puts value of "milk"  into category. Because saying categoryUppercased after switch will show the first category on the product, not the main identifying one which is milk. itd show "Vegan" etc. this is because we are going through a for loop, and 1st item might not be correct category
                break;
            default:
                //dont put owt here as it is called for every category in the category array (the api provides many categories)
                break;
        }


}
    ArrayList<IngredientItem> mIngredientList;
    private void jsonParse(String url) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    //  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            JSONArray jsonArray = response.getJSONArray("items");//get the items array from the returned object
                            Log.d("barcode", "onResponse: Got reponse"+jsonArray.toString());//Show full reply in console
                            JSONObject childObject = jsonArray.getJSONObject(0);//Go into the array, access the child which has the attributes - as this is a barcode search, we only need the first result, as this is the barcode's (normally only) match.
                             name = childObject.getString("name");//Get name of the product out the child
                           // barcodeText.setText(name);//Show name of scanned product

                            //Sort Categories of the returned product.
                            JSONArray categories = childObject.getJSONArray("keywords");//Get category array out the child
                            for (int i=0; i<categories.length(); i++){//Go through each category
                                String categoryUppercased= categories.getString(i).toUpperCase();//each string, make all lowercase to fit in the category sorter
                                getCategory(categoryUppercased);//send each category to category sorter
                            }
                            if(category==null){//If the product doesnt match any of the categories ive predefined in order to set a custom best-by date, set it to the first category defined by the API
                                category=categories.getString(0).toUpperCase();
                                bestby=5;//Set a relatively "safe" best by as this product could be anything, fish or other expire quick products etc.
                            }

                            barcodeText.setText(name);
                            categoryText.setText(category);
                            bestByText.setText(String.valueOf(bestby));

                            saveItem.setOnClickListener(buttonListener);//listen for the cancel and the save buttons which are used to add/try again with the product
                            cancelItem.setOnClickListener(buttonListener);
                        } catch (JSONException ex) {//for some reason, the try failed.
                            ex.printStackTrace();
                            barcodeText.setText("Error. The following happened: "+ex);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {//This will be triggered by API not finding product
                showError(error);
            }
        });
        mQueue.add(request);//add the call to the volley queue
        productInfo.setVisibility(view.VISIBLE);//show the popup with info
    }
    private void showError(VolleyError error){//Hide product displays, tell user to just enter product manually
        Log.d("Error","Name:"+error);
        productInfo.setVisibility(View.INVISIBLE);
        errorText.setVisibility(View.VISIBLE);
        closeError.setVisibility(View.VISIBLE);
        errorText.setText("Product not found! \nPlease enter it manually.");
        closeError.setOnClickListener(buttonListener);//Listen for user to close the error
    }

    private void hideError(){
        errorText.setVisibility(View.INVISIBLE);
        closeError.setVisibility(View.INVISIBLE);
    }

    final View.OnClickListener buttonListener = new View.OnClickListener() {
        public void onClick(final View view) {
            switch(view.getId()) {
                case R.id.save:
                    insertItem(name, category);//save attributes

                    cancelSelection();
                    break;
                case R.id.cancel:
                   cancelSelection();
                    break;
                case R.id.closeError:
                    cancelSelection();
                    hideError();
                    break;
            }
        }
    };


   // private IngredientAdapter mAdapter;
    private void insertItem(String name, String category){
        mIngredientList.add(new IngredientItem(name, category));
        //mAdapter.notifyItemInserted(mIngredientList.size());
        saveData();
    }

    private void cancelSelection(){//dont actually need to clear anything here as if it isnt saved, the next time the barcode sees something it will overwrite anyway. this is purely aesthetics for the user
       productInfo.setVisibility(View.INVISIBLE);
        scanOnce=false;//allow scanning again
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("task list", null);
        Type type = new TypeToken<ArrayList<IngredientItem>>() {}.getType();
        mIngredientList = gson.fromJson(json, type);
        if (mIngredientList == null) {
            mIngredientList = new ArrayList<>();
        }
    }


    void saveData() {//To add recipe
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mIngredientList);
        editor.putString("task list", json);
        editor.apply();
    }



    private void initialiseDetectorsAndSources() {

        //Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();

        barcodeDetector = new BarcodeDetector.Builder(getActivity())
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(getActivity(), barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true)
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(getActivity(), new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                // Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    barcodeText.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!scanOnce) {//remove this and other commented out
                                if (barcodes.valueAt(0).email != null) {
                                    barcodeText.removeCallbacks(null);
                                    barcodeData = barcodes.valueAt(0).email.address;
                                   // barcodeText.setText(barcodeData);
                                    Log.d("barcode", "run: " + barcodeData);
                                    toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                                } else {
                                    barcodeData = barcodes.valueAt(0).displayValue;
                                 //   barcodeText.setText(barcodeData);
                                    Log.d("barcode", "run: " + barcodeData);
                                    toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                                }
                                //Send data to the API function
                                urlBarcode = barcodeData;
                                Log.d("barcode", "Barcode registered as " + urlBarcode);
                                url = urlStart + apiKey + urlEnd + urlBarcode;
                                jsonParse(url);

                            }//hi remove us too to get rid of the scan once feature
                            scanOnce=true;//me 3 :)
                        }
                    });

                }
            }
        });
    }

}