package com.ichano.rvs.streamer.bean;

public class MediaDataDesc {
	private int videoType;
	private int videoWidth;
	private int videoHeight;
	private int audioType;
	private int sampRate;
	private int channel;
	private int depth;

	public int getVideoType() {
		return this.videoType;
	}

	public void setVideoType(int videoType) {
		this.videoType = videoType;
	}

	public int getVideoWidth() {
		return this.videoWidth;
	}

	public void setVideoWidth(int videoWidth) {
		this.videoWidth = videoWidth;
	}

	public int getVideoHeight() {
		return this.videoHeight;
	}

	public void setVideoHeight(int videoHeight) {
		this.videoHeight = videoHeight;
	}

	public int getAudioType() {
		return this.audioType;
	}

	public void setAudioType(int audioType) {
		this.audioType = audioType;
	}

	public int getSampRate() {
		return this.sampRate;
	}

	public void setSampRate(int sampRate) {
		this.sampRate = sampRate;
	}

	public int getChannel() {
		return this.channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public int getDepth() {
		return this.depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
}
