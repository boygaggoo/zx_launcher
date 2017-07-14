package com.ichano;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;

import com.ichano.athome.avs.libavs.X264VideoCodec;
import com.ichano.rvs.audio.AudioIOHandler;
import com.ichano.rvs.audio.AudioIOHandler.AuidoCallback;
import com.ichano.rvs.internal.RvsLog;
import com.ichano.rvs.streamer.Command;
import com.ichano.rvs.streamer.Command.ResultCode;
import com.ichano.rvs.streamer.Media;
import com.ichano.rvs.streamer.Streamer;
import com.ichano.rvs.streamer.bean.RvsAlarmRecordInfo;
import com.ichano.rvs.streamer.bean.RvsTimeRecordInfo;
import com.ichano.rvs.streamer.bean.ScheduleSetting;
import com.ichano.rvs.streamer.callback.AudioCallback;
import com.ichano.rvs.streamer.callback.CommandCallback;
import com.ichano.rvs.streamer.callback.CustomDataRecvCallback;
import com.ichano.rvs.streamer.callback.MediaChannelListener;
import com.ichano.rvs.streamer.callback.MotionDetectCallback;
import com.ichano.rvs.streamer.callback.MotionDetectSettingsCallback;
import com.ichano.rvs.streamer.callback.RecordCallback;
import com.ichano.rvs.streamer.callback.RevAudioCallback;
import com.ichano.rvs.streamer.callback.TimeRecordSettingsCallback;
import com.ichano.rvs.streamer.callback.VideoCallback;
import com.ichano.rvs.streamer.codec.AACEncoder;
import com.ichano.rvs.streamer.codec.AudioType;
import com.ichano.rvs.streamer.codec.G711;
import com.ichano.rvs.streamer.codec.VideoType;
import com.ichano.rvs.streamer.constant.JpegType;
import com.ichano.rvs.streamer.constant.MotionDetectState;
import com.ichano.rvs.streamer.constant.RvsRecordState;
import com.ichano.rvs.streamer.constant.RvsRecordType;
import com.ichano.rvs.streamer.param.AudioProperty;
import com.ichano.rvs.streamer.param.CameraCapacity;
import com.ichano.rvs.streamer.param.Capacity;
import com.ichano.rvs.streamer.param.StreamProperty;
import com.ichano.rvs.streamer.ui.HardwareEncoder;
import com.ichano.rvs.streamer.util.RotateUtil;

/**
 * 采集摄像头视频并可发送到观看端供观看端观看。
 * @author sunfred
 *
 */
@SuppressWarnings("deprecation")
public class MediaSurfaceView extends SurfaceView
	implements SurfaceHolder.Callback, VideoCallback, AudioCallback, MotionDetectCallback,
	MotionDetectSettingsCallback, RevAudioCallback, RecordCallback,TimeRecordSettingsCallback,
	CustomDataRecvCallback, CommandCallback, OnClickListener, AuidoCallback{
	
	private static final String TAG = MediaSurfaceView.class.getSimpleName();

	private static final int RESTART_CAM_DELAY = 500;// 500毫秒采集不到数据，就重启相机预览
	
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
	//private static final int[] sampleRateArray = new int[] { 44100, 32000, 22050, 11025, 8000 };
	private int screenOritation = Configuration.ORIENTATION_LANDSCAPE;
	
	private boolean mOpenBackCamera = true;
	private SurfaceHolder mSurfaceHolder = null;
	private SurfaceTexture mSurfaceTexture = null;
	private boolean mRunInBackground;
	private boolean enableRunInBackground = true;
	private Camera mCamera;
	private Parameters mCameraParameters;
	protected int videoWidth, videoHeight;
	private int yuv_buffersize;
	private byte[] yuvData;
	private byte[] rotateData;
	private int mGetyuvloop = 0;
	private byte[] previewBuffer;
	private boolean mIsSupportCameraLight = false;
	private boolean mIsTorchLight = false;
	private int mCameraId;
	
	protected int previewformat = ImageFormat.NV21;// 默认都支持
	private int mFrontCameraColorMode = CameraPreviewColorMode.VIDEO_DEFAULT;
	private int mBackCameraColorMode = CameraPreviewColorMode.VIDEO_DEFAULT;
	private VideoEncoder mVideoEncoder;
	private Runnable mRebootCamTask;
	
	private boolean mNeedEncodeVideo = true;
	private boolean mInMotionDetect = false;// 是否在运动侦测时间段内
	
	private final Lock yuvLock = new ReentrantLock();
	private boolean needGetYuv = false;
	private final Condition needGetYuvCondt = yuvLock.newCondition();
	
	//private AudioHandler mAudioHandler;
//	private IchanoAEC mAudioDevice;
	//private boolean mEnableAEC = false;
	private AudioIOHandler audioHanlder;
	private long audioChannel;
	private long revAudioStream = 0;
	private byte[] audioData = new byte[PCM_INPUT_SIZE];
	private static final int PCM_INPUT_SIZE = 2048;
	private static final int PCM_OUTPUT_SIZE = 1024;
	
	private boolean enableTimeWatermark;
	
	public MediaSurfaceView(Context context){
		super(context);
		init(context);
	}
	
	public MediaSurfaceView(Context context, AttributeSet attrs){
		super(context, attrs);
		init(context);
	}
	
	public MediaSurfaceView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		init(context);
	}
	
//	public MediaSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
//		super(context, attrs, defStyleAttr, defStyleRes);
//		init(context);
//	}

	private void init(Context context){
		setOnClickListener(this);
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		if (!isLowVersion()){
			mSurfaceTexture = new SurfaceTexture(10);
		}
	}

	/**
	 * 设置打开前置还是后置摄像头，默认打开后置摄像头。
	 * @param backCamera 打开前置传入false，否则传入true。
	 */
	public void setDefaultCamera(boolean backCamera){
		this.mOpenBackCamera = backCamera;
	}
	
	private boolean customVideoParam = false;
	
	/**
	 * 设置视频参数，需在{@link MediaSurfaceView#openCamera(int)}或{@link MediaSurfaceView#openCamera(VideoSize, int)} 
	 * 或{@link MediaSurfaceView#openCamera(int, int, int)}之前调用；
	 * 如果没有调用此函数，会选取默认参数。
	 * @param videoBitrate 码率
	 * @param frameRate 帧率
	 * @param iframeInterval i帧间隔,多少帧输出一个i帧
	 */
	public void setVideoParam(int videoBitrate, int frameRate, int iframeInterval){
		this.videoBitrate = videoBitrate;
		this.frameRate = frameRate;
		this.iframeInterval = iframeInterval;
		
		customVideoParam = true;
	}
	
	/**
	 * 获取当前的视频码率
	 * @return
	 */
	public int getVideoBitrate(){
		return videoBitrate;
	}
	
	/**
	 * 获取当前的视频帧率
	 * @return
	 */
	public int getVideoFrameRate(){
		return frameRate;
	}
	
	/**
	 * 获取当前的视频i帧间隔，多少帧输出一个i帧
	 * @return
	 */
	public int getIframeInterval(){
		return iframeInterval;
	}
	
	/**
	 * 实时调整视频编码参数
	 * @param videoBitrate 码率
	 * @param frameRate 帧率
	 * @param iframeInterval i帧间隔,多少帧输出一个i帧
	 */
	public void adjustVideoQuality(int videoBitrate, int frameRate, int iframeInterval){
		if(null != mVideoEncoder)
			mVideoEncoder.adjustStreamQuality(frameRate, videoBitrate, iframeInterval);
	}
	
	private void getVideoParam(int videoWith, int videoHeight){
		if(!customVideoParam){
			int size = videoHeight * videoWith;
			int size320 = VIDEO_320[0] * VIDEO_320[1];
			int size480 = VIDEO_480[0] * VIDEO_480[1];
			int size720 = VIDEO_720[0] * VIDEO_720[1];
			int size1080 = VIDEO_1080[0] * VIDEO_1080[1];
			if(size <= size320){
				this.videoBitrate = VIDEO_BITRATE_320;
			}else if(size > size320 && size <= size480){
				this.videoBitrate = VIDEO_BITRATE_480;
			}else if(size > size480 && size <= size720){
				this.videoBitrate = VIDEO_BITRATE_720;
			}else if(size > size1080){
				this.videoBitrate = VIDEO_BITRATE_1080;
			}
		}
	}
	
	/**
	 * 是否使用时间水印
	 * @param enable true为使用，false为不使用；
	 * 需在需在{@link MediaSurfaceView#openCamera(int)}或{@link MediaSurfaceView#openCamera(VideoSize, int)} 
	 * 或{@link MediaSurfaceView#openCamera(int, int, int)}之前调用；
	 */
	public void enableTimeWatermark(boolean enable){
		enableTimeWatermark = enable;
	}
	
	/**
	 * 打开摄像头
	 * 
	 * @param screenOritation 采集视频时屏幕的方向 Configuration.ORIENTATION_LANDSCAPE or Configuration.ORIENTATION_PORTRAIT
	 */
	public void openCamera(int screenOritation){
		int[] size = isLowVersion() ? VIDEO_320 : VIDEO_480;
		openCamera(size[0], size[1], screenOritation);
	}
	
	/**
	 * 打开摄像头
	 * @param size {@link VideoSize}
	 * @param screenOritation 采集视频时屏幕的方向 Configuration.ORIENTATION_LANDSCAPE or Configuration.ORIENTATION_PORTRAIT
	 */
	public void openCamera(VideoSize size, int screenOritation){
		int[] videoSize = VideoSize.getVideoSize(size);
		openCamera(videoSize[0], videoSize[1], screenOritation);
	}
	/**
	 * 打开摄像头
	 * 
	 * @param videoWidth 采集视频的宽
	 * @param videoHeight 采集视频的高
	 * @param screenOritation 采集视频时屏幕的方向 Configuration.ORIENTATION_LANDSCAPE or Configuration.ORIENTATION_PORTRAIT
	 */
	public void openCamera(int videoWidth, int videoHeight, int screenOritation){
		this.screenOritation = screenOritation;
		this.videoWidth = videoWidth;
		this.videoHeight = videoHeight;
		getVideoParam(videoWidth, videoHeight);
		streamer = Streamer.getStreamer();
		media = streamer.getMedia();
		audioChannel = media.getAudioWriteChannel();
		media.setVideoCallback(this);
		media.setAudioCallback(this);
		media.setRecordCallback(this);
		media.setRevAudioCallback(this);
		media.setMotionDetectCallback(this);
		media.setRecordSettingsCallback(this);
		media.setMotionDetectSettingsCallback(this);
		command = streamer.getCommand();
		command.setCallback(this);
		command.setCustomDataRecvCallback(this);

		AACEncoder.init(64000, channelConfig, audioSampleRateInHz, bitsPerSample);

		audioHanlder = AudioIOHandler.getInstance(getContext());
		audioHanlder.setAuidoCallback(this, PCM_INPUT_SIZE, PCM_OUTPUT_SIZE);
		audioHanlder.startAudio(false, false);
		
		if(screenOritation == Configuration.ORIENTATION_PORTRAIT){
			mVideoEncoder = new VideoEncoder(videoHeight, videoWidth);
		}else{
			mVideoEncoder = new VideoEncoder(videoWidth, videoHeight);
		}
		cameraState = CameraState.START;
		if(null != cameraStateListener) cameraStateListener.onCameraStateChange(cameraState);
		RvsLog.i(MediaSurfaceView.class, "openCamera()", "start to open camera.");
		initCamera();
		RvsLog.i(MediaSurfaceView.class, "openCamera()", "end of open camera.");
	}

	public void closeCamera(){
		stopPreview();
		releaseCamera();
		mVideoEncoder.destroy();
		audioHanlder.releaseAudio();
		AACEncoder.destroy();
	}
	
	/**
	 * 重启相机
	 */
	public void restartCamera(){
		stopPreview();
		releaseCamera();
		initCamera();
		startPreview();
	}
	
	public int[] getVideoSize(){
		int[] size = {videoWidth, videoHeight};
		return size;
	}
	
	private boolean isLowVersion(){
		return Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1;
	}
			
	@SuppressWarnings("deprecation")
	private int findCamera(boolean front){
		int cameraCount = 0;
		try{
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			cameraCount = Camera.getNumberOfCameras(); // get cameras number

			for (int camIdx = 0; camIdx < cameraCount; camIdx++){
				Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
				int facing = front ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
				if (cameraInfo.facing == facing){
					return camIdx;
				}
			}
		} catch (Exception e){
			e.printStackTrace();
			RvsLog.e(MediaSurfaceView.class, "findCamera()", "can not find " + (front ? "front" : "back") + " camera");
		}
		return -1;
	}
	
	@SuppressWarnings("deprecation")
	private void initCamera(){
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO){
			if (mOpenBackCamera){
				mCameraId = findCamera(false);
			}else{
				mCameraId = findCamera(true);
			}
			if (mCameraId == -1){
				mCameraId = 0;
			}
		}
		try{
			if (Build.VERSION.SDK_INT == Build.VERSION_CODES.FROYO){
				mCamera = Camera.open();
			} else{
				mCamera = Camera.open(mCameraId);
			}
		} catch (Exception ee){
			mCamera = null;
			cameraState = CameraState.ERROR;
			if(null != cameraStateListener) cameraStateListener.onCameraStateChange(cameraState);
			RvsLog.e(MediaSurfaceView.class, "initCamera()", "init camera failed.");
		}
		if (mCamera == null){
			return;
		}
		
		initCameraFormat();
	}
	
	private boolean isSamsungS4(){
		if("GT-I9502".equals(Build.MODEL)){
			return true;
		}
		return false;
	}
	
	private boolean isChe2UL00(){
		if("Che2-UL00".equals(Build.MODEL)){
			return true;
		}
		return false;
	}
	
	private boolean isMeizu(){
		if ("meizu".equalsIgnoreCase(Build.BRAND)){
			return true;
		}
		try{
			Method method = Build.class.getMethod("hasSmartBar");
			if (method != null){
				return true;
			}
		} catch (NoSuchMethodException e){
			return false;
		}
		return false;
	}
	
	private void initCameraFormat(){
		if (isLowVersion()){
			previewformat = ImageFormat.YV12;
		} else{
			previewformat = ImageFormat.NV21;
		}
		if (isMeizu()){
			previewformat = ImageFormat.NV21;
		}
		if (mOpenBackCamera){
			mVideoEncoder.setColorMode(mBackCameraColorMode);
			if (mBackCameraColorMode == CameraPreviewColorMode.VIDEO_NV21){
				previewformat = ImageFormat.NV21;
			}
		} else{
			mVideoEncoder.setColorMode(mFrontCameraColorMode);
			if (mFrontCameraColorMode == CameraPreviewColorMode.VIDEO_NV21){
				previewformat = ImageFormat.NV21;
			}
		}
		mVideoEncoder.setPreviewformat(previewformat);
	}
	
	/**
	 * 切换视频颜色模式，不同的手机摄像头颜色模式可能不同，当无法正常显示时可尝试切换。
	 * @param colorMode 视频颜色模式{@link CameraPreviewColorMode}
	 */
	public void switchCameraPreviewColorMode(int colorMode){
		if (mOpenBackCamera){
			mBackCameraColorMode = colorMode;
		}else{
			mFrontCameraColorMode = colorMode;
		}
		if (colorMode == CameraPreviewColorMode.VIDEO_NV21){// 实际采集的是NV21
			stopPreview();
			previewformat = ImageFormat.NV21;
			mVideoEncoder.setPreviewformat(previewformat);
			startPreview();
		}
	}
	
	@SuppressWarnings("deprecation")
	private void releaseCamera(){
		try{
			if (mCamera != null){
				mCamera.setPreviewCallback(null);
				mCamera.setPreviewCallbackWithBuffer(null);
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
		} catch (Exception ee){
			RvsLog.e(MediaSurfaceView.class, "releaseCamera()", "releaseCamera  failed");
		}
	}
	
	private void isSupportCameraLight(){
		Object a;
		try{
			if (mCamera != null){
				Parameters parameter = mCamera.getParameters();
				a = parameter.getSupportedFlashModes();
				if (a == null){
					mIsSupportCameraLight = false;
				} else{
					mIsSupportCameraLight = true;
				}
			}
		} catch (Exception e){
			mIsSupportCameraLight = false;
			e.printStackTrace();
		}
	}
	
	private boolean flip = false;
	/**
	 * 翻转采集到的图像，观看端会看到图像翻转。
	 */
	public void flip(){
		flip = !flip;
	}
	
	private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback(){
		public synchronized void onPreviewFrame(byte[] data, Camera camera){
			if (data == null){
				releaseCamera();
				RvsLog.e(MediaSurfaceView.class, "onPreviewFrame()", "preview get null data!");
				return;
			}
			if(cameraState != CameraState.PREVIEW) {
				cameraState = CameraState.PREVIEW;
				if(null != cameraStateListener) cameraStateListener.onCameraStateChange(cameraState);
			}
			if(screenOritation == Configuration.ORIENTATION_PORTRAIT){
				if(flip){
					if(previewformat == ImageFormat.NV21){
						rotateData = RotateUtil.rotate(data, RotateUtil.NV21, videoWidth, videoHeight, RotateUtil.R270);
					}else{
						rotateData = RotateUtil.rotate(data, RotateUtil.YV12, videoWidth, videoHeight, RotateUtil.R270);
					}
				}else{
					if(previewformat == ImageFormat.NV21){
						rotateData = RotateUtil.rotate(data, RotateUtil.NV21, videoWidth, videoHeight, RotateUtil.R90);
					}else{
						rotateData = RotateUtil.rotate(data, RotateUtil.YV12, videoWidth, videoHeight, RotateUtil.R90);
					}
				}
				if (yuvLock.tryLock()){
					if (data.length == yuvData.length){
						System.arraycopy(rotateData, 0, yuvData, 0, rotateData.length);
					}
					if(checkYUV(data)){
						needGetYuv = false;
						needGetYuvCondt.signalAll();
					}
					yuvLock.unlock();
				}
				if (mNeedEncodeVideo || mInMotionDetect){
					mVideoEncoder.writeYuvData(rotateData, mNeedEncodeVideo, mInMotionDetect);
				}
			}else{
				if(flip){
					if(previewformat == ImageFormat.NV21){
						rotateData = RotateUtil.rotate(data, RotateUtil.NV21, videoWidth, videoHeight, RotateUtil.R180);
					}else{
						rotateData = RotateUtil.rotate(data, RotateUtil.YV12, videoWidth, videoHeight, RotateUtil.R180);
					}
					data = rotateData;
				}
				if (yuvLock.tryLock()){
					if (data.length == yuvData.length){
						System.arraycopy(data, 0, yuvData, 0, data.length);
					}
					if(checkYUV(data)){
						needGetYuv = false;
						needGetYuvCondt.signalAll();
					}
					yuvLock.unlock();
				}
				if (mNeedEncodeVideo || mInMotionDetect){
					mVideoEncoder.writeYuvData(data, mNeedEncodeVideo, mInMotionDetect);
				}
			}
//			if (mRunInBackground){
				mCamera.addCallbackBuffer(previewBuffer);
//			}
		}
	};
	
	private boolean checkYUV(byte[] data){
		if(data.length <= 0){
			return false;
		}
		
		Random rnd = new Random();
		if(data[rnd.nextInt(data.length)] == 0 && data[rnd.nextInt(data.length)] == 0 && data[rnd.nextInt(data.length)] == 0){
			if(data[rnd.nextInt(data.length)] == 0 && data[rnd.nextInt(data.length)] == 0 && data[rnd.nextInt(data.length)] == 0){
				if(data[rnd.nextInt(data.length)] == 0 && data[rnd.nextInt(data.length)] == 0 && data[rnd.nextInt(data.length)] == 0){
					if(data[rnd.nextInt(data.length)] == 0 && data[rnd.nextInt(data.length)] == 0 && data[rnd.nextInt(data.length)] == 0){
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public static void nv21_rotate_90(byte[] des, byte[] src, int width, int height){
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				des[height - i - 1 + j * height] = src[i * width + j];
			}
		}
		
		int halfw = width / 2;
		int halfH = height / 2;
		int yl = width * height;
		int ul = halfw * halfH;
		
		byte[] v = new byte[ul];
		byte[] u = new byte[ul];
		byte[] tv = new byte[ul];
		byte[] tu = new byte[ul];
		
		for(int i=0; i<ul; i++){
			v[i] = src[i*2 + yl];
			u[i] = src[i*2 + 1 + yl];
		}
		
		for (int i = 0; i < halfH; i++) {
			for (int j = 0; j < halfw; j++) {
				tv[halfH - i - 1 + j * halfH] = v[i * halfw + j];
				tu[halfH - i - 1 + j * halfH] = u[i * halfw + j];
			}
		}
		
		for(int i=0; i<ul; i++){
			des[yl + 2*i] = tv[i];
			des[yl + 2*i + 1] = tu[i];
		}
	}
	
	public static void yv12_rotate_90(byte[] temp, byte[] data, int videoWidth, int videoHeight){
		for (int i = 0; i < videoHeight; i++) {
			for (int j = 0; j < videoWidth; j++) {
				temp[videoHeight - i - 1 + j * videoHeight] = data[i * videoWidth + j];
			}
		}

		int halfw = videoWidth / 2;
		int halfH = videoHeight / 2;
		int yl = videoWidth * videoHeight;
		int ul = halfw * halfH;

		for (int i = 0; i < halfH; i++) {
			for (int j = 0; j < halfw; j++) {
				temp[halfH - i - 1 + j * halfH + yl] = data[i * halfw + j + yl];
				temp[halfH - i - 1 + j * halfH + yl + ul] = data[i * halfw + j + yl + ul];
			}
		}

	}
	
	private void startPreview(){
		if (mCamera == null)
			return;
		try{
			mCamera.setErrorCallback(new ErrorCallback() {
				
				@Override
				public void onError(int error, Camera camera) {
					if(cameraState != CameraState.ERROR) {
						cameraState = CameraState.ERROR;
						if(null != cameraStateListener) cameraStateListener.onCameraStateChange(cameraState);
					}
				}
			});
			mCameraParameters = mCamera.getParameters();

			yuv_buffersize = videoWidth * videoHeight * ImageFormat.getBitsPerPixel(previewformat) / 8;
			mCameraParameters.setPreviewFormat(previewformat);
			mCameraParameters.setJpegQuality(100);

			previewBuffer = new byte[yuv_buffersize];
			yuvData = new byte[yuv_buffersize];
			rotateData = new byte[yuv_buffersize];
			
			if (screenOritation != Configuration.ORIENTATION_LANDSCAPE) {
				mCameraParameters.setPreviewSize(videoWidth, videoHeight);
				mCameraParameters.set("orientation", "portrait");
				mCameraParameters.setRotation(0);
				mCameraParameters.set("rotation", 0);
				mCamera.setDisplayOrientation(90);
			}else{
			// 如果是横屏
				mCameraParameters.setPreviewSize(videoWidth, videoHeight);
				mCameraParameters.set("orientation", "landscape");
				mCamera.setDisplayOrientation(0);
				mCameraParameters.setRotation(0);
				mCameraParameters.set("rotation", 0);
			}

			RvsLog.e(MediaSurfaceView.class, "startPreview()", "mRunInBackground = " + mRunInBackground);
			if (mRunInBackground){
				if (!isLowVersion()){
					mCamera.setPreviewTexture(mSurfaceTexture);
					mCamera.addCallbackBuffer(previewBuffer);
					mCamera.setPreviewCallbackWithBuffer(previewCallback);
				}
			} else{
				mCamera.setPreviewDisplay(mSurfaceHolder);
				mCamera.setPreviewCallback(previewCallback);
			}
			mCamera.setParameters(mCameraParameters);
			isSupportCameraLight();
			mCamera.startPreview();
			if(cameraState != CameraState.START) {
				cameraState = CameraState.START;
				if(null != cameraStateListener) cameraStateListener.onCameraStateChange(cameraState);
			}
		} catch (Exception e){
			releaseCamera();
			RvsLog.e(MediaSurfaceView.class, "startPreview()", "start preview failed");
			return;
		}
		try{
			String mode = mCamera.getParameters().getFocusMode();

			if (Parameters.FOCUS_MODE_AUTO.equals(mode) || Parameters.FOCUS_MODE_MACRO.equals(mode)){
				mCamera.autoFocus(null);
			}
		} catch (Exception e){
			RvsLog.e(MediaSurfaceView.class, "startPreview()", "autoFocus failed");
		}
	}
	
	private void stopPreview(){
		if (mCamera == null) return;
		try{
			if (mRunInBackground){
				mCamera.setPreviewCallbackWithBuffer(null);
				mCamera.stopPreview();
			} else{
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();
			}
			if(cameraState != CameraState.STOP) {
				cameraState = CameraState.STOP;
				if(null != cameraStateListener) cameraStateListener.onCameraStateChange(cameraState);
			}
		} catch (Exception ee){
			RvsLog.e(MediaSurfaceView.class, "stopPreview()", "stopPreview failed");
		}
	}
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg){
			switch (msg.what)
			{
//			case Constants.INIT_AUDIORECORD_FAIL:
//				break;
//			case Constants.INIT_AUDIOPLAY_FAIL:
//				break;
			default:
				break;
			}
		};
	};
	
	@Override
	public void onClick(View v){
		try{
			if(null != mCamera) mCamera.autoFocus(null);
		}catch(RuntimeException e){
			
		}
	}
	
	private boolean autoOpenCamera = true;
	
	/**
	 * 是否自动开启相机；默认为自动开启相机；需在{@link MediaSurfaceView#openCamera(int)}或{@link MediaSurfaceView#openCamera(VideoSize, int)} 
	 * 或{@link MediaSurfaceView#openCamera(int, int, int)}之前调用；
	 * @param auto true为自动开启相机，false为不自动开启。若设置为false，请自行调用{@link MediaSurfaceView#startCameraPreview()} 和 {@link MediaSurfaceView#stopCameraPreview()}
	 */
	public void autoOpenCamera(boolean auto){
		autoOpenCamera = auto;
	}
	
	/**
	 * 是否开起后台采集；默认为自动开启后台采集；需在{@link MediaSurfaceView#openCamera(int)}或{@link MediaSurfaceView#openCamera(VideoSize, int)}
	 * 或{@link MediaSurfaceView#openCamera(int, int, int)}之前调用；
	 * @param runInBackground
     */
	public void enableRunInBackground(boolean runInBackground) {
		enableRunInBackground = runInBackground;
	}
	
	/**
	 * 开启相机预览，当{@link MediaSurfaceView#autoOpenCamera(boolean)}设置为不自动启动相机时需调用此函数来开启相机预览。
	 */
	public void startCameraPreview(){
		stopPreview();
		startPreview();
	}
	
	/**
	 * 关闭相机预览，当{@link MediaSurfaceView#autoOpenCamera(boolean)}设置为不自动启动相机时需调用此函数来关闭相机预览。
	 */
	public void stopCameraPreview(){
		stopPreview();
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder){
		RvsLog.i(MediaSurfaceView.class, "surfaceCreated()", "surfaceCreated");
		if (enableRunInBackground) {
			mRunInBackground = false;
		}
		if (autoOpenCamera){
			startCameraPreview();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder){
		RvsLog.i(MediaSurfaceView.class, "surfaceDestroyed()", "surfaceDestroyed");
		if (enableRunInBackground) {
			mRunInBackground = true;
		}
		if (autoOpenCamera){
			stopPreview();
			startPreview();
		}
	}
	

	/**
	 * 通知采集端编出一个关键帧并发送到客户端
	 */
	@Override
	public void onKeyFrameRequired(){
		if (isLowVersion()){

		} else{
			mVideoEncoder.reqIframe();
		}
	}

	/**
	 * sdk通知采集端返回一帧yuv数据
	 * @return yuv数据帧
	 */
//	@Override
//	public byte[] onGetOneYUVFrame(){
//		yuvLock.lock();
//		if (yuvData != null && yuvForMotion != null){
//			System.arraycopy(yuvData, 0, yuvForMotion, 0, yuvData.length);
//		}
//		yuvLock.unlock();
//
//		if (mGetyuvloop == 0){
//			Log.i(TAG, "onGetOneYUVFrame");
//		}
//		mGetyuvloop = (mGetyuvloop + 1) % 2000;
//
//		return yuvForMotion;
//	}
	
	private boolean isRecording = false;
	/**
	 * 开启本地视频录制，录制的文件保存在{@link AvsInitHelper#getCachePath()}
	 * @return true表示成功开始录制，false表示开始录制失败
	 */
	public boolean startRecord(){
		if(cameraState != CameraState.PREVIEW) return false;
		isRecording = media.startCustomRecord(0, 0);
		return isRecording;
	}
	
	/**
	 * 停止本地视频录制，录制的文件保存在{@link AvsInitHelper#getCachePath()}
	 * @return true表示成功停止录制，false表示停止录制失败
	 */
	public boolean stopRecord(){
		boolean ret = media.stopCustomRecord(0, 0);
		if(ret) isRecording = false;
		return ret;
	}
	
	/**
	 * 是否正在进行本地视频录制
	 * @return true表示正在录制，false表示不在录制。
	 */
	public boolean isRecording(){
		return isRecording;
	}
	
	/**
	 * 
	 * @param type 采集图片的类型{@link JpegType}
	 * @return 返回截图，失败返回null
	 */
	public Bitmap capture(JpegType type){
		byte[] data = onGetOneJpegFrame(type.intValue());
		if(null != data){
			return BitmapFactory.decodeByteArray(data, 0, data.length);
		}else{
			return null;
		}
	}
	
	/**
	 * sdk通知采集端返回一个jpeg图片
	 * @param type 大中小图:0、1、2
	 */
	@Override
	public byte[] onGetOneJpegFrame(int type){
		RvsLog.i(MediaSurfaceView.class, "onGetOneJpegFrame()", "type = " + JpegType.valueToString(type));
        if (!yuvLock.tryLock()){
    		RvsLog.i(MediaSurfaceView.class, "onGetOneJpegFrame()", "yuvLock fail.");
            return null;
        }
        try{
            needGetYuv = true;
            needGetYuvCondt.await(500, TimeUnit.MILLISECONDS);
        	if(needGetYuv || !checkYUV(yuvData)){
                needGetYuvCondt.await(500, TimeUnit.MILLISECONDS);
        	}
        } catch (InterruptedException e){
    		RvsLog.i(MediaSurfaceView.class, "onGetOneJpegFrame()", "needGetYuvCondt.await fail.");
        }
        byte[] out = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try{
            YuvImage yuvImage = null;
            Rect rect = null ;
            if(screenOritation == Configuration.ORIENTATION_PORTRAIT){
                yuvImage = new YuvImage(yuvData, previewformat, videoHeight, videoWidth, null);
                rect = new Rect(0, 0,  videoHeight,videoWidth);
            }else{
                yuvImage = new YuvImage(yuvData, previewformat, videoWidth, videoHeight, null);
                rect = new Rect(0, 0, videoWidth, videoHeight);
            }
            int quality = 100;
            if (type == 0){// hd
            	quality = 100;
                yuvImage.compressToJpeg(rect, quality, bos);
            } else if (type == 1){// normal
            	quality = 60;
                yuvImage.compressToJpeg(rect, quality, bos);
            } else if (type == 2){// small
            	quality = 30;
                yuvImage.compressToJpeg(rect, quality, bos);
            }

			BitmapFactory.Options options = new BitmapFactory.Options();
			if (videoWidth == 640)
				options.inSampleSize = 4;
			else if (videoWidth == 320)
				options.inSampleSize = 2;

			byte[] data = bos.toByteArray();
			bos.reset();
			Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length,options);

			bmp.compress(CompressFormat.JPEG, quality, bos);
			out = bos.toByteArray();

        } catch (Exception e){
    		RvsLog.i(MediaSurfaceView.class, "onGetOneJpegFrame()", "get image fail.");
        } finally{
            try{
                bos.close();
            } catch (IOException ee){
            }
            yuvLock.unlock();
        }

        return out;
	}
	
	/**
	 * 当sdk需要或者不需要采集端输入视频数据时回调通知
	 * @param needVideoData true:sdk需要输入视频数据，false:不需要
	 */
	@Override
	public void onVideoDataNotify(boolean needVideoData){
//		if (needVideoData){
//			int colorMode = mOpenBackCamera ? mBackCameraColorMode : mFrontCameraColorMode;
//
//			mVideoEncoder.setColorMode(colorMode);
//
//			mRebootCamTask = new Runnable(){
//				@Override
//				public void run(){
//					RvsLog.i(MediaSurfaceView.class, "onVideoDataNotify()", "Camera onPreview error,restart camera preview!");
//					stopPreview();
//					startPreview();
//				}
//			};
//			mHandler.postDelayed(mRebootCamTask, RESTART_CAM_DELAY);
//		}
//		mNeedEncodeVideo = needVideoData;
	}
	
	/**
	 * 运动侦测状态回调
	 * @param state 状态信息，包括侦测开始、结束、发生运动事件
	 */
	@Override
	public void onMotionDetectState(MotionDetectState state){
		RvsLog.i(MediaSurfaceView.class, "onMotionDetectState()", "state = " + state);
		switch (state)
		{
		case START:
			break;
		case STOP:
			break;
		case MOTIONDECTED:
			RvsLog.i(MediaSurfaceView.class, "onMotionDetectState()", "motion happened.");
			break;
		case ERROR:
			break;
		default:
			break;
		}
	}
	
	/**
	 * 采集端运动侦测的设定回调
	 * @param schedule 报警设置信息
	 */
	@Override
	public void onMotionDetectSettingUpdate(RvsAlarmRecordInfo schedule){
		if (schedule != null && schedule.getScheduleSettings() != null){
			for (ScheduleSetting setting : schedule.getScheduleSettings()){
				RvsLog.i(MediaSurfaceView.class, "onMotionDetectSettingUpdate()", "alarm record:" + setting.isEnable() + "," + setting.getIntervalValue() 
					+ "," + setting.getStartSecond() + "," + setting.getEndSecond() 
					+ "," + setting.getWeekFlag());
			}
		}
	}

	/**
	 * 当sdk需要或者不需要采集端输入音频数据时回调通知
	 * @param needAudioData true:sdk需要端输入音频数据，false:不需要
	 */
	@Override
	public void onAudioDataNotify(boolean needAudioData){
		RvsLog.i(MediaSurfaceView.class, "onAudioDataNotify()", "need write:" + needAudioData);
		if (needAudioData){
			audioHanlder.resumeAudioRecord();
		} else{
			audioHanlder.pauseAudioRecord();
		}
	}

	/**
	 * 回调通知采集端，客户端发送音频流到采集端， 采集端可以根据clientCid决定是否接受等操作。
	 * @param audioStreamId 逆向音频流id，用于标识该媒体流，获取描述等
	 * @param clientCid 客户端cid
	 * @param status 音频流状态 0：打开，1：关闭
	 */
	@Override
	public void onRevAudioStatus(long audioStreamId, long clientCid, int status){
		RvsLog.i(MediaSurfaceView.class, "onRevAudioStatus()", "clientCid:" + clientCid + ",status:" + status + ",audioStreamId:" + audioStreamId);
		revAudioStream = audioStreamId;
		
		if (status == 2){
			audioHanlder.resumeAudioPlay();
			RvsLog.i(MediaSurfaceView.class, "onRevAudioStatus()", "resumeAudioPlay");
		} else{
			audioHanlder.pauseAudioPlay();
			media.closeRevAudioStream(revAudioStream);
			RvsLog.i(MediaSurfaceView.class, "onRevAudioStatus()", "closeRevAudioStream");
		}
	}

	/**
	 * 录制发生或者结束回调通知。
	 * @param type 录制类型
	 * @param state 录制状态
	 */
	@Override
	public void onRecordState(RvsRecordType type, RvsRecordState state){
		RvsLog.i(MediaSurfaceView.class, "onRecordState()", "onRecordState : " + type.toString() + "," + state.toString());
	}
	
	/**
	 * 定时录制设置发生变化的通知回调
	 * 
	 * @param schedule 定时录制设置信息
	 */
	@Override
	public void onTimeRecordSettingUpdate(RvsTimeRecordInfo schedule){
		if (schedule != null && schedule.getScheduleSettings() != null){
			for (ScheduleSetting setting : schedule.getScheduleSettings()){
				RvsLog.i(MediaSurfaceView.class, "onTimeRecordSettingUpdate()", 
					"time record:" + setting.isEnable() + "," + setting.getStartSecond() + "," + setting.getEndSecond() + "," + setting.getWeekFlag());
			}
		}
	}

	/**
	 * 接收采集端发到客户端的自定义数据
	 * 
	 * @param remoteCID 远端cid，包括客户端和采集端
	 * @param data 自定义数据
	 */
	@Override
	public void onReceiveCustomData(long remoteCID, byte[] data){
		RvsLog.i(MediaSurfaceView.class, "onReceiveCustomData()", 
			"remoteCID:" + remoteCID + ", data : " + new String(data));
	}

	@Override
	public void onCustomCommandListener(long remoteCID, int commandId, String command){
		RvsLog.i(MediaSurfaceView.class, "onCustomCommandListener()", 
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

	/**
	 * 客户端请求修改实时视频流的参数。
	 * 
	 * @param remoteCid 客户端cid
	 * @param msgId 这条命令的消息id,用于调用{@linkplain Command#submitProcessResult
	 * 						(long, long, int, com.ichano.rvs.streamer.Command.ResultCode) submitProcessResult()} 返回处理结果
	 * @param msgType 这条命令的type 用于调用{@linkplain Command#submitProcessResult
	 * 						(long, long, int, com.ichano.rvs.streamer.Command.ResultCode) submitProcessResult()} 返回处理结果
	 * @param camId 摄像头id
	 * @param streamid 摄像头视频流id
	 * @param frameRate 修改的帧率
	 * @param bitrate 修改的比特率
	 * @param streamQuality 修改的视频质量
	 * @param iframeInterval 修改的I帧间隔（假定采集端h264编码）
	 */
	@Override
	public void onSetStreamQuality(long remoteCid, long msgId, int msgType, int camId, int streamid, int frameRate, int bitrate, int streamQuality, int iframeInterval){
		RvsLog.i(MediaSurfaceView.class, "onSetStreamQuality()", "streamQuality:" + streamQuality);
		if (isLowVersion())
		{

		} else
		{
			if (streamQuality == 25)
			{
				mVideoEncoder.adjustStreamQuality(frameRate, 192000, iframeInterval);
			} else if (streamQuality == 50)
			{
				mVideoEncoder.adjustStreamQuality(frameRate, videoBitrate, iframeInterval);
			}
		}
		command.submitProcessResult(remoteCid, msgId, msgType, ResultCode.OK);
	}

	/**
	 * 客户端请求采集端切换前后摄像头。
	 * 
	 * @param remoteCid remoteCid 客户端cid
	 * @param msgId 这条命令的消息id,用于调用{@linkplain Command#submitProcessResult
	 * 						(long, long, int, com.ichano.rvs.streamer.Command.ResultCode) submitProcessResult()} 返回处理结果
	 * @param msgType 这条命令的type 用于调用{@linkplain Command#submitProcessResult
	 * 						(long, long, int, com.ichano.rvs.streamer.Command.ResultCode) submitProcessResult()} 返回处理结果
	 */
	@Override
	public void onSwitchFrontRearCamera(long remoteCid, long msgId, int msgType){
		RvsLog.i(MediaSurfaceView.class, "onSwitchFrontRearCamera()", "onSwitchFrontRearCamera");
		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.FROYO || Camera.getNumberOfCameras() < 2){
			command.submitProcessResult(remoteCid, msgId, msgType, ResultCode.UNSUPPORT);
			return;
		}
		mIsSupportCameraLight = false;
		mIsTorchLight = false;
		mOpenBackCamera = !mOpenBackCamera;
		stopPreview();
		releaseCamera();
		initCamera();
		startPreview();
		command.submitProcessResult(remoteCid, msgId, msgType, ResultCode.OK);
	}

	private void switchLight(String open){
		try{
			if (mCamera != null){
				if (open.equals("1")){
					Parameters parameter = mCamera.getParameters();
					if (parameter.getFlashMode().equals(Parameters.FLASH_MODE_OFF)){
						parameter.setFlashMode(Parameters.FLASH_MODE_TORCH);
						mCamera.setParameters(parameter);
						mIsTorchLight = true;
					} else{
						parameter.setFlashMode(Parameters.FLASH_MODE_OFF);
						mCamera.setParameters(parameter);
						mIsTorchLight = false;
					}
				} else if (open.equals("0")){
					Parameters parameter = mCamera.getParameters();

					if (parameter.getFlashMode() != null){
						if (parameter.getFlashMode().equals(Parameters.FLASH_MODE_TORCH)){
							parameter.setFlashMode(Parameters.FLASH_MODE_OFF);
							mCamera.setParameters(parameter);
							mIsTorchLight = false;
						}
					}
				}
			}
		} catch (Exception e){
			mIsTorchLight = false;
			e.printStackTrace();
		}
	}
	
	/**
	 * 客户端请求采集端开关闪光灯。
	 * 
	 * @param remoteCid remoteCid 客户端cid
	 * @param msgId 这条命令的消息id,用于调用{@linkplain Command#submitProcessResult
	 * 						(long, long, int, com.ichano.rvs.streamer.Command.ResultCode) submitProcessResult()} 返回处理结果
	 * @param msgType 这条命令的type 用于调用{@linkplain Command#submitProcessResult
	 * 						(long, long, int, com.ichano.rvs.streamer.Command.ResultCode) submitProcessResult()} 返回处理结果
	 */
	@Override
	public void onSwitchTorch(long remoteCid, long msgId, int msgType){
		RvsLog.i(MediaSurfaceView.class, "onSwitchTorch()", "onSwitchTorch");
		if (mIsSupportCameraLight){
			if (mIsTorchLight){
				switchLight("0");
			} else{
				switchLight("1");
			}

			command.submitProcessResult(remoteCid, msgId, msgType, ResultCode.OK);
		} else{
			command.submitProcessResult(remoteCid, msgId, msgType, ResultCode.UNSUPPORT);
		}
	}

	/*
	@Override
	public void onPTZorMove(long remoteCid, int camIndex, int type, int PorAxis_X, int TorAxis_Y, int ZorAxis_Z){
		Log.d(TAG, "from client :" + remoteCid+",type:"+type+",PorAxis_X:"+PorAxis_X+",TorAxis_Y:"+TorAxis_Y+",ZorAxis_Z:"+ZorAxis_Z);
	}
	*/
	
	public static class CameraPreviewColorMode{
		public static final int VIDEO_DEFAULT = 0;
		public static final int VIDEO_YUV420 = 1;
		public static final int VIDEO_NV21 = 2;
		public static final int VIDEO_NV12 = 3;
	}
	
	class VideoEncoder{
		
		private final VideoType colorspace = VideoType.NV21;// x264 颜色空间 X264_CSP_NV21
//		private static final int _480Bitrate = 768000;
//		private static final int _240Bitrate = 384000;
//		private static final int frameRate = 15;
//		private static final int iframeInterval = 30;

		private long videoChannel;

		private int colorMode;

		private int previewformat;

		private int videoWidth;

		private int videoHeight;

		private byte[] yv12;
		
		private Media media;
		private boolean useMediaCodec = false;
		private HardwareEncoder avcEncoder;
		
		public VideoEncoder(int width, int height){
			this.media = Streamer.getStreamer().getMedia();
			this.videoWidth = width;
			this.videoHeight = height;

			this.videoChannel = media.getVideoWriteChannel();

			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){// 4.1 以下用x264
				useMediaCodec = false;
			} else{
				useMediaCodec = true;
			}
			if(isSamsungS4()){
				RvsLog.i(MediaSurfaceView.class, "VideoEncoder()", "isSamsungS4");
				useMediaCodec = false;
			}
			if(isChe2UL00()){
				RvsLog.i(MediaSurfaceView.class, "VideoEncoder()", "isisChe2UL00");
				useMediaCodec = false;
			}
			if (useMediaCodec){
				initAvcEncoder();
			} else{
				initX264Codec();
			}
		}

		private void initAvcEncoder(){
			RvsLog.i(MediaSurfaceView.class, "initAvcEncoder()", "hard encoder");
			try{
				avcEncoder = new HardwareEncoder(media, videoWidth, videoHeight, frameRate, videoBitrate, iframeInterval);
			} catch (Exception e){
				RvsLog.i(MediaSurfaceView.class, "onSwitchTorch()", "init hardware encoder error: " + e.getMessage());
				avcEncoder = null;
			}
			if (avcEncoder != null){
			} else{
				useMediaCodec = false;
			}
		}

		private void initX264Codec(){
			RvsLog.i(MediaSurfaceView.class, "initX264Codec()", "soft encoder");
			//int bitrate = (videoWidth == 320 && videoHeight == 240) ? _240Bitrate : _480Bitrate;
			int init264 = X264VideoCodec.init(videoWidth, videoHeight, colorspace.intValue(), videoBitrate, frameRate, iframeInterval);
		}
		
		private int tryTime = 0;
		private int loop = 0;
		public void writeYuvData(byte[] data,boolean needEncode,boolean inMotionDetect){
			if (inMotionDetect){
				media.writeYUVData(data, 0, data.length);
			}
			if (needEncode){
				if (useMediaCodec){
					int result = avcEncoder.sendVideoData(data, colorMode, enableTimeWatermark);
					if (result == -1){
						tryTime++;
						if (tryTime == 10){
							RvsLog.i(MediaSurfaceView.class, "writeYuvData()", "hardware encocer encode fail, use soft encoder, 1");
							switchX264();
						}
					}else if (result == -2) {
						RvsLog.i(MediaSurfaceView.class, "writeYuvData()", "hardware encocer encode fail, use soft encoder, 2");
						switchX264();
					}
				} else{
					X264VideoCodec.sendVideoData(videoChannel, data, enableTimeWatermark);
				}
			}
		}
		
		private void switchX264(){
			useMediaCodec = false;
			if (avcEncoder != null){
				avcEncoder.release();
				avcEncoder = null;
			}
			initX264Codec();
		}
		
		public void resetLoop(){
			if (useMediaCodec){

			} else{
				X264VideoCodec.resetLogloop();
			}
		}
		
		public void reqIframe(){
			if (useMediaCodec){

			} else{
				X264VideoCodec.reqIframe();
			}
		}

		public void adjustStreamQuality(int framerate, int bitrate, int keyinterval){
			if (useMediaCodec){
				avcEncoder.adjustStreamQuality(framerate, bitrate, keyinterval);
			} else{
				X264VideoCodec.adjustStreamQuality(framerate, bitrate, keyinterval);
			}
		}

		public void destroy(){
			if (useMediaCodec){
				if (avcEncoder != null){
					avcEncoder.release();
				}
			} else{
				X264VideoCodec.destroy();
			}
		}

		public void setColorMode(int colorMode){
			this.colorMode = colorMode;
		}
		public void setPreviewformat(int format){
			this.previewformat = format;
			int yuv_buffersize = videoWidth * videoHeight * ImageFormat.getBitsPerPixel(previewformat) / 8;
			this.yv12 = new byte[yuv_buffersize];
		}
	}
	
	/**
	 * 客户端发送云台ptz或者xyz轴移动命令。
	 * 
	 * @param remoteCid 客户端cid
	 * @param camIndex 摄像头id
	 * @param type 进行ptz或者xyz运动，值参见{@link Command#PTZMOVECTRL_PTZ}、 {@link Command#PTZMOVECTRL_MOVE}
	 * @param PorAxis_X Pan或者x轴移动
	 * @param TorAxis_Y Tilt或者y轴移动
	 * @param ZorAxis_Z Zoom或者z轴移动
	 */
	@Override
	public void onPTZorMove(long remoteCid, int camIndex, int type, int PorAxis_X, int TorAxis_Y, int ZorAxis_Z){
		
	}

	@Override
	public void onYuvDataNotify(boolean needYuvData){
//		mInMotionDetect = needYuvData;
	}

	@Override
	public int onPcmInput(short[] buffer, int length) {
		int size = media.getRevAudioData(revAudioStream, audioData);
		if (size > 0){
			for (int i = 0; i < size; i++){
				buffer[i] = (short) G711.ulaw2linear(audioData[i]);
			}
		}
		return size;
	}

	@Override
	public void onPcmOutput(short[] buffer, int size) {
		AACEncoder.writeAudioData(audioChannel, buffer, size);
	}

	public enum VideoSize{
		VIDEO_320P, VIDEO_480P, VIDEO_720P, VIDEO_1080P;
		
		public static int[] getVideoSize(VideoSize size){
			int[] videoSize = VIDEO_480;
			switch(size){
			case VIDEO_320P:
				return VIDEO_320;
			case VIDEO_480P:
				return VIDEO_480;
			case VIDEO_720P:
				return VIDEO_720;
			case VIDEO_1080P:
				return VIDEO_1080;
			}
			return videoSize;
		}
	}
	
	protected CameraState cameraState;
	public enum CameraState{
		/**启动相机*/
		START, 
		/**相机开始预览*/
		PREVIEW, 
		/**相机停止预览*/
		STOP, 
		/**相机遇到错误，比如：相机被占用等*/
		ERROR;
	}
	
	public interface CameraStateListener{
		public void onCameraStateChange(CameraState state);
	}
	
	private CameraStateListener cameraStateListener;
	public void setOnCameraStateListener(CameraStateListener listener){
		this.cameraStateListener = listener;
	}

	@Override
	public void onRecordError(int camID, int streamID) {
		// TODO Auto-generated method stub
		
	}
}
