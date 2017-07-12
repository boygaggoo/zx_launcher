package com.ichano.rvs.streamer.param;

public class CameraCapacity {
	public static final int PTZMOVECTRL_PCTRL = 1;
	public static final int PTZMOVECTRL_TCTRL = 2;
	public static final int PTZMOVECTRL_ZCTRL = 4;
	public static final int PTZMOVECTRL_XMOVE = 8;
	public static final int PTZMOVECTRL_YMOVE = 16;
	public static final int PTZMOVECTRL_ZMOVE = 32;
	public static final int ONE_STREAM_CONCURRENTLY = 0;
	public static final int MULTI_STREAM_CONCURRENTLY = 1;
	public static final int STREAMER_DEFINITION_NONE = 0;
	public static final int STREAMER_DEFINITION_SD_640_480 = 1;
	public static final int STREAMER_DEFINITION_SD_800_600 = 2;
	public static final int STREAMER_DEFINITION_HD_1280_720 = 4;
	private int ptzMoveMode;
	private boolean torchEnable;
	private boolean rotateEnable;
	private int streamType;
	private int definition;

	public int getDefinition() {
		return this.definition;
	}

	public void setDefinition(int definition) {
		this.definition = definition;
	}

	public int getPtzMoveMode() {
		return this.ptzMoveMode;
	}

	public void setPtzMoveMode(int ptzMoveMode) {
		this.ptzMoveMode = ptzMoveMode;
	}

	public boolean isTorchEnable() {
		return this.torchEnable;
	}

	public void setTorchEnable(boolean torchEnable) {
		this.torchEnable = torchEnable;
	}

	public boolean isRotateEnable() {
		return this.rotateEnable;
	}

	public void setRotateEnable(boolean rotateEnable) {
		this.rotateEnable = rotateEnable;
	}

	public int getStreamType() {
		return this.streamType;
	}

	public void setStreamType(int streamType) {
		this.streamType = streamType;
	}
}
