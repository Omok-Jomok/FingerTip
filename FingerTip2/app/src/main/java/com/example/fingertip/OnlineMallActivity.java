package com.example.fingertip;

import static android.speech.tts.TextToSpeech.ERROR;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.view.GestureDetector;
import android.os.Handler;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class OnlineMallActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private WebView mWebView; // 웹뷰 선언
    private WebSettings mWebSettings; //웹뷰세팅
    private EditText et_search;
    private ImageButton btn_record;
    private FirebaseUser firebaseUser;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    SpeechRecognizer speechRecognizer;

    Intent intent;
    final int PERMISSION = 1;	//permission 변수
    boolean recording = false;  //현재 녹음중인지 여부

    long mNow;
    Date mDate;

    SimpleDateFormat mFormatMonth;
    SimpleDateFormat mFormatDay;
    SimpleDateFormat mFormatTime;

    GestureDetector gestureDetector = null;
    GestureDetector gestureDetector2 = null;
    static final int SWIPE_MIN_DISTANCE = 120; //swipe 인식 최소 거리
    static final int SWIPE_MAX_OFF_PATH = 400; //swipe 인식 최대 거리
    static final int SWIPE_THRESHOLD_VELOCITY = 200; //swipe 인식 최소 속도
    private Boolean x = false;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_mall);

        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);

        mFormatTime = new SimpleDateFormat("hh:mm:ss");
        mFormatMonth = new SimpleDateFormat("MM");
        mFormatDay = new SimpleDateFormat("dd");

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("FingerTip");
        firebaseUser = mAuth.getCurrentUser();

        String month = mFormatMonth.format(mDate);
        String day = mFormatDay.format(mDate);
        String time = mFormatTime.format(mDate);

        Intent intent2 = getIntent(); //전달할 데이터를 받을 Intent
        //text 키값으로 데이터를 받는다. String을 받아야 하므로 getStringExtra()를 사용함
        String text = intent2.getStringExtra("now_search_product");
        // 웹뷰 시작
        mWebView = (WebView) findViewById(R.id.webView);

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

        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view,String url){
                //터치를 했을때 작동하는 메서드
                mWebView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        gestureDetector.onTouchEvent(event);
                        return false;
                    }
                });

                gestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.OnGestureListener() {
                    @Override
                    public boolean onDown(MotionEvent motionEvent) {
                            return false;
                        }

                    @Override
                    public void onShowPress(MotionEvent motionEvent) {
                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent motionEvent) {
                            return false;
                        }

                    @Override
                    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                        return false;
                    }

                    @Override
                    public void onLongPress(MotionEvent motionEvent) {
                    }

                    @Override
                    public boolean onFling(MotionEvent motionEvent1, MotionEvent motionEvent2, float v, float v1) {
                        if(url.contains("/nm/search?") || url.contains("/np/search?")){ //상품 검색 페이지
                            try {
                                if (Math.abs(motionEvent1.getY() - motionEvent2.getY()) > SWIPE_MAX_OFF_PATH) //swipe 최대거리를 넘었을 때
                                    return false;

                                // right to left swipe
                                if (motionEvent1.getX() - motionEvent2.getX() > SWIPE_MIN_DISTANCE
                                        && Math.abs(v) > SWIPE_THRESHOLD_VELOCITY) {
                                    x = true;
                                    System.out.println("swipe recognize");
                                    touch_force(motionEvent1.getX(),motionEvent1.getY()); //강제 터치
                                }
                                // left to right swipe
                                else if (motionEvent2.getX() - motionEvent1.getX() > SWIPE_MIN_DISTANCE
                                        && Math.abs(v) > SWIPE_THRESHOLD_VELOCITY) {
                                }
                                // down to up swipe
                                else if (motionEvent1.getY() - motionEvent2.getY() > SWIPE_MIN_DISTANCE
                                        && Math.abs(v1) > SWIPE_THRESHOLD_VELOCITY) {
                                }
                                // up to down swipe
                                else if (motionEvent2.getY() - motionEvent1.getY() > SWIPE_MIN_DISTANCE
                                        && Math.abs(v1) > SWIPE_THRESHOLD_VELOCITY) {
                                }
                            } catch (Exception e) {
                            }
                        }
                            return false;
                        }
                    });
                if(url.contains("/vm/products") || url.contains("/vp/products")){ //모바일 웹뷰 상품 페이지
                    if(x){ //swipe -> 상품페이지를 넘어 왔을 때
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(url.contains("/vm/")) { // 모바일 전용 웹뷰
                                    test1(); // 터치 다운
                                    test2(); // 터치 업
                                }
                                else if(url.contains("/vp/")) { // pc 버전 웹뷰
                                    test3(); // 터치 다운
                                    test4(); // 터치 업
                                }
                            }
                        },5000);	//4000밀리 초 동안 딜레이
                    }
                }
                else {
                    x = false;
                }
                return super.shouldOverrideUrlLoading(view,url);
            }
        }); // 클릭시 새창 안뜨게

        mWebSettings = mWebView.getSettings(); //세부 세팅 등록
        mWebSettings.setJavaScriptEnabled(true); // 웹페이지 자바스클비트 허용 여부
        mWebSettings.setSupportMultipleWindows(false); // 새창 띄우기 허용 여부
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(false); // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        mWebSettings.setLoadWithOverviewMode(true); // 메타태그 허용 여부
        mWebSettings.setUseWideViewPort(true); // 화면 사이즈 맞추기 허용 여부
        mWebSettings.setSupportZoom(false); // 화면 줌 허용 여부
        mWebSettings.setBuiltInZoomControls(false); // 화면 확대 축소 허용 여부
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기
        mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부
        mWebSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부

        mWebView.loadUrl("https://www.coupang.com/np/search?component=&q=" + text + "&channel=user"); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작

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

                    mWebView.loadUrl("https://www.coupang.com/np/search?component=&q=" + input + "&channel=user"); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작

                    return true;
                }
                return false;
            }

        });

        btn_record = findViewById(R.id.btn_record);
        CheckPermission();  //녹음 퍼미션 체크

        //RecognizerIntent 객체 생성
        intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");   //한국어
    }

    public void startRecord2(View view){
        if (!recording) {   //녹음 시작
            StartRecord2();
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
    void StartRecord2() {
        recording = true;

        //마이크 이미지와 텍스트 변경
        btn_record.setImageResource(R.drawable.stop_record);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        speechRecognizer.setRecognitionListener(listener);
        speechRecognizer.startListening(intent);
    }

    //녹음 중지
    void StopRecord() {
        recording = false;

        //마이크 이미지와 텍스트 변경
        btn_record.setImageResource(R.drawable.mic);

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
                        StartRecord2();
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

    public void addSearchRecord(String product, int month, int day, String time) {

        SearchRecordItem searchRecordItem = new SearchRecordItem();
        searchRecordItem.setProduct(product);
        searchRecordItem.setMonth(month);
        searchRecordItem.setDay(day - 31);

        mDatabase.child("UserSearchRecord").child(firebaseUser.getUid()).child(time).setValue(searchRecordItem);
    }



    private void touch_force(float x, float y) {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        int metaState = 0;

        MotionEvent motionEvent1 = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_DOWN,
                x,
                y,
                metaState
        );
        MotionEvent motionEvent2 = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_UP,
                x,
                y,
                metaState
        );

        mWebView.dispatchTouchEvent(motionEvent1);
        mWebView.dispatchTouchEvent(motionEvent2);
    }

    private void test1() {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        float x = 170.0f;
        float y = 1970.0f;
        int metaState = 0;

        MotionEvent motionEvent1 = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_DOWN,
                x,
                y,
                metaState
        );

        mWebView.dispatchTouchEvent(motionEvent1);
    }

    private void test2() {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        float x = 170.0f;
        float y = 1970.0f;
        int metaState = 0;

        tts.speak("장바구니에 상품이 담겼습니다",TextToSpeech.QUEUE_FLUSH, null);

        MotionEvent motionEvent1 = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_UP,
                x,
                y,
                metaState
        );

        mWebView.dispatchTouchEvent(motionEvent1);
    }

    private void test3() {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        float x = 975.0f;
        float y = 244.0f;
        int metaState = 0;

        MotionEvent motionEvent1 = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_DOWN,
                x,
                y,
                metaState
        );

        mWebView.dispatchTouchEvent(motionEvent1);
    }

    private void test4() {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        float x = 975.0f;
        float y = 244.0f;
        int metaState = 0;

        MotionEvent motionEvent1 = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_UP,
                x,
                y,
                metaState
        );

        mWebView.dispatchTouchEvent(motionEvent1);
    }

}
