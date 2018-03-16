package com.ifeng.common.dm;

import org.apache.commons.collections.map.LRUMap;
import org.w3c.dom.Element;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.misc.XmlLoader;

/**
 * <title>LRUSizeCachedDataManager </title>
 * 
 * <pre>
 *  基于LRU策略缓存固定的条数的DataManager,支持ID查询时使用缓存，LRU淘汰策略。
 * </pre>
 * 
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public class LRUSizeCachedDataManager extends AbstractCachedDataManager {

	public LRUSizeCachedDataManager(DataManager manager, int size) {
		super(manager);
		//data = new LRUMap(size);
		data = new ConcurrentLinkedHashMap.Builder()
			    .maximumWeightedCapacity(size)
			    .build();
				
	}
	/**
	 * for config
	 *
	 */
	public LRUSizeCachedDataManager(){
		super(null);
	}

	/**
	 * <pre>
	 *   &lt;... type=&quot;com.ifeng.common.dm.LRUSizeCachedDataManager&quot; size=&quot;最大缓存的项数&quot;&gt;
	 *     &lt;data-manager ... 内部datamanager的配置
	 *     /&gt;
	 *   &lt;/...&gt;
	 * </pre>
	 */
	public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
		//data = new LRUMap(XmlLoader.getAttributeAsInteger(configEle, "size"));
		int size = XmlLoader.getAttributeAsInteger(configEle, "size");
		data = new ConcurrentLinkedHashMap.Builder()
	    .maximumWeightedCapacity(size)
	    .build();
		return super.config(configRoot, parent, configEle);
	}
}
