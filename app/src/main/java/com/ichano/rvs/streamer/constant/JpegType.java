package com.ichano.rvs.streamer.constant;

public enum JpegType {
	HD(0), NORMAL(1), ICON(2);

	private int value;

	private JpegType(int val) {
		this.value = val;
	}

	public int intValue() {
		return this.value;
	}

	public static JpegType valueOfInt(int value) {
		switch (value) {
		case 0:
			return HD;
		case 1:
			return NORMAL;
		case 2:
			return ICON;
		}
		return NORMAL;
	}

	public static String valueToString(int value) {
		switch (value) {
		case 0:
			return "HD";
		case 1:
			return "NORMAL";
		case 2:
			return "ICON";
		}
		return "NORMAL";
	}
}
