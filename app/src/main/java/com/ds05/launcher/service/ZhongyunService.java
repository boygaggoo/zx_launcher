package com.ds05.launcher.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.ds05.launcher.common.ConnectUtils;
import com.ds05.launcher.common.config.MyAvsHelper;

/**
 * Created by kabru on 2017/6/11.
 */

public class ZhongyunService extends Service {

    private final static String TAG="ZhongyunService";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ZhongyunService onCreate<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        ConnectUtils.mMyAvsHelper = MyAvsHelper.getInstance(getApplicationContext());
        ConnectUtils.mMyAvsHelper.login();
    }
}
