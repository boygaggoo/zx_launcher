package com.ds05.launcher.service;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.util.Log;

import com.ds05.launcher.CameraActivity_ZY;
import com.ds05.launcher.MainActivity;
import com.ds05.launcher.common.Constants;
import com.ds05.launcher.common.manager.PrefDataManager;
import com.ds05.launcher.common.utils.AppUtil;
import com.ds05.launcher.net.SessionManager;
import com.ds05.launcher.receiver.CameraReceiver;

import org.apache.mina.core.buffer.IoBuffer;

import java.io.File;
import java.util.List;


/**
 *  create by vincent
 */
public class CameraService extends IntentService {

	private static final String TAG = "CameraService";
	private static final int ALARMVALIDTIME = 10000;
	private static final int WAITCOUNT = 5;
	private static boolean isDoorBelling = false;
	private static boolean isFirstHumanMonitor = false;
	private static boolean isValidTime = false;
	private static boolean isIntervalTime = false;
	private Handler mHandler = new Handler();

	public CameraService(String name) {
		super(name);
	}

	public CameraService() {
		super("CameraService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		String action = intent.getAction();
		if (Constants.BROADCAST_REPORT_SYSTEM_CONFIG.equals(action)) {
			Log.d("ZXH","BROADCAST_REPORT_SYSTEM_CONFIG");
			Constants.humanMonitorState = intent.getBooleanExtra("HumanMonitorState", true);
			Constants.autoAlarmTime = intent.getLongExtra("AutoAlarmTime", 5000);
			Constants.monitorSensitivity = intent.getIntExtra("MonitorSensitivity", 1);
			Constants.alarmMode = intent.getIntExtra("AlarmMode", 1);
			Constants.shootNumber = intent.getIntExtra("ShootNumber", 3);
			Constants.alarmSound = intent.getIntExtra("AlarmSound", 2);
			Constants.alarmSoundVolume = intent.getIntExtra("AlarmSoundVolume", 2);

/*			String msg = "[" + System.currentTimeMillis() + ",T3,"+Constants.SOFT_VERSION+","+Constants.ZHONGYUN_LINCESE+","+humanMonitorState+","+autoAlarmTime+","+monitorSensitivity+","+alarmMode+","+shootNumber+","+alarmSound+","+alarmSoundVolume+"]";
			IoBuffer buffer = IoBuffer.allocate(msg.length());
			buffer.put(msg.getBytes());
			SessionManager.getInstance().writeToServer(buffer);*/

			// 系统向后台APP发送当前的配置
			Log.i(TAG, "收到当前的配置广播");
		} else if (Constants.BROADCAST_NOTIFY_DOORBELL_PRESSED.equals(action)) {
			Log.d("ZXH","##############BROADCAST_NOTIFY_DOORBELL_PRESSED  isDoorBelling = " + isDoorBelling);
			// 门铃事件通知
			Intent broadcast = new Intent(HWSink.ACTION_DOORBELL_PRESSED);
			sendBroadcast(broadcast,null);
			if(isDoorBelling){
				return;
			}
			isDoorBelling = true;

			Log.d("ZXH","##############waitCaptureCount");
			int waitCaptureCount = 0;
			while(PictureService.isCapturing){
				SystemClock.sleep(100);
				waitCaptureCount++;
				if(waitCaptureCount >= WAITCOUNT){
					Log.i(TAG, "wait capturing is timeout");
					stopCapture();
					break;
				}
			}

			Log.d("ZXH","##############waitRecordCount");
			int waitRecordCount = 0;
			if(VideoService.getRecordStatus()){
				stopRecording();
			}
			while(VideoService.getRecordStatus()){
				SystemClock.sleep(200);
				waitRecordCount++;
				if(waitRecordCount >= WAITCOUNT){
					Log.d("ZXH","##############waitRecordCount return");
					Log.i(TAG, "wait recording is timeout");
					isDoorBelling = false;
					return;
				}
			}

			Log.d("ZXH","##############isForeground");
			if(!isForeground(CameraService.this,"com.ds05.launcher.CameraActivity_ZY")){
				Log.d("ZXH","##############isForeground startActivity");
				String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.DOORBELL_PATH;
				File dirFile = new File(dirPath);
				if (!dirFile.exists()) {
					dirFile.mkdirs();
				}
				String fileName = AppUtil.getPhotoFileName();
				String filePath = dirPath + fileName;

				AppUtil.uploadDoorbellMsgToServer(this, fileName);
				Intent activity = new Intent(CameraService.this, CameraActivity_ZY.class);
				activity.putExtra(Constants.EXTRA_CAPTURE,true);
				activity.putExtra(Constants.EXTRA_CAPTURE_PATH,filePath);
				activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(activity);
			}

			isDoorBelling = false;
			Log.i(TAG, "收到门铃事件通知: ");

		} else if (Constants.BROADCAST_NOTIFY_HUMAN_MONITORING.equals(action)) {
			// 人体监测通知
			Log.d("ZXH","############## BROADCAST_NOTIFY_HUMAN_MONITORING");
			if(!PrefDataManager.getHumanMonitorState() || isIntervalTime){
				isFirstHumanMonitor = false;
				isValidTime = false;
				return;
			}
			if(isForeground(CameraService.this,"com.ds05.launcher.CameraActivity_ZY")){
				return;
			}

			if(!isFirstHumanMonitor){
				Log.d("ZXH","############## isFirstHumanMonitor");
				isFirstHumanMonitor = true;
				isValidTime = false;
				long mAutoAlarmTime = PrefDataManager.getAutoAlarmTime();
				Log.d("ZXH","############## mAutoAlarmTime = " + mAutoAlarmTime);
				mHandler.postDelayed(autoAlarmTimeRunnable, mAutoAlarmTime);
			}

			if(!isValidTime){
				return;
			}

			Log.d("ZXH","############## BROADCAST_NOTIFY_HUMAN_MONITORING DOING");
			mHandler.removeCallbacks(validTimeRunnable);
			isIntervalTime = true;
			isFirstHumanMonitor = false;
			long mAlarmIntervalTime = PrefDataManager.getAlarmIntervalTime();
			Log.d("ZXH","############## mAlarmIntervalTime = " + mAlarmIntervalTime);
			mHandler.postDelayed(intervalTimeRunnable, mAlarmIntervalTime);

			if(PrefDataManager.getAlarmMode() == PrefDataManager.AlarmMode.Capture){
				Log.d("ZXH","############## Capture");
				startCapture();
			}else if(PrefDataManager.getAlarmMode() == PrefDataManager.AlarmMode.Recorder){
				Log.d("ZXH","############## Recorder");
				startRecording();
			}

			Log.i(TAG, "收到人体监测通知: " );

		} else if (Constants.BROADCAST_NOTIFY_QRCODE_RESULT.equals(action)) {
			//Log.i(TAG, "收到消息，############################################################################################################################################收到扫二维码广播消息");
			// 二维码码
			Log.d("ZXH","############## QR");
			String userid = intent.getStringExtra("QRCodeResult_UserId");
			String ssid = intent.getStringExtra("QRCodeResult_WifiSSID");
			String pwd = intent.getStringExtra("QRCodeResult_WifiPassword");
			Constants.userId = userid;
			String msg = "[" + System.currentTimeMillis() + ",T6," + Constants.SOFT_VERSION + "," + AppUtil.getZYLicense() + "," + userid + "," + ssid + "," + pwd + "]";
			IoBuffer buffer = IoBuffer.allocate(msg.length());
			buffer.put(msg.getBytes());
			SessionManager.getInstance().writeToServer(buffer);

		} else if (Constants.BROADCAST_ACTION_OPEN_CAMERA.equals(action)) {
			//Log.i(TAG, "收到消息，############################################################################################################################################要求打开camera");
			Intent intent2 = new Intent(getApplicationContext(), CameraActivity_ZY.class);
			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent2.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			startActivity(intent2);

		} else if (Constants.BROADCAST_ACTION_CLOSE_CAMERA.equals(action)) {
			Intent intent2 = new Intent(getApplicationContext(), MainActivity.class);
			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent2.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			startActivity(intent2);
			//Log.i(TAG, "收到消息，############################################################################################################################################要求关闭camera");

		} else if (Constants.BROADCAST_ACTION_TEST_CAMERA.equals(action)) {

			Log.i(TAG, "收到消息，############################################################################################################################################测试camera");

		} else if (Constants.BROADCAST_ACTION_RECEIVE_REBOOT_FROM_SERVER.equals(action)) {
			Log.i(TAG, "收到消息，重启camera");
			String msg = "[" + System.currentTimeMillis() + ",S10," + "True" + "]";
			IoBuffer buffer = IoBuffer.allocate(msg.length());
			buffer.put(msg.getBytes());
			SessionManager.getInstance().writeToServer(buffer);
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					PowerManager pm = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
					pm.reboot(null);
				}
			}, 1000);
		} else if (Constants.BROADCAST_ACTION_RECEIVE_CONFIG_FROM_SERVER.equals(action)) {
			Log.d("FPP","action");
			Log.i(TAG, "收到消息，配置信息");
			String data = intent.getStringExtra(Constants.MSG_FROM_SERVER);
			String dataStr[] = data.substring(1, data.length() - 1).split(",");
			Log.d("PP"," dataStr.length = " + dataStr.length);
			Boolean humanMonitorState = Boolean.valueOf(dataStr[4]);
			long autoAlarmTime = Integer.parseInt(dataStr[5]);
			int humanMonitorSensi =  Integer.parseInt(dataStr[6]);
			int alarmMode =  Integer.parseInt(dataStr[7]);
			int alarmSound = Integer.parseInt(dataStr[9]);

			float f = Float.parseFloat(dataStr[10]);
			double temp = (f*1.0)/10;
			float alarmSoundVolume = (float)temp;

			long alarmIntervalTime = Integer.parseInt(dataStr[13]);
			PrefDataManager.setHumanMonitorState(humanMonitorState);//boolean
			PrefDataManager.setAutoAlarmTime(autoAlarmTime);//long
			PrefDataManager.setHumanMonitorSensi(humanMonitorSensi);//int
			PrefDataManager.setAlarmMode(alarmMode);//int
			PrefDataManager.setAlarmSound(alarmSound);//int
			PrefDataManager.setAlarmSoundVolume(alarmSoundVolume);//float
			PrefDataManager.setAlarmIntervalTime(alarmIntervalTime);//long
			AppUtil.uploadConfigMsgToServer(getApplicationContext());
			AppUtil.respondReceiveConfigFromServer(getApplicationContext(),true);
		}  else {
			Log.d(TAG, "收到未知消息，忽略处理: " + action);
		}
		// 未知广播消息

		CameraReceiver.completeWakefulIntent(intent);
	}

	Runnable autoAlarmTimeRunnable = new Runnable() {
		@Override
		public void run() {
			Log.d("ZXH","######### autoAlarmTimeRunnable");
			isValidTime = true;
			mHandler.postDelayed(validTimeRunnable, ALARMVALIDTIME);
		}
	};

	Runnable validTimeRunnable = new Runnable() {
		@Override
		public void run() {
			Log.d("ZXH","######### validTimeRunnable");
			isValidTime = false;
			isFirstHumanMonitor = false;
		}
	};

	Runnable intervalTimeRunnable = new Runnable() {
		@Override
		public void run() {
			isIntervalTime = false;
		}
	};


	private void startCapture(){
		ResultReceiver receiver = new ResultReceiver(new Handler()) {
			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				Log.d("ZXH","########## resultCode = " + resultCode);
				Log.d("ZXH","########## resultData = " + resultData);
			}
		};
		PictureService.startToStartCapture(this,  Camera.CameraInfo.CAMERA_FACING_BACK, receiver);
	}

	private void stopCapture(){
		ResultReceiver receiver = new ResultReceiver(new Handler()) {
			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				Log.d("ZXH","########## resultCode = " + resultCode);
				Log.d("ZXH","########## resultData = " + resultData);
			}
		};
		PictureService.startToStopCapture(this, receiver);
	}

	private void startRecording(){
		ResultReceiver receiver = new ResultReceiver(new Handler()) {
			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				Log.d("ZXH","########## resultCode = " + resultCode);
				Log.d("ZXH","########## resultData = " + resultData);
			}
		};
		VideoService.startToStartRecording(this,  Camera.CameraInfo.CAMERA_FACING_BACK, receiver);
	}

	private void stopRecording() {
		ResultReceiver receiver = new ResultReceiver(new Handler()) {
			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				Log.d("ZXH","########## resultCode = " + resultCode);
				Log.d("ZXH","########## resultData = " + resultData);
			}
		};
		VideoService.startToStopRecording(this, receiver);
	}

	private boolean isForeground(Context context, String className) {
		if (context == null) {
			return false;
		}
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
		if (list != null && list.size() > 0) {
			ComponentName cpn = list.get(0).topActivity;
			if (className.equals(cpn.getClassName())) {
				return true;
			}
		}
		return false;
	}
}
