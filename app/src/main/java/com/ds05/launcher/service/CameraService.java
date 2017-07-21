package com.ds05.launcher.service;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ds05.launcher.CameraActivity_ZY;
import com.ds05.launcher.MainActivity;
import com.ds05.launcher.common.Constants;
import com.ds05.launcher.net.SessionManager;
import com.ds05.launcher.receiver.CameraReceiver;

import org.apache.mina.core.buffer.IoBuffer;

import java.util.List;


/**
 *  create by vincent
 */
public class CameraService extends IntentService {

	private static final String TAG = "CameraService";

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
			// 门铃事件通知
			String msg = "[" + System.currentTimeMillis() + ",T4," + Constants.SOFT_VERSION + "," + Constants.ZHONGYUN_LINCESE + "]";
			IoBuffer buffer = IoBuffer.allocate(msg.length());
			buffer.put(msg.getBytes());
			SessionManager.getInstance().writeToServer(buffer);

			if(!isForeground(CameraService.this,"com.ds05.launcher.CameraActivity_ZY")){
				Intent activity = new Intent(CameraService.this, CameraActivity_ZY.class);
				activity.putExtra(Constants.EXTRA_CAPTURE,true);
				activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(activity);
			}
			Log.i(TAG, "收到门铃事件通知: " + msg);

		} else if (Constants.BROADCAST_NOTIFY_HUMAN_MONITORING.equals(action)) {
			// 人体监测通知
			int humanStatus = intent.getIntExtra("HumanStatus", 0);

			String msg = "[" + System.currentTimeMillis() + ",T5," + Constants.SOFT_VERSION + "," + Constants.ZHONGYUN_LINCESE + "," + humanStatus + "]";
			IoBuffer buffer = IoBuffer.allocate(msg.length());
			buffer.put(msg.getBytes());
			SessionManager.getInstance().writeToServer(buffer);
			Log.i(TAG, "收到人体监测通知: " + msg);

		} else if (Constants.BROADCAST_NOTIFY_QRCODE_RESULT.equals(action)) {
			//Log.i(TAG, "收到消息，############################################################################################################################################收到扫二维码广播消息");
			// 二维码码
			String userid = intent.getStringExtra("QRCodeResult_UserId");
			String ssid = intent.getStringExtra("QRCodeResult_WifiSSID");
			String pwd = intent.getStringExtra("QRCodeResult_WifiPassword");
			String msg = "[" + System.currentTimeMillis() + ",T6," + Constants.SOFT_VERSION + "," + Constants.ZHONGYUN_LINCESE + "," + userid + "," + ssid + "," + pwd + "]";
			IoBuffer buffer = IoBuffer.allocate(msg.length());
			buffer.put(msg.getBytes());
			Log.d("ZXH","###  writeToServer msg = " + msg);
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

		} else {
			Log.d(TAG, "收到未知消息，忽略处理: " + action);
		}
		// 未知广播消息

		CameraReceiver.completeWakefulIntent(intent);
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
