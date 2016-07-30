package com.example.shinoharanaoki.browserusemonitorservice;

import android.app.Service;
import android.content.Intent;
<<<<<<< HEAD
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
=======
import android.os.IBinder;
import android.util.Log;
>>>>>>> origin/master

import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {
    private static final String TAG = "MyService";

<<<<<<< HEAD
    private static boolean service_started = false;

    private Timer timer = null;
    private int count = 0;//テスト用！！
    private int interval_seconds = 3;

    private Handler handler;
=======
    private Timer timer = null;
    private int count = 0;//テスト用！！
    private int interval_seconds = 30;
>>>>>>> origin/master

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
<<<<<<< HEAD

        timer = new Timer();

        /**
         * 参考:Handlerクラスの正しい使い方（Androidでスレッド間通信）
         *     http://d.hatena.ne.jp/sankumee/20120329/1333021847
         * */
        handler = new Handler();

=======
        timer = new Timer();

>>>>>>> origin/master
        /*
        * TEST サービス駆動確認用*/
        timer.schedule( new TimerTask(){
            @Override
            public void run(){
                Log.d( "Count Test" , "count = "+ count );
<<<<<<< HEAD
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyService.this, "count = "+ count, Toast.LENGTH_SHORT).show();
                    }
                });
                count++;
            }
        }, 0, 1000*interval_seconds);

        //明示的にサービスの起動、停止が決められる場合の返り値
        return START_STICKY;
=======
                count++;
            }
        }, 0, 1000);

        return super.onStartCommand(intent, flags, startId);
>>>>>>> origin/master
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
<<<<<<< HEAD
        timer.cancel();
=======
>>>>>>> origin/master
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
