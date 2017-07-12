package com.ichano.rvs.jni;

import com.ichano.rvs.streamer.bean.MediaDataDesc;
import com.ichano.rvs.streamer.callback.RevAudioCallback;
import com.ichano.rvs.streamer.callback.RevVideoCallback;

public class NativeChannel {
	private RevAudioCallback revAudioCallback;
	private RevVideoCallback revVideoCallback;
	private static NativeChannel instance;

	public static NativeChannel getInstance() {
		if (instance == null) {
			instance = new NativeChannel();
		}
		return instance;
	}

	public native int init();

	public native int destroy();

	public native int readMediaData(long paramLong, byte[] paramArrayOfByte);

	public native int getMediaDataDes(long paramLong, MediaDataDesc paramMediaDataDesc);
}
