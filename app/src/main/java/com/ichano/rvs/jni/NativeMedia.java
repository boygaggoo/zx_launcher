package com.ichano.rvs.jni;

import com.ichano.rvs.streamer.callback.RecordFilesCallback;
import java.util.HashMap;
import java.util.Map;

public class NativeMedia {
	private static NativeMedia instance;
	private AutoPutVideodataforCbmd callback;
	private RecordFilesCallback recordFileCallback;
	private Map<Long, String> fileMap = new HashMap();

	public void setCallback(AutoPutVideodataforCbmd callback) {
		this.callback = callback;
	}

	public static NativeMedia getInstance() {
		if (instance == null) {
			instance = new NativeMedia();
		}
		return instance;
	}

	public native int init();

	public native int destroy();

	public native long videoOpenWritenChannel(int paramInt1, int paramInt2, int paramInt3, int paramInt4,
			int paramInt5);

	public native int videoCloseWritenChannel(long paramLong);

	public native long audioOpenWritenChannel(int paramInt1, int paramInt2, int paramInt3, int paramInt4,
			int paramInt5);

	public native int audioCloseWritenChannel(long paramLong);

	public native int yuvWriteData(long paramLong, byte[] paramArrayOfByte, int paramInt);

	public native int addTimewatermark(byte[] paramArrayOfByte, int paramInt1, int paramInt2);

	public native int videoWriteData(long paramLong, byte[] paramArrayOfByte, int paramInt);

	public native int videoWriteData2(long paramLong, byte[] paramArrayOfByte, int paramInt, boolean paramBoolean);

	public native int audioWriteData(long paramLong, byte[] paramArrayOfByte, int paramInt);

	public native int audioWriteData2(long paramLong, short[] paramArrayOfShort, int paramInt);

	public native void setGetJpegDataCallback(int paramInt1, int paramInt2, Object paramObject);

	public native long BAdjustAlloc(int paramInt);

	public native int BAdjustFree(long paramLong);

	public native int BAdjust(long paramLong, byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3);

	public native int changeResolution(long paramLong, int paramInt1, int paramInt2, int paramInt3);

	byte[] onGetJpegData(int type) {
		if (this.callback != null) {
			return this.callback.onGetJpegData(type);
		}
		return null;
	}

	public native int stopStream(long paramLong);

	public native int setRecordCallback(RecordFilesCallback paramRecordFilesCallback);

	public void setRecordcallback(RecordFilesCallback callback) {
		this.recordFileCallback = callback;
		setRecordCallback(callback);
	}

	private String onGetRecordFilePath(String fileName) {
		if (this.recordFileCallback != null) {
			return this.recordFileCallback.onRecordFileOpen(fileName);
		}
		return null;
	}

	private void onCloseFile(long fileHandle) {
		if (this.recordFileCallback != null) {
			this.recordFileCallback.onRecordFileClose((String) this.fileMap.get(Long.valueOf(fileHandle)));
		}
		this.fileMap.remove(Long.valueOf(fileHandle));
	}

	private void onOpenFile(String fileName, long handle) {
		this.fileMap.put(Long.valueOf(handle), fileName);
	}

	public static abstract interface AutoPutVideodataforCbmd {
		public abstract byte[] onGetJpegData(int paramInt);
	}
}
