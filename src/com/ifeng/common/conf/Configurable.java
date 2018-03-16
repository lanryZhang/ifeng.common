package com.ifeng.common.conf;

import org.w3c.dom.Element;
/**
 * <title>可配置的对象接口</title>
 * 
 * <pre>除了传统IOC容器的构造函数和set get外,提供一种“更灵活”的配置方式.<br>
 * 需要更灵活的配置方式的场景一般为配置的对象需要注入的数据相对比较复杂,类似的对象
 * 可以实现这个接口，并实现
 * config(ConfigRoot configRoot, Object parent,Element configEle)
 * 方法,框架解析到此对象的配置时,会将此对象依赖的Element通过调用config方法的方
 * 式传递到此对象中,由此对象实现的config方式完成自身的注入工作.同时configRoot
 * 提供了非常方便的配置装载流程,可是使得实现此方法的复杂度非常小.<br>
 * 
 * 此接口的额外一个福利是可以通过实现此接口的方法扩展属于自己的xml标签，定义一个更
 * 接近自身含义的配置形式.
 * 
 * 例如：
 *public class TestConfigurable implements Configurable{
 *    private String name;
 *    private String value;
 *    public Object config(ConfigRoot configRoot, Object parent, 
 *    		Element configEle) throws ConfigException {
 *        name = (String)configRoot.createChildObject(parent, configEle, "name");
 *        value =(String)configRoot.createChildObject(parent, configEle, "value");
 *        return this;
 *    }
 *}
 *<br>
 *配置方式如下：
 *	&lt;config name="testConfigurable" type="com.ifeng.common.conf.TestConfigurable"&gt;
 *		&lt;name value="testname"/&gt;
 *		&lt;value value="testvalue"/&gt;
 *	&lt;/config&gt;
 *
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public interface Configurable {
	/**
	 * 配置一个对象.
	 * 
	 * @param configRoot 当前的配置上下文对象(在一个子系统中可能只有一个)
	 * @param parent 上级对象
	 * @param configEle 配置元素
	 * @return 配置完成的对象，一般应该是this,但是也可以不是它自己
	 * @throws ConfigException 配置出错时 (unchecked)
	 */
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException /* unchecked */;
}
