package com.ichano.rvs.streamer.bean;

import com.ichano.rvs.streamer.constant.RvsSensorType;

public class RvsSensorInfo {
	public static final int FLAG_NO_SET = 0;
	public static final int FLAG_SET_OPEN = 1;
	public static final int FLAG_SET_CLOSE = 2;
	private int sensorIndex;
	private RvsSensorType sensorType;
	private int setFlag;
	private String sensorName;
	private ScheduleSetting[] scheduleSettings;

	public int getSensorIndex() {
		return this.sensorIndex;
	}

	public RvsSensorType getSensorType() {
		return this.sensorType;
	}

	public String getSensorName() {
		return this.sensorName;
	}

	public ScheduleSetting[] getScheduleSettings() {
		return this.scheduleSettings;
	}

	public int getSetFlag() {
		return this.setFlag;
	}

	public void setSetFlag(int setFlag) {
		this.setFlag = setFlag;
	}

	public void setSensorIndex(int sensorIndex) {
		this.sensorIndex = sensorIndex;
	}

	public void setSensorType(RvsSensorType sensorType) {
		this.sensorType = sensorType;
	}

	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}

	public void setScheduleSettings(ScheduleSetting[] scheduleSettings) {
		this.scheduleSettings = scheduleSettings;
	}
}
