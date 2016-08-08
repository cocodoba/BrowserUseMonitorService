package com.example.shinoharanaoki.browserusemonitorservice;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import android.os.IBinder;
import android.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class MyService extends Service {
    private static final String TAG = "MyService";

    private static int SERVICE = 0;
    private static final int OFF = 0;
    private static final int ON = 1;

    private SharedPreferences mPreference;


    private Handler handler;
    private Timer usage_interval_timer;

    private String[] usage_checked_package_names;
    private int usageStats_interval_seconds = 10;//TODO Setting
    private int over_use_count;
    private int limit = 38; //TODO Setting

    private String[] alternative_apps;
                                            /*= {"com.hellochinese",
                                               "com.nowpro.nar03_f",
                                               "me.phrase.phrase",
                                               "link.mikan.mikanandroid",
                                               "com.github.client",
                                               "com.mintflag.hatuonatoz"};//TODO UserSelect*/
    private int app_select_num;

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
        mPreference = PreferenceManager.getDefaultSharedPreferences(this);

        /**
         * チェックするアプリの配列を用意する
         * */
        Set<String> check_package_name_set = mPreference.getStringSet("CHECK_APPS", new HashSet<String>());
        if (!check_package_name_set.isEmpty()) {
            usage_checked_package_names = new String[check_package_name_set.size()  +1 ];//FIXME この+1は、YahooNewsなどが設定リストに反映されないのでその応急処置
            /**HashSetを配列に順次変換*/
            int index = 0;//FIXME indexを使わない方法は？
            for (String package_name : check_package_name_set) {
                usage_checked_package_names[index] = package_name;
                Log.d(TAG, "onCreate: usage_checked_package_names = " + "["+index+"]" + package_name);
                index++;
            }
            //FIXME YahooNewsなどが設定リストに反映されないのでその応急処置
            usage_checked_package_names[index] ="jp.co.yahoo.android.news";
            Log.d(TAG, "onCreate: usage_checked_package_names = " + "["+(index)+"]" + usage_checked_package_names[index]);
        } else {
            Log.d(TAG, "onCreate: getStringSet(\"CHECK_APPS\") is empty");
            usage_checked_package_names = new String[0]; //これが無いとNullPointer
        }

        /**
         * 起動するアプリの配列を用意する
         * */
        Set<String> break_package_name_set = mPreference.getStringSet("BREAK_APPS", new HashSet<String>());
        if (!break_package_name_set.isEmpty()) {
            alternative_apps = new String[break_package_name_set.size()];
            /**HashSetを配列に順次変換*/
            int index = 0;//FIXME indexを使わない方法は？
            for (String package_name : break_package_name_set) {
                alternative_apps[index] = package_name;
                Log.d(TAG, "onCreate: alternative_apps = " + "["+index+"]" + package_name);
                index++;
            }
        } else {
            Log.d(TAG, "onCreate: getStringSet(\"BREAK_APPS\") is empty");
            alternative_apps = new String[0]; //これが無いとNullPointer
        }
        limit = mPreference.getInt("LIMIT", 38);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: ");

        /**
         * 参考:Handlerクラスの正しい使い方（Androidでスレッド間通信）
         *     http://d.hatena.ne.jp/sankumee/20120329/1333021847
         * */
        handler = new Handler();
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        /**
         * 一定秒毎にUsageStatsを取得してChromeやYoutubeの使用履歴があればカウントする
         * */
        if (SERVICE == OFF && usage_checked_package_names.length != 0) {
            usage_interval_timer = new Timer();
            usage_interval_timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (pm.isInteractive()) { //端末スリープ中は履歴の取得を止める
                        String now_foreground_app = getTopActivityPackageName();
                        Log.d(TAG, "run: 現在最前面のアプリ ＝ " + now_foreground_app);
                        //DONE 拡張forに
                        for(String package_name : usage_checked_package_names)
                        if (now_foreground_app.equals(package_name)) {
                            over_use_count++;
                            Log.d(TAG, "run: over_use_count = " + over_use_count);
                            if (over_use_count >= limit) {
                                PackageManager pm = getPackageManager();
                                Intent intent = pm.getLaunchIntentForPackage(alternative_apps[app_select_num]);
                                if (app_select_num == alternative_apps.length - 1) {
                                    app_select_num = 0;
                                } else {
                                    app_select_num++;
                                }
                                try {
                                    startActivity(intent);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MyService.this, "limit" + limit + "です", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } catch (Exception e) {
                                    Log.e(TAG, "run: 指定したアプリは見つかりません", e);
                                }
                                over_use_count = 0;
                            }
                        }
                    }
                }
            },0, 1000 * usageStats_interval_seconds);
            SERVICE = ON;
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyService.this, "チェック対象アプリが設定されていないか、すでにサービス実行中です", Toast.LENGTH_LONG).show();
                }
            });
        }

        //明示的にサービスの起動、停止が決められる場合の返り値
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (SERVICE == ON) {
            usage_interval_timer.cancel();
            SERVICE = OFF;
        }

        //FIXME 要らない？ ＜ーーここから
        /**配列をにHashSetに順次変換*/
        Set<String> check_package_name_set  = new HashSet<>();
        if (usage_checked_package_names.length != 0) {
            for (String package_name : usage_checked_package_names) {
                check_package_name_set.add(package_name);
            }
        }
        /**
         * チェックアプリ・起動アプリリスト、更新間隔、制限時間をPreferenceに保存
         * */
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putStringSet("CHECK_APPS", check_package_name_set);
        //TEST
        editor.putInt("LIMIT",35);
        editor.commit();  //TODO commit() OR Apply() ?


        for (String package_name : check_package_name_set) {
            Log.d(TAG, "onDestroy: check_package_name_set = " + package_name);
        }
        Log.d(TAG, "onDestroy: SharedPreferences.editor.commit()");
        //FIXME ここまでーー＞ 要らない？
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
            long beginTime = endTime - 7 * 24 * 60 * 60 * 1000; //FIXME 短くしても動作する?
            List<UsageStats> list = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, beginTime, endTime);
            if (list != null && list.size() > 0) {
                SortedMap<Long, UsageStats> map = new TreeMap<>();
                for (UsageStats usageStats : list) {
                    map.put(usageStats.getLastTimeUsed(), usageStats);
                    /*UsageStatsで取得できたアプリ履歴をすべて表示*/
                    //Log.d(TAG, "package: " + usageStats.getPackageName());
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