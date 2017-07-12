package com.ds05.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ds05.launcher.common.ConnectUtils;
import com.ds05.launcher.service.LauncherService;
import com.ds05.launcher.service.WeatherService;

/**
 * Created by Chongyang.Hu on 2017/1/7 0007.
 */

public class BootReceiver extends BroadcastReceiver {

    private  static final  String TAG="BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {


        Log.e(TAG, "########################################################################  initClientMina ,当前的网络状态是："+ ConnectUtils.NETWORK_IS_OK);


        String action = intent.getAction();
        if(action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            context.startService(new Intent(LauncherApplication.getContext(), WeatherService.class));
            // 增加了网路心跳service by Vincent
          //  context.startService(new Intent(LauncherApplication.getContext(), ConnectSocketService.class));
            context.startService(new Intent(LauncherApplication.getContext(), LauncherService.class));
        }
    }
}
