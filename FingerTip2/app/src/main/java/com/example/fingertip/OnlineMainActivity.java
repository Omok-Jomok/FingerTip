package com.example.fingertip;

import static android.speech.tts.TextToSpeech.ERROR;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;

import java.util.Locale;

public class OnlineMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_main);

    }

    public void goShoppingMallCoupangActivity(View view){
        Intent intent = new Intent(OnlineMainActivity.this, SearchCoupangActivity.class);
        startActivity(intent);
    }
}