package com.ichano.rvs.jni;

public class NativeAuth {
	public native int init();
	  
	  public native int start();
	  
	  public native int stop();
	  
	  public native int destroy();
	  
	  public native Object[] getOwnSecret();
	  
	  public native int setOwnSecret(String paramString1, String paramString2);
	  
	  public native int setAuthInfo(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7);
	  
	  public native int setLoacalIp(String paramString);
	  
	  public native int setMaxSessionNum(int paramInt);
	  
	  public native int getCurrentSessionNum();
	  
	  public native int getCurrentSessionCidList(long[] paramArrayOfLong);
	  
	  public native boolean checkSameLanNetWork(long paramLong);
	}
