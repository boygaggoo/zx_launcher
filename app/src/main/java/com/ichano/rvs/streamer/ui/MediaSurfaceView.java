package com.ichano.rvs.streamer.ui;

import android.view.SurfaceView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
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
import com.ichano.rvs.streamer.util.RotateUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressLint("NewApi")
public class MediaSurfaceView extends
		SurfaceView implements SurfaceHolder.Callback,VideoCallback,AudioCallback,MotionDetectCallback,MotionDetectSettingsCallback,RevAudioCallback,RecordCallback,TimeRecordSettingsCallback,MediaChannelListener,CustomDataRecvCallback,CommandCallback,View.OnClickListener,AudioIOHandler.AuidoCallback
{
	private static final String TAG = MediaSurfaceView.class.getSimpleName();
	private static final int RESTART_CAM_DELAY = 500;
	protected Streamer streamer;
	protected Media media;
	protected Command command;
	public int videoBitrate = 384000;
	public int frameRate = 15;
	public int iframeInterval = 30;
	protected static final int[] VIDEO_320 = { 320, 240 };
	protected static final int[] VIDEO_480 = { 640, 480 };
	protected static final int[] VIDEO_720 = { 1280, 720 };
	protected static final int[] VIDEO_1080 = { 1920, 1080 };
	protected static final int VIDEO_BITRATE_320 = 384000;
	protected static final int VIDEO_BITRATE_480 = 768000;
	protected static final int VIDEO_BITRATE_720 = 1024000;
	protected static final int VIDEO_BITRATE_1080 = 1728000;
	protected int channelConfig = 1;
	protected int bitsPerSample = 16;
	public static int audioSampleRateInHz = 8000;
	private int screenOritation = 2;
	private boolean mOpenBackCamera = true;
	private SurfaceHolder mSurfaceHolder = null;
	private SurfaceTexture mSurfaceTexture = null;
	private boolean mRunInBackground;
	private boolean enableRunInBackground = true;
	private Camera mCamera;
	private Camera.Parameters mCameraParameters;
	protected int videoWidth;
	protected int videoHeight;
	private int yuv_buffersize;
	private byte[] yuvData;
	private byte[] rotateData;
	private int mGetyuvloop = 0;
	private byte[] previewBuffer;
	private boolean mIsSupportCameraLight = false;
	private boolean mIsTorchLight = false;
	private int mCameraId;
	protected int previewformat = 17;
	private int mFrontCameraColorMode = 0;
	private int mBackCameraColorMode = 0;
	private VideoEncoder mVideoEncoder;
	private Runnable mRebootCamTask;
	private boolean mNeedEncodeVideo;
	private boolean mInMotionDetect = false;
	private final Lock yuvLock = new ReentrantLock();
	private boolean needGetYuv = false;
	private final Condition needGetYuvCondt = this.yuvLock.newCondition();
	private AudioIOHandler audioHanlder;
	private long audioChannel;
	private long revAudioStream = 0L;
	private byte[] audioData = new byte[2048];
	private static final int PCM_INPUT_SIZE = 2048;
	private static final int PCM_OUTPUT_SIZE = 1024;
	private boolean enableTimeWatermark;

	public MediaSurfaceView(Context context) {
		super(context);
		init(context);
	}

	public MediaSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MediaSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
//
//	public MediaSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//		super(context, attrs, defStyleAttr, defStyleRes);
//		init(context);
//	}

	private void init(Context context) {
		setOnClickListener(this);
		this.mSurfaceHolder = getHolder();
		this.mSurfaceHolder.addCallback(this);
		this.mSurfaceHolder.setType(3);
		if (!isLowVersion()) {
			this.mSurfaceTexture = new SurfaceTexture(10);
		}
	}

	public void setDefaultCamera(boolean backCamera) {
		this.mOpenBackCamera = backCamera;
	}

	private boolean customVideoParam = false;

	public void setVideoParam(int videoBitrate, int frameRate, int iframeInterval) {
		this.videoBitrate = videoBitrate;
		this.frameRate = frameRate;
		this.iframeInterval = iframeInterval;

		this.customVideoParam = true;
	}

	private void getVideoParam(int videoWith, int videoHeight) {
		if (!this.customVideoParam) {
			int size = videoHeight * videoWith;
			int size320 = VIDEO_320[0] * VIDEO_320[1];
			int size480 = VIDEO_480[0] * VIDEO_480[1];
			int size720 = VIDEO_720[0] * VIDEO_720[1];
			int size1080 = VIDEO_1080[0] * VIDEO_1080[1];
			if (size <= size320) {
				this.videoBitrate = 384000;
			} else if ((size > size320) && (size <= size480)) {
				this.videoBitrate = 768000;
			} else if ((size > size480) && (size <= size720)) {
				this.videoBitrate = 1024000;
			} else if (size > size1080) {
				this.videoBitrate = 1728000;
			}
		}
	}

	public void enableTimeWatermark(boolean enable) {
		this.enableTimeWatermark = enable;
	}

	public void openCamera(int screenOritation) {
		int[] size = isLowVersion() ? VIDEO_320 : VIDEO_480;
		openCamera(size[0], size[1], screenOritation);
	}

	public void openCamera(VideoSize size, int screenOritation) {
		int[] videoSize = VideoSize.getVideoSize(size);
		openCamera(videoSize[0], videoSize[1], screenOritation);
	}

	public void openCamera(int videoWidth, int videoHeight, int screenOritation) {
		this.screenOritation = screenOritation;
		this.videoWidth = videoWidth;
		this.videoHeight = videoHeight;
		getVideoParam(videoWidth, videoHeight);
		this.streamer = Streamer.getStreamer();
		this.media = this.streamer.getMedia();
		initMedia(false);
		this.media.setVideoCallback(this);
		this.media.setAudioCallback(this);
		this.media.setChannelLister(this);
		this.media.setRecordCallback(this);
		this.media.setRevAudioCallback(this);
		this.media.setMotionDetectCallback(this);
		this.media.setRecordSettingsCallback(this);
		this.media.setMotionDetectSettingsCallback(this);
		this.command = this.streamer.getCommand();
		this.command.setCallback(this);
		this.command.setCustomDataRecvCallback(this);

		this.audioHanlder = AudioIOHandler.getInstance(getContext());
		this.audioHanlder.setAuidoCallback(this, 2048, 1024);
		this.audioHanlder.startAudio(false, false);
		if (screenOritation == 1) {
			this.mVideoEncoder = new VideoEncoder(videoHeight, videoWidth);
		} else {
			this.mVideoEncoder = new VideoEncoder(videoWidth, videoHeight);
		}
		this.cameraState = CameraState.START;
		if (this.cameraStateListener != null) {
			this.cameraStateListener.onCameraStateChange(this.cameraState);
		}
		RvsLog.i(MediaSurfaceView.class, "openCamera()", "start to open camera.");
		initCamera();
		RvsLog.i(MediaSurfaceView.class, "openCamera()", "end of open camera.");
	}

	public void closeCamera() {
		stopPreview();
		releaseCamera();
		this.mVideoEncoder.destroy();
		this.audioHanlder.releaseAudio();
		AACEncoder.destroy();
	}

	public void restartCamera() {
		stopPreview();
		releaseCamera();
		initCamera();
		startPreview();
	}

	public int[] getVideoSize() {
		int[] size = { this.videoWidth, this.videoHeight };
		return size;
	}

	private boolean isLowVersion() {
		return Build.VERSION.SDK_INT <= 10;
	}

	private void selectCorrectSampleRate() {
		int minBufSize = -1;
	}

	private void initMedia(boolean enableAEC) {
		Capacity capacity = new Capacity();
		if (enableAEC) {
			capacity.setEchoCancelMode(1);
			audioSampleRateInHz = 8000;
		} else {
			capacity.setEchoCancelMode(0);
			selectCorrectSampleRate();
		}
		capacity.setRecordMode(2);
		capacity.setRunMode(3);
		capacity.setTimeZoneMode(0);
		this.streamer.setCapacity(capacity);

		CameraCapacity cc = new CameraCapacity();
		cc.setStreamType(0);
		cc.setTorchEnable(true);
		this.media.setCameraCapacity(cc);
		int[] videoSize = getVideoSize();
		StreamProperty streamProperty = null;
		if (this.screenOritation == 1) {
			streamProperty = new StreamProperty(videoSize[1], videoSize[0], 0, this.videoBitrate, this.frameRate,
					this.iframeInterval, VideoType.H264);
		} else {
			streamProperty = new StreamProperty(videoSize[0], videoSize[1], 0, this.videoBitrate, this.frameRate,
					this.iframeInterval, VideoType.H264);
		}
		RvsLog.i(MediaSurfaceView.class, "initMedia()", "Video param : videoSize = " + videoSize + ", videoBitrate = "
				+ this.videoBitrate + ", frameRate = " + this.frameRate + ", iframeInterval = " + this.iframeInterval);
		this.media.setCameraStreamProperty(streamProperty);

		AudioProperty audioProperty = new AudioProperty(audioSampleRateInHz, this.channelConfig, this.bitsPerSample,
				AudioType.AAC);
		RvsLog.i(MediaSurfaceView.class, "initMedia()", "Audio param : audioSampleRateInHz = " + audioSampleRateInHz
				+ ", channelConfig = " + this.channelConfig + ", bitsPerSample = " + this.bitsPerSample);
		this.media.setMicProperty(audioProperty);

		this.audioChannel = this.media.getAudioWriteChannel();
		AACEncoder.init(64000, this.channelConfig, audioSampleRateInHz, this.bitsPerSample);
	}

	private int findCamera(boolean front) {
		int cameraCount = 0;
		try {
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			cameraCount = Camera.getNumberOfCameras();
			for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
				Camera.getCameraInfo(camIdx, cameraInfo);
				int facing = front ? 1 : 0;
				if (cameraInfo.facing == facing) {
					return camIdx;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			RvsLog.e(MediaSurfaceView.class, "findCamera()", "can not find " + (front ? "front" : "back") + " camera");
		}
		return -1;
	}

	private void initCamera() {
		if (Build.VERSION.SDK_INT > 8) {
			if (this.mOpenBackCamera) {
				this.mCameraId = findCamera(false);
			} else {
				this.mCameraId = findCamera(true);
			}
			if (this.mCameraId == -1) {
				this.mCameraId = 0;
			}
		}
		try {
			if (Build.VERSION.SDK_INT == 8) {
				this.mCamera = Camera.open();
			} else {
				this.mCamera = Camera.open(this.mCameraId);
			}
		} catch (Exception ee) {
			this.mCamera = null;
			this.cameraState = CameraState.ERROR;
			if (this.cameraStateListener != null) {
				this.cameraStateListener.onCameraStateChange(this.cameraState);
			}
			RvsLog.e(MediaSurfaceView.class, "initCamera()", "init camera failed.");
		}
		if (this.mCamera == null) {
			return;
		}
		initCameraFormat();
	}

	private boolean isMeizu() {
		if ("meizu".equalsIgnoreCase(Build.BRAND)) {
			return true;
		}
		try {
			Method method = Build.class.getMethod("hasSmartBar", new Class[0]);
			if (method != null) {
				return true;
			}
		} catch (NoSuchMethodException e) {
			return false;
		}
		return false;
	}

	private void initCameraFormat() {
		if (isLowVersion()) {
			this.previewformat = 842094169;
		} else {
			this.previewformat = 17;
		}
		if (isMeizu()) {
			this.previewformat = 17;
		}
		if (this.mOpenBackCamera) {
			this.mVideoEncoder.setColorMode(this.mBackCameraColorMode);
			if (this.mBackCameraColorMode == 2) {
				this.previewformat = 17;
			}
		} else {
			this.mVideoEncoder.setColorMode(this.mFrontCameraColorMode);
			if (this.mFrontCameraColorMode == 2) {
				this.previewformat = 17;
			}
		}
		this.mVideoEncoder.setPreviewformat(this.previewformat);
	}

	public void switchCameraPreviewColorMode(int colorMode) {
		if (this.mOpenBackCamera) {
			this.mBackCameraColorMode = colorMode;
		} else {
			this.mFrontCameraColorMode = colorMode;
		}
		if (colorMode == 2) {
			stopPreview();
			this.previewformat = 17;
			this.mVideoEncoder.setPreviewformat(this.previewformat);
			startPreview();
		}
	}

	private void releaseCamera() {
		try {
			if (this.mCamera != null) {
				this.mCamera.setPreviewCallback(null);
				this.mCamera.setPreviewCallbackWithBuffer(null);
				this.mCamera.stopPreview();
				this.mCamera.release();
				this.mCamera = null;
			}
		} catch (Exception ee) {
			RvsLog.e(MediaSurfaceView.class, "releaseCamera()", "releaseCamera  failed");
		}
	}

	private void isSupportCameraLight() {
		try {
			if (this.mCamera != null) {
				Camera.Parameters parameter = this.mCamera.getParameters();
				Object a = parameter.getSupportedFlashModes();
				if (a == null) {
					this.mIsSupportCameraLight = false;
				} else {
					this.mIsSupportCameraLight = true;
				}
			}
		} catch (Exception e) {
			this.mIsSupportCameraLight = false;
			e.printStackTrace();
		}
	}

	private boolean flip = false;

	public void flip() {
		this.flip = (!this.flip);
	}

	private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback()
	  {
	    public synchronized void onPreviewFrame(byte[] data,Camera camera){if(data==null){MediaSurfaceView.this.releaseCamera();RvsLog.e(MediaSurfaceView.class,"onPreviewFrame()","preview get null data!");return;}if(MediaSurfaceView.this.cameraState!=MediaSurfaceView.CameraState.PREVIEW){MediaSurfaceView.this.cameraState=MediaSurfaceView.CameraState.PREVIEW;if(MediaSurfaceView.this.cameraStateListener!=null){MediaSurfaceView.this.cameraStateListener.onCameraStateChange(MediaSurfaceView.this.cameraState);}}if(MediaSurfaceView.this.screenOritation==1){if(MediaSurfaceView.this.flip){if(MediaSurfaceView.this.previewformat==17){MediaSurfaceView.this.rotateData=RotateUtil.rotate(data,1,MediaSurfaceView.this.videoWidth,MediaSurfaceView.this.videoHeight,3);}else{MediaSurfaceView.this.rotateData=RotateUtil.rotate(data,2,MediaSurfaceView.this.videoWidth,MediaSurfaceView.this.videoHeight,3);}}else if(MediaSurfaceView.this.previewformat==17){MediaSurfaceView.this.rotateData=RotateUtil.rotate(data,1,MediaSurfaceView.this.videoWidth,MediaSurfaceView.this.videoHeight,1);}else{MediaSurfaceView.this.rotateData=RotateUtil.rotate(data,2,MediaSurfaceView.this.videoWidth,MediaSurfaceView.this.videoHeight,1);}if(MediaSurfaceView.this.yuvLock.tryLock()){if(data.length==MediaSurfaceView.this.yuvData.length){System.arraycopy(MediaSurfaceView.this.rotateData,0,MediaSurfaceView.this.yuvData,0,MediaSurfaceView.this.rotateData.length);}MediaSurfaceView.this.needGetYuv=false;MediaSurfaceView.this.needGetYuvCondt.signalAll();MediaSurfaceView.this.yuvLock.unlock();}if((MediaSurfaceView.this.mNeedEncodeVideo)||(MediaSurfaceView.this.mInMotionDetect)){MediaSurfaceView.this.mVideoEncoder.writeYuvData(MediaSurfaceView.this.rotateData,MediaSurfaceView.this.mNeedEncodeVideo,MediaSurfaceView.this.mInMotionDetect);}}else{if(MediaSurfaceView.this.flip){if(MediaSurfaceView.this.previewformat==17){MediaSurfaceView.this.rotateData=RotateUtil.rotate(data,1,MediaSurfaceView.this.videoWidth,MediaSurfaceView.this.videoHeight,2);}else{MediaSurfaceView.this.rotateData=RotateUtil.rotate(data,2,MediaSurfaceView.this.videoWidth,MediaSurfaceView.this.videoHeight,2);}data=MediaSurfaceView.this.rotateData;}if(MediaSurfaceView.this.yuvLock.tryLock()){if(data.length==MediaSurfaceView.this.yuvData.length){System.arraycopy(data,0,MediaSurfaceView.this.yuvData,0,data.length);}MediaSurfaceView.this.needGetYuv=false;MediaSurfaceView.this.needGetYuvCondt.signalAll();MediaSurfaceView.this.yuvLock.unlock();}if((MediaSurfaceView.this.mNeedEncodeVideo)||(MediaSurfaceView.this.mInMotionDetect)){MediaSurfaceView.this.mVideoEncoder.writeYuvData(data,MediaSurfaceView.this.mNeedEncodeVideo,MediaSurfaceView.this.mInMotionDetect);}}MediaSurfaceView.this.mCamera.addCallbackBuffer(MediaSurfaceView.this.previewBuffer);}};

	public static void nv21_rotate_90(byte[] des, byte[] src, int width, int height) {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				des[(height - i - 1 + j * height)] = src[(i * width + j)];
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
		for (int i = 0; i < ul; i++) {
			v[i] = src[(i * 2 + yl)];
			u[i] = src[(i * 2 + 1 + yl)];
		}
		for (int i = 0; i < halfH; i++) {
			for (int j = 0; j < halfw; j++) {
				tv[(halfH - i - 1 + j * halfH)] = v[(i * halfw + j)];
				tu[(halfH - i - 1 + j * halfH)] = u[(i * halfw + j)];
			}
		}
		for (int i = 0; i < ul; i++) {
			des[(yl + 2 * i)] = tv[i];
			des[(yl + 2 * i + 1)] = tu[i];
		}
	}

	public static void yv12_rotate_90(byte[] temp, byte[] data, int videoWidth, int videoHeight) {
		for (int i = 0; i < videoHeight; i++) {
			for (int j = 0; j < videoWidth; j++) {
				temp[(videoHeight - i - 1 + j * videoHeight)] = data[(i * videoWidth + j)];
			}
		}
		int halfw = videoWidth / 2;
		int halfH = videoHeight / 2;
		int yl = videoWidth * videoHeight;
		int ul = halfw * halfH;
		for (int i = 0; i < halfH; i++) {
			for (int j = 0; j < halfw; j++) {
				temp[(halfH - i - 1 + j * halfH + yl)] = data[(i * halfw + j + yl)];
				temp[(halfH - i - 1 + j * halfH + yl + ul)] = data[(i * halfw + j + yl + ul)];
			}
		}
	}

	private void startPreview() {
		if (this.mCamera == null) {
			return;
		}
		try {
			this.mCamera.setErrorCallback(new Camera.ErrorCallback() {
				public void onError(int error, Camera camera) {
					if (MediaSurfaceView.this.cameraState != MediaSurfaceView.CameraState.ERROR) {
						MediaSurfaceView.this.cameraState = MediaSurfaceView.CameraState.ERROR;
						if (MediaSurfaceView.this.cameraStateListener != null) {
							MediaSurfaceView.this.cameraStateListener
									.onCameraStateChange(MediaSurfaceView.this.cameraState);
						}
					}
				}
			});
			this.mCameraParameters = this.mCamera.getParameters();

			this.yuv_buffersize = (this.videoWidth * this.videoHeight * ImageFormat.getBitsPerPixel(this.previewformat)
					/ 8);
			this.mCameraParameters.setPreviewFormat(this.previewformat);
			this.mCameraParameters.setJpegQuality(100);

			this.previewBuffer = new byte[this.yuv_buffersize];
			this.yuvData = new byte[this.yuv_buffersize];
			this.rotateData = new byte[this.yuv_buffersize];
			if (this.screenOritation != 2) {
				this.mCameraParameters.setPreviewSize(this.videoWidth, this.videoHeight);
				this.mCameraParameters.set("orientation", "portrait");
				this.mCameraParameters.setRotation(0);
				this.mCameraParameters.set("rotation", 0);
				this.mCamera.setDisplayOrientation(90);
			} else {
				this.mCameraParameters.setPreviewSize(this.videoWidth, this.videoHeight);
				this.mCameraParameters.set("orientation", "landscape");
				this.mCamera.setDisplayOrientation(0);
				this.mCameraParameters.setRotation(0);
				this.mCameraParameters.set("rotation", 0);
			}
			if (this.mRunInBackground) {
				if (!isLowVersion()) {
					this.mCamera.setPreviewTexture(this.mSurfaceTexture);
					this.mCamera.addCallbackBuffer(this.previewBuffer);
					this.mCamera.setPreviewCallbackWithBuffer(this.previewCallback);
				}
			} else {
				this.mCamera.setPreviewDisplay(this.mSurfaceHolder);
				this.mCamera.setPreviewCallback(this.previewCallback);
			}
			this.mCamera.setParameters(this.mCameraParameters);
			isSupportCameraLight();
			this.mCamera.startPreview();
			if (this.cameraState != CameraState.START) {
				this.cameraState = CameraState.START;
				if (this.cameraStateListener != null) {
					this.cameraStateListener.onCameraStateChange(this.cameraState);
				}
			}
		} catch (Exception e) {
			releaseCamera();
			RvsLog.e(MediaSurfaceView.class, "startPreview()", "start preview failed");
			return;
		}
		try {
			String mode = this.mCamera.getParameters().getFocusMode();
			if (("auto".equals(mode)) || ("macro".equals(mode))) {
				this.mCamera.autoFocus(null);
			}
		} catch (Exception e) {
			RvsLog.e(MediaSurfaceView.class, "startPreview()", "autoFocus failed");
		}
	}

	private void stopPreview() {
		if (this.mCamera == null) {
			return;
		}
		try {
			if (this.mRunInBackground) {
				this.mCamera.setPreviewCallbackWithBuffer(null);
				this.mCamera.stopPreview();
			} else {
				this.mCamera.setPreviewCallback(null);
				this.mCamera.stopPreview();
			}
			if (this.cameraState != CameraState.STOP) {
				this.cameraState = CameraState.STOP;
				if (this.cameraStateListener != null) {
					this.cameraStateListener.onCameraStateChange(this.cameraState);
				}
			}
		} catch (Exception ee) {
			RvsLog.e(MediaSurfaceView.class, "stopPreview()", "stopPreview failed");
		}
	}

	private Handler mHandler=new Handler(){public void handleMessage(Message msg){}};

	public void onClick(View v) {
		try {
			if (this.mCamera != null) {
				this.mCamera.autoFocus(null);
			}
		} catch (RuntimeException localRuntimeException) {
		}
	}

	private boolean autoOpenCamera = true;

	public void autoOpenCamera(boolean auto) {
		this.autoOpenCamera = auto;
	}

	public void enableRunInBackground(boolean runInBackground) {
		this.enableRunInBackground = runInBackground;
	}

	public void startCameraPreview() {
		stopPreview();
		startPreview();
	}

	public void stopCameraPreview() {
		stopPreview();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		RvsLog.i(MediaSurfaceView.class, "surfaceCreated()", "surfaceCreated");
		if (this.enableRunInBackground) {
			this.mRunInBackground = false;
		}
		if (this.autoOpenCamera) {
			startCameraPreview();
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		RvsLog.i(MediaSurfaceView.class, "surfaceDestroyed()", "surfaceDestroyed");
		if (this.enableRunInBackground) {
			this.mRunInBackground = true;
		}
		if (this.autoOpenCamera) {
			stopPreview();
			startPreview();
		}
	}

	public void onKeyFrameRequired() {
		if (!isLowVersion()) {
			this.mVideoEncoder.reqIframe();
		}
	}

	private boolean isRecording = false;
	protected CameraState cameraState;
	private CameraStateListener cameraStateListener;

	public boolean startRecord() {
		if (this.cameraState != CameraState.PREVIEW) {
			return false;
		}
		this.isRecording = this.media.startCustomRecord(0, 0);
		return this.isRecording;
	}

	public boolean stopRecord() {
		boolean ret = this.media.stopCustomRecord(0, 0);
		if (ret) {
			this.isRecording = false;
		}
		return ret;
	}

	public boolean isRecording() {
		return this.isRecording;
	}

	public Bitmap capture(JpegType type) {
		byte[] data = onGetOneJpegFrame(type.intValue());
		if (data != null) {
			return BitmapFactory.decodeByteArray(data, 0, data.length);
		}
		return null;
	}

	public byte[] onGetOneJpegFrame(int type) {
		RvsLog.i(MediaSurfaceView.class, "onGetOneJpegFrame()", "type = " + JpegType.valueToString(type));
		if (!this.yuvLock.tryLock()) {
			RvsLog.i(MediaSurfaceView.class, "onGetOneJpegFrame()", "yuvLock fail.");
			return null;
		}
		try {
			this.needGetYuv = true;
			this.needGetYuvCondt.await(500L, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			RvsLog.i(MediaSurfaceView.class, "onGetOneJpegFrame()", "needGetYuvCondt.await fail.");
		}
		byte[] out = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			YuvImage yuvImage = null;
			Rect rect = null;
			if (this.screenOritation == 1) {
				yuvImage = new YuvImage(this.yuvData, this.previewformat, this.videoHeight, this.videoWidth, null);
				rect = new Rect(0, 0, this.videoHeight, this.videoWidth);
			} else {
				yuvImage = new YuvImage(this.yuvData, this.previewformat, this.videoWidth, this.videoHeight, null);
				rect = new Rect(0, 0, this.videoWidth, this.videoHeight);
			}
			int quality = 100;
			if (type == 0) {
				quality = 100;
				yuvImage.compressToJpeg(rect, quality, bos);
			} else if (type == 1) {
				quality = 60;
				yuvImage.compressToJpeg(rect, quality, bos);
			} else if (type == 2) {
				quality = 30;
				yuvImage.compressToJpeg(rect, quality, bos);
			}
			BitmapFactory.Options options = new BitmapFactory.Options();
			if (this.videoWidth == 640) {
				options.inSampleSize = 4;
			} else if (this.videoWidth == 320) {
				options.inSampleSize = 2;
			}
			byte[] data = bos.toByteArray();
			bos.reset();
			Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);

			bmp.compress(Bitmap.CompressFormat.JPEG, quality, bos);
			out = bos.toByteArray();
		} catch (Exception e) {
			RvsLog.i(MediaSurfaceView.class, "onGetOneJpegFrame()", "get image fail.");
		} finally {
			try {
				bos.close();
			} catch (IOException localIOException1) {
			}
			this.yuvLock.unlock();
		}
		return out;
	}

	public void onVideoDataNotify(boolean needVideoData) {
		if (needVideoData) {
			int colorMode = this.mOpenBackCamera ? this.mBackCameraColorMode : this.mFrontCameraColorMode;

			this.mVideoEncoder.setColorMode(colorMode);

			this.mRebootCamTask = new Runnable() {
				public void run() {
					RvsLog.i(MediaSurfaceView.class, "onVideoDataNotify()",
							"Camera onPreview error,restart camera preview!");
					MediaSurfaceView.this.stopPreview();
					MediaSurfaceView.this.startPreview();
				}
			};
			this.mHandler.postDelayed(this.mRebootCamTask, 500L);
		}
		this.mNeedEncodeVideo = needVideoData;
	}

	public void onMotionDetectState(MotionDetectState state) {
		RvsLog.i(MediaSurfaceView.class, "onMotionDetectState()", "state = " + state);
		switch (state) {
		case ERROR:
			break;
		case INVALID:
			break;
		case START:
			RvsLog.i(MediaSurfaceView.class, "onMotionDetectState()", "motion happened.");
			break;
		case MOTIONDECTED:
			break;
		}
	}

	public void onMotionDetectSettingUpdate(RvsAlarmRecordInfo schedule) {
		if ((schedule != null) && (schedule.getScheduleSettings() != null)) {
			for (ScheduleSetting setting : schedule.getScheduleSettings()) {
				RvsLog.i(MediaSurfaceView.class, "onMotionDetectSettingUpdate()",
						"alarm record:" + setting.isEnable() + "," + setting.getIntervalValue() + ","
								+ setting.getStartSecond() + "," + setting.getEndSecond() + ","
								+ setting.getWeekFlag());
			}
		}
	}

	public void onAudioDataNotify(boolean needAudioData) {
		RvsLog.i(MediaSurfaceView.class, "onAudioDataNotify()", "need write:" + needAudioData);
		if (needAudioData) {
			this.audioHanlder.resumeAudioRecord();
		} else {
			this.audioHanlder.pauseAudioRecord();
		}
	}

	public void onRevAudioStatus(long audioStreamId, long clientCid, int status) {
		RvsLog.i(MediaSurfaceView.class, "onRevAudioStatus()",
				"clientCid:" + clientCid + ",status:" + status + ",audioStreamId:" + audioStreamId);
		this.revAudioStream = audioStreamId;
		if (status == 2) {
			this.audioHanlder.resumeAudioPlay();
			RvsLog.i(MediaSurfaceView.class, "onRevAudioStatus()", "resumeAudioPlay");
		} else {
			this.audioHanlder.pauseAudioPlay();
			this.media.closeRevAudioStream(this.revAudioStream);
			RvsLog.i(MediaSurfaceView.class, "onRevAudioStatus()", "closeRevAudioStream");
		}
	}

	public void onRecordState(RvsRecordType type, RvsRecordState state) {
		RvsLog.i(MediaSurfaceView.class, "onRecordState()",
				"onRecordState : " + type.toString() + "," + state.toString());
	}

	public void onTimeRecordSettingUpdate(RvsTimeRecordInfo schedule) {
		if ((schedule != null) && (schedule.getScheduleSettings() != null)) {
			for (ScheduleSetting setting : schedule.getScheduleSettings()) {
				RvsLog.i(MediaSurfaceView.class, "onTimeRecordSettingUpdate()", "time record:" + setting.isEnable()
						+ "," + setting.getStartSecond() + "," + setting.getEndSecond() + "," + setting.getWeekFlag());
			}
		}
	}

	public void onMediaChannelState(long clientCID, int stateCode, int currentChannelCount) {
		RvsLog.i(MediaSurfaceView.class, "onMediaChannelState()",
				"clientCID:" + clientCID + ", state : " + stateCode + ",currentChannelCount:" + currentChannelCount);
		if (currentChannelCount <= 0) {
			switchLight("0");
		}
	}

	public void onReceiveCustomData(long remoteCID, byte[] data) {
		RvsLog.i(MediaSurfaceView.class, "onReceiveCustomData()",
				"remoteCID:" + remoteCID + ", data : " + new String(data));
	}

	public void onCustomCommandListener(long remoteCID, int commandId, String command) {
		RvsLog.i(MediaSurfaceView.class, "onCustomCommandListener()",
				"remoteCID:" + remoteCID + "commandId:" + commandId + ", command : " + command);
	}

	public void onSetUserInfo(String userName, String password) {
		this.streamer.setUserNameAndPwd(userName, password);
	}

	public void onSetStreamQuality(long remoteCid, long msgId, int msgType, int camId, int streamid, int frameRate,
			int bitrate, int streamQuality, int iframeInterval) {
		RvsLog.i(MediaSurfaceView.class, "onSetStreamQuality()", "streamQuality:" + streamQuality);
		if (!isLowVersion()) {
			if (streamQuality == 25) {
				this.mVideoEncoder.adjustStreamQuality(frameRate, 192000, iframeInterval);
			} else if (streamQuality == 50) {
				this.mVideoEncoder.adjustStreamQuality(frameRate, this.videoBitrate, iframeInterval);
			}
		}
		this.command.submitProcessResult(remoteCid, msgId, msgType, Command.ResultCode.OK);
	}

	public void onSwitchFrontRearCamera(long remoteCid, long msgId, int msgType) {
		RvsLog.i(MediaSurfaceView.class, "onSwitchFrontRearCamera()", "onSwitchFrontRearCamera");
		if ((Build.VERSION.SDK_INT == 8) || (Camera.getNumberOfCameras() < 2)) {
			this.command.submitProcessResult(remoteCid, msgId, msgType, Command.ResultCode.UNSUPPORT);
			return;
		}
		this.mIsSupportCameraLight = false;
		this.mIsTorchLight = false;
		this.mOpenBackCamera = (!this.mOpenBackCamera);
		stopPreview();
		releaseCamera();
		initCamera();
		startPreview();
		this.command.submitProcessResult(remoteCid, msgId, msgType, Command.ResultCode.OK);
	}

	private void switchLight(String open) {
		try {
			if (this.mCamera != null) {
				if (open.equals("1")) {
					Camera.Parameters parameter = this.mCamera.getParameters();
					if (parameter.getFlashMode().equals("off")) {
						parameter.setFlashMode("torch");
						this.mCamera.setParameters(parameter);
						this.mIsTorchLight = true;
					} else {
						parameter.setFlashMode("off");
						this.mCamera.setParameters(parameter);
						this.mIsTorchLight = false;
					}
				} else if (open.equals("0")) {
					Camera.Parameters parameter = this.mCamera.getParameters();
					if ((parameter.getFlashMode() != null) && (parameter.getFlashMode().equals("torch"))) {
						parameter.setFlashMode("off");
						this.mCamera.setParameters(parameter);
						this.mIsTorchLight = false;
					}
				}
			}
		} catch (Exception e) {
			this.mIsTorchLight = false;
			e.printStackTrace();
		}
	}

	public void onSwitchTorch(long remoteCid, long msgId, int msgType) {
		RvsLog.i(MediaSurfaceView.class, "onSwitchTorch()", "onSwitchTorch");
		if (this.mIsSupportCameraLight) {
			if (this.mIsTorchLight) {
				switchLight("0");
			} else {
				switchLight("1");
			}
			this.command.submitProcessResult(remoteCid, msgId, msgType, Command.ResultCode.OK);
		} else {
			this.command.submitProcessResult(remoteCid, msgId, msgType, Command.ResultCode.UNSUPPORT);
		}
	}

	public void onPTZorMove(long remoteCid, int camIndex, int type, int PorAxis_X, int TorAxis_Y, int ZorAxis_Z) {
	}

	class VideoEncoder {
		private final VideoType colorspace = VideoType.NV21;
		private long videoChannel;
		private int colorMode;
		private int previewformat;
		private int videoWidth;
		private int videoHeight;
		private byte[] yv12;
		private Media media;
		private boolean useMediaCodec = false;
		private HardwareEncoder avcEncoder;

		public VideoEncoder(int width, int height) {
			this.media = Streamer.getStreamer().getMedia();
			this.videoWidth = width;
			this.videoHeight = height;

			this.videoChannel = this.media.getVideoWriteChannel();
			if (Build.VERSION.SDK_INT < 16) {
				this.useMediaCodec = false;
			} else {
				this.useMediaCodec = true;
			}
			if (this.useMediaCodec) {
				initAvcEncoder();
			} else {
				initX264Codec();
			}
		}

		private void initAvcEncoder() {
			RvsLog.i(MediaSurfaceView.class, "initAvcEncoder()", "hard encoder");
			try {
				this.avcEncoder = new HardwareEncoder(this.media, this.videoWidth, this.videoHeight,
						MediaSurfaceView.this.frameRate, MediaSurfaceView.this.videoBitrate,
						MediaSurfaceView.this.iframeInterval);
			} catch (Exception e) {
				RvsLog.i(MediaSurfaceView.class, "onSwitchTorch()", "init hardware encoder error: " + e.getMessage());
				this.avcEncoder = null;
			}
			if (this.avcEncoder == null) {
				this.useMediaCodec = false;
			}
		}

		private void initX264Codec() {
			RvsLog.i(MediaSurfaceView.class, "initX264Codec()", "soft encoder");

			int init264 = X264VideoCodec.init(this.videoWidth, this.videoHeight, this.colorspace.intValue(),
					MediaSurfaceView.this.videoBitrate, MediaSurfaceView.this.frameRate,
					MediaSurfaceView.this.iframeInterval);
		}

		private int tryTime = 0;
		private int loop = 0;

		public void writeYuvData(byte[] data, boolean needEncode, boolean inMotionDetect) {
			if (inMotionDetect) {
				this.media.writeYUVData(data, 0, data.length);
			}
			if (needEncode) {
				if (this.useMediaCodec) {
					int result = this.avcEncoder.sendVideoData(data, this.colorMode,
							MediaSurfaceView.this.enableTimeWatermark);
					if (result == -1) {
						this.tryTime += 1;
						if (this.tryTime == 10) {
							RvsLog.i(MediaSurfaceView.class, "writeYuvData()",
									"hardware encocer encode fail, use soft encoder, 1");
							switchX264();
						}
					} else if (result == -2) {
						RvsLog.i(MediaSurfaceView.class, "writeYuvData()",
								"hardware encocer encode fail, use soft encoder, 2");
						switchX264();
					}
				} else {
					X264VideoCodec.sendVideoData(this.videoChannel, data, MediaSurfaceView.this.enableTimeWatermark);
				}
			}
		}

		private void switchX264() {
			this.useMediaCodec = false;
			if (this.avcEncoder != null) {
				this.avcEncoder.release();
				this.avcEncoder = null;
			}
			initX264Codec();
		}

		public void resetLoop() {
			if (!this.useMediaCodec) {
				X264VideoCodec.resetLogloop();
			}
		}

		public void reqIframe() {
			if (!this.useMediaCodec) {
				X264VideoCodec.reqIframe();
			}
		}

		public void adjustStreamQuality(int framerate, int bitrate, int keyinterval) {
			if (!this.useMediaCodec) {
				X264VideoCodec.adjustStreamQuality(framerate, bitrate, keyinterval);
			}
		}

		public void destroy() {
			if (this.useMediaCodec) {
				if (this.avcEncoder != null) {
					this.avcEncoder.release();
				}
			} else {
				X264VideoCodec.destroy();
			}
		}

		public void setColorMode(int colorMode) {
			this.colorMode = colorMode;
		}

		public void setPreviewformat(int format) {
			this.previewformat = format;
			int yuv_buffersize = this.videoWidth * this.videoHeight * ImageFormat.getBitsPerPixel(this.previewformat)
					/ 8;
			this.yv12 = new byte[yuv_buffersize];
		}
	}

	public void onYuvDataNotify(boolean needYuvData) {
		this.mInMotionDetect = needYuvData;
	}

	public int onPcmInput(short[] buffer, int length) {
		int size = this.media.getRevAudioData(this.revAudioStream, this.audioData);
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				buffer[i] = ((short) G711.ulaw2linear(this.audioData[i]));
			}
		}
		return size;
	}

	public void onPcmOutput(short[] buffer, int size) {
		AACEncoder.writeAudioData(this.audioChannel, buffer, size);
	}

	public static enum VideoSize {
		VIDEO_320P, VIDEO_480P, VIDEO_720P, VIDEO_1080P;

		public static int[] getVideoSize(VideoSize size) {
			int[] videoSize = MediaSurfaceView.VIDEO_480;
			switch (size) {
			case VIDEO_1080P:
				return MediaSurfaceView.VIDEO_320;
			case VIDEO_320P:
				return MediaSurfaceView.VIDEO_480;
			case VIDEO_480P:
				return MediaSurfaceView.VIDEO_720;
			case VIDEO_720P:
				return MediaSurfaceView.VIDEO_1080;
			}
			return videoSize;
		}
	}

	public static enum CameraState {
		START, PREVIEW, STOP, ERROR;
	}

	public void setOnCameraStateListener(CameraStateListener listener) {
		this.cameraStateListener = listener;
	}

	public static class CameraPreviewColorMode {
		public static final int VIDEO_DEFAULT = 0;
		public static final int VIDEO_YUV420 = 1;
		public static final int VIDEO_NV21 = 2;
		public static final int VIDEO_NV12 = 3;
	}

	public static abstract interface CameraStateListener {
		public abstract void onCameraStateChange(MediaSurfaceView.CameraState paramCameraState);
	}
}
