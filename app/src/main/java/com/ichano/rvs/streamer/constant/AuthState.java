package com.ichano.rvs.streamer.constant;

public enum AuthState {
	UNKNOW(-1),  INIT(0),  AUTHER(3),  CONNECTING(4),  SUCCESS(5),  FAIL(10);
	  
	  private int value;
	  
	  private AuthState(int val)
	  {
	    this.value = val;
	  }
	  
	  public int intValue()
	  {
	    return this.value;
	  }
	  
	  public static AuthState valueOfInt(int value)
	  {
	    switch (value)
	    {
	    case 0: 
	      return INIT;
	    case 3: 
	      return AUTHER;
	    case 4: 
	      return CONNECTING;
	    case 5: 
	      return SUCCESS;
	    case 10: 
	      return FAIL;
	    }
	    return UNKNOW;
	  }
}
