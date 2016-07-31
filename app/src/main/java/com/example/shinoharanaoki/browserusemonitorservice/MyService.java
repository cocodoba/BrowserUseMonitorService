package com.example.shinoharanaoki.browserusemonitorservice;

import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class MyService extends Service {
    private static final String TAG = "MyService";

    private int count = 0;//テスト用！！
    private int count_interval_seconds = 3;
    private int usageStats_interval_seconds = 15;

    private Handler handler;
    private Timer count_timer = null;
    private Timer usage_interval_timer;
    private final String chrome_package_name = "chrome";

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
        count_timer = new Timer();
        usage_interval_timer = new Timer();

        /**
         * 参考:Handlerクラスの正しい使い方（Androidでスレッド間通信）
         *     http://d.hatena.ne.jp/sankumee/20120329/1333021847
         * */
        handler = new Handler();

        /*
        * TEST サービス駆動確認用 LogとToastを数秒ごとに表示*/
        count_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("run: Count Test", "count = " + count);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyService.this, "count = " + count, Toast.LENGTH_SHORT).show();
                    }
                });
                count++;
            }
        }, 0, 1000 * count_interval_seconds);

        /**
         *
         * 一定秒毎にUsageStatsを取得してChromeの使用履歴があれば出力する
         * */
        usage_interval_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Map<String, UsageStats> myStatsMap = getUsageStatsMap();
                if(myStatsMap.isEmpty()!= true){
                    Log.i(TAG, "run: stats exist:" + myStatsMap.toString());
                }else{
                    Log.i(TAG, "run: stats is empty");
                }
                if (myStatsMap.containsKey(chrome_package_name)) {
                    final long chrome_used_time = myStatsMap.get(chrome_package_name).getTotalTimeInForeground();
                    Log.d(TAG, "run: chrome usage found:" + String.valueOf(chrome_used_time) + "milliseconds");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MyService.this, "Chrome Usage Found!: " + String.valueOf(chrome_used_time/1000)+"秒", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.d(TAG, "run: 指定したキーは存在しません:" + myStatsMap.get(chrome_package_name));
                }
                Log.d(TAG, "run: monitor usage stats");
            }
        },0, 1000 * usageStats_interval_seconds);

        //明示的にサービスの起動、停止が決められる場合の返り値
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        count_timer.cancel();
        usage_interval_timer.cancel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * OSからアプリ使用履歴情報を取得
     *
     * @return Map<Stringパッケージ名, UsageStats使用履歴>
     */
    private Map<String, UsageStats> getUsageStatsMap() {

        Map<String, UsageStats> myStatsMap;

        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();

        // We get usage stats for the last interval
        myStatsMap = mUsageStatsManager.queryAndAggregateUsageStats(time - 1000*usageStats_interval_seconds, time);
        // Sort the stats by the last time used
        if (!myStatsMap.isEmpty()) {Log.i(TAG, "getUsageStatsMap: stats != empty");}

        return myStatsMap;
    }


}