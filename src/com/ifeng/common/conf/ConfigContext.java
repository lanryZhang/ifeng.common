package com.ifeng.common.conf;

import java.net.URL;
/**
 * <title>配置过程上下文</title>
 * 
 * <pre>配置过程的上下文.它是不可变对象（immutable）,不提供set方法.<br>
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */


public class ConfigContext {
	
	private Object currentFileRoot;

	private URL currentURL;

	/**
	 * @param currentFileRoot 当前文件的根配置对象 (用于访问局部引用－以'.'开头的引用)
	 * @param currentURL 当前配置文件的URL
	 */
	public ConfigContext(Object currentFileRoot, URL currentURL) {
		this.currentFileRoot = currentFileRoot;
		this.currentURL = currentURL;
	}

	public URL getCurrentURL() {
		return currentURL;
	}

	public Object getCurrentFileRoot() {
		return currentFileRoot;
	}

}
