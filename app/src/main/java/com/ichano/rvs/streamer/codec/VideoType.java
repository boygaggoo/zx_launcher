package com.ichano.rvs.streamer.codec;

public enum VideoType {
	H264(10100),  H264NAL(10101),  JPEG(19010),  YV12(21111),  YUV420(21100),  NV21(
		    21122),  YUV420P(21110),  INVALID(29999);
		  
		  private int value;
		  
		  private VideoType(int val)
		  {
		    this.value = val;
		  }
		  
		  public int intValue()
		  {
		    return this.value;
		  }
		  
		  public static VideoType vauleOfInt(int value)
		  {
		    switch (value)
		    {
		    case 10100: 
		      return H264;
		    case 10101: 
		      return H264NAL;
		    case 19010: 
		      return JPEG;
		    case 21111: 
		      return YV12;
		    case 21100: 
		      return YUV420;
		    case 21110: 
		      return YUV420P;
		    case 21122: 
		      return NV21;
		    }
		    return INVALID;
		  }
}
