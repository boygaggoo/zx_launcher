package com.ds05.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.ds05.launcher.common.ConnectUtils;
import com.ds05.launcher.service.LauncherService;
import com.ds05.launcher.service.WeatherService;

/**
 * Created by Chongyang.Hu on 2017/1/7 0007.
 */

public class UpgradeApkReceiver extends BroadcastReceiver {

    private  static final  String TAG="ZXH";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
            Intent newIntent = new Intent(context, MainActivity.class);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(newIntent);
        }
    }
}
