package com.example.pantry;

import android.Manifest;
import android.content.ClipData;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
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
import android.widget.Toast;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

//This class is made with help from TinyDB to store some data in shared preferences, find docs here:
//https://tinydb.readthedocs.io/en/latest/

//Barcode scanning in this class is made with help from:
//https://medium.com/analytics-vidhya/creating-a-barcode-scanner-using-android-studio-71cff11800a2

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
    private TextView manualButton;
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
    private int bestby;//This is used to define how many days the product should last. that is then added to a custom Date
    private String bestByDate;//i turn date into string so i can save, it goes here.
    TinyDB tinyDB;
    private RequestQueue mQueue;
    String url;//This is the full url concatenated from the below
    String urlStart = "https://chompthis.com/api/v2/food/branded/barcode.php?api_key=";
    String apiKey = "Azq8PVSRvs3Ht2c12";
    String urlBarcode;// 9780140157376 <- example of barcode appearance
    String urlEnd = "&code=";
    public boolean scanOnce = false;//just a lil bool to stop continous scanning
    ArrayList<IngredientItem> mIngredientList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_recipe, container, false);
        toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        surfaceView = view.findViewById(R.id.surface_view);
        barcodeText = view.findViewById(R.id.barcode_text);
        tip = view.findViewById(R.id.tip);
        errorText = view.findViewById(R.id.error);
        categoryText = view.findViewById(R.id.category_text);
        manualButton = view.findViewById(R.id.manual_button);
        manualButton.setOnClickListener(buttonListener);
        closeError = view.findViewById(R.id.closeError);
        saveItem = view.findViewById(R.id.save);
        cancelItem = view.findViewById(R.id.cancel);
        productInfo = view.findViewById(R.id.product_table);
        //    actionButtons=view.findViewById(R.id.action_buttons);
        bestByText = view.findViewById(R.id.bestby_text);
        mQueue = Volley.newRequestQueue(getActivity());
        //product=new JSONObject();
        // products=new JSONArray();
        tinyDB = new TinyDB(getContext());
        productInfo.setVisibility(View.GONE);//hide, this will appear when product is actually found
        initialiseDetectorsAndSources();
        loadData();

        Log.d("Warning!", "This fragment will only work properly on a real life device or with an emulated camera!");
        return view;
    }

    public void getCategory(String categoryUppercased) {
        Log.d("TAG", "Received category of: " + categoryUppercased);//say what category is getting chucked into the sorter
        //This is a category sorter. when called in for loop it goes thru all product categories and finds most identifiable one. For example, If a product has: "Milk, Vegan, Dairy" as categories, obviously milk is the main identifiable one for a human.
        //Most products tested have like 5-6 categories, all relatively vague so this should pick up a good chunk and give alright date estimates
        switch (categoryUppercased) {
            case "COOKED":
                bestby = 2;
                getDate(bestby);
                category = categoryUppercased;
                break;
            case "MEAT":
            case "FISH":
            case "RAW":
                bestby = 3;
                getDate(bestby);
                category = categoryUppercased;
                break;
            case "MILK":
            case "BREAD":
            case "YOGHURT":
            case "YOGURT":
            case "FRUIT":
            case "CREAM":
            case "CREME":
                bestby = 5;
                getDate(bestby);
                category = categoryUppercased;//swap categoryuppercased into category. For example, this puts value of "milk"  into category. Because saying categoryUppercased after switch will show the first category on the product, not the main identifying one which is milk. itd show "Vegan" etc. this is because we are going through a for loop, and 1st item might not be correct category
                break;
            case "VEGETABLE":
            case "LEGUME":
                bestby = 6;
                getDate(bestby);
                category = categoryUppercased;
                break;
            case "CHEESE":
                bestby = 10;
                getDate(bestby);
                category = categoryUppercased;
                break;
            case "JUICE":
                bestby = 14;
                getDate(bestby);
                category = categoryUppercased;
                break;
            case "CHOCOLATE":
            case "SWEETS":
            case "CANDY":
            case "CONFECTIONERY":
            case "JAM":
            case "MAYO":
            case "MAYONNAISE":
            case "KETCHUP":
            case "CONDIMENT":
            case "SAUCE":
            case "SALAD CREAM":
                bestby = 180;
                getDate(bestby);
                category = categoryUppercased;
                break;
            case "FROZEN":
            case "ICECREAM":
            case "ICE":
            case "ICED":
            case "TIN":
            case "TINNED":
            case "CAN":
            case "CANNED":
            case "PASTA":
            case "GRAINS":
            case "RICE":
            case "FLOUR":
            case "SUGAR":
            case "HONEY":
            case "CEREAL":
            case "COFFEE":
            case "TEA":
            case "NUTS":
                bestby = 365;
                getDate(bestby);
                category = categoryUppercased;
                break;
            default:
                //dont put owt here as it is called each time for every category in the category array (the api provides many categories)
                break;
        }
    }

    //String bestByDate;
    private void getDate(int bestby) {//Take the amount of days the product will last and add this to a date, then this gets turned to a string and stored with the other product info
        Calendar calendar = Calendar.getInstance();//initiate calendar
        calendar.add(Calendar.DAY_OF_YEAR, bestby);//adds how many days product lasts for onto the date
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");//format UK style date
        bestByDate = simpleDateFormat.format(calendar.getTime());//save this new date as a string as Date objects cant go into shared prefs
        Log.d("TAG", "Date set as:" + bestByDate);
    }


    private void jsonParse(String url) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    //  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            JSONArray jsonArray = response.getJSONArray("items");//get the items array from the returned object
                            Log.d("barcode", "onResponse: Got reponse" + jsonArray.toString());//Show full reply in console
                            JSONObject childObject = jsonArray.getJSONObject(0);//Go into the array, access the child which has the attributes - as this is a barcode search, we only need the first result, as this is the barcode's (normally only) match.
                            name = childObject.getString("name");//Get name of the product out the child
                            // barcodeText.setText(name);//Show name of scanned product

                            //Sort Categories of the returned product.
                            JSONArray categories = childObject.getJSONArray("keywords");//Get category array out the child
                            for (int i = 0; i < categories.length(); i++) {//Go through each category
                                String categoryUppercased = categories.getString(i).toUpperCase();//each string, make all lowercase to fit in the category sorter
                                getCategory(categoryUppercased);//send each category to category sorter
                                getDate(bestby);//send the custom bestby date of the the date calculator calendar function
                            }
                            if (category == null) {//If the product doesnt match any of the categories ive predefined in order to set a custom best-by date, set it to the first category defined by the API
                                category = categories.getString(0).toUpperCase();
                                bestby = 5;//Set a relatively "safe" best by as this product could be anything, fish or other expire quick products etc.
                                getDate(bestby);
                            }

                            barcodeText.setText(name);
                            categoryText.setText(category);
                            bestByText.setText(String.valueOf("Best in: " + bestby + " days"));

                            saveItem.setOnClickListener(buttonListener);//listen for the cancel and the save buttons which are used to add/try again with the product
                            cancelItem.setOnClickListener(buttonListener);
                        } catch (JSONException ex) {//for some reason, the try failed.
                            ex.printStackTrace();
                            barcodeText.setText("Error. The following happened: " + ex);
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

    private void showError(VolleyError error) {//Hide product displays, tell user to just enter product manually
        Log.d("Error", "Name:" + error);
        productInfo.setVisibility(View.INVISIBLE);
        errorText.setVisibility(View.VISIBLE);
        closeError.setVisibility(View.VISIBLE);
        errorText.setText("Product not found! \nPlease enter it manually.");
        closeError.setOnClickListener(buttonListener);//Listen for user to close the error
    }

    private void hideError() {
        errorText.setVisibility(View.INVISIBLE);
        closeError.setVisibility(View.INVISIBLE);
    }

    final View.OnClickListener buttonListener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void onClick(final View view) {
            switch (view.getId()) {
                case R.id.save:
                    insertItem(name, category, bestByDate);//save attributes
                    cancelSelection();
                    break;
                case R.id.cancel:
                    cancelSelection();
                    break;
                case R.id.closeError:
                    cancelSelection();
                    hideError();
                    break;
                case R.id.manual_button:
                    addManually();
                    break;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void addManually() {//launches the add manually fragment
        //TODO start manually adding fraggy
        Log.d("TAG", "snosig");
        AddManually addManually = new AddManually();
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame, addManually).addToBackStack(null).commit();

    }

    // private IngredientAdapter mAdapter;
    private void insertItem(String name, String category, String bestByDate) {
        mIngredientList.add(new IngredientItem(name, category, bestByDate));
        //mAdapter.notifyItemInserted(mIngredientList.size());
        saveData();
    }

    private void cancelSelection() {
        productInfo.setVisibility(View.INVISIBLE);
        category=null;// if breaks. clear this.
        resetTexts();
        scanOnce = false;//allow scanning again
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("ingredient list", null);
        Type type = new TypeToken<ArrayList<IngredientItem>>() {
        }.getType();
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
        editor.putString("ingredient list", json);
        editor.apply();
        resetTexts();
        Toast.makeText(getContext(), "Item Saved!", Toast.LENGTH_SHORT).show();
        category = null;/* if categories start messing up, check this line here*/
    }

    private void resetTexts() {
        barcodeText.setText("Loading...");
        categoryText.setText("Category:");
        bestByText.setText("Approx best-by:");
    }


    private void initialiseDetectorsAndSources() {
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
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    barcodeText.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!scanOnce) {
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
                            }
                            scanOnce = true;
                        }
                    });
                }
            }
        });
    }
}