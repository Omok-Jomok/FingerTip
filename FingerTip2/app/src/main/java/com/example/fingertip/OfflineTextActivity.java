package com.example.fingertip;

import static android.net.wifi.p2p.WifiP2pManager.ERROR;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

public class OfflineTextActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private TextView ocr_tv;

    Context cThis;//context 설정
    private FloatingActionButton fab;
    Intent SttIntent;
    SpeechRecognizer mRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        cThis=this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_text);

        Intent intent = getIntent();
        String ocr_text = intent.getExtras().getString("ocr_text");
        ocr_tv = findViewById(R.id.ocr_tv);
        ocr_tv.setMovementMethod(new ScrollingMovementMethod());

        ocr_tv.setText(ocr_text);

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

        SttIntent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getApplicationContext().getPackageName());
        SttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");//한국어 사용
        mRecognizer= SpeechRecognizer.createSpeechRecognizer(cThis);
        mRecognizer.setRecognitionListener(listener);

        fab = (FloatingActionButton)findViewById(R.id.fab_btn);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(cThis, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(OfflineTextActivity.this,new String[]{Manifest.permission.RECORD_AUDIO},1);
                    //권한을 허용하지 않는 경우
                }else{
                    //권한을 허용한 경우
                    try {
                        mRecognizer.startListening(SttIntent);
                    }catch (SecurityException e){e.printStackTrace();}
                }
            }
        });

    }

    private RecognitionListener listener=new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            //txtSystem.setText("onReadyForSpeech..........."+"\r\n"+txtSystem.getText());
        }

        @Override
        public void onBeginningOfSpeech() {
            //txtSystem.setText("지금부터 말을 해주세요..........."+"\r\n"+txtSystem.getText());
        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {
            //txtSystem.setText("onBufferReceived..........."+"\r\n"+txtSystem.getText());
        }

        @Override
        public void onEndOfSpeech() {
            //txtSystem.setText("onEndOfSpeech..........."+"\r\n"+txtSystem.getText());
        }

        @Override
        public void onError(int i) {
            //txtSystem.setText("천천히 다시 말해 주세요..........."+"\r\n"+txtSystem.getText());
        }

        @Override
        public void onResults(Bundle results) {
            String key= "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult =results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            //txtInMsg.setText(rs[0]+"\r\n"+txtInMsg.getText());
            FuncVoiceOrderCheck(rs[0]);
            mRecognizer.startListening(SttIntent);

        }

        @Override
        public void onPartialResults(Bundle bundle) {
            //txtSystem.setText("onPartialResults..........."+"\r\n"+txtSystem.getText());
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
            //txtSystem.setText("onEvent..........."+"\r\n"+txtSystem.getText());
        }
    };

    //입력된 음성 메세지 확인 후 동작 처리
    private void FuncVoiceOrderCheck(String VoiceMsg){
        if(VoiceMsg.length()<1)return;

        VoiceMsg=VoiceMsg.replace(" ","");//공백제거

        if(VoiceMsg.indexOf("쿠팡")>-1) {
            System.out.println(VoiceMsg);
            if (VoiceMsg.indexOf("장바구니") > -1) {
                Intent intent = new Intent(OfflineTextActivity.this, OnlineMallActivity.class);
                intent.putExtra("now_search_product", "bag");
                startActivity(intent);
            }else if (VoiceMsg.indexOf("마이페이지") > -1) {
                Intent intent = new Intent(OfflineTextActivity.this, OnlineMallActivity.class);
                intent.putExtra("now_search_product", "my");
                startActivity(intent);
            } else {
                Intent coupangIntent = new Intent(OfflineTextActivity.this, SearchCoupangActivity.class);
                startActivity(coupangIntent);
                onDestroy();
            }
        }
        else if(VoiceMsg.indexOf("어플꺼")>-1){
            ActivityCompat.finishAffinity(this);
            System.exit(0);
            //FuncVoiceOut("전등을 끕니다");//전등을 끕니다 라는 음성 출력
        }
        else if(VoiceMsg.indexOf("오프라인")>-1){
            Intent intent = new Intent(OfflineTextActivity.this, OfflineMainActivity.class);
            startActivity(intent);
        }
    }

    public void makeBig(View view){
        float currentSize = ocr_tv.getTextSize()/2;
        System.out.println(currentSize);
        ocr_tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentSize);
    }

    public void makeSmall(View view){
        float currentSize = ocr_tv.getTextSize()/4 - 2;
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