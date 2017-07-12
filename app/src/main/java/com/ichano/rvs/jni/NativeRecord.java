package com.ichano.rvs.jni;

public class NativeRecord {
	private static NativeRecord instance;

	public static NativeRecord getInstance() {
		if (instance == null) {
			instance = new NativeRecord();
		}
		return instance;
	}

	public native int init();

	public native int destroy();

	public native int setRecordPath(String paramString);

	public native int setSpaceAllocate(int paramInt1, int paramInt2, int paramInt3);

	public native int setRecordDayTime(int paramInt1, int paramInt2, int paramInt3, int[] paramArrayOfInt1,
			boolean[] paramArrayOfBoolean, int[] paramArrayOfInt2, int[] paramArrayOfInt3);

	public native int startRecord(int paramInt1, int paramInt2);

	public native int stopRecord(int paramInt1, int paramInt2);

	public native int getRecordTime(int paramInt1, int paramInt2);
}
