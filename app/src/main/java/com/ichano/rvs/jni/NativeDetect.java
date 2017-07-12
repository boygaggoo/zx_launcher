package com.ichano.rvs.jni;

public class NativeDetect {
	 public native int init();
	  
	  public native int destroy();
	  
	  public native int addCusPlug(int paramInt1, int paramInt2);
	  
	  public native int delCusPlug(int paramInt1, int paramInt2);
	  
	  public native int setAlarmCount(int paramInt1, int paramInt2);
	  
	  public native int setName(int paramInt1, int paramInt2, String paramString);
	  
	  public native int setDayTime(int paramInt1, int paramInt2, int paramInt3, boolean[] paramArrayOfBoolean, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3, int[] paramArrayOfInt4);
	  
	  public void onProcessStart(int sensorId) {}
	  
	  public void onProcess(int threshold) {}
	  
	  public void onProcessStop() {}
	}
