package com.ifeng.common.misc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * 作为一个InputStream的decorator。增加了属性替换和赋值功能。<br/> 功能描述：
 * <ol>
 * <li>将 ${propname} 替换为property中相应名字指定的值</li>
 * <li>如果有 ${propname=value}形式，则将propname对应value放到property中。
 * 注意：如果propName或value中有'}'，需要在前面用'\'转义，否则会被认为是这个标记的结束</li>
 * <li>如果有${#property文件名}，将property文件读入。可以有多个这种标记。</li>
 * <li>property可以嵌套。嵌套内部的标记必须是第一种形式。</li>
 * </ol>
 * 可以指定stream的encoding，以便能正确处理property中非ASCII的字符。
 * 
 * @author jinmy
 * @see InputStream
 */
public class PropertyReplacingStream extends InputStream {

	private final Properties properties;

	private final InputStream inputStream;

	private byte[] outBuffer;

	private int outBufferPos;

	private String streamEncoding;

	public void close() throws IOException {
		inputStream.close();
	}

	/**
	 * Construcotr PropertyReplacingStream with specified InputStream,
	 * Properties and system default encoding.
	 * 
	 * @param inputStream
	 *            InputStream object.
	 * @param properties
	 *            Properties object.
	 * @throws IllegalArgumentException
	 *             If inputStream or properties is null.
	 */
	public PropertyReplacingStream(InputStream inputStream,
			Properties properties) {
		this(inputStream, properties, null);
	}

	/**
	 * Construcotr PropertyReplacingStream with specified InputStream,
	 * Properties and encoding for the stream.
	 * 
	 * @param inputStream
	 *            InputStream object.
	 * @param properties
	 *            Properties object.
	 * 
	 * @throws IllegalArgumentException
	 *             If inputStream or properties is null.
	 */
	public PropertyReplacingStream(InputStream inputStream,
			Properties properties, String streamEncoding) {
		if (inputStream == null || properties == null) {
			throw new IllegalArgumentException();
		}
		this.properties = properties;
		this.inputStream = inputStream;
		this.streamEncoding = streamEncoding;
	}

	/**
	 * 覆盖InputStream的方法。 jdk的InputStream的read(byte[],..)方法，会吃掉一些IOException
	 * 这里改正，不让它吃掉.
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		if (len == 0) {
			return 0;
		}
		int c = read();
		if (c == -1) {
			return -1;
		}
		b[off] = (byte) c;

		int i = 1;
		for (; i < len; i++) {
			c = read();
			if (c == -1) {
				break;
			}
			b[off + i] = (byte) c;
		}
		return i;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		while (true) {
			if (this.outBuffer != null) {
				// 需要输出buffer
				if (this.outBufferPos < this.outBuffer.length) {
					return toInt(this.outBuffer[this.outBufferPos++]);
				} else {
					this.outBuffer = null;
					this.outBufferPos = 0;
				}
			}

			int nextByte = this.inputStream.read();
			if (nextByte == -1) {
				return nextByte;
			}

			if (nextByte != '$') {
				// 原样输出
				return nextByte;
			}
			int nextNextByte = this.inputStream.read();
			if (nextNextByte != '{') {
				// 不是 ${，原样输出这两个字符，这一次输出'$'，下次输出'{'
				// 这里可能会由于EOF，有点问题，因为EOF被表示为byte了，会多输出一个255
				this.outBuffer = new byte[] { (byte) nextNextByte };
				return nextByte;
			}
			handleProperty();
		}
	}

	/**
	 * 处理property的赋值或替换
	 */
	private void handleProperty() throws IOException {
		IByteBuffer name = new IByteBuffer();
		boolean isIncludeFile = false;
		// 需要替换的property
		while (true) {
			int nextByte = tryEmbeddedProperty(name);
			switch (nextByte) {
			case -1:
				throw new IOException("Unexpected EOF in '${' sequence");
			case '}':
				if (isCloseBrace(name)) {
					if (isIncludeFile) {
						includeProperty(toString(name));
					} else {
						replaceProperty(toString(name));
					}
					return;
				}
				break;
			case '=':
				assignProperty(toString(name));
				return;
			case '#':
				// 如果第一个字符是#，表示include一个property文件
				if (name.length() == 0) {
					isIncludeFile = true;
				} else {
					// 否则认为是一个普通字符
					name.append((byte) nextByte);
				}
				break;
			default:
				name.append((byte) nextByte);
			}
		}
	}

	/**
	 * 处理一个嵌套的property(只支持${name}的形式。)。
	 * 如果遇到嵌套的property，则将property的替换结果附加到buffer上。 不管任何情况，均返回输入stream的下一个字节。
	 * 特殊情况是：遇到$，但下一个字节不是{，则将$附加到buffer上，返回下一个字节。
	 */
	private int tryEmbeddedProperty(IByteBuffer buffer) throws IOException {
		while (true) { // 处理连续的多个${...}${...}
			int nextByte = this.inputStream.read();
			if (nextByte != '$') {
				return nextByte;
			}
			nextByte = this.inputStream.read();
			if (nextByte != '{') {
				// 不是连续的'${'，将$附加到buffer上，把当前字符返回，供调用者进一步处理
				buffer.append((byte) '$');
				return nextByte;
			}
			IByteBuffer name = new IByteBuffer();
			// 需要替换的property
			while (true) {
				// 嵌套调用
				nextByte = tryEmbeddedProperty(name);
				if (nextByte == -1) {
					throw new IOException("Unexpected EOF in '${' sequence");
				} else if (nextByte == '}') {
					if (isCloseBrace(name)) {
						buffer.append(getProperty(toString(name)));
						break; // inner while, try next ${...}
					}
					// else the '}' is appended in isCloseBrace
				} else {
					name.append((byte) nextByte);
				}
			}
		}
	}

	/**
	 * 读入一个property文件，合并到当前的properties中
	 * 
	 * @param fileName
	 *            property文件名
	 */
	private void includeProperty(String fileName) throws IOException {
		InputStream input = new FileInputStream(fileName);
		try {
			this.properties.load(new PropertyReplacingStream(input,
					this.properties, "ISO8859-1"));
		} finally {
			input.close();
		}
	}

	/**
	 * 按照指定的streamEncoding，将一个ByteBuffer转换为字符串。
	 */
	private String toString(IByteBuffer buffer)
			throws UnsupportedEncodingException {
		byte[] value = buffer.getBuffer();
		if (this.streamEncoding == null) {
			return new String(value, 0, buffer.length());
		}
		return new String(value, 0, buffer.length(), this.streamEncoding);
	}

	/**
	 * 进行property的赋值，返回下一个字符。赋值部分（${...=...}）从stream中去除
	 */
	private void assignProperty(String name) throws IOException {
		IByteBuffer value = new IByteBuffer();
		while (true) {
			int nextByte = tryEmbeddedProperty(value);
			switch (nextByte) {
			case -1:
				throw new IOException("Unexpected EOF in '${ = sequence");
			case '}':
				if (isCloseBrace(value)) {
					this.properties.setProperty(name, toString(value));
					// ${...=... }被从stream中去除
					return;
				}
				break;
			default:
				// 这种方法实际上将输入内容当作8859-1来处理
				value.append((byte) nextByte);
				break;
			}
		}
	}

	/**
	 * 判断当前读入的一个'}'是否是被转义的。如果是被转义的，将这个字符替换原先的'\'
	 */
	private boolean isCloseBrace(IByteBuffer buffer) {
		int length = buffer.length();
		if (length > 1 && buffer.get(length - 1) == '\\') {
			// escaped '}'
			buffer.set(length - 1, (byte) '}');
			return false;
		}
		return true;
	}

	/**
	 * 替换一个property，放到outBuffer中
	 */
	private void replaceProperty(String name) throws IOException {
		pushBuffer(getProperty(name));
	}

	private byte[] getProperty(String name) throws IOException {
		// 替换property name为property value
		String value = this.properties.getProperty(name);
		if (value == null) {
			throw new IOException("Undefined property: " + name);
		}
		if (this.streamEncoding == null) {
			return value.getBytes();
		}
		return value.getBytes(this.streamEncoding);
	}

	/**
	 * 将一个value缓存到outBuffer中，等待后续的read调用时输出
	 */
	private void pushBuffer(byte[] value) {
		this.outBuffer = value;
		this.outBufferPos = 0;
	}

	/**
	 * 将byte转换为-1~254的int。我们假定在正常的文档中，没有byte值为-1的字节
	 */
	private int toInt(byte b) {
		return (b >= -1 ? b : b + 256);
	}

}
