package com.ichano.rvs.streamer.codec;

public class G711 {
	static final int SIGN_BIT = 128;
	static final int QUANT_MASK = 15;
	static final int NSEGS = 8;
	static final int SEG_SHIFT = 4;
	static final int SEG_MASK = 112;
	static final int[] seg_end = { 255, 511, 1023, 2047, 4095, 8191, 16383, 32767 };
	static final int[] _u2a = {

			1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
			24, 25, 27, 29, 31, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 46, 48, 49, 50, 51, 52, 53, 54, 55, 56,
			57, 58, 59, 60, 61, 62, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 81, 82, 83, 84, 85,
			86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109,
			110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128 };
	static final int[] _a2u = {

			1, 3, 5, 7, 9, 11, 13, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 32, 33, 33,
			34, 34, 35, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 48, 49, 49, 50, 51, 52, 53, 54, 55, 56,
			57, 58, 59, 60, 61, 62, 63, 64, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 79, 80, 81,
			82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106,
			107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127 };
	public static final int BIAS = 132;

	static int search(int val, int[] table) {
		for (int i = 0; i < table.length; i++) {
			if (val <= table[i]) {
				return i;
			}
		}
		return table.length;
	}

	public static int linear2alaw(int pcm_val) {
		int mask;
		if (pcm_val >= 0) {
			mask = 213;
		} else {
			mask = 85;
			pcm_val = -pcm_val - 8;
		}
		int seg = search(pcm_val, seg_end);
		if (seg >= 8) {
			return 0x7F ^ mask;
		}
		int aval = seg << 4;
		if (seg < 2) {
			aval |= pcm_val >> 4 & 0xF;
		} else {
			aval |= pcm_val >> seg + 3 & 0xF;
		}
		return aval ^ mask;
	}

	public static int alaw2linear(int a_val) {
		a_val ^= 0x55;
		int t = (a_val & 0xF) << 4;

		int seg = (a_val & 0x70) >> 4;
		switch (seg) {
		case 0:
			t += 8;
			break;
		case 1:
			t += 264;
			break;
		default:
			t += 264;
			t <<= seg - 1;
		}
		return (a_val & 0x80) != 0 ? t : -t;
	}

	public static int linear2ulaw(int pcm_val) {
		int mask;
		if (pcm_val < 0) {
			pcm_val = 132 - pcm_val;
			mask = 127;
		} else {
			pcm_val += 132;
			mask = 255;
		}
		int seg = search(pcm_val, seg_end);
		if (seg >= 8) {
			return 0x7F ^ mask;
		}
		int uval = seg << 4 | pcm_val >> seg + 3 & 0xF;
		return uval ^ mask;
	}

	public static int ulaw2linear(int u_val) {
		u_val ^= 0xFFFFFFFF;

		int t = ((u_val & 0xF) << 3) + 132;

		t <<= (u_val & 0x70) >> 4;

		return (u_val & 0x80) != 0 ? 132 - t : t - 132;
	}

	public static int alaw2ulaw(int aval) {
		aval &= 0xFF;
		return (aval & 0x80) != 0 ? 0xFF ^ _a2u[(aval ^ 0xD5)] : 0x7F ^ _a2u[(aval ^ 0x55)];
	}

	public static int ulaw2alaw(int uval) {
		uval &= 0xFF;
		return (uval & 0x80) != 0 ? 0xD5 ^ _u2a[(0xFF ^ uval)] - 1 : 0x55 ^ _u2a[(0x7F ^ uval)] - 1;
	}
}
