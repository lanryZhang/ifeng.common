package com.ifeng.common.dm.persist.hibernate;

import java.net.URL;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.XmlLoader;

/**
 * <title>Mapping配置 </title>
 * 
 * <pre>O/R mapping配置，与Hibernate的properties配置分离，便于减少模块依赖度。<br>
 *  &lt; ... type="com.ifeng.common.dm.MappingConfig"&gt;
 *    &lt;mapping class="类名"/&gt;  
 *      将类名.hbm.xml添加为mapping
 *    &lt;mapping resource="文件名"/&gt;
 *      从类路径中读取文件
 *    &lt;mapping file="mapping文件名"/&gt; 
 *      如果文件名不是绝对路径，则相对于configRoot的root路径
 *  &lt;/...&gt;
 *  上述方式任选一种。建议用class或package方式，将.hbm.xml文件放在源程序jar包中
 *  用class方式，将在类路径的class路径中查找类名+".hbm.xml"的文件
 *  
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class MappingConfig implements Configurable {

    public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
        for (Node node = configEle.getFirstChild(); node != null;
                node = node.getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element ele = (Element)node;
            String mappingType = null;
            String value = null;
            
            String tag = ele.getTagName();
            if (tag.equals("mapping")) {
                // old format
                value = XmlLoader.getAttribute(ele, "class", null);
                if (value != null) {
                    mappingType = "class";
                } else {
                    value = XmlLoader.getAttribute(ele, "resource", null);
                    if (value != null) {
                        mappingType = "resource";
                    } else {
                        value = XmlLoader.getAttribute(ele, "file", null);
                        if (value != null) {
                            mappingType = "file";
                        } else {
                            throw new ConfigException(ele,
                                "missing 'class'(preferred), 'resource' or 'file' attribute");
                        }
                    }
                }
            } 
            
            try {
                if (mappingType.equals("class")) {
                    Class clazz = Class.forName(value);
                    HibernateConfig.addMapping(clazz);
                } else if (mappingType.equals("resource")) {
                    HibernateConfig.addMappingFromResource(value);
                } else if (mappingType.equals("file")) {
                    // 默认 protocol 为 null
                    String protocol = XmlLoader.getAttribute(ele, "protocol", null);

                    URL url = configRoot.getConfigFileURL(value, protocol);
                    HibernateConfig.addMapping(url);
                } else {
                    throw new ConfigException(ele, "Unknown mapping type: "
                            + mappingType);
                }
            } catch (ConfigException e) {
                throw e;
            } catch (Exception e) {
                throw new ConfigException(ele, e);
            }
        }
        return null;
    }
}
