package com.example.greenbookapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;

public class Realtime_Mentoring extends AppCompatActivity
{

    private static final String API_KEY = "704b7bc8efeb4178a84151556242404";  //weather api

    private String loc,query_text="";

    double lat,lon;

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private LocationManager locationManager;
    private static final String API_URL = "https://api.weatherapi.com/v1/current.json?key=" + API_KEY + "&q=";
    String url2 = "https://api.weatherapi.com/v1/forecast.json?key=" + API_KEY + "&q=";
    private TextView tv;
    private LottieAnimationView load;

    private androidx.appcompat.widget.Toolbar mtoolbar;

    private String address;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_mentoring);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        tv=findViewById(R.id.realtime_tv);
        load=findViewById(R.id.realtime_loading);
        tv.setMovementMethod(new ScrollingMovementMethod());
        mtoolbar=(Toolbar) findViewById(R.id.mentoring_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Live Guide");
        mtoolbar.setTitleTextColor(Color.parseColor("#04C370"));
        Drawable upArrow = ContextCompat.getDrawable(this, androidx.appcompat.R.drawable.abc_ic_ab_back_material); // Get default back icon
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.purple_200), PorterDuff.Mode.SRC_ATOP); // Set color filter
        getSupportActionBar().setHomeAsUpIndicator(upArrow);



        /*if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            Toast.makeText(this, "Please grant permission for speedometer", Toast.LENGTH_SHORT).show();
        }*/

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, get location
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            getLocation();
        }
    }

    private void getLocation()
    {

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                lat = location.getLatitude();
                                lon = location.getLongitude();
                                try {
                                    Geocoder geocoder=new Geocoder(Realtime_Mentoring.this,Locale.getDefault());
                                    List<Address> addresses=geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                                    String throughfare=addresses.get(0).getThoroughfare();
                                    String sublocality=addresses.get(0).getSubLocality();

                                    if(!throughfare.equals("") && !sublocality.equals("")) address=throughfare+","+sublocality;
                                    else if(throughfare.equals("") && !sublocality.equals("")) address=sublocality;
                                    else if(!throughfare.equals("") && sublocality.equals("")) address=throughfare;
                                    else address="";

                                    if(address.equals("")) address=addresses.get(0).getAdminArea()+","+addresses.get(0).getCountryName()+".";
                                    else address=address+","+addresses.get(0).getAdminArea()+","+addresses.get(0).getCountryName()+".";
                                }
                                catch (Exception e)
                                {

                                }

                                callAPI();

                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Realtime_Mentoring.this, "Failed to get location : " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "SecurityException: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void callAPI()
    {
        String apiUrl =API_URL+String.valueOf(lat)+","+String.valueOf(lon);



        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, apiUrl, null, new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject current = response.getJSONObject("current");

                            double tempC = current.getDouble("temp_c");
                            double windKph = current.getDouble("wind_kph");
                            double precipMm = current.getDouble("precip_mm");
                            int humidity = current.getInt("humidity");
                            int cloud = current.getInt("cloud");

                            query_text+="The current temperature at your location is "+tempC+" degree centigrade, windspeed is "+windKph+" kph,";
                            query_text+=" precipitation amount is "+precipMm+" mm, humidity is "+humidity+"% and cloud cover is "+cloud+"%.";
                            callAPI2();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", error.toString());
                    }
                });

        queue.add(jsonObjectRequest);
    }

    private void callAPI2()
    {
        url2+=String.valueOf(lat)+","+String.valueOf(lon)+"&days=1&aqi=no&alerts=no";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONObject forecast = jsonObject.getJSONObject("forecast");
                            JSONObject forecastday = forecast.getJSONArray("forecastday").getJSONObject(0);
                            JSONObject hourInfo = forecastday.getJSONArray("hour").getJSONObject(0); // Check the first hour in the array
                            JSONObject astro = forecastday.getJSONObject("astro");
                            String sunrise = astro.getString("sunrise");
                            String sunset = astro.getString("sunset");
                            // Check the possibility of rain
                            double chanceOfRain = hourInfo.getDouble("chance_of_rain");
                            if (chanceOfRain > 0) {
                                query_text+="There are "+chanceOfRain+"% chances of rain today.";
                            } else {
                                query_text+="There are no chances of rain today.";
                            }

                            query_text+="The sunrise time is at "+sunrise+" and ";
                            query_text+="sunset time is at "+sunset;

                            if(!address.equals("")) {
                                address = "You are at " + address;
                                query_text = address + query_text;
                            }
                            tv.setText(query_text);

                            callGeminiAPI();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("JSONError", "Error parsing JSON response");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VolleyError", "Error fetching weather data: " + error.getMessage());
            }
        });

        // Add the request to the RequestQueue.
        Volley.newRequestQueue(getApplicationContext()).add(stringRequest);

    }

    private void callGeminiAPI()
    {
        GenerativeModel gm2 = new GenerativeModel("gemini-pro", "AIzaSyBwhL1SIXnClCCIXcT1cKaLvHqjAf3AbqY");
        GenerativeModelFutures model2 = GenerativeModelFutures.from(gm2);
        Content content2 = new Content.Builder()
                .addText(query_text+".Tell me how to maintain and protect my delicate plants (both indoor and outdoor) and trees in this weather condition in a detailed and structured way.")
                .build();

        ListenableFuture<GenerateContentResponse> response2 = model2.generateContent(content2);
        Futures.addCallback(response2, new FutureCallback<GenerateContentResponse>()
        {
            @Override
            public void onSuccess(GenerateContentResponse result)
            {
                load.setVisibility(View.INVISIBLE);
                String resultText = result.getText().toString();

                String resultText_new="";
                for(char c : resultText.toCharArray())
                {
                    if(c!='*') resultText_new+=c;
                }

                query_text=query_text+"\n\n"+resultText_new;
                tv.setText(query_text);
            }
            @Override
            public void onFailure(Throwable t)
            {
                load.setVisibility(View.INVISIBLE);
                Toast.makeText(Realtime_Mentoring.this, "Text checking failed !!", Toast.LENGTH_SHORT).show();
            }
        }, getMainExecutor());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get location
                getLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

