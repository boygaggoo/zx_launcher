package com.ichano.athome.avs.libavs;

public class X264VideoCodec {
	public static native int init(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5,
			int paramInt6);

	public static native void changeVideoSize(int paramInt1, int paramInt2);

	public static native int sendVideoData(long paramLong, byte[] paramArrayOfByte, boolean paramBoolean);

	public static native int reqIframe();

	public static native int adjustStreamQuality(int paramInt1, int paramInt2, int paramInt3);

	public static native void resetLogloop();

	public static native int destroy();
}