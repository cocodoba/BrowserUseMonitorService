package com.example.shinoharanaoki.browserusemonitorservice;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
    private int count_interval_seconds = 10;
    private int usageStats_interval_seconds = 10; //サービスの更新間隔

    private Handler handler;
    private Timer count_timer = null;
    private Timer usage_interval_timer;

    private final String chrome_package_name = "com.android.chrome";
    private int chrome_usagetime_counter; //Chrome使用時間1分につき＋１カウント
    private final int chrome_use_limit = 5; //Chrome使用制限時間

    private final String[] alternative_apps_packnames = {"com.google.android.calendar","com.google.android.maps"};//途中で起動するアプリのパッケージネーム


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
        /*count_timer.schedule(new TimerTask() {
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
        }, 0, 1000 * count_interval_seconds);*/

        /**
         *
         * 一定秒毎にUsageStatsを取得してChromeの使用履歴があれば出力する
         * */
        usage_interval_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String now_foreground_app = getTopActivityPackageName();
                Log.d(TAG, "run: 現在最前面で使用中のアプリ ＝ " + now_foreground_app);

                if (chrome_package_name.equals(now_foreground_app)){
                    chrome_usagetime_counter++;
                    Log.d(TAG, "run: chrome_usage_counter = " + chrome_usagetime_counter);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MyService.this, "Chrome Count = " + chrome_usagetime_counter, Toast.LENGTH_SHORT).show();
                        }
                    });

                    if(chrome_usagetime_counter >=chrome_use_limit){
                        PackageManager pm = getPackageManager();
                        Intent intent = pm.getLaunchIntentForPackage("com.google.android.apps.maps");
                        try{
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.d(TAG, "run: 対象のアプリが見つかりません");
                        }
                        chrome_usagetime_counter = 0;

                    }
                }
            }
        },0, 1000*usageStats_interval_seconds);

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
        if (!myStatsMap.isEmpty()) {Log.i(TAG, "getUsageStatsMap: UsageStatsにアクセスできました");}

        return myStatsMap;
    }

    private String getTopActivityPackageName() {
        String packageName = "";
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> list = am.getRunningAppProcesses();
            packageName = list.get(0).processName;
        } else {
            UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long endTime = System.currentTimeMillis();
            long beginTime = endTime - 7 * 24 * 60 * 60 * 1000;
            List<UsageStats> list = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, beginTime, endTime);
            if (list != null && list.size() > 0) {
                SortedMap<Long, UsageStats> map = new TreeMap<>();
                for (UsageStats usageStats : list) {
                    map.put(usageStats.getLastTimeUsed(), usageStats);
                    Log.d(TAG, "package: " + usageStats.getPackageName());
                }
                Log.d(TAG, "size: " + map.size());
                if (!map.isEmpty()) {
                    packageName = map.get(map.lastKey()).getPackageName();
                }
            }
        }
        Log.d(TAG, "Current packageName: " + packageName);

        return packageName;
    }


}