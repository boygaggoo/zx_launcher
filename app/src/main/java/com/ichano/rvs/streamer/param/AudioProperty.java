package com.ichano.rvs.streamer.param;

import com.ichano.rvs.streamer.codec.AudioType;

public class AudioProperty {
	private int samplerate;
	private int channel;
	private int bitWidth;
	private AudioType encodeType;

	public AudioProperty(int samplerate, int channel, int bitWidth, AudioType encodeType) {
		this.samplerate = samplerate;
		this.channel = channel;
		this.bitWidth = bitWidth;
		this.encodeType = encodeType;
	}

	public AudioType getEncodeType() {
		return this.encodeType;
	}

	public void setEncodeType(AudioType encodeType) {
		this.encodeType = encodeType;
	}

	public int getSamplerate() {
		return this.samplerate;
	}

	public void setSamplerate(int samplerate) {
		this.samplerate = samplerate;
	}

	public int getChannel() {
		return this.channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public int getBitWidth() {
		return this.bitWidth;
	}

	public void setBitWidth(int bitWidth) {
		this.bitWidth = bitWidth;
	}
}
