package com.ds05.launcher.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.ds05.launcher.common.manager.PrefDataManager;
import com.ds05.launcher.common.utils.AppUtil;
import com.ds05.launcher.common.utils.FileUtils;
import com.ds05.launcher.service.rs.RemoteServerSink;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Chongyang.Hu on 2017/1/7 0007.
 */

public class LauncherService extends Service {

    private InternalHandler mInternalHandler = new InternalHandler();
    private RemoteServerSink mRemoteServerSink;
    Timer checkTimer;
    TimerTask checkTask;
    private Handler mHandler;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private SoundManager mSoundManager;

    @Override
    public void onCreate() {

        super.onCreate();
        mSoundManager = new SoundManager(this);
        registerBroadcast();

        mRemoteServerSink = new RemoteServerSink();
        mRemoteServerSink.createSink();

        mInternalHandler.sendEmptyMessageDelayed(EVT_CONFIG_DRIVER_PARAMS, 1 * 1000);

        mHandler = new Handler(Looper.getMainLooper());
        checkTimer  = new Timer();
        checkTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(checkTime(0,5)){
                            FileUtils.checkStorageSpace(getApplicationContext());
                        }
                    }
                });
            }
        };
        checkTimer.scheduleAtFixedRate(checkTask,0, 29*60*60*1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        unregisterBroadcast();
        if (mRemoteServerSink != null) {
            mRemoteServerSink.destroySink();
        }

        super.onDestroy();
        Log.d("DS05", "LauncherService onDestroy");
        startService(new Intent(this, LauncherService.class));
    }


    private static final int EVT_PLAY_DOORBELL = 0;
    private static final int EVT_STOP_DOORBELL = 1;
    private static final int EVT_PLAY_ALARM = 2;
    private static final int EVT_STOP_ALARM = 3;
    private static final int EVT_CONFIG_DRIVER_PARAMS = 4;

    private class InternalHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Intent intent = new Intent();
            switch (msg.what) {
                case EVT_PLAY_DOORBELL:
                    mSoundManager.stopAlarmSound();
                    mSoundManager.playDoorbellSound();
                    break;
                case EVT_STOP_DOORBELL:
                    mSoundManager.stopDoorbellSound();
                    break;
                case EVT_PLAY_ALARM:
                    mSoundManager.playAlarmSound();
                    break;
                case EVT_STOP_ALARM:
                    mSoundManager.stopAlarmSound();
                    break;
                case EVT_CONFIG_DRIVER_PARAMS:
                    mRemoteServerSink.notifyToServer();
                    configDriverParams();
                    break;
            }
        }
    }

    ;

    private void configDriverParams() {
        Intent intent = new Intent();

        /** 逆光补光灯是否开启 boolean */
        intent.putExtra(HWSink.EXTRA_DRV_CFG_BACK_LIGHT_STATE,PrefDataManager.getBackLightState());
        /** 夜视补光灵敏度 int */
        int index = PrefDataManager.getNightFillLightSensi();
        int sensi = -1;
        if (index == 0)
            sensi = HWSink.NIGHT_LIGHT_SENSI_LOW;
        else if (index == 1)
            sensi = HWSink.NIGHT_LIGHT_SENSI_MIDDLE;
        else if (index == 2)
            sensi = HWSink.NIGHT_LIGHT_SENSI_HIGHT;
        intent.putExtra(HWSink.EXTRA_DRV_CFG_NIGHT_LIGHT_SENSI, sensi);
        /** 光源频率 */
        index = PrefDataManager.getLightSourceFreq();
        int freq = HWSink.LIGHT_SRC_FREQ_50HZ;
        if (index == 0)
            freq = HWSink.LIGHT_SRC_FREQ_50HZ;
        else if (index == 1)
            freq = HWSink.LIGHT_SRC_FREQ_60HZ;
        intent.putExtra(HWSink.EXTRA_DRV_CFG_LIGHT_SRC_FREQ, freq);
        /** 智能人体监测是否开启 boolean */
        boolean state = PrefDataManager.getHumanMonitorState();
        intent.putExtra(HWSink.EXTRA_DRV_CFG_HUMAN_MONITOR_STATE, state);
        /** 智能报警时间 */
        intent.putExtra(HWSink.EXTRA_DRV_CFG_AUTO_ALARM_TIME, PrefDataManager.getAutoAlarmTime());
        /** 监控灵敏度 */
        PrefDataManager.MonitorSensitivity ms = PrefDataManager.getHumanMonitorSensi();
        if (ms == PrefDataManager.MonitorSensitivity.High)
            sensi = HWSink.MONITOR_SENSI_HIGHT;
        else if (ms == PrefDataManager.MonitorSensitivity.Low)
            sensi = HWSink.MONITOR_SENSI_LOW;
        else
            sensi = HWSink.MONITOR_SENSI_HIGHT;
        intent.putExtra(HWSink.EXTRA_DRV_CFG_MONITOR_SENSI, sensi);

        HWSink.updateDriverConfig(intent);
    }

    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(HWSink.ACTION_DOORBELL_PRESSED);
        filter.addAction(HWSink.ACTION_HUMAN_MONITOR_NOTIFY);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    private void unregisterBroadcast() {
        unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(HWSink.ACTION_DOORBELL_PRESSED)) {
                Log.d("DS05", "{LauncherService}{onReceive}ACTION_DOORBELL_PRESSED");
                mInternalHandler.sendEmptyMessage(EVT_PLAY_DOORBELL);
            } else if (action.equals(HWSink.ACTION_HUMAN_MONITOR_NOTIFY)) {
                if (!PrefDataManager.getHumanMonitorState()) return;

                int status = intent.getIntExtra(HWSink.EXTRA_STATUS, HWSink.INVALIDE_STATUS);
                switch (status) {
                    case HWSink.STATUS_HUMAN_IN: {
                        Log.d("DS05", "{LauncherService}{onReceive}STATUS_HUMAN_IN");
                        if (!mInternalHandler.hasMessages(EVT_PLAY_ALARM)) {
//                            long time = PrefDataManager.getAutoAlarmTime();
                            mInternalHandler.sendEmptyMessage(EVT_PLAY_ALARM);
                        }
                        break;
                    }
                    case HWSink.STATUS_HUMAN_OUT: {
                        Log.d("DS05", "{LauncherService}{onReceive}STATUS_HUMAN_OUT");
                        mInternalHandler.removeMessages(EVT_PLAY_ALARM);
                        mInternalHandler.sendEmptyMessage(EVT_STOP_ALARM);
                        break;
                    }
                    default:
                        Log.d("DS05", "{LauncherService}{onReceive}ACTION_HUMAN_MONITOR_NOTIFY");
                }
            }else if(action.equals(Intent.ACTION_BATTERY_CHANGED)){
                int current=intent.getExtras().getInt("level");//获得当前电量
                int total=intent.getExtras().getInt("scale");//获得总电量
                int percent = current*100/total;
                AppUtil.BATTERY_LEVEL = String.valueOf(percent);
            }
        }//onReceive
    };//BroadcastReceiver

    private boolean checkTime(int begin, int end){
        Calendar cal = Calendar.getInstance();// 当前日期
        int hour = cal.get(Calendar.HOUR_OF_DAY);// 获取小时
        int minute = cal.get(Calendar.MINUTE);// 获取分钟
        int minuteOfDay = hour * 60 + minute;// 从0:00分开是到目前为止的分钟数

        int beginTime = begin * 60;
        int endTime   = end * 60;

        if(minuteOfDay >= beginTime && minuteOfDay <= endTime){
            return true;
        }
        return false;
    }
}
