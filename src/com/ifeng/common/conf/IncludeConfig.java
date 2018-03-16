package com.ifeng.common.conf;

import java.net.URL;

import org.w3c.dom.Element;

import com.ifeng.common.misc.XmlLoader;


/**
 * <title> 引用配置 </title>
 * 
 * <pre>
 * 用于引用其他的配置文件,这样就可以将不同种类的配置配置在不同的配置文件中.并
 * 可以相互装载和引用.使得配置文件可以模块化和组件化.
 * 也可以使用这种方式加载一些环境,如加载log4j的配置等.
 * 
 * 配置的形式为:
 *    &lt;config name=&quot;xxx&quot; type=&quot;.IncludeConfig&quot; file=&quot;fileName&quot; [direct="boolean"] /&gt; 
 * 其中direct的含义为：
 * true:  直接引用,即直接将引用的文件加载在当前的ConfigRoot中,
 * 		和直接把引用的文件的内容拷贝到此文件的效果相同.
 * false: 非直接引用，即将应用的文件的对象作为一个值赋值给当前的配置对应的name.
 * 
 * 在不指定dirct时,默认为false.
 * 
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public class IncludeConfig extends MapConfig {

	private static final long serialVersionUID = -9077194935350213989L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ifeng.common.conf.Configurable#config(com.ifeng.common.conf.ConfigRoot,
	 *      java.lang.Object, org.w3c.dom.Element)
	 */
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		String fileName = XmlLoader.getAttribute(configEle, "file");
		URL configFileURL = configRoot.getConfigFileURL(fileName, null);
		boolean direct = XmlLoader.getAttributeAsBoolean(configEle,
                "direct", false);
		Element docEle = configRoot.loadConfigFile(fileName, null);
		Object result = null;
		
		if (direct) {
            result = configDirectInclude(configRoot, parent, configFileURL, docEle);
        } else {
            result = configIndirectInclude(configRoot, parent, configFileURL, docEle);
        }
		return result;

	}
	
	private Object configIndirectInclude(ConfigRoot configRoot, Object parent,
            URL configFileURL, Element docEle) {
        Object result;
        if (configRoot.getTypeClass(docEle, "type", MapConfig.class) == MapConfig.class) {
            configRoot.pushContext(new ConfigContext(this, configFileURL));
            try {
                super.config(configRoot, parent, docEle);
                result = this;
            } finally {
                configRoot.popContext();
            }
        } else {
            result = configRoot.createValueObjectOnly(parent, docEle);
            configRoot.pushContext(new ConfigContext(result, configFileURL));
            try {
                result = configRoot.configValueObject(result, parent, docEle);
            } finally {
                configRoot.popContext();
            }
        }
        return result;
    }

    private Object configDirectInclude(ConfigRoot configRoot, Object parent,
            URL configFileURL, Element docEle) {
        Object result;
        configRoot.pushContext(new ConfigContext(parent, configFileURL));
        try {
            result = configRoot.createValueObjectOnly(parent, docEle,
                    MapConfig.class);
            if (result instanceof ConfigAppendable) {
                ((ConfigAppendable)result).appendTo(configRoot, parent, docEle);
            } else {
                throw new ConfigException(docEle,
                        "Config must be appendable when direct='true'");
            }
            result = null;
        } finally {
            configRoot.popContext();
        }
        return result;
    }
}
