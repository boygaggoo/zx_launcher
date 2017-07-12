package com.ds05.launcher.service.rs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ds05.launcher.LauncherApplication;
import com.ds05.launcher.common.manager.PrefDataManager;
import com.ds05.launcher.service.HWSink;

/**
 * Created by Chongyang.Hu on 2017/4/4 0004.
 */

final class RemoteServerReceiver extends BroadcastReceiver {
    private static final String TAG = "RemoteBroadcastReceiver";

    private InternalObserver mObserver;

    public RemoteServerReceiver() {
        super();
    }

    void setInternalObserver(InternalObserver o) {
        mObserver = o;
    }

    /*package*/ void registerBroadcastReceiver() {
        Context ctx = LauncherApplication.getContext();

        IntentFilter filter = new IntentFilter();
        filter.addAction(RemoteServerSink.ACTION_SET_HUMAN_MONITOR_STATE);
        filter.addAction(RemoteServerSink.ACTION_SET_AUTO_ALARM_TIME);
        filter.addAction(RemoteServerSink.ACTION_SET_MONITOR_SENSITIVITY);
        filter.addAction(RemoteServerSink.ACTION_SET_ALARM_MODE);
        filter.addAction(RemoteServerSink.ACTION_SET_ALARM_SOUND);
        filter.addAction(RemoteServerSink.ACTION_SET_ALARM_SOUND_VOLUME);

        ctx.registerReceiver(this, filter);
    }
    /*package*/ void unregisterBroadcastReceiver() {
        Context ctx = LauncherApplication.getContext();
        ctx.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "{onReceive}action: " + action);

        if(action.equals(RemoteServerSink.ACTION_SET_HUMAN_MONITOR_STATE)) {
            if(intent.hasExtra(RemoteServerSink.EXTRA_HUMAN_MONITOR_STATE)) {
                mInternalHandler.obtainMessage(EVT_SET_HUMAN_MONITOR_STATE, intent).sendToTarget();
            }
        } else if(action.equals(RemoteServerSink.ACTION_SET_AUTO_ALARM_TIME)) {
            if(intent.hasExtra(RemoteServerSink.EXTRA_AUTO_ALARM_TIME)) {
                mInternalHandler.obtainMessage(EVT_SET_AUTO_ALARM_TIME, intent) .sendToTarget();
            }
        }else if(action.equals(RemoteServerSink.ACTION_SET_MONITOR_SENSITIVITY)) {
            if(intent.hasExtra(RemoteServerSink.EXTRA_MONITOR_SENSI)) {
                mInternalHandler.obtainMessage(EVT_SET_MONITOR_SENSITIVITY, intent).sendToTarget();
            }
        }else if(action.equals(RemoteServerSink.ACTION_SET_ALARM_MODE)) {
            if(intent.hasExtra(RemoteServerSink.EXTRA_ALARM_MODE)) {
                mInternalHandler.obtainMessage(EVT_SET_ALARM_MODE, intent) .sendToTarget();
            }
        }else if(action.equals(RemoteServerSink.ACTION_SET_ALARM_SOUND)) {
            if(intent.hasExtra(RemoteServerSink.EXTRA_ALARM_SOUND)) {
                mInternalHandler.obtainMessage(EVT_SET_ALARM_SOUND, intent).sendToTarget();
            }
        }else if(action.equals(RemoteServerSink.ACTION_SET_ALARM_SOUND_VOLUME)) {
            if(intent.hasExtra(RemoteServerSink.EXTRA_ALARM_SOUND_VOLUME)) {
                mInternalHandler.obtainMessage(EVT_SET_ALARM_SOUND_VOLUME, intent).sendToTarget();
            }
        }
    }

    private void notifyObserver(String key, Object obj, boolean sendBroadcast) {
        if(mObserver != null) {
            mObserver.onReceiverChanged(key, obj, sendBroadcast);
        }
    }

    private static final int EVT_SET_HUMAN_MONITOR_STATE = 0;
    private static final int EVT_SET_AUTO_ALARM_TIME = 1;
    private static final int EVT_SET_MONITOR_SENSITIVITY = 2;
    private static final int EVT_SET_ALARM_MODE = 3;
    private static final int EVT_SET_ALARM_SOUND = 4;
    private static final int EVT_SET_ALARM_SOUND_VOLUME = 5;
    private Handler mInternalHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case EVT_SET_HUMAN_MONITOR_STATE: {
                    Intent intent = (Intent)msg.obj;
                    boolean state = intent.getBooleanExtra(RemoteServerSink.EXTRA_HUMAN_MONITOR_STATE, false);
                    Log.d(TAG, "{handleMessage}EVT_SET_HUMAN_MONITOR_STATE state:" + state);
                    if(state != PrefDataManager.getHumanMonitorState()) {
                        PrefDataManager.setHumanMonitorState(state);
                        Intent i = new Intent();
                        i.putExtra(HWSink.EXTRA_DRV_CFG_HUMAN_MONITOR_STATE, state);
                        HWSink.updateDriverConfig(i);

                        notifyObserver(RemoteServerSink.EXTRA_HUMAN_MONITOR_STATE, state, true);
                    }
                    break;
                }
                case EVT_SET_AUTO_ALARM_TIME: {
                    Intent intent = (Intent)msg.obj;
                    long time = intent.getLongExtra(RemoteServerSink.EXTRA_AUTO_ALARM_TIME, -1);
                    Log.d(TAG, "{handleMessage}EVT_SET_HUMAN_MONITOR_STATE time:" + time);
                    if(time != PrefDataManager.getAutoAlarmTime() && time >= 0) {
                        PrefDataManager.setAutoAlarmTime(time);
                        Intent i = new Intent();
                        i.putExtra(HWSink.EXTRA_DRV_CFG_AUTO_ALARM_TIME, time);
                        HWSink.updateDriverConfig(i);

                        notifyObserver(RemoteServerSink.EXTRA_AUTO_ALARM_TIME, time, true);
                    }
                    break;
                }
                case EVT_SET_MONITOR_SENSITIVITY: {
                    Intent intent = (Intent)msg.obj;
                    int sensi = intent.getIntExtra(RemoteServerSink.EXTRA_MONITOR_SENSI, -1);
                    Log.d(TAG, "{handleMessage}EVT_SET_HUMAN_MONITOR_STATE sensi:" + sensi);
                    if(sensi >= 0 && sensi != PrefDataManager.getHumanMonitorSensi().sensitivity()) {
                        PrefDataManager.setHumanMonitorSensi(sensi);
                        Intent i = new Intent();
                        i.putExtra(HWSink.EXTRA_DRV_CFG_MONITOR_SENSI, sensi);
                        HWSink.updateDriverConfig(i);

                        notifyObserver(RemoteServerSink.EXTRA_MONITOR_SENSI, sensi, true);
                    }
                    break;
                }
                case EVT_SET_ALARM_MODE: {
                    Intent intent = (Intent)msg.obj;
                    int mode = intent.getIntExtra(RemoteServerSink.EXTRA_ALARM_MODE, -1);
                    if(mode == 2) {
                        int shootNum = intent.getIntExtra(RemoteServerSink.EXTRA_SHOOT_NUMBER, -1);
                        Log.d(TAG, "{handleMessage}EVT_SET_HUMAN_MONITOR_STATE mode:" + mode + " shootNum" + shootNum);
                        PrefDataManager.setAlarmMode(mode);
                        PrefDataManager.setShootNumber(shootNum >= 0 ? shootNum : 3);

                        notifyObserver(RemoteServerSink.EXTRA_ALARM_MODE, mode, false);
                        notifyObserver(RemoteServerSink.EXTRA_SHOOT_NUMBER, shootNum, true);
                    } else if(mode != -1) {
                        Log.d(TAG, "{handleMessage}EVT_SET_HUMAN_MONITOR_STATE mode:" + mode);
                        PrefDataManager.setAlarmMode(mode);
                        notifyObserver(RemoteServerSink.EXTRA_ALARM_MODE, mode, true);
                    }
                    break;
                }
                case EVT_SET_ALARM_SOUND: {
                    Intent intent = (Intent)msg.obj;
                    int sound = intent.getIntExtra(RemoteServerSink.EXTRA_ALARM_SOUND, -1);
                    Log.d(TAG, "{handleMessage}EVT_SET_HUMAN_MONITOR_STATE sound:" + sound);
                    if(sound != -1) {
                        PrefDataManager.setAlarmSound(sound);
                        notifyObserver(RemoteServerSink.EXTRA_ALARM_SOUND, sound, true);
                    }
                    break;
                }
                case EVT_SET_ALARM_SOUND_VOLUME: {
                    Intent intent = (Intent)msg.obj;
                    int volume = intent.getIntExtra(RemoteServerSink.EXTRA_ALARM_SOUND_VOLUME, -1);
                    Log.d(TAG, "{handleMessage}EVT_SET_HUMAN_MONITOR_STATE volume:" + volume);
                    if((volume / 10f) != PrefDataManager.getAlarmSoundVolume()) {
                        PrefDataManager.setAlarmSoundVolume(volume / 10f);
                        notifyObserver(RemoteServerSink.EXTRA_ALARM_SOUND_VOLUME, volume, true);
                    }
                    break;
                }
            }//switch(msg.what)
        }//handleMessage
    };//mInternalHandler
}
