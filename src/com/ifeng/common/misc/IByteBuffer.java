package com.ifeng.common.misc;

import java.io.Serializable;

/**
 * 类似一个StringBuffer，只是里面存的是byte[]. 与StringBuffer不同的是，它不是线程安全的。
 * 
 * @author jinmy
 */
public final class IByteBuffer implements Serializable {

	private static final long serialVersionUID = -4216148968053467649L;

	private byte[] value;

	private int count;

	/**
	 * 创建一个空的ByteBuffer，缺省的内部空间为16个字节
	 */
	public IByteBuffer() {
		this(16);
	}

	/**
	 * 创举见一个空的ByteBuffer，给定初始的内部空间大小
	 * 
	 * @param length
	 *            初始的内部空间大小
	 */
	public IByteBuffer(int length) {
		this.value = new byte[length];
	}

	/**
	 * 返回当前的实际长度。
	 */
	public int length() {
		return this.count;
	}

	/**
	 * 返回当前的内部空间大小。
	 */
	public int capacity() {
		return this.value.length;
	}

	/**
	 * 保证内部空间大小。 如果内部空间大小小于minimumCapacity，将把内部空间扩展为至少minimumCapacity。
	 */
	public void ensureCapacity(int minimumCapacity) {
		if (minimumCapacity > this.value.length) {
			expandCapacity(minimumCapacity);
		}
	}

	/**
	 * 扩展内部空间。
	 */
	private void expandCapacity(int minimumCapacity) {
		int newCapacity = (this.value.length + 1) * 2;
		if (newCapacity < 0) {
			newCapacity = Integer.MAX_VALUE;
		} else if (minimumCapacity > newCapacity) {
			newCapacity = minimumCapacity;
		}

		byte[] newValue = new byte[newCapacity];
		System.arraycopy(this.value, 0, newValue, 0, this.count);
		this.value = newValue;
	}

	/**
	 * 设置长度。如果比当前长度长，则长出来的用0填充。如果比当前长度短，则截断(不重新分配空间)。
	 */
	public synchronized void setLength(int newLength) {
		ensureCapacity(newLength);
		this.count = newLength;
	}

	/**
	 * 返回index指定位置的byte。
	 * 
	 * @throws IndexOutOfBoundsException
	 *             如果越界
	 */
	public byte get(int index) {
		if (index < 0 || index >= this.count) {
			throw new IndexOutOfBoundsException();
		}
		return this.value[index];
	}

	/**
	 * 设置某个位置的字符。
	 * 
	 * @throws IndexOutOfBoundsException
	 *             如果越界
	 */
	public void set(int index, byte b) {
		if (index < 0 || index >= this.count) {
			throw new IndexOutOfBoundsException();
		}
		this.value[index] = b;
	}

	/**
	 * 增加一个元素。
	 * 
	 * @return 返回自己，方便一串append
	 */
	public IByteBuffer append(byte b) {
		ensureCapacity(this.count + 1);
		this.value[this.count++] = b;
		return this;
	}

	public IByteBuffer append(byte[] b) {
		ensureCapacity(this.count + b.length);
		System.arraycopy(b, 0, this.value, this.count, b.length);
		this.count += b.length;
		return this;
	}

	/**
	 * 返回buffer的拷贝
	 */
	public byte[] getValue() {
		byte[] result = new byte[this.count];
		System.arraycopy(this.value, 0, result, 0, this.count);
		return result;
	}

	/**
	 * 返回内部buffer，避免一次拷贝。 注意：它的实际长度可能会超过length()，使用时要避免错误。
	 */
	public byte[] getBuffer() {
		return this.value;
	}

}
