package com.ichano.rvs.streamer;

import com.ichano.cbp.CbpMessage;
import com.ichano.cbp.CbpSys;
import com.ichano.cbp.CbpSysCb;
import com.ichano.rvs.internal.RvsLog;
import com.ichano.rvs.jni.NativeChannel;
import com.ichano.rvs.jni.NativeDeviceInfo;
import com.ichano.rvs.jni.NativeMedia;
import com.ichano.rvs.jni.NativeMedia.AutoPutVideodataforCbmd;
import com.ichano.rvs.jni.NativeRecord;
import com.ichano.rvs.streamer.bean.MediaDataDesc;
import com.ichano.rvs.streamer.bean.RvsTimeRecordInfo;
import com.ichano.rvs.streamer.callback.AudioCallback;
import com.ichano.rvs.streamer.callback.MediaChannelListener;
import com.ichano.rvs.streamer.callback.MotionDetectCallback;
import com.ichano.rvs.streamer.callback.MotionDetectSettingsCallback;
import com.ichano.rvs.streamer.callback.RecordCallback;
import com.ichano.rvs.streamer.callback.RecordFilesCallback;
import com.ichano.rvs.streamer.callback.RevAudioCallback;
import com.ichano.rvs.streamer.callback.TimeRecordSettingsCallback;
import com.ichano.rvs.streamer.callback.VideoCallback;
import com.ichano.rvs.streamer.codec.AudioType;
import com.ichano.rvs.streamer.codec.VideoType;
import com.ichano.rvs.streamer.constant.RvsRecordState;
import com.ichano.rvs.streamer.constant.RvsRecordType;
import com.ichano.rvs.streamer.param.AudioProperty;
import com.ichano.rvs.streamer.param.CameraCapacity;
import com.ichano.rvs.streamer.param.StreamProperty;
import java.util.ArrayList;

public final class Media implements CbpSysCb, NativeMedia.AutoPutVideodataforCbmd {
	private static final String TAG = "Media";
	private static Media instance;
	private long yuvWriteChannel = -1L;
	private long videoWriteChannel = -1L;
	private long audioWriteChannel = -1L;
	private long NightVisionChannel = -1L;
	private VideoCallback videoCallback;
	private AudioCallback audioCallback;
	private MediaChannelListener channelLister;
	private RevAudioCallback revAudioCallback;
	private long revAudioHandle = 0L;
	private RecordCallback recordCallback;
	private TimeRecordSettingsCallback recordSettingsCallback;
	private MotionDetectCallback motionDetectCallback;
	private MotionDetectSettingsCallback motionDetectSettingsCallback;
	private NativeMedia nativeMedia;
	private NativeChannel nativeChannel;
	private NativeRecord nativeRecord;
	private NativeDeviceInfo nativeDeviceInfo;
	private ArrayList<Long> watchingClientsList = new ArrayList();
	private int uiWidth;
	private int uiHeight;

	private Media() {
		this.nativeMedia = NativeMedia.getInstance();
		this.nativeChannel = NativeChannel.getInstance();
		this.nativeRecord = NativeRecord.getInstance();
		this.nativeDeviceInfo = NativeDeviceInfo.getInstance();
	}

	static Media getInstance() {
		if (instance == null) {
			instance = new Media();
		}
		return instance;
	}

	boolean init() {
		RvsLog.i(Media.class, "init()", "sdk media init");

		int ret = this.nativeMedia.init();
		if (ret != 0) {
			return false;
		}
		RvsLog.i(Media.class, "init()", "sdk record init");
		ret = this.nativeRecord.init();
		if (ret != 0) {
			return false;
		}
		CbpSys.registerCallBack(11, this);
		CbpSys.registerCallBack(4, this);
		CbpSys.registerCallBack(6, this);
		CbpSys.registerCallBack(13, this);

		this.nativeMedia.setCallback(this);
		openNightVision(0);
		return true;
	}

	void destroy() {
		CbpSys.unregisterCallBack(11, this);
		CbpSys.unregisterCallBack(4, this);
		CbpSys.unregisterCallBack(6, this);
		CbpSys.unregisterCallBack(13, this);

		this.nativeMedia.destroy();

		this.nativeRecord.destroy();
		this.nativeDeviceInfo.destroy();
		if (-1L != this.videoWriteChannel) {
			this.nativeMedia.videoCloseWritenChannel(this.videoWriteChannel);
		}
		if (-1L != this.yuvWriteChannel) {
			this.nativeMedia.videoCloseWritenChannel(this.yuvWriteChannel);
		}
		if (-1L != this.audioWriteChannel) {
			this.nativeMedia.audioCloseWritenChannel(this.audioWriteChannel);
		}
		closeNightVision();
		this.yuvWriteChannel = -1L;
		this.videoWriteChannel = -1L;
		this.audioWriteChannel = -1L;
		this.NightVisionChannel = -1L;
	}

	public void setCameraCapacity(CameraCapacity cameraCapacity) {
		this.nativeDeviceInfo.setRotateFlag(0, cameraCapacity.isRotateEnable() ? 1 : 0);
		this.nativeDeviceInfo.setTorchFlag(0, cameraCapacity.isTorchEnable() ? 1 : 0);
		this.nativeDeviceInfo.setPTZMode(0, cameraCapacity.getPtzMoveMode());
		this.nativeDeviceInfo.setStreamMode(0, cameraCapacity.getStreamType());
		this.nativeDeviceInfo.setDefinition(0, cameraCapacity.getDefinition());
	}

	public void setCameraStreamProperty(StreamProperty streamProperty) {
		this.uiWidth = streamProperty.getWidth();
		this.uiHeight = streamProperty.getHeight();
		this.nativeDeviceInfo.setCameraCount(1);
		this.nativeDeviceInfo.setStreamCount(0, 1);
		if (this.videoWriteChannel != -1L) {
			this.nativeMedia.videoCloseWritenChannel(this.videoWriteChannel);
		}
		this.videoWriteChannel = this.nativeMedia.videoOpenWritenChannel(0, 0,
				streamProperty.getEncodeType().intValue(), streamProperty.getWidth(), streamProperty.getHeight());
		if (this.yuvWriteChannel != -1L) {
			this.nativeMedia.videoCloseWritenChannel(this.yuvWriteChannel);
		}
		this.yuvWriteChannel = this.nativeMedia.videoOpenWritenChannel(0, 0, VideoType.NV21.intValue(),
				streamProperty.getWidth(), streamProperty.getHeight());
	}

	public void changeCameraStreamProperty(StreamProperty streamProperty) {
		this.uiWidth = streamProperty.getWidth();
		this.uiHeight = streamProperty.getHeight();
		this.nativeMedia.changeResolution(this.yuvWriteChannel, streamProperty.getWidth(), streamProperty.getHeight(),
				VideoType.NV21.intValue());
		this.nativeMedia.changeResolution(this.videoWriteChannel, streamProperty.getWidth(), streamProperty.getHeight(),
				streamProperty.getEncodeType().intValue());
	}

	public void setMicProperty(AudioProperty audioProperty) {
		this.nativeDeviceInfo.setMicCount(1);
		if (this.audioWriteChannel != -1L) {
			this.nativeMedia.audioCloseWritenChannel(this.audioWriteChannel);
		}
		this.audioWriteChannel = this.nativeMedia.audioOpenWritenChannel(0, audioProperty.getEncodeType().intValue(),
				audioProperty.getSamplerate(), audioProperty.getChannel(), audioProperty.getBitWidth());
		RvsLog.i(Media.class, "setMicProperty()", "audio channel :" + this.audioWriteChannel);
	}

	public void addTimewatermark(byte[] yuv, int uiWidth, int uiHeight) {
		this.nativeMedia.addTimewatermark(yuv, uiWidth, uiHeight);
	}

	public void writeYUVData(byte[] data, int offsetInBytes, int sizeInBytes) {
		this.nativeMedia.yuvWriteData(this.yuvWriteChannel, data, sizeInBytes);
	}

	public void writeVideoData(byte[] data) {
		if (data == null) {
			return;
		}
		this.nativeMedia.videoWriteData(this.videoWriteChannel, data, data.length);
	}

	public void writeVideoData(byte[] data, int offsetInBytes, int sizeInBytes) {
		if (data == null) {
			return;
		}
		this.nativeMedia.videoWriteData(this.videoWriteChannel, data, sizeInBytes);
	}

	public void writeVideoData(byte[] data, int offsetInBytes, int sizeInBytes, boolean isKeyFrame) {
		if (data == null) {
			return;
		}
		this.nativeMedia.videoWriteData2(this.videoWriteChannel, data, sizeInBytes, isKeyFrame);
	}

	public void writeAudioData(byte[] data) {
		if (data == null) {
			return;
		}
		this.nativeMedia.audioWriteData(this.audioWriteChannel, data, data.length);
	}

	public void writeAudioData(byte[] data, int offsetInBytes, int sizeInBytes) {
		if (data == null) {
			return;
		}
		this.nativeMedia.audioWriteData(this.audioWriteChannel, data, sizeInBytes);
	}

	public void writeAudioData(short[] data) {
		if (data == null) {
			return;
		}
		this.nativeMedia.audioWriteData2(this.audioWriteChannel, data, data.length);
	}

	public void writeAudioData(short[] data, int offsetInShorts, int sizeInShorts) {
		if (data == null) {
			return;
		}
		this.nativeMedia.audioWriteData2(this.audioWriteChannel, data, sizeInShorts);
	}

	public int getRevAudioData(long audioStreamId, byte[] data) {
		if (audioStreamId == 0L) {
			return 0;
		}
		return this.nativeChannel.readMediaData(audioStreamId, data);
	}

	public MediaDataDesc getRevAudioDesc(long audioStreamId) {
		MediaDataDesc desc = new MediaDataDesc();
		this.nativeChannel.getMediaDataDes(audioStreamId, desc);
		return desc;
	}

	public void setRevAudioCallback(RevAudioCallback callback) {
		this.revAudioCallback = callback;
	}

	public void closeRevAudioStream(long streamId) {
		this.nativeMedia.stopStream(streamId);
	}

	public void setChannelLister(MediaChannelListener listener) {
		this.channelLister = listener;
	}

	/**
	 * @deprecated
	 */
	public byte[] onGetJpegData(int type) {
		if (this.videoCallback != null) {
			return this.videoCallback.onGetOneJpegFrame(type);
		}
		return null;
	}

	/**
	 * @deprecated
	 */
	public int onRecvMsg(CbpMessage uspMsg) {
		switch (uspMsg.getSrcPid()) {
		case 11:
			handleMediaMsg(uspMsg);

			break;
		case 4:
			handleRecordMsg(uspMsg);

			break;
		case 6:
			handleCmdMsg(uspMsg);

			break;
		case 13:
			break;
		case 5:
		case 7:
		case 8:
		case 9:
		case 10:
		case 12:
		default:
			RvsLog.w(Media.class, "onRecvMsg()", "unkonwn msg from source id: " + uspMsg.getSrcPid());
		}
		return 0;
	}

	private void handleMediaMsg(CbpMessage uspMsg) {
		
		long cid=0L;
		
		switch (uspMsg.getMsgID()) {
		case 10:
			if (this.videoCallback != null) {
				boolean writeToChannel = uspMsg.getUI(32, 0) == 1;
				long channel = uspMsg.getHandle(8);
				if (channel == this.videoWriteChannel) {
					this.videoCallback.onVideoDataNotify(writeToChannel);
				} else if (channel == this.yuvWriteChannel) {
					this.videoCallback.onYuvDataNotify(writeToChannel);
				}
			}
			break;
		case 11:
			if (this.audioCallback != null) {
				boolean writeToChannel = uspMsg.getUI(32, 0) == 1;

				this.audioCallback.onAudioDataNotify(writeToChannel);
			}
			break;
		case 12:
			if (this.videoCallback != null) {
				this.videoCallback.onKeyFrameRequired();
			}
			break;
		case 6:
			int status = uspMsg.getUI(9, -1);
			cid = uspMsg.getXXLSIZE(2, 0L);
			int count = uspMsg.getUI(4, -1);
			if (status == 1) {
				if (!this.watchingClientsList.contains(Long.valueOf(cid))) {
					this.watchingClientsList.add(Long.valueOf(cid));
				}
			} else if ((status == 0) && (this.watchingClientsList.contains(Long.valueOf(cid)))) {
				this.watchingClientsList.remove(Long.valueOf(cid));
			}
			if (this.channelLister != null) {
				this.channelLister.onMediaChannelState(cid, status, count);
			}
			break;
		case 0:
			long handle = uspMsg.getHandle(3);
			int value = uspMsg.getUI(0, -1);
			cid = uspMsg.getXXLSIZE(2, 0L);
			if (this.revAudioCallback != null) {
				this.revAudioCallback.onRevAudioStatus(handle, cid, value);
			}
			break;
		default:
			RvsLog.w(Media.class, "handleMediaMsg()", "no case msg type: " + uspMsg.getMsgID());
		}
	}

	private void handleRecordMsg(CbpMessage uspMsg) {
		switch (uspMsg.getMsgID()) {
		case 0:
			if (this.recordSettingsCallback != null) {
				int type = uspMsg.getUI(0, -1);
				int camId = uspMsg.getUI(1, -1);
				if (type == 1) {
					RvsTimeRecordInfo schedule = this.nativeDeviceInfo.getStreamerRecordSchedule(camId);
					this.recordSettingsCallback.onTimeRecordSettingUpdate(schedule);
				}
			}
			break;
		case 1:
			if (this.recordCallback == null) {
				return;
			}
			int code = uspMsg.getUI(8, -1);
			int type = uspMsg.getUI(0, -1);

			this.recordCallback.onRecordState(RvsRecordType.valueOfInt(type), RvsRecordState.vauleOfInt(code));

			break;
		default:
			RvsLog.w(Media.class, "handleRecordMsg()", "no case msg type: " + uspMsg.getMsgID());
		}
	}

	private void handleCmdMsg(CbpMessage uspMsg) {
		switch (uspMsg.getMsgID()) {
		case 10:
			if (this.videoCallback != null) {
				this.videoCallback.onKeyFrameRequired();
			}
			break;
		default:
			RvsLog.w(Media.class, "handleCmdMsg()", "no case msg type: " + uspMsg.getMsgID());
		}
	}

	VideoCallback getVideoCallback() {
		return this.videoCallback;
	}

	RecordCallback getRecordCallback() {
		return this.recordCallback;
	}

	TimeRecordSettingsCallback getRecordSettingsCallback() {
		return this.recordSettingsCallback;
	}

	MotionDetectCallback getMotionDetectCallback() {
		return this.motionDetectCallback;
	}

	MotionDetectSettingsCallback getMotionDetectSettingsCallback() {
		return this.motionDetectSettingsCallback;
	}

	public void setVideoCallback(VideoCallback videoCallback) {
		this.videoCallback = videoCallback;
		this.nativeMedia.setGetJpegDataCallback(0, 0, videoCallback);
	}

	public void setAudioCallback(AudioCallback audioCallback) {
		this.audioCallback = audioCallback;
	}

	public void setRecordCallback(RecordCallback recordCallback) {
		this.recordCallback = recordCallback;
	}

	public void setRecordSettingsCallback(TimeRecordSettingsCallback recordSettingsCallback) {
		this.recordSettingsCallback = recordSettingsCallback;
	}

	public void setMotionDetectCallback(MotionDetectCallback motionDetectCallback) {
		this.motionDetectCallback = motionDetectCallback;
	}

	public void setMotionDetectSettingsCallback(MotionDetectSettingsCallback motionDetectSettingsCallback) {
		this.motionDetectSettingsCallback = motionDetectSettingsCallback;
	}

	public long getAudioWriteChannel() {
		return this.audioWriteChannel;
	}

	public long getVideoWriteChannel() {
		return this.videoWriteChannel;
	}

	public boolean startCustomRecord(int camId, int streamId) {
		return this.nativeRecord.startRecord(camId, streamId) == 0;
	}

	public boolean stopCustomRecord(int camId, int streamId) {
		return this.nativeRecord.stopRecord(camId, streamId) == 0;
	}

	public int getRecordTime() {
		return this.nativeRecord.getRecordTime(0, 0);
	}

	public int setRecordPath(String localPath) {
		return this.nativeRecord.setRecordPath(localPath);
	}

	public void setRecordFilesCallback(RecordFilesCallback callback) {
		this.nativeMedia.setRecordcallback(callback);
	}

	public ArrayList<Long> getWatchingClientList() {
		return this.watchingClientsList;
	}

	public void openNightVision(int checkTimes) {
		if (this.NightVisionChannel != -1L) {
			this.nativeMedia.BAdjustFree(this.NightVisionChannel);
		}
		this.NightVisionChannel = this.nativeMedia.BAdjustAlloc(checkTimes);
	}

	public void closeNightVision() {
		if (-1L != this.NightVisionChannel) {
			this.nativeMedia.BAdjustFree(this.NightVisionChannel);
		}
	}

	public void getNightVisionData(byte[] pucBuf) {
		if (this.NightVisionChannel == -1L) {
			return;
		}
		this.nativeMedia.BAdjust(this.NightVisionChannel, pucBuf, this.uiWidth, this.uiHeight, this.uiWidth);
	}
}
