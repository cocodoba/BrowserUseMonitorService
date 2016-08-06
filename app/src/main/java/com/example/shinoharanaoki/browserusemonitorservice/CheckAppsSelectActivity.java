package com.example.shinoharanaoki.browserusemonitorservice;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
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
                "listView item 5",
                "listView item 6",
                "listView item 7",
                "listView item 8",
                "listView item 9",
                "listView item 10",
                "listView item 11",
                "listView item 12",
                "listView item 13",
                "listView item 14",
                "listView item 15"
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
        final ListView listView1 = (ListView)findViewById(R.id.listView1); //FIXME name
        listView1.setAdapter(adapter);

        /**「android ListView 複数リスト　選択した値を取得する”」
         *   http://k-1-ne-jp.blogspot.jp/2013/09/android-listview.html*/

        /**
         * リストの項目をクリックしたときの処理
         * @params position タッチした場所（一番上は0）
         */
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){

                //TEST クリックされたアイテムのポジションを確認 （一番上は0）
                Toast.makeText( CheckAppsSelectActivity.this, "clicked position = " + position + "  (1番上は0)", Toast.LENGTH_SHORT ).show();

                /*
                getCheckedItemPositions() ... 一度でもチェックされた項目の配列を取得する
                名前的に現在チェックしている項目の配列を取得してくれそうだけど、そうではない事に注意

                つまり現在何も選択されていないのに、結果的に全てのアイテムがcheckedに入ったままな状況もありうる*/
                SparseBooleanArray checked = listView1.getCheckedItemPositions();
                StringBuilder sb = new StringBuilder();

                //★配列の個数分ループ
                for( int index = 0; index < checked.size(); index++ ) {
                    //valueAtでチェックされていればtrueが返ってくる
                    if (checked.valueAt(index)) {
                        int key = checked.keyAt(index);//チェックされている配列のキーを取得
                        sb.append("\""+item[key]+"\"" + ",  ");//もともとの配列から値を取得する
                    }
                }
                Toast.makeText( CheckAppsSelectActivity.this, "NOW SELECTING: " + sb.substring(0, sb.length()-1), Toast.LENGTH_LONG ).show();

                //TEST 一度でも選択されたアイテムの数を出力
                //★配列の個数分ループ
                for( int index = 0; index < checked.size(); index++ ) {
                    Log.i(TAG, "onItemClick: " + checked.keyAt(index));
                }
            }
        });

        // ボタンクリックイベント
        Button btn = (Button)findViewById(R.id.btnOk);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // チェックされたアイテムと、配列でのそのアイテムの元々の順番を取得
                SparseBooleanArray checked = listView1.getCheckedItemPositions();

                Log.i(TAG, "onClick: checked.size() = " + checked.size());

                /*チェックされたアイテムの文字列を生成
                checked には、「チェックされているアイテム」ではなく、
                「一度でもチェックされたアイテム」が入ってくる。
                なので、現在チェックされているかどうかを valutAt の戻り値
                で判定する必要がある！！！*/
                StringBuilder sb = new StringBuilder();

                for (int index=0; index<checked.size(); index++) {
                    if (checked.valueAt(index)) {
                        int key_of_list_position = checked.keyAt(index);//リストビューの中での元々の順番を取得
                        sb.append("\""+item[key_of_list_position]+"\""+  " is String from item["+String.valueOf(key_of_list_position)+"]" + ",  ");
                    }
                }
                // 通知
                Toast.makeText(CheckAppsSelectActivity.this,
                        sb.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
