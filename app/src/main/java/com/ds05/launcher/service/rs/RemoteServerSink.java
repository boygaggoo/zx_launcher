package com.ds05.launcher.service.rs;

import android.content.Context;
import android.util.Log;

import com.ds05.launcher.LauncherApplication;

/**
 * Created by Chongyang.Hu on 2017/4/13 0013.
 */

public class RemoteServerSink implements InternalObserver {
    private static final String TAG = "RemoteServerSink";
    //上报系统配置
    public static final String ACTION_TO_SERVER = "com.ds05.Broadcast.ToServer.REPORT_SYSTEM_CONFIG";

    //设置人体感应状态
    public static final String ACTION_SET_HUMAN_MONITOR_STATE = "com.ds05.Broadcast.FromServer.SET_HUMAN_MONITOR_STATE";
    public static final String EXTRA_HUMAN_MONITOR_STATE = "HumanMonitorState";

    //设置自动报警时间
    public static final String ACTION_SET_AUTO_ALARM_TIME = "com.ds05.Broadcast.FromServer.SET_AUTO_ALARM_TIME";
    public static final String EXTRA_AUTO_ALARM_TIME = "AutoAlarmTime";

    //设置检测灵敏度
    public static final String ACTION_SET_MONITOR_SENSITIVITY = "com.ds05.Broadcast.FromServer.SET_MONITOR_SENSITIVITY";
    public static final String EXTRA_MONITOR_SENSI = "MonitorSensitivity";
    public static final int MONITOR_SENSI_HIGH = 1;
    public static final int MONITOR_SENSI_LOW = 2;


    //设置报警模式
    public static final String ACTION_SET_ALARM_MODE = "com.ds05.Broadcast.FromServer.SET_ALARM_MODE";
    public static final String EXTRA_ALARM_MODE = "AlarmMode";
    public static final String EXTRA_SHOOT_NUMBER = "ShootNumber";


    //设置报警铃声
    public static final String ACTION_SET_ALARM_SOUND = "com.ds05.Broadcast.FromServer.SET_ALARM_SOUND";
    public static final String EXTRA_ALARM_SOUND = "AlarmSound";


    //设置报警铃声音量
    public static final String ACTION_SET_ALARM_SOUND_VOLUME = "com.ds05.Broadcast.FromServer.SET_ALARM_SOUND_VOLUME";
    public static final String EXTRA_ALARM_SOUND_VOLUME = "AlarmSoundVolume";


    private Context mContext;
    private RemoteServerReceiver mReceiver;
    private RemoteServerNotifier mNotifier;

    public RemoteServerSink() {
        mContext = LauncherApplication.getContext();
        mReceiver = new RemoteServerReceiver();
        mNotifier = new RemoteServerNotifier();
    }

    public void createSink() {
        mReceiver.registerBroadcastReceiver();
        mReceiver.setInternalObserver(this);
    }

    public void destroySink() {
        mReceiver.unregisterBroadcastReceiver();
        mReceiver.setInternalObserver(null);
    }

    public void notifyToServer() {
        mNotifier.notifyOrResp();
    }

    @Override
    public void onReceiverChanged(String key, Object obj, boolean sendBroadcast) {
        Log.d(TAG, "{RemoteServerSink}onReceiverChanged key:" + key
                + " obj:" + obj + " sendBroadcast:" + sendBroadcast);
        mNotifier.setData(key, obj);
        if(sendBroadcast)
            mNotifier.notifyOrResp();
    }
}
