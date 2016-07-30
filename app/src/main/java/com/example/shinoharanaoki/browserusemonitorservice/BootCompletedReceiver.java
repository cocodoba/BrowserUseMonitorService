package com.example.shinoharanaoki.browserusemonitorservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by shinoharanaoki on 2016/07/28.
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = BootCompletedReceiver.class.getSimpleName();
    private final BootCompletedReceiver self = this;

    @Override
    public void onReceive(Context context, Intent intent){
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            Intent serviceIntent = new Intent(context, MyService.class);
            context.startService(serviceIntent);

            Log.i(TAG, "onReceive: ");
            // Boot completed!
        }
    }
}
