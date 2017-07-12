package com.ichano.rvs.streamer.param;

public class Capacity {
	public static final int MANUAL_RUN_MODE = 0;
	public static final int AUTO_RUN_MODE = 1;
	public static final int BACKGROUND_RUN_MODE = 2;
	public static final int SUSPEND_RUN_MODE = 4;
	public static final int NO_RECORD_MODE = 0;
	public static final int SDCARD_RECORD_MODE = 1;
	public static final int STORAGE_RECORD_MODE = 2;
	public static final int SETTIMEZONE_NOT_SUPPORT = 0;
	public static final int SETTIMEZONE_SUPPORT = 1;
	public static final int ECHOCANCEL_NOT_SUPPORT = 0;
	public static final int ECHOCANCEL_SUPPORT = 1;
	private int runMode;
	private int recordMode;
	private int timeZoneMode;
	private int echoCancelMode;

	public int getRunMode() {
		return this.runMode;
	}

	public void setRunMode(int runMode) {
		this.runMode = runMode;
	}

	public int getRecordMode() {
		return this.recordMode;
	}

	public void setRecordMode(int recordMode) {
		this.recordMode = recordMode;
	}

	public int getTimeZoneMode() {
		return this.timeZoneMode;
	}

	public void setTimeZoneMode(int timeZoneMode) {
		this.timeZoneMode = timeZoneMode;
	}

	public int getEchoCancelMode() {
		return this.echoCancelMode;
	}

	public void setEchoCancelMode(int echoCancelMode) {
		this.echoCancelMode = echoCancelMode;
	}
}
