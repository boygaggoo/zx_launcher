package com.ds05.launcher.service;

import android.content.Intent;

import com.ds05.launcher.LauncherApplication;

/**
 * Created by Chongyang.Hu on 2017/2/28 0028.
 */

public final class HWSink {
    public static final String ACTION_DOORBELL_PRESSED = "com.ds05.launcher.service.ACTION.DoorbellPressed";
    public static final String ACTION_HUMAN_MONITOR_NOTIFY = "com.ds05.launcher.service.ACTION.HumanMonitorNotify";
    public static final String ACTION_DISPLAY_CAMERA_UI = "com.ds05.launcher.service.ACTION.DisplayCameraUI";
    public static final String ACTION_STOP_CAMERA_NOTIFY = "com.ds05.launcher.service.ACTION.StopCameraNotify";
    public static final String ACTION_STOP_CAMERA_NOTIFY_RESP = "com.ds05.launcher.service.ACTION.StopCameraNotifyRESP";

    public static final String EXTRA_STATUS = "HWSinkStatus";
    public static final int INVALIDE_STATUS = -1;
    public static final int STATUS_HUMAN_IN = 1;
    public static final int STATUS_HUMAN_OUT = 2;

    public static final String EXTRA_REASON = "HWSinkReason";
    public static final int REASON_INVALIDE = -1;
    public static final int REASON_DOORBELL_PRESSED = 1;
    public static final int REASON_DOORBELL_CANCEL = 2;
    public static final int REASON_ALARM_RING = 3;
    public static final int REASON_ALARM_STOP = 4;


    /** 配置参数到驱动的广播 */
    public static final String ACTION_CONFIG_DRIVER_PARAMS = "com.ds05.launcher.service.ACTION.ConfigDriverParams";
    /** 逆光补光灯是否开启 boolean */
    public static final String EXTRA_DRV_CFG_BACK_LIGHT_STATE = "BackFillLightState";
    /** 夜视补光灵敏度 int */
    public static final String EXTRA_DRV_CFG_NIGHT_LIGHT_SENSI = "NightFillLightSensitivity";
    public static final int NIGHT_LIGHT_SENSI_LOW = 1;
    public static final int NIGHT_LIGHT_SENSI_MIDDLE = 2;
    public static final int NIGHT_LIGHT_SENSI_HIGHT = 3;
    /** 光源频率 */
    public static final String EXTRA_DRV_CFG_LIGHT_SRC_FREQ = "LightSourceFrequency";
    public static final int LIGHT_SRC_FREQ_50HZ = 50;
    public static final int LIGHT_SRC_FREQ_60HZ = 60;
    /** 智能人体监测是否开启 boolean */
    public static final String EXTRA_DRV_CFG_HUMAN_MONITOR_STATE = "HumanMonitorState";
    /** 智能报警时间 */
    public static final String EXTRA_DRV_CFG_AUTO_ALARM_TIME = "AutoAlarmTime";
    public static final int AUTO_ALARM_TIME_3SEC = 3;
    public static final int AUTO_ALARM_TIME_8SEC = 8;
    public static final int AUTO_ALARM_TIME_15SEC = 15;
    public static final int AUTO_ALARM_TIME_25SEC = 25;
    /** 监控灵敏度 */
    public static final String EXTRA_DRV_CFG_MONITOR_SENSI = "MonitorSensitivity";
    public static final int MONITOR_SENSI_HIGHT = 1;
    public static final int MONITOR_SENSI_LOW = 2;

    public static void updateDriverConfig(Intent intent) {
        intent.setAction(ACTION_CONFIG_DRIVER_PARAMS);
        LauncherApplication.getContext().sendBroadcast(intent);
    }
}
