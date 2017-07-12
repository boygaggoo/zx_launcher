package com.ichano.rvs.streamer.util;

public class RingBuffer {
	private int capacity;
	private int wIndex;
	private int rIndex;
	private short[] buffer;
	private short[] taken;
	private int takeSize;

	public RingBuffer(int capacity, int takeSize) {
		this.capacity = capacity;
		this.takeSize = takeSize;
		this.buffer = new short[capacity];
		this.taken = new short[takeSize];
		this.wIndex = 0;
		this.rIndex = 0;
	}

	public boolean putAll(short[] data, int len) {
		int tmpIndex = this.rIndex;
		if (this.wIndex >= tmpIndex) {
			if (this.wIndex + len + 1 <= this.capacity) {
				System.arraycopy(data, 0, this.buffer, this.wIndex, len);
				this.wIndex += len;
				return true;
			}
			if ((this.wIndex + len + 1) % this.capacity < tmpIndex) {
				System.arraycopy(data, 0, this.buffer, this.wIndex, this.capacity - this.wIndex);
				if (len - (this.capacity - this.wIndex) > 0) {
					System.arraycopy(data, this.capacity - this.wIndex, this.buffer, 0,
							len - (this.capacity - this.wIndex));
				}
				this.wIndex = ((this.wIndex + len) % this.capacity);
				return true;
			}
		} else if (this.wIndex + len < tmpIndex) {
			System.arraycopy(data, 0, this.buffer, this.wIndex, len);
			this.wIndex += len;
			return true;
		}
		return false;
	}

	public short[] takeAll() {
		int tmpIndex = this.wIndex;
		if (this.rIndex == tmpIndex) {
			return null;
		}
		if (this.rIndex < tmpIndex) {
			if (this.rIndex + this.takeSize <= tmpIndex) {
				System.arraycopy(this.buffer, this.rIndex, this.taken, 0, this.takeSize);
				this.rIndex += this.takeSize;
				return this.taken;
			}
		}
		if (this.rIndex > tmpIndex) {
			if (this.rIndex + this.takeSize <= this.capacity) {
				System.arraycopy(this.buffer, this.rIndex, this.taken, 0, this.takeSize);
				this.rIndex = ((this.rIndex + this.takeSize) % this.capacity);
				return this.taken;
			}
			if ((this.rIndex + this.takeSize) % this.capacity <= tmpIndex) {
				System.arraycopy(this.buffer, this.rIndex, this.taken, 0, this.capacity - this.rIndex);
				System.arraycopy(this.buffer, 0, this.taken, this.capacity - this.rIndex,
						this.takeSize - (this.capacity - this.rIndex));
				this.rIndex = ((this.rIndex + this.takeSize) % this.capacity);
				return this.taken;
			}
		}
		return null;
	}
}
