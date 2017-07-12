package com.ichano.rvs.streamer.codec;

public class AACEncoder {
	public static native int init(int paramInt1, int paramInt2, int paramInt3, int paramInt4);
	  
	  public static native void writeAudioData(long paramLong, short[] paramArrayOfShort, int paramInt);
	  
	  public static native void writeAudioData2(long paramLong, byte[] paramArrayOfByte, int paramInt);
	  
	  public static native void destroy();
}
