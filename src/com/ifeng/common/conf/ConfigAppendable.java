package com.ifeng.common.conf;

import org.w3c.dom.Element;
/**
 * <title>动态加载配置接口</title>
 * 
 * <pre>可以添加内容的config接口,适应需要加载配置的场景。<br>
 *需要添加的配置为一个Element @see org.w3c.dom.Element.
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public interface ConfigAppendable {
	/**
	 * 将一个配置元素添加到原来的configRoot上.
	 * 
	 * @param configRoot 当前的configRoot
	 * @param config 具体类型取决于添加的配置类型
	 * @param configEle 需要添加的配置元素
	 */
	public void appendTo(ConfigRoot configRoot, Object config, Element configEle);
}
