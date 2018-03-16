package com.ifeng.common.conf;

import org.w3c.dom.Element;

import com.ifeng.common.misc.XmlLoader;



/**
 * <title> 动态调用方法的配置 </title>
 * 
 * <pre>
 * 用于调用一个方法,并将返回值作为结果赋值给配置的对象.
 * 
 * 配置的形式为:
 * &lt;config type=".InvokeConfig" method="方法名"&gt;
 *   &lt;object type="... /&gt;
 *   &lt;arg type="... /&gt;
 *   &lt;arg type="... /&gt;
 * &lt;/config&gt;
 * 
 * object中可以使用.ReferenceConfig，引用一个已经配置的对象
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public class InvokeConfig implements Configurable {

    public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
        String methodName = XmlLoader.getAttribute(configEle, "method");
        Object object = configRoot.createChildObject(this, configEle, "object", true);
        return configRoot.configInvoke(configEle, object.getClass(),
                methodName, object);
    }
    
}

