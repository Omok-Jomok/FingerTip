package com.example.fingertip;

import static android.speech.tts.TextToSpeech.ERROR;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView tv_current_user;
    private String current_user_nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_current_user = findViewById(R.id.username_tv);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("FingerTip");

        FirebaseUser firebaseUser = mAuth.getCurrentUser();

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
        tts.speak(name+"님 안녕하세요 온라인 오프라인 쇼핑을 선택해 주세요",TextToSpeech.QUEUE_FLUSH, null);
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