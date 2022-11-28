package com.example.fingertip;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class OnlineMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_main);
    }

    public void goOnlineShoppingMallActivity(View view){
        Intent intent = new Intent(OnlineMainActivity.this, OnlineMallActivity.class);
        startActivity(intent);
    }
}