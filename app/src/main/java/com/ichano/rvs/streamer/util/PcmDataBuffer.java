package com.ichano.rvs.streamer.util;

public class PcmDataBuffer {
	private int capacity;
	private int wIndex;
	private int rIndex;
	private short[] buffer;

	public PcmDataBuffer(int size) {
		this.capacity = size;
		this.buffer = new short[this.capacity];
		this.wIndex = 0;
		this.rIndex = 0;
	}

	public void putAll(short[] data, int len) {
		int tmpIndex = this.rIndex;
		if (this.wIndex >= tmpIndex) {
			if (this.wIndex + len + 1 <= this.capacity) {
				System.arraycopy(data, 0, this.buffer, this.wIndex, len);
				this.wIndex += len;
				return;
			}
			if ((this.wIndex + len + 1) % this.capacity < tmpIndex) {
				System.arraycopy(data, 0, this.buffer, this.wIndex, this.capacity - this.wIndex);
				if (len - (this.capacity - this.wIndex) > 0) {
					System.arraycopy(data, this.capacity - this.wIndex, this.buffer, 0,
							len - (this.capacity - this.wIndex));
				}
				this.wIndex = ((this.wIndex + len) % this.capacity);
			}
		} else if (this.wIndex + len < tmpIndex) {
			System.arraycopy(data, 0, this.buffer, this.wIndex, len);
			this.wIndex += len;
			return;
		}
	}

	public short[] takeAll(int size) {
		int tmpIndex = this.wIndex;
		short[] ret = new short[size];
		if (this.rIndex == tmpIndex) {
			return null;
		}
		if (this.rIndex < tmpIndex) {
			if (this.rIndex + size <= tmpIndex) {
				System.arraycopy(this.buffer, this.rIndex, ret, 0, size);
				this.rIndex += size;
				return ret;
			}
		}
		if (this.rIndex > tmpIndex) {
			if (this.rIndex + size <= this.capacity) {
				System.arraycopy(this.buffer, this.rIndex, ret, 0, size);
				this.rIndex = ((this.rIndex + size) % this.capacity);
				return ret;
			}
			if ((this.rIndex + size) % this.capacity <= tmpIndex) {
				System.arraycopy(this.buffer, this.rIndex, ret, 0, this.capacity - this.rIndex);
				System.arraycopy(this.buffer, 0, ret, this.capacity - this.rIndex,
						size - (this.capacity - this.rIndex));
				this.rIndex = ((this.rIndex + size) % this.capacity);
				return ret;
			}
		}
		return null;
	}
}
