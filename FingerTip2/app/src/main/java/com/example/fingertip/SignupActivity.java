package com.example.fingertip;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("FingerTip");
    }

    public void signUp(View view){
        // 이메일
        EditText emailEditText = findViewById(R.id.email);
        String email = emailEditText.getText().toString();
        // 비밀번호
        EditText passwordEditText = findViewById(R.id.password);
        String password = passwordEditText.getText().toString();

        EditText phoneNumberEditText = findViewById(R.id.phoneNumber);
        String phoneNumber = phoneNumberEditText.getText().toString().trim();

        EditText nicknameEditText = findViewById(R.id.nickname);
        String nickname = nicknameEditText.getText().toString().trim();

        //회원가입
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();

                            UserInfo userInfo = new UserInfo();
                            userInfo.setUserUID(firebaseUser.getUid());
                            userInfo.setEmail(firebaseUser.getEmail());
                            userInfo.setPhoneNumber(phoneNumber);
                            userInfo.setNickname(nickname);

                            mDatabase.child("UserAccount").child(firebaseUser.getUid()).setValue(userInfo);

                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(intent);
                            Toast.makeText(SignupActivity.this, "등록 완료", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(SignupActivity.this, "등록 에러", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });

    }

}