package com.example.fingertip;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.view.GestureDetector;
import android.os.Handler;


public class OnlineMallActivity extends AppCompatActivity {

    private WebView mWebView; // 웹뷰 선언
    private WebSettings mWebSettings; //웹뷰세팅

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

        // 웹뷰 시작
        mWebView = (WebView) findViewById(R.id.webView);

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

        mWebView.loadUrl("https://www.coupang.com"); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작

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
