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
    private int count_interval_seconds = 3;
    private int usageStats_interval_seconds = 10;

    private Handler handler;
    private Timer count_timer = null;
    private Timer usage_interval_timer;

    private final String chrome_package_name = "com.android.chrome";
    private final String y_news_package_name = "jp.co.yahoo.android.news";
    private final String youtube_package_name = "com.google.android.youtube";

    private int over_use_count;
    private int limit = 15;

    private final String[] alternative_apps = {"com.moneyforward.android.app",
                                               "com.nowpro.nar03_f",
                                               "me.phrase.phrase",
                                               };
    private int app_select_num;

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
         * 一定秒毎にUsageStatsを取得してChromeの使用履歴があればカウントする
         * */
        usage_interval_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String now_foreground_app = getTopActivityPackageName();
                Log.d(TAG, "run: 現在最前面のアプリ ＝ "+ now_foreground_app);
                if(now_foreground_app.equals(chrome_package_name)
                    ||now_foreground_app.equals(y_news_package_name)
                    ||now_foreground_app.equals(youtube_package_name)){
                    over_use_count++;
                    Log.d(TAG, "run: over_use_count = " + over_use_count);
                    if(over_use_count >= limit) {
                        PackageManager pm = getPackageManager();
                        Intent intent = pm.getLaunchIntentForPackage(alternative_apps[app_select_num]);
                        if(app_select_num == alternative_apps.length-1){
                            app_select_num = 0;
                        }else{
                            app_select_num++;
                        }
                        try {
                            startActivity(intent);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MyService.this, "limit"+limit+"です", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "run: 指定したアプリは見つかりません", e);
                        }
                        over_use_count = 0;
                    }
                }
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
                    /*UsageStatsで取得できたアプリ履歴をすべて表示*/
                    Log.d(TAG, "package: " + usageStats.getPackageName());
                }
                /*UsageStatsで取得できたアプリ履歴の件数*/
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