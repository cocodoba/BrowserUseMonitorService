package com.example.shinoharanaoki.browserusemonitorservice;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CheckAppsSelectActivity extends AppCompatActivity {

    private static final String TAG = "CheckAppsSelectActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_apps_select);

        //final String[] item; //FIXME name
        final String[] item = new String [] {
                "listView item 1",
                "listView item 2",
                "listView item 3",
                "listView item 4",
                "listView item 5"
        };

        /*端末にインストール済のアプリケーション一覧情報を取得*/
        /*final PackageManager pm = getPackageManager();
        List<ApplicationInfo> installedAppInfos = pm.getInstalledApplications(0);
        int i = 1;
        List<ApplicationInfo> selectableAppInfos = new ArrayList<>();
        for(ApplicationInfo info : installedAppInfos){
            if(pm.getLaunchIntentForPackage(info.packageName) != null){
                Log.i(TAG, "onCreate: " + "AppName = "+ info.loadLabel(pm) + " PackName = " + info.packageName);
                selectableAppInfos.add(info);
                i++;
            }
        }*/
        /*item = new String[i];
        int index = 0;//FIXME indexを使わない方法は？
        for (ApplicationInfo info : selectableAppInfos) {
            item[index] = (String)info.loadLabel(pm);
            Log.d(TAG, "onCreate: selectable_App_names = " + "["+index+"]" + item[index]);
            index++;
        }*/

        // 追加するアイテムを生成する
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_multiple_choice, item);

        // リストビューにアイテム (adapter) を追加
        ListView listView1 = (ListView)findViewById(R.id.listView1);
        listView1.setAdapter(adapter);

        // ボタンクリックイベント
        Button btn = (Button)findViewById(R.id.btnOk);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // 選択アイテムを取得
                ListView listView1 = (ListView)findViewById(R.id.listView1);
                SparseBooleanArray checked = listView1.getCheckedItemPositions();

                /*チェックされたアイテムの文字列を生成
                checked には、「チェックされているアイテム」ではなく、
                「一度でもチェックされたアイテム」が入ってくる。
                なので、現在チェックされているかどうかを valutAt の戻り値
                で判定する必要がある！！！*/
                StringBuilder sb = new StringBuilder();
                for (int i=0; i<checked.size(); i++) {
                    if (checked.valueAt(i)) {
                        /**i+1としないとなぜか1つ少ないポジションのアイテムが参照される*/
                        sb.append(item[i+1] + "["+String.valueOf(i+1)+"]" + ",");
                    }
                }
                // 通知
                Toast.makeText(CheckAppsSelectActivity.this,
                        sb.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
