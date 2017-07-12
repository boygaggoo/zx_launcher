package com.ichano.rvs.streamer.constant;

public enum MotionDetectState {
	START(1), STOP(2), ERROR(3), MOTIONDECTED(99), INVALID(-1);

	private int value;

	private MotionDetectState(int val) {
		this.value = val;
	}

	public int intValue() {
		return this.value;
	}

	public static MotionDetectState vauleOfInt(int value) {
		switch (value) {
		case 1:
			return START;
		case 2:
			return STOP;
		case 3:
			return ERROR;
		case 99:
			return MOTIONDECTED;
		}
		return INVALID;
	}
}