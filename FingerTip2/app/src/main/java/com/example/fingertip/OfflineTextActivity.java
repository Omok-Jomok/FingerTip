package com.example.fingertip;

import static android.net.wifi.p2p.WifiP2pManager.ERROR;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class OfflineTextActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private TextView ocr_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_text);

        ocr_tv = findViewById(R.id.ocr_tv);
        ocr_tv.setMovementMethod(new ScrollingMovementMethod());

        // TTS를 생성하고 OnInitListener로 초기화 한다.
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });
    }

    public void makeBig(View view){
        float currentSize = ocr_tv.getTextSize()/2;
        System.out.println(currentSize);
        ocr_tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentSize);
    }

    public void makeSmall(View view){
        float currentSize = ocr_tv.getTextSize()/2 - 2;
        System.out.println(currentSize);
        ocr_tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentSize);
    }

    public void listenText(View view){
        tts.speak(ocr_tv.getText().toString(),TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TTS 객체가 남아있다면 실행을 중지하고 메모리에서 제거한다.
        if(tts != null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    public void copyText(View view){
        //클립보드 사용 코드
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("CODE", ocr_tv.getText().toString().trim());
        clipboardManager.setPrimaryClip(clipData);

        //복사가 되었다면 토스트메시지 노출
        Toast.makeText(getApplicationContext(), "글자가 복사되었습니다.", Toast.LENGTH_SHORT).show();
    }

    public void goOfflineMainActivity(View view){
        Intent intent = new Intent(OfflineTextActivity.this, OfflineMainActivity.class);
        startActivity(intent);
    }
}