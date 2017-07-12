package com.ichano.rvs.streamer.param;

import com.ichano.rvs.streamer.codec.VideoType;

public class StreamProperty {
	private int width;
	private int height;
	private int quality;
	private int bitrate;
	private int framerate;
	private int keyinterval;
	private VideoType encodeType;

	public StreamProperty(int width, int height, int quality, int bitrate, int framerate, int keyinterval,
			VideoType encodeType) {
		this.width = width;
		this.height = height;
		this.quality = quality;
		this.bitrate = bitrate;
		this.framerate = framerate;
		this.keyinterval = keyinterval;
		this.encodeType = encodeType;
	}

	public int getWidth() {
		return this.width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return this.height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getQuality() {
		return this.quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public int getBitrate() {
		return this.bitrate;
	}

	public void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}

	public int getFramerate() {
		return this.framerate;
	}

	public void setFramerate(int framerate) {
		this.framerate = framerate;
	}

	public int getKeyinterval() {
		return this.keyinterval;
	}

	public void setKeyinterval(int keyinterval) {
		this.keyinterval = keyinterval;
	}

	public VideoType getEncodeType() {
		return this.encodeType;
	}

	public void setEncodeType(VideoType encodeType) {
		this.encodeType = encodeType;
	}
}
