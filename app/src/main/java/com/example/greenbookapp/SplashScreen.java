package com.example.greenbookapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;

import com.airbnb.lottie.LottieAnimationView;

public class SplashScreen extends AppCompatActivity
{
    LottieAnimationView lottie;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        lottie=findViewById(R.id.lottieeee);

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                Intent i=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        },1800);
    }
}