package com.ifeng.common.conf;

import org.w3c.dom.Element;

import com.ifeng.common.misc.XmlLoader;
/**
 * <title> 静态调用方法的配置 </title>
 * 
 * <pre>
 * 用于调用一个静态方法,并将返回值作为结果赋值给配置的对象.
 * 通常用于调用一个静态工厂方法，来创建一个新的对象
 * 配置的形式为:
 * &lt;config type=".StaticInvokeConfig" class="类名" method="方法名"&gt;
 *   &lt;arg type="... /&gt;
 *   &lt;arg type="... /&gt;
 * &lt;/config&gt;
 * 
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public class StaticInvokeConfig implements Configurable {

    /* (non-Javadoc)
     * @see com.ifeng.common.conf.Configurable#config(com.ifeng.common.conf.ConfigRoot, java.lang.Object, org.w3c.dom.Element)
     */
    public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
        String className = XmlLoader.getAttribute(configEle, "class", null);
        String methodName = XmlLoader.getAttribute(configEle, "method");
        Class clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ConfigException(configEle, e);
        }
        return configRoot.configInvoke(configEle, clazz, methodName, null);
    }
}

