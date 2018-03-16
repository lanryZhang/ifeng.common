package com.ifeng.common.conf;

import org.w3c.dom.Element;

import com.ifeng.common.misc.XmlLoader;


/**
 * <title> 引用配置 </title>
 * <pre>
 * 用于配置一个对另一个配置对象的引用.
 * 不允许引用下文的配置(即在被引用的对象定义之前引用).
 * 主要的使用场景：
 * 		1 保障单例，即某一个类只希望存在一个对象，或者大家共享某一个对象.
 * 		2 引用其他已经生成的配置，在单例的同时减少重复的配置.
 * 		3 一些配置方式限制的场景.
 * 
 * 使用的方式为：
 * 
 * &lt;config ... type=".ReferenceConfig" reference="xxx"/&gt;
 * </pre> 
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class ReferenceConfig implements Configurable {

    /* (non-Javadoc)
     * @see com.ifeng.common.conf.Configurable#config(com.ifeng.common.conf.ConfigRoot, java.lang.Object, org.w3c.dom.Element)
     */
    public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
        return configRoot.getValue(XmlLoader.getAttribute(configEle, "reference"));
    }

}