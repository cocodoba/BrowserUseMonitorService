package com.example.shinoharanaoki.browserusemonitorservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /*
    * サービス開始ボタン
    */
    public void onStartClick(View view) {
        Intent i = new Intent(this, MyService.class);
        startService(i);

        Log.i(TAG, "onStartClick: ");

    }

    /*
     * サービス停止ボタン
     */
    public void onStopClick(View view) {
        Intent i = new Intent(this, MyService.class);
        stopService(i);

        Log.i(TAG, "onStopClick: ");
    }
}
