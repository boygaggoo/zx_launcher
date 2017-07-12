package com.ichano.rvs.streamer.constant;

public enum RvsRecordType {
	TIMINGRECORD(1), PRERECORD(2), SHORTVIDEO(4), CUSTOMRECORD(8), INVALID(-1);

	private int value;

	private RvsRecordType(int val) {
		this.value = val;
	}

	public int intValue() {
		return this.value;
	}

	public static RvsRecordType valueOfInt(int value) {
		switch (value) {
		case 1:
			return TIMINGRECORD;
		case 2:
			return PRERECORD;
		case 4:
			return SHORTVIDEO;
		case 8:
			return CUSTOMRECORD;
		}
		return INVALID;
	}
}
