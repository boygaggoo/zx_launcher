package com.ichano.rvs.jni;

import com.ichano.rvs.streamer.bean.RvsAlarmRecordInfo;
import com.ichano.rvs.streamer.bean.RvsSensorInfo;
import com.ichano.rvs.streamer.bean.RvsTimeRecordInfo;

public class NativeDeviceInfo {
	private static NativeDeviceInfo instance;

	public static NativeDeviceInfo getInstance() {
		if (instance == null) {
			instance = new NativeDeviceInfo();
		}
		return instance;
	}

	public native int destroy();

	public native int setVersion(String paramString);

	public native int setName(String paramString);

	public native int setOsVersion(String paramString);

	public native int setLanguage(int paramInt);

	public native int setCameraCount(int paramInt);

	public native int setStreamCount(int paramInt1, int paramInt2);

	public native int setMicCount(int paramInt);

	public native int setTorchFlag(int paramInt1, int paramInt2);

	public native int setRotateFlag(int paramInt1, int paramInt2);

	public native int setPTZMode(int paramInt1, int paramInt2);

	public native int setStreamMode(int paramInt1, int paramInt2);

	public native int setDefinition(int paramInt1, int paramInt2);

	public native int setDeviceAbility(int paramInt);

	public native int setRecordMode(int paramInt);

	public native int setAlarmFlag(int paramInt);

	public native int setAlarmRecordFlag(int paramInt);

	public native long getCid();

	public native String getName();

	public native RvsTimeRecordInfo getStreamerRecordSchedule(int paramInt);

	public native RvsAlarmRecordInfo getStreamerMotionSchedule(int paramInt);

	public native RvsSensorInfo getStreamerSensors(int paramInt1, int paramInt2);

	public native int getLocalCloudFlag();

	public native int getPushFlag();

	public native int getEmailFlag();

	public native boolean hasCloud();
}
