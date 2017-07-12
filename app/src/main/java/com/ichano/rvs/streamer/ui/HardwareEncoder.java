package com.ichano.rvs.streamer.ui;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import com.ichano.rvs.internal.RvsLog;
import com.ichano.rvs.streamer.Media;
import java.nio.ByteBuffer;
import java.util.Arrays;

@SuppressLint({ "InlinedApi" })
public class HardwareEncoder {
	private static final String TAG = HardwareEncoder.class.getSimpleName();
	private MediaCodec mediaCodec;
	private MediaCodec.BufferInfo mBufferInfo;
	private int supportColorFormat = 19;
	private Media m_media;
	private int ysize;
	private int uvsize;
	private byte[] m_info = null;
	private byte[] ubuffer;
	private byte[] vbuffer;
	private byte[] outFrame = null;
	private int videoWidth;
	private int videoHeight;

	@SuppressLint({ "NewApi" })
	public HardwareEncoder(Media media, int width, int height, int framerate, int bitrate, int iFrameInterval)
			throws Exception {
		this.videoWidth = width;
		this.videoHeight = height;
		this.ysize = (width * height);
		this.uvsize = (this.ysize / 4);

		this.ubuffer = new byte[this.uvsize];
		this.vbuffer = new byte[this.uvsize];

		this.outFrame = new byte[width * height * 3 / 2];

		this.m_media = media;

		this.mBufferInfo = new MediaCodec.BufferInfo();

		this.supportColorFormat = getSupportColorFormat();

		int iFrame = iFrameInterval / framerate + (iFrameInterval % framerate == 0 ? 0 : 1);
		if (iFrame == 0) {
			iFrame = 1;
		}
		MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
		mediaFormat.setInteger("bitrate", bitrate);
		mediaFormat.setInteger("frame-rate", framerate);
		mediaFormat.setInteger("color-format", this.supportColorFormat);
		mediaFormat.setInteger("i-frame-interval", iFrame);

		this.mediaCodec = MediaCodec.createEncoderByType("video/avc");
		this.mediaCodec.configure(mediaFormat, null, null, 1);
		this.mediaCodec.start();
	}

	public void release() {
		if (this.mediaCodec != null) {
			RvsLog.i(HardwareEncoder.class, "release()", "release HardwareEncoder");
			this.mediaCodec.release();
			this.mediaCodec = null;
		}
	}

	@SuppressLint({ "NewApi" })
	public int sendVideoData(byte[] input, int flag, boolean needAddTimeWm) {
		if (this.mediaCodec == null) {
			return -1;
		}
		if (needAddTimeWm) {
			this.m_media.addTimewatermark(input, this.videoWidth, this.videoHeight);
		}
		try {
			if (flag == 0) {
				if ((this.supportColorFormat == 19) || (this.supportColorFormat == 20)) {
					NV21toYUV420Planar(input);
				} else if (this.supportColorFormat == 21) {
					NV21toYUV420SemiPlanar(input);
				}
			} else if (flag == 1) {
				NV21toYUV420Planar(input);
			} else if (flag == 2) {
				NV21toYUV420SemiPlanar(input);
			}
			ByteBuffer[] inputBuffers = this.mediaCodec.getInputBuffers();
			int inputBufferIndex = this.mediaCodec.dequeueInputBuffer(0L);
			if (inputBufferIndex >= 0) {
				ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
				inputBuffer.clear();
				if (inputBuffer != null) {
					inputBuffer.put(input);
				}
				this.mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, System.currentTimeMillis() * 1000L,
						0);
			}
			ByteBuffer[] outputBuffers = this.mediaCodec.getOutputBuffers();
			int outputBufferIndex = this.mediaCodec.dequeueOutputBuffer(this.mBufferInfo, 200000L);
			if (outputBufferIndex >= 0) {
				ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
				if (outputBuffer == null) {
					this.mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
					return -1;
				}
				if (this.m_info == null) {
					this.m_info = new byte[this.mBufferInfo.size];
					outputBuffer.get(this.m_info);
					this.mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
					return 0;
				}
				int len = 0;
				boolean isKeyFrame = false;
				if ((this.mBufferInfo.flags & 0x1) != 0) {
					System.arraycopy(this.m_info, 0, this.outFrame, 0, this.m_info.length);
					outputBuffer.get(this.outFrame, this.m_info.length, this.mBufferInfo.size);
					len = this.m_info.length + this.mBufferInfo.size;
					isKeyFrame = true;
					if ((outputBuffer.get(4) & 0x1F) != 5) {
						return -2;
					}
				} else {
					outputBuffer.get(this.outFrame, 0, this.mBufferInfo.size);
					len = this.mBufferInfo.size;
				}
				if (len > 0) {
					this.m_media.writeVideoData(this.outFrame, 0, len, isKeyFrame);
				}
				this.mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
			} else {
				return -1;
			}
		} catch (Exception e) {
			RvsLog.e(HardwareEncoder.class, "sendVideoData()", "encode error :" + e);
			return -1;
		}
		return 0;
	}

	private void NV21toYUV420SemiPlanar(byte[] input) {
		int idx = this.ysize;
		for (int i = 0; i < this.uvsize; i++) {
			byte tmp = input[idx];
			input[idx] = input[(++idx)];
			input[(idx++)] = tmp;
		}
	}

	private void NV21toYUV420Planar(byte[] input) {
		int idx = this.ysize;
		for (int i = 0; i < this.uvsize; i++) {
			this.vbuffer[i] = input[idx];
			this.ubuffer[i] = input[(idx + 1)];
			idx += 2;
		}
		System.arraycopy(this.ubuffer, 0, input, this.ysize, this.uvsize);
		System.arraycopy(this.vbuffer, 0, input, this.ysize + this.uvsize, this.uvsize);
	}

	@SuppressLint({ "NewApi" })
	private int getSupportColorFormat() {
		 int numCodecs = MediaCodecList.getCodecCount();
	        MediaCodecInfo codecInfo = null;
	        for (int i = 0; i < numCodecs && codecInfo == null; i++) {
	            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
	            if (!info.isEncoder()) {
	                continue;
	            }
	            String[] types = info.getSupportedTypes();
	            boolean found = false;
	            for (int j = 0; j < types.length && !found; j++) {
	                if (types[j].equals("video/avc")) {
	                    System.out.println("found");
	                    found = true;
	                }
	            }
	            if (!found)
	                continue;
	            codecInfo = info;
	        }
	        
		RvsLog.i(HardwareEncoder.class, "getSupportColorFormat()","find codec " + codecInfo.getName() + " supporting " + "video/avc");

		MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType("video/avc");
		
		RvsLog.i(HardwareEncoder.class, "getSupportColorFormat()","color arrays :" + Arrays.toString(capabilities.colorFormats));

		 for (int i = 0; i < capabilities.colorFormats.length; i++) {

	            switch (capabilities.colorFormats[i]) {
	            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
	            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
	            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
	            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
	            case MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar:
	            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:

	            	RvsLog.i(HardwareEncoder.class, "supported color format::" + capabilities.colorFormats[i]);
	                return capabilities.colorFormats[i];
	            default:
	            	RvsLog.i(HardwareEncoder.class, "unsupported color format " + capabilities.colorFormats[i]);
	                break;
	            }
	        }

		return -1;
	}
}
