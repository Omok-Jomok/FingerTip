package com.example.fingertip;

import static android.speech.tts.TextToSpeech.ERROR;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;

import java.util.Locale;

public class OnlineMainActivity extends AppCompatActivity {

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_main);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                    speakOnline();
                }
            }
        });
    }

    public void speakOnline(){
        tts.speak("온라인 몰 선택 화면 입니다 원하시는 온라인 쇼핑몰을 선택해주세요",TextToSpeech.QUEUE_FLUSH, null);
    }

    public void goShoppingMallCoupangActivity(View view){
        tts.speak("쿠팡",TextToSpeech.QUEUE_FLUSH, null);
        Intent intent = new Intent(OnlineMainActivity.this, ShopCoupangActivity.class);
        startActivity(intent);
    }
}