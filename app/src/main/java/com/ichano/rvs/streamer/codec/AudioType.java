package com.ichano.rvs.streamer.codec;

public enum AudioType {
	 AAC(40100),  G711A(40201),  G711U(40202),  PCM8(51000),  PCM16(52000),  INVALID(59999);
	  
	  private int value;
	  
	  private AudioType(int val)
	  {
	    this.value = val;
	  }
	  
	  public int intValue()
	  {
	    return this.value;
	  }
	  
	  public static AudioType vauleOfInt(int value)
	  {
	    switch (value)
	    {
	    case 40100: 
	      return AAC;
	    case 40201: 
	      return G711A;
	    case 40202: 
	      return G711U;
	    case 51000: 
	      return PCM8;
	    case 52000: 
	      return PCM16;
	    }
	    return INVALID;
	  }
}
