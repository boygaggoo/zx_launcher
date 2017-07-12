package com.ichano.rvs.internal;


import android.util.Log;
import com.ichano.rvs.streamer.util.LogUtil;


public class RvsLog {
	 private static final String TAG = "Streamer";
	  private static boolean enableLog = true;
	  
	  public static void enableLog(boolean enable)
	  {
	    enableLog = enable;
	  }
	  
	  public static void v(Class<?> cls, String msg)
	  {
	    if (enableLog)
	    {
	      Log.v("Streamer", "Class : " + cls.getSimpleName() + " , Log : " + msg);
	      LogUtil.writeLog("Streamer-->" + cls.getSimpleName() + " , Log : " + msg);
	    }
	  }
	  
	  public static void v(Class<?> cls, String functionName, String msg)
	  {
	    if (enableLog)
	    {
	      Log.v("Streamer", "Class : " + cls.getSimpleName() + " , Function : " + functionName + " , Log : " + msg);
	      LogUtil.writeLog("Streamer-->" + cls.getSimpleName() + " , Function : " + functionName + " , Log : " + msg);
	    }
	  }
	  
	  public static void d(Class<?> cls, String msg)
	  {
	    if (enableLog)
	    {
	      Log.d("Streamer", "Class : " + cls.getSimpleName() + " , Log : " + msg);
	      LogUtil.writeLog("Streamer-->" + cls.getSimpleName() + " , Log : " + msg);
	    }
	  }
	  
	  public static void d(Class<?> cls, String functionName, String msg)
	  {
	    if (enableLog)
	    {
	      Log.d("Streamer", "Class : " + cls.getSimpleName() + " , Function : " + functionName + " , Log : " + msg);
	      LogUtil.writeLog("Streamer-->" + cls.getSimpleName() + " , Function : " + functionName + " , Log : " + msg);
	    }
	  }
	  
	  public static void i(Class<?> cls, String msg)
	  {
	    if (enableLog)
	    {
	      Log.i("Streamer", "Class : " + cls.getSimpleName() + " , Log : " + msg);
	      LogUtil.writeLog("Streamer-->" + cls.getSimpleName() + " , Log : " + msg);
	    }
	  }
	  
	  public static void i(Class<?> cls, String functionName, String msg)
	  {
	    if (enableLog)
	    {
	      Log.i("Streamer", "Class : " + cls.getSimpleName() + " , Function : " + functionName + " , Log : " + msg);
	      LogUtil.writeLog("Streamer-->" + cls.getSimpleName() + " , Function : " + functionName + " , Log : " + msg);
	    }
	  }
	  
	  public static void w(Class<?> cls, String msg)
	  {
	    if (enableLog)
	    {
	      Log.w("Streamer", "Class : " + cls.getSimpleName() + " , Log : " + msg);
	      LogUtil.writeLog("Streamer-->" + cls.getSimpleName() + " , Log : " + msg);
	    }
	  }
	  
	  public static void w(Class<?> cls, String functionName, String msg)
	  {
	    if (enableLog)
	    {
	      Log.w("Streamer", "Class : " + cls.getSimpleName() + " , Function : " + functionName + " , Log : " + msg);
	      LogUtil.writeLog("Streamer-->" + cls.getSimpleName() + " , Function : " + functionName + " , Log : " + msg);
	    }
	  }
	  
	  public static void e(Class<?> cls, String msg)
	  {
	    if (enableLog)
	    {
	      Log.e("Streamer", "Class : " + cls.getSimpleName() + " , Log : " + msg);
	      LogUtil.writeLog("Streamer-->" + cls.getSimpleName() + " , Log : " + msg);
	    }
	  }
	  
	  public static void e(Class<?> cls, String functionName, String msg)
	  {
	    if (enableLog)
	    {
	      Log.e("Streamer", "Class : " + cls.getSimpleName() + " , Function : " + functionName + " , Log : " + msg);
	      LogUtil.writeLog("Streamer-->" + cls.getSimpleName() + " , Function : " + functionName + " , Log : " + msg);
	    }
	  }
}
