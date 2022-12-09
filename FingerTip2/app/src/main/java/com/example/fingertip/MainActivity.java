package com.example.fingertip;

import static android.speech.tts.TextToSpeech.ERROR;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    private TextToSpeech tts;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView tv_current_user;
    private String current_user_nickname;
    Context cThis;//context 설정
    private FloatingActionButton fab;
    Intent SttIntent;
    SpeechRecognizer mRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        cThis=this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SttIntent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getApplicationContext().getPackageName());
        SttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");//한국어 사용
        mRecognizer= SpeechRecognizer.createSpeechRecognizer(cThis);
        mRecognizer.setRecognitionListener(listener);

        tv_current_user = findViewById(R.id.username_tv);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("FingerTip");

        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        fab = (FloatingActionButton)findViewById(R.id.fab_btn);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(cThis, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECORD_AUDIO},1);
                    //권한을 허용하지 않는 경우
                }else{
                    //권한을 허용한 경우
                    try {
                        mRecognizer.startListening(SttIntent);
                    }catch (SecurityException e){e.printStackTrace();}
                }
            }
        });

        mDatabase.child("UserAccount").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserInfo user = dataSnapshot.getValue(UserInfo.class);

                current_user_nickname = user.getNickname();

                //텍스트뷰에 받아온 문자열 대입하기
                tv_current_user.setText(current_user_nickname + " 님");

                setTts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
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
                Intent intent = new Intent(MainActivity.this, OnlineMallActivity.class);
                intent.putExtra("now_search_product", "bag");
                startActivity(intent);
            }else if (VoiceMsg.indexOf("마이페이지") > -1) {
                Intent intent = new Intent(MainActivity.this, OnlineMallActivity.class);
                intent.putExtra("now_search_product", "my");
                startActivity(intent);
            } else {
                Intent coupangIntent = new Intent(MainActivity.this, SearchCoupangActivity.class);
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
            Intent intent = new Intent(MainActivity.this, OfflineMainActivity.class);
            startActivity(intent);
        }
    }

    public void setTts(){
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                    speakChoose(current_user_nickname);
                }
            }
        });
    }
    public void speakChoose(String name){
        //tts.speak(name+"님 안녕하세요 온라인 오프라인 쇼핑을 선택해 주세요",TextToSpeech.QUEUE_FLUSH, null);
    }

    public void goOnlineMainActivity(View view){
        Intent intent = new Intent(MainActivity.this, OnlineMainActivity.class);
        startActivity(intent);
    }

    public void goOfflineMainActivity(View view){
        Intent intent = new Intent(MainActivity.this, OfflineMainActivity.class);
        startActivity(intent);
    }
}