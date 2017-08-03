package com.ds05.launcher.common;

public class Constants {
	
	public static String ZHONGYUN_LINCESE="ds000002";

    public static boolean humanMonitorState=true;
    public static long autoAlarmTime=5000;
    public static int monitorSensitivity=1;
    public static int alarmMode=1;
    public static int shootNumber=3;
    public static int alarmSound=2;
    public static int alarmSoundVolume=2;

    public static final String SOFT_VERSION="v1.0.1";

    public static  String MAC_ADDRESS="ds000002";

    public static  boolean mRecording = false;
    public static  boolean mHandlingEvent;


    public static final String APP_HEARBEAT_ACTION = "com.zhongyun.android.intent.app_heartbeat";

    public static final String DAEMON_HEARBEAT_ACTION = "com.zhongyun.android.intent.daemon_heartbeat";
    
    
    //智能人体侦测开关
    public static final String BROADCAST_SET_PIR_STATUS="com.ds05.Broadcast.FromServer.SET_HUMAN_MONITOR_STATE";
     
    //自动报警时间设置
    public static final String BROADCAST_SET_ALARM_TIME="com.ds05.Broadcast.FromServer.SET_AUTO_ALARM_TIME";
    
    //监控灵敏度设置
    public static final String BROADCAST_SET_MONITOR_SENSITIVITY="com.ds05.Broadcast.FromServer.SET_MONITOR_SENSITIVITY";
    
    //报警模式设置
    public static final String BROADCAST_SET_ALARM_MODE="com.ds05.Broadcast.FromServer.SET_ALARM_MODE";
    
    //自动报警铃声设置
    public static final String BROADCAST_SET_PIR_ALARM_SOUND="com.ds05.Broadcast.FromServer.SET_ALARM_SOUND";
    
    //自动报警铃声音量
    public static final String BROADCAST_SET_PIR_ALARM_SOUND_VOLUME="com.ds05.Broadcast.FromServer.SET_ALARM_SOUND_VOLUME";
    
    
    
    //系统向后台APP发送当前的配置
    public static final String BROADCAST_REPORT_SYSTEM_CONFIG="com.ds05.Broadcast.ToServer.REPORT_SYSTEM_CONFIG";
    
    //门铃事件通知
    public static final String BROADCAST_NOTIFY_DOORBELL_PRESSED="com.ds05.Broadcast.ToServer.NOTIFY_DOORBELL_PRESSED";
    
    //人体监测通知
    public static final String BROADCAST_NOTIFY_HUMAN_MONITORING="com.ds05.Broadcast.ToServer.NOTIFY_HUMAN_MONITORING";
    
    //二维码扫描之后调用的广播消息
    public static final String BROADCAST_NOTIFY_QRCODE_RESULT="com.ds05.Broadcast.ToServer.NOTIFY_QRCODE_RESULT";

    public static final String BROADCAST_ACTION_OPEN_CAMERA="com.ds05.Broadcast.FromServer.Action.OPEN_CAMERA";

    public static final String BROADCAST_ACTION_CLOSE_CAMERA="com.ds05.Broadcast.FromServer.Action.CLOSE_CAMERA";


    public static final String BROADCAST_ACTION_TEST_CAMERA="com.ds05.Broadcast.FromServer.Action.TEST_CAMERA";


    public static final String EXTRA_CAPTURE="extra_capture";

    public static final String  DOORBELL_PATH = "/DS05/Camera/Doorbell/";
    public static final String  ALARM_CAPTURE_PATH = "/DS05/Camera/Capture/";
    public static final String  ALARM_VIDEO_PATH = "/DS05/Camera/Alarm/";

}
