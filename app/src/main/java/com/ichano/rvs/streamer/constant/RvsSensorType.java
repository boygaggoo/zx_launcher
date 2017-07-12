package com.ichano.rvs.streamer.constant;

public enum RvsSensorType {
	MOTION(1), SMOG(2), INFRARED(3), DOORSENSOR(4), UNKNOWN(-1), USER_MOTION(5), SENSOR1(6), SENSOR2(7), SENSOR3(
			8), SENSOR4(9);

	private int value;

	private RvsSensorType(int val) {
		this.value = val;
	}

	public int intValue() {
		return this.value;
	}

	public static RvsSensorType valueOfInt(int value) {
		switch (value) {
		case 1:
			return MOTION;
		case 2:
			return SMOG;
		case 3:
			return INFRARED;
		case 4:
			return DOORSENSOR;
		case 5:
			return USER_MOTION;
		case 6:
			return SENSOR1;
		case 7:
			return SENSOR2;
		case 8:
			return SENSOR3;
		case 9:
			return SENSOR4;
		}
		return UNKNOWN;
	}
}