package com.ds05.launcher.receiver;


import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import com.ds05.launcher.common.ConnectUtils;
import com.ds05.launcher.service.CameraService;
import com.ds05.launcher.service.PictureService;
import com.ds05.launcher.service.VideoService;

import static com.ds05.launcher.common.Constants.BROADCAST_NOTIFY_DOORBELL_PRESSED;
import static com.ds05.launcher.common.Constants.mHandlingEvent;

public class CameraReceiver extends WakefulBroadcastReceiver {
	private static final String TAG="CameraReceiver";  
	private static boolean linkstart=true;

	private Context context;
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		Log.i(TAG, "收到广播消息："+intent.getAction());
		this.context=context;
		Log.i("ZXH", "onReceive：" + intent.getAction());

		if((!ConnectUtils.NETWORK_IS_OK || !ConnectUtils.CONNECT_SERVER_STATUS) &&
		!BROADCAST_NOTIFY_DOORBELL_PRESSED.equals(intent.getAction())){
			Toast.makeText(context, "收到广播消息,但是网络不可用,不进行网络发送。"+ConnectUtils.NETWORK_IS_OK +" ,"+ConnectUtils.CONNECT_SERVER_STATUS, Toast.LENGTH_SHORT).show();
			if(ConnectUtils.NETWORK_IS_OK && !ConnectUtils.CONNECT_SERVER_STATUS && linkstart){
				linkstart=false;
			//	context.startService(new Intent(LauncherApplication.getContext(), ConnectSocketService.class));

			}
//			return;
		}
		Toast.makeText(context, "收到广播消息："+intent.getAction(), Toast.LENGTH_SHORT).show();

		if("com.ds05.Broadcast.FromServer.Action.TEST_CAMERA".equals(intent.getAction())){
//			if (mRecording) {
//				mRecording=false;
//				stopRecording();
//			} else {
//				mRecording=true;
//				startRecording();
//			}
            startCapture();

		}else{
			Intent  regIntent = new Intent(context, CameraService.class);
			if(bundle!=null){
				regIntent.putExtras(bundle);
			}
			regIntent.setAction(intent.getAction());
			startWakefulService(context, regIntent);
		}
	}


	private void stopRecording() {
		if (!mHandlingEvent) {
			mHandlingEvent = true;
			ResultReceiver receiver = new ResultReceiver(new Handler()) {
				@Override
				protected void onReceiveResult(int resultCode, Bundle resultData) {

					handleStopRecordingResult(resultCode, resultData);
					mHandlingEvent = false;
				}
			};
			VideoService.startToStopRecording(this.context, receiver);
		}
	}

	private void startCapture(){
        if (!mHandlingEvent) {
            mHandlingEvent = true;
            ResultReceiver receiver = new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    handleStartRecordingResult(resultCode, resultData);
                    mHandlingEvent = false;
                }
            };

            PictureService.startToStartCapture(this.context,  Camera.CameraInfo.CAMERA_FACING_FRONT, receiver);
        }
    }


	private void startRecording() {
		if (!mHandlingEvent) {
			mHandlingEvent = true;
			ResultReceiver receiver = new ResultReceiver(new Handler()) {
				@Override
				protected void onReceiveResult(int resultCode, Bundle resultData) {
					handleStartRecordingResult(resultCode, resultData);
					mHandlingEvent = false;
				}
			};

			VideoService.startToStartRecording(this.context,  Camera.CameraInfo.CAMERA_FACING_FRONT, receiver);
		}
	}

	private void handleStartRecordingResult(int resultCode, Bundle resultData) {
		if (resultCode == VideoService.RECORD_RESULT_OK) {
			Toast.makeText(this.context, "Start recording...", Toast.LENGTH_SHORT).show();
		} else {
			// start recording failed.
			Toast.makeText(this.context, "Start recording failed...", Toast.LENGTH_SHORT).show();
		}
	}

	private void handleStopRecordingResult(int resultCode, Bundle resultData) {
		if (resultCode == VideoService.RECORD_RESULT_OK) {
			String videoPath = resultData.getString(VideoService.VIDEO_PATH);
			Log.i("videoPath",videoPath);
			Toast.makeText(this.context, "Record succeed, file saved in " + videoPath,Toast.LENGTH_LONG).show();
		} else if (resultCode == VideoService.RECORD_RESULT_UNSTOPPABLE) {
			Toast.makeText(this.context, "Stop recording failed...", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this.context, "Recording failed...", Toast.LENGTH_SHORT).show();
		}
	}
}
