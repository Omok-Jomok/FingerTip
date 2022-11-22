package com.example.fingertip;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView tv_current_user;

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

                String current_user_nickname = user.getNickname();

                //텍스트뷰에 받아온 문자열 대입하기
                tv_current_user.setText(current_user_nickname + " 님");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
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