package com.example.fingertip;

import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SearchCoupangActivity extends AppCompatActivity {

    private SearchRecordAdapter adapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<SearchRecordItem> arrayList;
    private EditText et_search;
    private FirebaseUser firebaseUser;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    ImageButton recordBtn;
    SpeechRecognizer speechRecognizer;

    long mNow;
    Date mDate;

    Context cThis;//context 설정
    private FloatingActionButton fab;
    Intent SttIntent;
    SpeechRecognizer mRecognizer;

    SimpleDateFormat mFormatMonth;
    SimpleDateFormat mFormatDay;
    SimpleDateFormat mFormatTime;

    Intent intent;
    final int PERMISSION = 1;	//permission 변수
    boolean recording = false;  //현재 녹음중인지 여부

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        cThis=this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_coupang);

        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);

        mFormatTime = new SimpleDateFormat("hh:mm:ss");
        mFormatMonth = new SimpleDateFormat("MM");
        mFormatDay = new SimpleDateFormat("dd");

        String month = mFormatMonth.format(mDate);
        String day = mFormatDay.format(mDate);
        String time = mFormatTime.format(mDate);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("FingerTip");
        firebaseUser = mAuth.getCurrentUser();
        mRecyclerView = (RecyclerView)findViewById(R.id.rv_search_list);
        mRecyclerView.setHasFixedSize(true); // 리사이클러뷰 기존성능 강화
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>();

        mDatabase.child("UserSearchRecord").child(firebaseUser.getUid()).orderByChild("day").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 파이어베이스 데이터베이스의 데이터를 받아오는 곳
                arrayList.clear(); // 기존 배열리스트가 존재하지않게 초기화
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) { // 반복문으로 데이터 List를 추출해냄
                    SearchRecordItem searchRecordItem = snapshot.getValue(SearchRecordItem.class); // 만들어뒀던 User 객체에 데이터를 담는다.
                    arrayList.add(searchRecordItem); // 담은 데이터들을 배열리스트에 넣고 리사이클러뷰로 보낼 준비
                }
                adapter.notifyDataSetChanged(); // 리스트 저장 및 새로고침해야 반영이 됨
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 디비를 가져오던중 에러 발생 시

            }
        });

        SttIntent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getApplicationContext().getPackageName());
        SttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");//한국어 사용
        mRecognizer= SpeechRecognizer.createSpeechRecognizer(cThis);
        mRecognizer.setRecognitionListener(listener2);

        fab = (FloatingActionButton)findViewById(R.id.fab_btn);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(cThis, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(SearchCoupangActivity.this,new String[]{Manifest.permission.RECORD_AUDIO},1);
                    //권한을 허용하지 않는 경우
                }else{
                    //권한을 허용한 경우
                    try {
                        mRecognizer.startListening(SttIntent);
                    }catch (SecurityException e){e.printStackTrace();}
                }
            }
        });

        adapter = new SearchRecordAdapter(arrayList, this);
        mRecyclerView.setAdapter(adapter); // 리사이클러뷰에 어댑터 연결

        et_search = findViewById(R.id.et_search);
        et_search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //Enter key Action
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String input = et_search.getText().toString();
                    //키패드 내리기
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);

                    mNow = System.currentTimeMillis();
                    mDate = new Date(mNow);

                    mFormatTime = new SimpleDateFormat("hh:mm:ss");
                    mFormatMonth = new SimpleDateFormat("MM");
                    mFormatDay = new SimpleDateFormat("dd");

                    String month = mFormatMonth.format(mDate);
                    String day = mFormatDay.format(mDate);
                    String time = mFormatTime.format(mDate);

                    addSearchRecord(input, Integer.parseInt(month), Integer.parseInt(day), time);

                    Intent intent = new Intent(SearchCoupangActivity.this, OnlineMallActivity.class);
                    intent.putExtra("now_search_product", input);
                    startActivity(intent);

                    return true;
                }
                return false;
            }

        });

        recordBtn = findViewById(R.id.btn_record);
        CheckPermission();  //녹음 퍼미션 체크

        //RecognizerIntent 객체 생성
        intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");   //한국어
    }

    private RecognitionListener listener2 =new RecognitionListener() {
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
                Intent intent = new Intent(SearchCoupangActivity.this, OnlineMallActivity.class);
                intent.putExtra("now_search_product", "bag");
                startActivity(intent);
            }else if (VoiceMsg.indexOf("마이페이지") > -1) {
                Intent intent = new Intent(SearchCoupangActivity.this, OnlineMallActivity.class);
                intent.putExtra("now_search_product", "my");
                startActivity(intent);
            } else {
                Intent coupangIntent = new Intent(SearchCoupangActivity.this, SearchCoupangActivity.class);
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
            Intent intent = new Intent(SearchCoupangActivity.this, OfflineMainActivity.class);
            startActivity(intent);
        }
    }

    public void addSearchRecord(String product, int month, int day, String time) {

        SearchRecordItem searchRecordItem = new SearchRecordItem();
        searchRecordItem.setProduct(product);
        searchRecordItem.setMonth(month);
        searchRecordItem.setDay(day - 31);

        mDatabase.child("UserSearchRecord").child(firebaseUser.getUid()).child(time).setValue(searchRecordItem);
    }

    public void startRecord(View view){
        if (!recording) {   //녹음 시작
            StartRecord();
            Toast.makeText(getApplicationContext(), "지금부터 음성으로 기록합니다.", Toast.LENGTH_SHORT).show();
        }
        else {  //이미 녹음 중이면 녹음 중지
            StopRecord();
        }
    }

    void CheckPermission() {
        //안드로이드 버전이 6.0 이상
        if ( Build.VERSION.SDK_INT >= 23 ){
            //인터넷이나 녹음 권한이 없으면 권한 요청
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED ) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.INTERNET,
                                Manifest.permission.RECORD_AUDIO},PERMISSION);
            }
        }
    }
    //녹음 시작
    void StartRecord() {
        recording = true;

        //마이크 이미지와 텍스트 변경
        recordBtn.setImageResource(R.drawable.stop_record);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        speechRecognizer.setRecognitionListener(listener);
        speechRecognizer.startListening(intent);
    }

    //녹음 중지
    void StopRecord() {
        recording = false;

        //마이크 이미지와 텍스트 변경
        recordBtn.setImageResource(R.drawable.mic);

        speechRecognizer.stopListening();   //녹음 중지
        Toast.makeText(getApplicationContext(), "음성 기록을 중지합니다.", Toast.LENGTH_SHORT).show();
    }

    RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {

        }

        @Override
        public void onBeginningOfSpeech() {
            //사용자가 말하기 시작
        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {
            //사용자가 말을 멈추면 호출
            //인식 결과에 따라 onError나 onResults가 호출됨
        }

        @Override
        public void onError(int error) {    //토스트 메세지로 에러 출력
            String message;
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    //message = "클라이언트 에러";
                    //speechRecognizer.stopListening()을 호출하면 발생하는 에러
                    return; //토스트 메세지 출력 X
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    //message = "찾을 수 없음";
                    //녹음을 오래하거나 speechRecognizer.stopListening()을 호출하면 발생하는 에러
                    //speechRecognizer를 다시 생성하여 녹음 재개
                    if (recording)
                        StartRecord();
                    return; //토스트 메세지 출력 X
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;
                default:
                    message = "알 수 없는 오류임";
                    break;
            }
            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message, Toast.LENGTH_SHORT).show();
        }

        //인식 결과가 준비되면 호출
        @Override
        public void onResults(Bundle bundle) {
            ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);	//인식 결과를 담은 ArrayList
            String originText = et_search.getText().toString();  //기존 text

            //인식 결과
            String newText="";
            for (int i = 0; i < matches.size() ; i++) {
                newText += matches.get(i);
            }

            et_search.setText(originText + newText + " ");	//기존의 text에 인식 결과를 이어붙임
            speechRecognizer.startListening(intent);    //녹음버튼을 누를 때까지 계속 녹음해야 하므로 녹음 재개
        }

        @Override
        public void onPartialResults(Bundle bundle) {

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    };
}