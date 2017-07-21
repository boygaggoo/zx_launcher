package com.ichano;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.ds05.launcher.CameraActivity_ZY;
import com.ichano.rvs.internal.RvsLog;
import com.ichano.rvs.streamer.Command;
import com.ichano.rvs.streamer.Media;
import com.ichano.rvs.streamer.Streamer;
import com.ichano.rvs.streamer.callback.CommandCallback;
import com.ichano.rvs.streamer.callback.CustomDataRecvCallback;
import com.ichano.rvs.streamer.callback.MediaChannelListener;
import com.ichano.rvs.streamer.codec.AudioType;
import com.ichano.rvs.streamer.codec.VideoType;
import com.ichano.rvs.streamer.param.AudioProperty;
import com.ichano.rvs.streamer.param.CameraCapacity;
import com.ichano.rvs.streamer.param.Capacity;
import com.ichano.rvs.streamer.param.StreamProperty;

/**
 * 可以运行于后台来采集端实时视频，只能运行在Android3.0及以上版本。
 * @author sunfred
 *
 */
@SuppressWarnings("deprecation")
public class MediaManagerService extends Service
	implements MediaChannelListener, CustomDataRecvCallback, CommandCallback{
	
	protected Streamer streamer;
	protected Media media;
	protected Command command;
	
	//视频参数
	public int videoBitrate = 384000;
	public int frameRate = 15;
	public int iframeInterval = 30;
	protected static final int[] VIDEO_320 = {320, 240};
	protected static final int[] VIDEO_480 = {640, 480};
	protected static final int[] VIDEO_720 = {1280, 720};
	protected static final int[] VIDEO_1080 = {1920, 1080};
	
	protected static final int VIDEO_BITRATE_320 = 384000;
	protected static final int VIDEO_BITRATE_480 = 768000;
	protected static final int VIDEO_BITRATE_720 = 1024000;
	protected static final int VIDEO_BITRATE_1080 = 1728000;
	
	// 音频采集相关参数
	protected int channelConfig = 1;// .CHANNEL_IN_MONO;
	protected int bitsPerSample = 16;
	public static int audioSampleRateInHz = 8000;
	int[] videoSize;
	public static boolean hasConnected = false;
			
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		videoSize = VIDEO_480;
		init();
//		Thread mThread = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				try {
//					Thread.sleep(5000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//		        Intent intent = new Intent(MediaManagerService.this, MainActivity1.class);
//		        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
//		        startActivity(intent);	
//			}
//		});
//		mThread.start();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
		

	public void init(){
		streamer = Streamer.getStreamer();
		media = streamer.getMedia();
		initMedia(false);
		media.setChannelLister(this);
		command = streamer.getCommand();
		command.setCallback(this);
		command.setCustomDataRecvCallback(this);
	}
		
	private void initMedia(boolean enableAEC){
		Capacity capacity = new Capacity();
		if (enableAEC){
			capacity.setEchoCancelMode(Capacity.ECHOCANCEL_SUPPORT);
			audioSampleRateInHz = 8000;
		}else{
			capacity.setEchoCancelMode(Capacity.ECHOCANCEL_NOT_SUPPORT);
		}
		capacity.setRecordMode(Capacity.STORAGE_RECORD_MODE);
		capacity.setRunMode(Capacity.AUTO_RUN_MODE | Capacity.BACKGROUND_RUN_MODE);
		capacity.setTimeZoneMode(Capacity.SETTIMEZONE_NOT_SUPPORT);
		streamer.setCapacity(capacity);

		CameraCapacity cc = new CameraCapacity();
		cc.setStreamType(CameraCapacity.ONE_STREAM_CONCURRENTLY);
		cc.setTorchEnable(true);
		media.setCameraCapacity(cc);
		StreamProperty streamProperty = null;
		streamProperty = new StreamProperty(videoSize[0],videoSize[1], 0, videoBitrate, frameRate, iframeInterval, VideoType.H264);
		RvsLog.i(MediaManagerService.class, "initMedia()", "Video param : videoSize = " + videoSize + ", videoBitrate = " + videoBitrate
			+ ", frameRate = " + frameRate + ", iframeInterval = " + iframeInterval);
		media.setCameraStreamProperty(streamProperty);

		AudioProperty audioProperty = new AudioProperty(audioSampleRateInHz, channelConfig, bitsPerSample, AudioType.AAC);
		RvsLog.i(MediaManagerService.class, "initMedia()", "Audio param : audioSampleRateInHz = " + audioSampleRateInHz + ", channelConfig = " + channelConfig
			+ ", bitsPerSample = " + bitsPerSample);
		media.setMicProperty(audioProperty);
		
	}


	/**
	 * 客户端连接采集端状态上报
	 * 
	 * @param clientCID 客户端cid
	 * @param stateCode 状态码 0:建立连接，1:断开连接
	 * @param currentChannelCount 当前连接到采集端的所有媒体通道数
	 */
	@Override
	public void onMediaChannelState(long clientCID, int stateCode, int currentChannelCount){
		Log.d("ZXH","################## currentChannelCount = " + currentChannelCount);
		if(currentChannelCount > 0){
			hasConnected = true;
			Log.d("ZXH","isForeground = " + isForeground(MediaManagerService.this,"com.ds05.launcher.CameraActivity_ZY"));
			if(!isForeground(MediaManagerService.this,"com.ds05.launcher.CameraActivity_ZY")){
		        Intent intent = new Intent(MediaManagerService.this, CameraActivity_ZY.class);
		        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			}
		}else{
			hasConnected = false;
			Log.d("ZXH","isForeground = " + isForeground(MediaManagerService.this,"com.ds05.launcher.CameraActivity_ZY"));
			if(isForeground(MediaManagerService.this,"com.ds05.launcher.CameraActivity_ZY")){
//				closeActivity("com.ds05.launcher.CameraActivity_ZY");
				CameraActivity_ZY.instance.finish();
			}
		}
	}

	private void closeActivity(String className){
		ActivityManager manager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);   
		manager.restartPackage(className); 
	}
	
	private boolean isForeground(Context context, String className) {    
	       if (context == null) {    
	           return false;    
	       }    
	    
	       ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);    
	       List<RunningTaskInfo> list = am.getRunningTasks(1);    
	       if (list != null && list.size() > 0) {    
	           ComponentName cpn = list.get(0).topActivity;    
	           if (className.equals(cpn.getClassName())) {    
	               return true;    
	           }    
	       }    
	       return false;    
	   } 
	
	/**
	 * 接收采集端发到客户端的自定义数据
	 * 
	 * @param remoteCID 远端cid，包括客户端和采集端
	 * @param data 自定义数据
	 */
	@Override
	public void onReceiveCustomData(long remoteCID, byte[] data){
		RvsLog.i(MediaManagerService.class, "onReceiveCustomData()", 
			"remoteCID:" + remoteCID + ", data : " + new String(data));
	}

	@Override
	public void onCustomCommandListener(long remoteCID, int commandId, String command){
		RvsLog.i(MediaManagerService.class, "onCustomCommandListener()", 
			"remoteCID:" + remoteCID + "commandId:" + commandId + ", command : " + command);
	}
	
	/**
	 * 客户端修改采集端用户名密码的命令，处理结果无需返回客户端。
	 * 
	 * @param userName 客户端设置的用户名
	 * @param password 客户端设置的密码
	 */
	@Override
	public void onSetUserInfo(String userName, String password){
		streamer.setUserNameAndPwd(userName, password);
	}

	@Override
	public void onSetStreamQuality(long remoteCid, long msgId, int msgType,
			int camId, int streamid, int frameRate, int bitrate,
			int streamQuality, int iframeInterval) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSwitchFrontRearCamera(long remoteCid, long msgId, int msgType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSwitchTorch(long remoteCid, long msgId, int msgType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPTZorMove(long remoteCid, int camIndex, int type,
			int PorAxis_X, int TorAxis_Y, int ZorAxis_Z) {
		// TODO Auto-generated method stub
		
	}

	
}
