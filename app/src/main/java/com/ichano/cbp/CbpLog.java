package com.ichano.cbp;

public class CbpLog {
	  private static final int JEN_LOG_LEVEL_ERROR = 1;
	  private static final int JEN_LOG_LEVEL_WARN = 2;
	  private static final int JEN_LOG_LEVEL_INFO = 4;
	  private static final int JEN_LOG_LEVEL_DEBUG = 8;
	  
	  public static native int printIn(String paramString1, int paramInt, String paramString2, String paramString3);
	  
	  public static void err(String tag, String pucFunName, String logMsg)
	  {
	    printIn(tag, 1, pucFunName, logMsg);
	  }
	  
	  public static void warn(String tag, String pucFunName, String logMsg)
	  {
	    printIn(tag, 2, pucFunName, logMsg);
	  }
	  
	  public static void inf(String tag, String pucFunName, String logMsg)
	  {
	    printIn(tag, 4, pucFunName, logMsg);
	  }
	  
	  public static void dbg(String tag, String pucFunName, String logMsg)
	  {
	    printIn(tag, 8, pucFunName, logMsg);
	  }
}
