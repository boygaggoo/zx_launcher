package com.ichano.rvs.streamer.constant;

public enum RemoteViewerState {
	UNKNOW(-1),  INIT(0),  AUTHER(1),  CONNECTED(2),  CANUSE(3),  FAIL(10);
	  
	  private int value;
	  
	  private RemoteViewerState(int val)
	  {
	    this.value = val;
	  }
	  
	  public int intValue()
	  {
	    return this.value;
	  }
	  
	  public static RemoteViewerState valueOfInt(int value)
	  {
	    switch (value)
	    {
	    case 0: 
	      return INIT;
	    case 1: 
	      return AUTHER;
	    case 2: 
	      return CONNECTED;
	    case 3: 
	      return CANUSE;
	    case 10: 
	      return FAIL;
	    }
	    return UNKNOW;
	  }
}
