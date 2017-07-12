package com.ichano.cbp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ichano.cbp.os.CbpSysDispatcher;

import android.util.Log;
public class CbpSys {
	private static final String Tag = "CbpSys";
	  public static final int JCBP_OK = 0;
	  public static final int JCBP_ERR = 1;
	  private static final int JCBP_DIR_SEND_MESSAGE = 100;
	  private static final int JCBP_DIR_RECV_MESSAGE = 101;
	  private static final int JCBP_DIR_DRIVE_MESSAGE = 102;
	  private static final int JEN_GHOST_FLAG_MAIN_DRIVE = 0;
	  private static final int JEN_GHOST_FLAG_UPPER_RUN = 1;
	  private static final int JEN_GHOST_FLAG_INVALID = 255;
	  static final int JVAL_TYPE_BOOL = 0;
	  static final int JVAL_TYPE_UINT = 1;
	  static final int JVAL_TYPE_STR = 2;
	  static final int JVAL_TYPE_HANDLE = 3;
	  static final int JVAL_TYPE_XXLSIZE = 5;
	  private static final int JVAL_TYPE_INVALID = 255;
	  
	  private static native int init();
	  
	  private static native int destroy();
	  
	  private static native String getSDKVersion();
	  
	  private static native String getSDKBuildTime();
	  
	  private static native long createMsg(int paramInt1, int paramInt2, int paramInt3);
	  
	  private static native int sendMsg(long paramLong);
	  
	  private static native int dispatchMsg(long paramLong);
	  
	  private static native int msgAddBool(long paramLong, int paramInt, boolean paramBoolean);
	  
	  private static native int msgAddtUlong(long paramLong1, int paramInt, long paramLong2);
	  
	  private static native int msgAddUI(long paramLong, int paramInt1, int paramInt2);
	  
	  private static native int msgAddStr(long paramLong, int paramInt, String paramString);
	  
	  private static native int msgAddByteArray(long paramLong, int paramInt, byte[] paramArrayOfByte);
	  
	  private static native int msgAddHandle(long paramLong1, int paramInt, long paramLong2);
	  
	  private static native int msgGetHdr(long paramLong, int[] paramArrayOfInt, long[] paramArrayOfLong);
	  
	  private static native int msgGetBVal(long paramLong, int[] paramArrayOfInt, long[] paramArrayOfLong);
	  
	  private static native boolean msgBValGetBool(long paramLong);
	  
	  private static native int msgBValGetUint(long paramLong);
	  
	  private static native long msgBValGetUlong(long paramLong);
	  
	  private static native String msgBValGetString(long paramLong);
	  
	  private static native byte[] msgBValGetByteArray(long paramLong);
	  
	  private static native long msgBValGetHandle(long paramLong);
	  
	  public static int initMsgLoop()
	  {
	    int iRet = init();
	    if (iRet != 0)
	    {
	      CbpLog.err("CBPSA", "initial", "Cbp system load err iRet:" + iRet);
	      Log.e("CbpSys", "CBPSA initial Cbp system load err iRet:" + iRet);
	      return 1;
	    }
	    return 0;
	  }
	  
	  public static int destroyMsgLoop()
	  {
	    int iRet = destroy();
	    if (iRet != 0)
	    {
	      Log.e("CbpSys", "CBPSA destroy Cbp system load err iRet:" + iRet);
	      return 1;
	    }
	    return 0;
	  }
	  
	  public static void driveMsg(int dir, Object obj)
	  {
	    switch (dir)
	    {
	    case 100: 
	      driveJavaMessage((CbpMessage)obj);
	      break;
	    case 101: 
	      driveCjniMessage((CbpMessage)obj);
	      break;
	    case 102: 
	      dispatchMsg(((Long)obj).longValue());
	      break;
	    }
	  }
	  
	  private static int onRecvMsg(int uiFlag, long objMsg)
	  {
	    switch (uiFlag)
	    {
	    case 1: 
	      CbpMessage cbpMsg = convertJniMessage(objMsg);
	      if (cbpMsg == null)
	      {
	        Log.d("CbpSys", "onRecvMsg : null");
	        return 1;
	      }
	      CbpSysDispatcher.sendMessage(101, cbpMsg);
	      return 0;
	    case 0: 
	      CbpSysDispatcher.sendMessage(102, Long.valueOf(objMsg));
	      return 0;
	    }
	    return 1;
	  }
	  
	  public static boolean sendMessage(CbpMessage cbpMsg)
	  {
	    return CbpSysDispatcher.sendMessage(100, cbpMsg);
	  }
	  
	  private static class LoopBunch
	    implements CbpBunch.GetRunner
	  {
	    private long mObjMsg;
	    
	    public LoopBunch(long objMsg)
	    {
	      this.mObjMsg = objMsg;
	    }
	    
	    public int doUI(int uiTag, int uiVal)
	    {
	      return CbpSys.msgAddUI(this.mObjMsg, uiTag, uiVal);
	    }
	    
	    public int doStr(int uiTag, String strVal)
	    {
	      return CbpSys.msgAddStr(this.mObjMsg, uiTag, strVal);
	    }
	    
	    public int doHandle(int uiTag, long hVal)
	    {
	      return CbpSys.msgAddHandle(this.mObjMsg, uiTag, hVal);
	    }
	    
	    public int doBool(int uiTag, boolean bVal)
	    {
	      return CbpSys.msgAddBool(this.mObjMsg, uiTag, bVal);
	    }
	    
	    public int doXXLSize(int uiTag, long lVal)
	    {
	      return CbpSys.msgAddtUlong(this.mObjMsg, uiTag, lVal);
	    }
	  }
	  
	  private static int driveJavaMessage(CbpMessage cbpMsg)
	  {
	    long objMsg = createMsg(cbpMsg.getSrcPid(), cbpMsg.getDstPid(), cbpMsg.getMsgID());
	    if (0L == objMsg) {
	      return 1;
	    }
	    cbpMsg.loopGetter(new LoopBunch(objMsg));
	    return sendMsg(objMsg);
	  }
	  
	  private static int driveCjniMessage(CbpMessage cbpMsg)
	  {
	    List<CbpSysCb> list = (List)mCallBackPool.get(Integer.valueOf(cbpMsg.getSrcPid()));
	    if ((list == null) || (list.isEmpty())) {
	      return 1;
	    }
	    for (CbpSysCb callback : list) {
	      callback.onRecvMsg(cbpMsg);
	    }
	    return 0;
	  }
	  
	  private static CbpMessage convertJniMessage(long objMsg)
	  {
	    int[] uiHdrParam = new int[6];
	    long[] objHdrNext = new long[1];
	    if (msgGetHdr(objMsg, uiHdrParam, objHdrNext) != 0) {
	      return null;
	    }
	    CbpMessage cbpMsg = new CbpMessage(uiHdrParam[0], uiHdrParam[1], uiHdrParam[4]);
	    if (0L == objHdrNext[0]) {
	      return cbpMsg;
	    }
	    long objBVal = objHdrNext[0];
	    int[] uiBValParam = new int[3];
	    long[] objBValNext = new long[1];
	    do
	    {
	      if (msgGetBVal(objBVal, uiBValParam, objBValNext) != 0) {
	        break;
	      }
	      try
	      {
	        switch (uiBValParam[0])
	        {
	        case 0: 
	          cbpMsg.addBool(uiBValParam[1], msgBValGetBool(objBVal));
	          break;
	        case 1: 
	          cbpMsg.addUI(uiBValParam[1], msgBValGetUint(objBVal));
	          break;
	        case 2: 
	          cbpMsg.addStr(uiBValParam[1], msgBValGetString(objBVal));
	          break;
	        case 3: 
	          cbpMsg.addHandle(uiBValParam[1], msgBValGetHandle(objBVal));
	          break;
	        case 5: 
	          cbpMsg.addXXLSIZE(uiBValParam[1], msgBValGetUlong(objBVal));
	          break;
	        case 4: 
	        default: 
	          CbpLog.dbg("CBPSA", "convertJniMessage", "not support type:" + 
	            uiBValParam[0]);
	          Log.d("CbpSys", "convertJniMessage not support type:" + 
	            uiBValParam[0]);
	        }
	      }
	      catch (IllegalArgumentException e)
	      {
	        e.printStackTrace();
	      }
	      objBVal = objBValNext[0];
	    } while (0L != objBVal);
	    return cbpMsg;
	  }
	  
	  private static HashMap<Integer, List<CbpSysCb>> mCallBackPool = new HashMap();
	  
	  public static int registerCallBack(int uiPid, CbpSysCb callback)
	  {
	    List<CbpSysCb> list = (List)mCallBackPool.get(Integer.valueOf(uiPid));
	    if (list == null)
	    {
	      mCallBackPool.put(Integer.valueOf(uiPid), new ArrayList());
	      list = (List)mCallBackPool.get(Integer.valueOf(uiPid));
	    }
	    list.add(callback);
	    return 0;
	  }
	  
	  public static int unregisterCallBack(int uiPid, CbpSysCb callback)
	  {
	    List<CbpSysCb> list = (List)mCallBackPool.get(Integer.valueOf(uiPid));
	    if (list != null) {
	      list.remove(callback);
	    } else {
	      mCallBackPool.remove(Integer.valueOf(uiPid));
	    }
	    return 0;
	  }
}
