package com.example.greenbookapp;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.annotations.Nullable;
import android.graphics.Bitmap;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Disease extends AppCompatActivity
{

    private static Button postb,retryb;
    private static LottieAnimationView load,scanner;
    private static TextView tv;
    private ImageButton ib;

    private Toolbar mToolbar;
    private Uri ImageUri;
    private Bitmap bitmap;
    private static String resultd,base64;
    


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease);

        postb=findViewById(R.id.disease_post_button);
        retryb=findViewById(R.id.retry);
        load=findViewById(R.id.pgbar);
        scanner=findViewById(R.id.scanner);
        tv=findViewById(R.id.disease_description);
        tv.setMovementMethod(new ScrollingMovementMethod());
        ib=findViewById(R.id.select_disease_image);
        mToolbar=(Toolbar) findViewById(R.id.disease_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Health Test");
        mToolbar.setTitleTextColor(Color.parseColor("#04C370"));
        Drawable upArrow = ContextCompat.getDrawable(this, androidx.appcompat.R.drawable.abc_ic_ab_back_material); // Get default back icon
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.purple_200), PorterDuff.Mode.SRC_ATOP); // Set color filter
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        tv.setMovementMethod(new ScrollingMovementMethod());

        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });

        postb.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(ImageUri!=null) 
                {
                    load.setVisibility(View.VISIBLE);
                    scanner.setVisibility(View.VISIBLE);
                    ib.setEnabled(false);
                    postb.setVisibility(View.INVISIBLE);
                    tv.setText("AI is analyzing the image...");
                    check_img();
                }
                else
                {
                    Toast.makeText(Disease.this, "Please select an image from gallery", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void check_img()
    {
        GenerativeModel gm = new GenerativeModel("gemini-pro-vision", "AIzaSyBwhL1SIXnClCCIXcT1cKaLvHqjAf3AbqY");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        Content content = new Content.Builder()
                .addText("Does this image convey anything about plants ? Answer in \"Yes\" or \"No\" and no extras ")
                .addImage(bitmap)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>()
        {
            @Override
            public void onSuccess(GenerateContentResponse result)
            {
                String resultText = result.getText().toString();
                if (resultText.trim().equalsIgnoreCase("Yes") || resultText.trim().equals("Yes."))
                {
                    performHealthAssessment(getApplicationContext(), "7LjYWRPccbKsWT3E7qNjPNei1iqvJ01Ph2IUcFhUmXx1om2y4E", base64, 22.97, 88.39);
                }
                else
                {
                    tv.setText("The image doesn't contain plants/leaves/trees.");
                    load.setVisibility(View.INVISIBLE);
                    scanner.setVisibility(View.INVISIBLE);
                    retryb.setVisibility(View.VISIBLE);
                    retryb.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i=new Intent(Disease.this,Disease.class);
                            startActivity(i);
                        }
                    });
                }
            }
            @Override
            public void onFailure(Throwable t) {
                scanner.setVisibility(View.INVISIBLE);
                load.setVisibility(View.INVISIBLE);
                Toast.makeText(Disease.this, "Image checking failed !", Toast.LENGTH_SHORT).show();
            }
        }, getMainExecutor());
    }

    public static String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void OpenGallery()
    {
        Intent galleryIntent=new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            ImageUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), ImageUri);
                base64=convertBitmapToBase64(bitmap);
            } catch (Exception e) {
                Toast.makeText(this, "Error in Bitmap", Toast.LENGTH_SHORT).show();
            }
            ib.setImageURI(ImageUri);
        }
    }

    public static void performHealthAssessment(Context context, String apiKey, String imageBase64, double latitude, double longitude)
    {
            String url = "https://plant.id/api/v3/health_assessment";


            JSONObject requestBody = new JSONObject();
            try {
                requestBody.put("images", imageBase64);
                requestBody.put("latitude", latitude);
                requestBody.put("longitude", longitude);
                requestBody.put("similar_images", true);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response)
                        {
                            try
                            {
                                JSONObject result = response.getJSONObject("result");
                                JSONObject disease = result.getJSONObject("disease");
                                JSONArray suggestions = disease.getJSONArray("suggestions");

                                resultd="Here's what we have extracted from the image : \n\n";

                                for (int i = 0; i < suggestions.length(); i++)
                                {
                                    JSONObject suggestion = suggestions.getJSONObject(i);
                                    String name = suggestion.getString("name");
                                    double probability = suggestion.getDouble("probability")*100;
                                    resultd=resultd+name+" : "+String.format("%.2f", probability)+" %\n";
                                }

                                JSONObject isHealthy = result.getJSONObject("is_healthy");
                                boolean binary = isHealthy.getBoolean("binary");

                                resultd=resultd+"\n";
                                if(binary==true) resultd+="The plant seems to be healthy according to our analysis";
                                else resultd+="The plant seems to be unhealthy according to our analysis";

                                Disease o=new Disease();
                                o.helper();

                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                            Toast.makeText(context, "Volley Error", Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Api-Key", apiKey);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(request);
    }

    void helper()
    {
        load.setVisibility(View.INVISIBLE);
        scanner.setVisibility(View.INVISIBLE);
        tv.setText("AI is analyzing the image...\n\nAlmost there...");
        Remedy rem=new Remedy();
        rem.start();
    }

    class Remedy extends Thread
    {
        public void run()
        {
            GenerativeModel gm2 = new GenerativeModel("gemini-pro", "AIzaSyBwhL1SIXnClCCIXcT1cKaLvHqjAf3AbqY");
            GenerativeModelFutures model2 = GenerativeModelFutures.from(gm2);
            Content content2 = new Content.Builder()
                    .addText(resultd + "\n\n" + "Having obtained these results, I'm seeking guidance on remedial actions or solutions, including recommendations for medications, fertilizers, or any other necessary interventions. Your assistance in this matter would be greatly appreciated.")
                    .build();

            Executor executor = Executors.newSingleThreadExecutor();

            ListenableFuture<GenerateContentResponse> response2 = model2.generateContent(content2);
            Futures.addCallback(response2, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result)
                {
                    String resultText = result.getText().toString();
                    String ans=resultd + "\n\n" + resultText;
                    String final_answer="";
                    for(char c : ans.toCharArray())
                    {
                        if(c!='*')
                        {
                            final_answer+=c;
                        }
                    }
                    tv.setText(final_answer);
                }

                @Override
                public void onFailure(Throwable t)
                {
                    Toast.makeText(Disease.this, "Error in analyzing.", Toast.LENGTH_SHORT).show();
                }
            }, executor);
        }
    }
}


