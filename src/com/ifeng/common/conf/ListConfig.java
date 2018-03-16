package com.ifeng.common.conf;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ifeng.common.misc.RegexPatternCache;
import com.ifeng.common.misc.XmlLoader;


/**
 *<title>  List类型的配置 </title>
 *
 * <pre>
 * 用于配置一个List @see java.util.List.<br>
 * 
 * 配置方式有两种：
 * 	方式1 － 单独指定每个元素：
 * &lt;config name="xxx" type=".ListConfig" [item-type="item的缺省类型"]&gt;
 *   &lt;item [type="xxx"] ...&gt;
 *   ... 
 * &lt;/config&gt;

 * 方式2－用一个逗号隔开的元素内容。value的写法取决于item-type，要求必须能用ConfigRoot的
 * decodeString解码 @see ConfigRoot#decodeString(Class, String)
 * &lt;config name="xxx" type=".ListConfig" item-type="数据项的缺省类型" 
 *      value="value1,value2,..."/&gt;
 * 
 * 如果下面item也指定了类型，它的实际类型不必是item-type的子类
 * </pre>
 * 
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class ListConfig implements Configurable, ConfigAppendable {
    /* (non-Javadoc)
     * @see com.ifeng.common.conf.Configurable#config(com.ifeng.common.conf.ConfigRoot, java.lang.Object, org.w3c.dom.Element)
     */
    public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
        List result = new ArrayList();
        doConfig(configRoot, result, configEle);
        return result;
    }

    private void doConfig(ConfigRoot configRoot, List result, Element configEle) {
        Class itemType = configRoot.getTypeClass(configEle, "item-type", null);
        addListData(configRoot, configEle, itemType, result, this);
    }

    /* (non-Javadoc)
     * @see com.ifeng.common.conf.ConfigAppendable#appendTo(com.ifeng.common.conf.ConfigRoot, java.lang.Object, org.w3c.dom.Element)
     */
    public void appendTo(ConfigRoot configRoot, Object config, Element configEle) {
        if (config instanceof List) {
            doConfig(configRoot, (List)config, configEle);
        } else {
            throw new ConfigException(configEle, "Incompatible type to append");
        }
    }

    static void addListData(ConfigRoot configRoot, Element configEle,
            Class itemType, List data, Object parent) {
        addValueData(configEle, itemType, data);
        addSubItemData(configRoot, configEle, itemType, data, parent);
    }
    
    /**
     * 将元素的文字内容解析为多个逗号隔开的字符串值，并把值添加到data中
     */
    static void addValueData(Element configEle, Class itemType, List data) {
        String value = XmlLoader.getAttribute(configEle, "value", null);
        if (value == null) {
            return;
        }
        // 要求必须指定itemType
        if (itemType == null) {
            throw new ConfigException(configEle,
                    "Missing 'item-type' when element text value provided");
        }
        String[] values = RegexPatternCache.split(",", value);
        for (int i = 0; i < values.length; i++) {
            try {
                data.add(ConfigRoot.decodeString(itemType, values[i]));
            } catch (Exception e) {
                throw new ConfigException(configEle, "Can't decode '"
                        + values[i] + "' to " + itemType, e);
            }
        }
    }

    /**
     * 解析所有的&lt;item/&gt;子元素，添加到data中
     */
    static void addSubItemData(ConfigRoot configRoot, Element configEle,
            Class itemType, List data, Object parent) {
        for (Node node = configEle.getFirstChild(); node != null; node = node
                .getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element itemEle = (Element)node;
            if (!itemEle.getTagName().equals("item")) {
                throw new ConfigException(itemEle, 
                        "Invalid element tag ('item' required): ");
            }
            data.add(configRoot.createValueObject(parent, itemEle, itemType));
        }
    }

}
