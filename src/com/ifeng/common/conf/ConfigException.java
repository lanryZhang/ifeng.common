package com.ifeng.common.conf;

import org.w3c.dom.Element;

import com.ifeng.common.misc.XmlLoader;
/**
 * <title> common.conf包异常</title>
 * 
 * <pre>
 * 用于common.conf包的异常,为unchecked异常(继承java.lang.RuntimeException) @see java.lang.RuntimeException .<br>
 * 在传递Element@see org.w3c.dom.Element 的构造函数被调用时,将返回Element所在的位置(行、列).
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class ConfigException extends RuntimeException {

	private static final long serialVersionUID = -4270714532370785670L;

	public ConfigException() {
		super();
	}

	public ConfigException(String message) {
		super(message);
	}

	public ConfigException(Throwable cause) {
		super(cause);
	}

	public ConfigException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigException(Element ele, String msg, Throwable cause) {
		this(msg + " AT: " + XmlLoader.getElementLocation(ele), cause);
	}

	public ConfigException(Element ele, Throwable cause) {
		this(cause.getClass().getName() + ':' + cause.getMessage() + " AT: "
				+ XmlLoader.getElementLocation(ele), cause);
	}

	public ConfigException(Element ele, String msg) {
		this(msg + " AT: " + XmlLoader.getElementLocation(ele));
	}

}
