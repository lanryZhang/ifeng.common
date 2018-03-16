package com.ifeng.common.conf;

import org.w3c.dom.Element;


/**
 * <title>  返回null的配置 </title>
 * <pre>
 * 总是返回null值的配置项.
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class NullConfig implements Configurable {
	
    /* (non-Javadoc)
     * @see com.ifeng.common.conf.Configurable#config(com.ifeng.common.conf.ConfigRoot, java.lang.Object, org.w3c.dom.Element)
     */
    public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
        return null;
    }

}