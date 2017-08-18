package com.ds05.launcher.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.ds05.launcher.common.Constants;
import com.ds05.launcher.common.manager.PrefDataManager;
import com.ds05.launcher.common.utils.AppUtil;

/**
 * Created by peng on 17-8-16.
 */

public class RespondReceiveConfigFromServer extends Service {

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Constants.BROADCAST_ACTION_RECEIVE_CONFIG_FROM_SERVER)) {
                String data = intent.getStringExtra(Constants.MSG_FROM_SERVER);
                String dataStr[] = data.substring(1, data.length() - 1).split(",");

                Boolean a1 = Boolean.valueOf(dataStr[4]);
                long a2 = Integer.parseInt(dataStr[5]);
                int a3 =  Integer.parseInt(dataStr[6]);
                int a4 =  Integer.parseInt(dataStr[7]);
                int a5 = Integer.parseInt(dataStr[9]);

                float f = Float.parseFloat("dataStr[10]");
                double a61 = (f*1.0)/10;
                float a6 = (float)a61;

                long a7 = Integer.parseInt(dataStr[12]);
                PrefDataManager.setHumanMonitorState(a1);//boolean
                PrefDataManager.setAutoAlarmTime(a2);//long
                PrefDataManager.setHumanMonitorSensi(a3);//int
                PrefDataManager.setAlarmMode(a4);//int
                PrefDataManager.setAlarmSound(a5);//int
                PrefDataManager.setAlarmSoundVolume(a6);//float
                PrefDataManager.setAlarmIntervalTime(a7);//long
                Log.d("PP"," a2 = " + a2);
                AppUtil.respondReceiveConfigFromServer(getApplicationContext(),true);
            }
        }
    };

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(Constants.BROADCAST_ACTION_RECEIVE_CONFIG_FROM_SERVER);
        registerReceiver(mReceiver, mFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}



