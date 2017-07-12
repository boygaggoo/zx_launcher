package com.ichano.cbp.os;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.ichano.cbp.CbpSys;

public class CbpSysDispatcher {
	private static final Handler mJniMsgHandler = new Handler(Looper.getMainLooper())
	  {
	    public void handleMessage(Message msg)
	    {
	      CbpSys.driveMsg(msg.what, msg.obj);
	    }
	  };
	  
	  public static boolean sendMessage(int dir, Object obj)
	  {
	    Message msg = mJniMsgHandler.obtainMessage(dir, obj);
	    return mJniMsgHandler.sendMessage(msg);
	  }
}
