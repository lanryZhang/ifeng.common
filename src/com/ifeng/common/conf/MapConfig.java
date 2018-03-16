package com.ifeng.common.conf;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ifeng.common.misc.Logger;
import com.ifeng.common.misc.XmlLoader;

/**
 * <title>  Map 配置 </title>
 * <pre>
 * 基本配置类型. 它反映为一个HashMap，可以在一些环境中使用.
 * 它实现的Configurable接口，使得它自己可以嵌套使用。<br>
 * 
 * 只要config树中是DynaBean、bean、Map等，都可以被key访问。<br>
 * 它的实现方式与其它Configurable有所不同，因为它需要在其配置过程中即可被访问到 
 * &lt;...type=".MapConfig" &gt;
 * 	&lt;entry key="" value="/&gt;
 * 		...
 * &lt/...&gt 
 * <br>
 * 或 
 * &lt;...type=".MapConfig" &gt;
 *  &lt;entry&gt;
 *  	&lt;key 标准配置 /&gt; 
 *  	&lt;value 标准配置 /&gt; 
 *  &lt;/entry&gt; 
 *  ...
 * &lt/...&gt
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class MapConfig implements Map, Configurable, ConfigAppendable,
		Serializable {

	private static final long serialVersionUID = 233785226014965916L;

	private static final Logger log = Logger.getLogger(MapConfig.class);

	private Map data = Collections.synchronizedMap(new HashMap());

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ifeng.common.conf.Configurable#config(com.ifeng.common.conf.ConfigRoot,
	 *      java.lang.Object, org.w3c.dom.Element)
	 */
	public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
		doConfig(this, configRoot, parent, configEle);
		return this;
	}

	protected static void doConfig(Map targetData, ConfigRoot configRoot,
			Object parent, Element configEle) {
		for (Node node = configEle.getFirstChild(); node != null; node = node
				.getNextSibling()) {

			if (node.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element subConfigEle = (Element) node;
			String tag = subConfigEle.getTagName();
			if (!tag.equals("config") && !tag.equals("entry")) {
				throw new ConfigException(subConfigEle,
						"Invalid element tag ('config' or 'entry' required): "
								+ subConfigEle.getTagName());
			}

			Object key = null;
			try {
				key = XmlLoader.getAttribute(subConfigEle, "name", null);
				if (key == null) {
                    key = XmlLoader.getAttribute(subConfigEle, "key", null);
                }
				Object value = null;
                boolean hasValueEle = false;
                if (key == null) {
                    key = configRoot.createChildObject(targetData,
                            subConfigEle, "key");
                    if (key != null) {
                        // 如果有key子元素，则必须有value子元素
                        value = configRoot.createChildObject(targetData,
                                subConfigEle, "value", true);
                        targetData.put(key, value);
                        hasValueEle = true;
                    }
                }
                if (!hasValueEle) {
                    value = configRoot.createValueObjectOnly(parent, subConfigEle);
                    if (key != null) {
                        targetData.put(key, value);
                    }
                    Object finalObj = configRoot.configValueObject(value,
                            targetData, subConfigEle);
                    if (finalObj != value && key != null) {
                        targetData.put(key, finalObj);
                    }
                }
			} catch (Throwable e) {
				// including errors
				log.error("Error loading config: " + subConfigEle + ": "
						+ XmlLoader.getElementLocation(subConfigEle), e);
				// 把这个exception保存在这里
				if (key != null) {
					if (e instanceof ConfigException) {
						targetData.put(key, e);
					} else {
						targetData.put(key, new ConfigException(e));
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ifeng.common.conf.ConfigAppendable#appendTo(com.ifeng.common.conf.ConfigRoot,
	 *      java.lang.Object, org.w3c.dom.Element)
	 */
	public void appendTo(ConfigRoot configRoot, Object configObj,
			Element configEle) {
		if (configObj instanceof Map) {
			doConfig((Map) configObj, configRoot, null, configEle);
		} else {
			throw new ConfigException(configEle, "Incompatible type to append");
		}
	}

	// delegated method to make this implements Map
	public void clear() {
		data.clear();
	}

	public boolean containsKey(Object key) {
		return data.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return data.containsValue(value);
	}

	public Set entrySet() {
		return data.entrySet();
	}

	public boolean equals(Object obj) {
		return data.equals(obj);
	}

	public Object get(Object key) {
		return data.get(key);
	}

	public int hashCode() {
		return data.hashCode();
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}

	public Set keySet() {
		return data.keySet();
	}

	public Object put(Object key, Object value) {
		return data.put(key, value);
	}

	public void putAll(Map t) {
		data.putAll(t);
	}

	public Object remove(Object key) {
		return data.remove(key);
	}

	public int size() {
		return data.size();
	}

	public Collection values() {
		return data.values();
	}

	public String toString() {
		return data.toString();
	}

	/**
	 * 当serialize时，serializa其data
	 */
	protected Object writeReplace() {
		return this.data;
	}

}
