package com.ds05.launcher.service.rs;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ds05.launcher.LauncherApplication;
import com.ds05.launcher.common.manager.PrefDataManager;
import com.ds05.launcher.service.HWSink;

/**
 * Created by Chongyang.Hu on 2017/4/13 0013.
 */

class RemoteServerNotifier {
    private static final String TAG = "RemoteServerNotifier";
    private Intent mData;
    private Context mContext;

    /*package*/ RemoteServerNotifier() {
        mContext = LauncherApplication.getContext();
        mData = new Intent(RemoteServerSink.ACTION_TO_SERVER);
        initData();
    }

    private void initData() {
        Log.d(TAG, "{RemoteServerNotifier}initData");
        /** 智能人体监测是否开启 */
        boolean state = PrefDataManager.getHumanMonitorState();
        mData.putExtra(RemoteServerSink.EXTRA_HUMAN_MONITOR_STATE, state);
        /** 自动报警时间 */
        mData.putExtra(RemoteServerSink.EXTRA_AUTO_ALARM_TIME,
                PrefDataManager.getAutoAlarmTime());
        /** 监控灵敏度 */
        PrefDataManager.MonitorSensitivity ms = PrefDataManager.getHumanMonitorSensi();
        int sensi;
        if (ms == PrefDataManager.MonitorSensitivity.High)
            sensi = RemoteServerSink.MONITOR_SENSI_HIGH;
        else if (ms == PrefDataManager.MonitorSensitivity.Low)
            sensi = RemoteServerSink.MONITOR_SENSI_LOW;
        else
            sensi = HWSink.MONITOR_SENSI_HIGHT;
        mData.putExtra(RemoteServerSink.EXTRA_MONITOR_SENSI, sensi);
        /** 报警模式 */
        PrefDataManager.AlarmMode mode = PrefDataManager.getAlarmMode();
        mData.putExtra(RemoteServerSink.EXTRA_ALARM_MODE, mode.mode());
        /** 连拍张数 */
        int num = -1;
        if(mode == PrefDataManager.AlarmMode.Capture)
            num = PrefDataManager.getShootNumber();
        mData.putExtra(RemoteServerSink.EXTRA_SHOOT_NUMBER, num);
        /** 自动报警铃声 */
        mData.putExtra(RemoteServerSink.EXTRA_ALARM_SOUND, PrefDataManager.getAlarmSound().sound());
        /** 自动报警铃声音量 */
        mData.putExtra(RemoteServerSink.EXTRA_ALARM_SOUND_VOLUME, (int)(PrefDataManager.getAlarmSoundVolume() * 10));
    }

    /*package*/ void setData(String key, Object obj) {
        Log.d(TAG, "{setData}key:" + key + " obj:" + obj);
        if(key.equals(RemoteServerSink.EXTRA_ALARM_MODE)) {
            mData.putExtra(key, (int)obj);
            if((int)obj == PrefDataManager.AlarmMode.Capture.mode())
                mData.putExtra(key, PrefDataManager.getShootNumber());
        } else if(key.equals(RemoteServerSink.EXTRA_ALARM_SOUND)) {
            mData.putExtra(key, (int)obj);
        } else if(key.equals(RemoteServerSink.EXTRA_ALARM_SOUND_VOLUME)) {
            mData.putExtra(key, (int)obj);
        } else if(key.equals(RemoteServerSink.EXTRA_AUTO_ALARM_TIME)) {
            mData.putExtra(key, (long)obj);
        } else if(key.equals(RemoteServerSink.EXTRA_HUMAN_MONITOR_STATE)) {
            mData.putExtra(key, (boolean) obj);
        } else if(key.equals(RemoteServerSink.EXTRA_MONITOR_SENSI)) {
            mData.putExtra(key, (int)obj);
        } else if(key.equals(RemoteServerSink.EXTRA_SHOOT_NUMBER)) {
            mData.putExtra(key, (int)obj);
        }
    }

    /*package*/ void notifyOrResp() {
        Log.d(TAG, "{notifyOrResp}");
        mContext.sendStickyBroadcast(mData);
    }
}






































