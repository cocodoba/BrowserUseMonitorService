package com.example.shinoharanaoki.browserusemonitorservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {
    private static final String TAG = "MyService";

    private Timer timer = null;
    private int count = 0;//テスト用！！
    private int interval_seconds = 30;

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "onStartCommand: ");
        timer = new Timer();

        /*
        * TEST サービス駆動確認用*/
        timer.schedule( new TimerTask(){
            @Override
            public void run(){
                Log.d( "Count Test" , "count = "+ count );
                count++;
            }
        }, 0, 1000);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
