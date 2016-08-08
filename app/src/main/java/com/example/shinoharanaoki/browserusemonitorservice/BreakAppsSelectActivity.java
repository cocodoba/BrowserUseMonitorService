package com.example.shinoharanaoki.browserusemonitorservice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BreakAppsSelectActivity extends AppCompatActivity {

    private SharedPreferences mPreference;

    private static final String TAG = "BreakAppsSelectActivity";

    private static final int ARRAYS = 2;

    private static final int ARRAY_APP_NAME = 0;
    private static final int ARRAY_PACKAGE_NAME = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_apps_select);
        mPreference = PreferenceManager.getDefaultSharedPreferences(this);

        Intent i = new Intent(BreakAppsSelectActivity.this, MyService.class);
        stopService(i);

        final String app_data[][];

        /*端末にインストール済のアプリケーション一覧情報を取得*/
        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> installedAppInfos = pm.getInstalledApplications(0);
        /*ユーザーが起動できるアプリを抽出*/
        List<ApplicationInfo> selectableAppInfos = new ArrayList<>();
        for(ApplicationInfo info : installedAppInfos){
            if(pm.getLaunchIntentForPackage(info.packageName) != null){
                Log.i(TAG, "onCreate: " + "AppName = "+ info.loadLabel(pm) + " PackName = " + info.packageName);
                selectableAppInfos.add(info);
            }
        }

        /*ApplicationInfoからアプリ名を取得して配列に格納*/
        app_data = new String[ARRAYS][];
        app_data[ARRAY_APP_NAME] = new String[selectableAppInfos.size()];
        app_data[ARRAY_PACKAGE_NAME] = new String[selectableAppInfos.size()];
        int index = 0;//FIXME indexを使わない方法は？
        for (ApplicationInfo info : selectableAppInfos) {
            app_data[ARRAY_APP_NAME][index] = (String)info.loadLabel(pm);
            app_data[ARRAY_PACKAGE_NAME][index] = info.packageName;
            Log.i(TAG, "onCreate: selectable_App_names = " + "["+index+"]" + app_data[ARRAY_APP_NAME][index]);
            Log.i(TAG, "onCreate: selectable_App_package = " + "["+index+"]" + app_data[ARRAY_PACKAGE_NAME][index]);
            index++;
        }

        // アイテムをアダプタにセット
        //TODO 既に設定済みのアプリをチェック状態にする
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_multiple_choice, app_data[ARRAY_APP_NAME]);

        // リストビューにアイテム (adapter) を追加
        final ListView mListView = (ListView)findViewById(R.id.listView1);
        mListView.setAdapter(adapter);

        /**「android ListView 複数リスト 選択した値を取得する”」
         *   http://k-1-ne-jp.blogspot.jp/2013/09/android-listview.html*/

        /**
         * リストの項目をクリックしたときの処理
         * @params position タッチした場所（一番上は0）
         */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){

                //TEST クリックされたアイテムのポジションを確認 （一番上は0）
                Toast.makeText( BreakAppsSelectActivity.this, "clicked position = " + position + "  (1番上は0)", Toast.LENGTH_SHORT ).show();

                /*
                getCheckedItemPositions() ... 一度でもチェックされた項目の配列を取得する
                名前的に現在チェックしている項目の配列を取得してくれそうだけど、そうではない事に注意

                つまり現在何も選択されていないのに、結果的に全てのアイテムがcheckedに入ったままな状況もありうる*/
                SparseBooleanArray checked = mListView.getCheckedItemPositions();
                StringBuilder sb = new StringBuilder();

                //★配列の個数分ループ
                for( int index = 0; index < checked.size(); index++ ) {
                    //valueAtでチェックされていればtrueが返ってくる
                    if (checked.valueAt(index)) {
                        int key = checked.keyAt(index);//チェックされている配列のキーを取得
                        sb.append("\""+app_data[ARRAY_APP_NAME][key]+"\"" + ",  ");//もともとの配列から値を取得する
                        Log.d(TAG, "onItemClick: app_data[ARRAY_PACKAGE_NAME] = " + app_data[ARRAY_PACKAGE_NAME][key]);
                    }
                }
                if (sb.length()>0) {
                    Toast.makeText( BreakAppsSelectActivity.this, "NOW SELECTING: " + sb.substring(0, sb.length()-1), Toast.LENGTH_LONG ).show();
                }
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
                SparseBooleanArray checked = mListView.getCheckedItemPositions();

                Log.i(TAG, "onClick: checked.size() = " + checked.size());

                /*チェックされたアイテムの文字列を生成
                checked には、「チェックされているアイテム」ではなく、
                「一度でもチェックされたアイテム」が入ってくる。
                なので、現在チェックされているかどうかを valutAt の戻り値
                で判定する必要がある！！！*/
                StringBuilder sb = new StringBuilder();
                Set<String> check_package_name_set  = new HashSet<>();

                for (int index=0; index<checked.size(); index++) {
                    if (checked.valueAt(index)) {
                        int key_of_list_position = checked.keyAt(index);//リストビューの中での元々の順番を取得
                        /**チェックしたアプリのパッケージ名をHashSetに保存する*/
                        String package_name = app_data[ARRAY_PACKAGE_NAME][key_of_list_position];
                        check_package_name_set.add(package_name);
                        Log.i(TAG, "onClick: check_package_name_set.add ..." + package_name);

                        sb.append("\""+app_data[ARRAY_APP_NAME][key_of_list_position]+"\""+  " is String from item["+String.valueOf(key_of_list_position)+"]" + ",  ");
                    }
                }
                // 通知
                Toast.makeText(BreakAppsSelectActivity.this,
                        sb.toString(), Toast.LENGTH_LONG).show();

                /**
                 * チェックアプリをPreferenceに保存
                 * */
                //FIXME 一度Preferenceのデータを消去したほうがいい
                SharedPreferences.Editor editor = mPreference.edit();
                editor.remove("BREAK_APPS");
                editor.commit();
                editor.putStringSet("BREAK_APPS", check_package_name_set);
                editor.commit();  //TODO commit() OR Apply() ?

                Toast.makeText(BreakAppsSelectActivity.this, "起動するアプリを保存しました", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
