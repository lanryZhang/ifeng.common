package com.ifeng.common.conf;

import java.net.URL;

import org.w3c.dom.Element;

import com.ifeng.common.misc.Logger;
import com.ifeng.common.misc.XmlLoader;

/**
 * <title>   Log4j配置 </title>
 * 配置log(log4j).
 * 有以下配置方式：
 * <pre>
 * 方式1：
 *  &lt;... type=".LogConfig"&gt;
 *    &lt;log4j:configuration xmlns:log4j="http://jakarta.apache.org/log4j/"&gt;
 *      ...
 *    &/lt;log4j:configuration&gt;
 *  &lt;/...&gt;
 * 方式1的变种：使用.IncludeConfig， 将上述内容放在分离的文件中。但如果是这种方式，下面提供
 * 更好的方式。
 * 
 * 方式2：
 *  &lt; ... type=".LogConfig" file="log4j配置文件名"&gt;
 *  配置文件名如果是相对路径,相对于configRoot文件的Home.
 *  
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public class LogConfig implements Configurable {

    public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
        String file = XmlLoader.getAttribute(configEle, "file", null);
        // 默认 protocol 为 null
        String protocol = XmlLoader.getAttribute(configEle, "protocol", null);

        if (file != null) {
            URL url = configRoot.getConfigFileURL(file, protocol);
            Logger.configure(url, configRoot.getProperties());
        } else {
            Element ele = XmlLoader.getChildElement(configEle,
                    "http://jakarta.apache.org/log4j/", "configuration");
            Logger.configure(ele);
        }
        return null;
    }

}

