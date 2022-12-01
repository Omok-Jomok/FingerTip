package com.example.fingertip;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ShopCoupangActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_coupang);


    }

    public void goSearchCoupangActivity(View view){
        Intent intent = new Intent(ShopCoupangActivity.this, SearchCoupangActivity.class);
        startActivity(intent);
    }
}