package com.ichano.rvs.jni;

import android.content.Context;

public class NativeBase {
	private NativeCrashListener listener;
	  
	  public native int sysInit(Context paramContext);
	  
	  public native int sysDestroy();
	  
	  public native String getSDKVersion();
	  
	  public native String getSDKBuildTime();
	  
	  public native int enableDebug(boolean paramBoolean);
	  
	  public native void closeLog();
	  
	  public native String getSymbol();
	  
	  public void setNativeCrashListener(NativeCrashListener nn)
	  {
	    this.listener = nn;
	  }
	  
	  public void nativeCrashed()
	  {
	    if (this.listener != null) {
	      this.listener.onNativeCrash();
	    }
	  }
	  
	  public static abstract interface NativeCrashListener
	  {
	    public abstract void onNativeCrash();
	  }
	}

