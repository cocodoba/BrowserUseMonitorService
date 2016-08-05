package com.example.shinoharanaoki.browserusemonitorservice;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class CheckAppsSelectActivity extends AppCompatActivity {

    private static final String TAG = "CheckAppsSelectActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_apps_select);

        /*端末にインストール済のアプリケーション一覧情報を取得*/
        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> installedAppInfos = pm.getInstalledApplications(0);

        int i = 0;
        for(ApplicationInfo info : installedAppInfos){
            /*//アプリケーションにSYSTEMフラグが立っていたら(0000 0001)それはプリインストールされたアプリである。*/
            Log.i(TAG, "onCreate: " + "AppName = "+ info.loadLabel(pm) + " PackName = " + info.packageName);
            i++;
            if((info.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM){
                Log.i(TAG, "PREINSTALLED");
                if(pm.getLaunchIntentForPackage(info.packageName) == null){
                    Log.i(TAG, "NOT LAUNCHABLE");
                }
            }
        }
        Log.i(TAG, "onCreate: InstalledAppsSize(NOT preInstalled) = " + i);
    }
}
