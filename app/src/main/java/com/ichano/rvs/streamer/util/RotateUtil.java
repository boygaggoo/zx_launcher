package com.ichano.rvs.streamer.util;

public class RotateUtil {
	public static final int NV21 = 1;
	  public static final int YV12 = 2;
	  public static final int NV12 = 3;
	  public static final int I420 = 4;
	  public static final int R90 = 1;
	  public static final int R180 = 2;
	  public static final int R270 = 3;
	  
	  public static native byte[] rotate(byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3, int paramInt4);
}
