package com.example.shinoharanaoki.browserusemonitorservice;

import android.app.AppOpsManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


/**
 * Lisence: MIT
 * 実業務でちゃんと使えるAndroidアプリ開発入門（1）：http://www.atmarkit.co.jp/ait/articles/1602/01/news156.html
 *
 * Source Code
 * https://github.com/satosystems/atmarkit-android/blob/master/No01/app/src/main/java/com/example/atmarkit/no01/MainActivity.java
*/
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_SETTINGS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SETTINGS) {
            startMyService();
        }
    }

    /*
    * サービス開始ボタン
    */
    public void onStartClick(View view) {
        startMyService();

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

    private void startMyService() {
        Intent intent = null;
        if (!canGetUsageStats()) {
            intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        }
        if (intent != null) {
            startActivityForResult(intent, REQUEST_SETTINGS);
            Toast.makeText(getApplicationContext(), "Please turn ON", Toast.LENGTH_SHORT).show();
        } else {
            startService(new Intent(MainActivity.this, MyService.class));
        }
    }

    public boolean canGetUsageStats() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return true;
        }
        AppOpsManager aom = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
        int uid = android.os.Process.myUid();
        int mode = aom.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, uid, getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
}
