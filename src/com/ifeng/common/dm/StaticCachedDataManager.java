package com.ifeng.common.dm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigRoot;

/**
 * <title>LRUSizeCachedDataManager </title>
 * 
 * <pre>
 *  缓存全部数据的DataManager,支持ID查询时使用缓存。适用于小数据量但高查询频率的场景，会在初次使用时加载数据。
 * </pre>
 * 
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public class StaticCachedDataManager extends AbstractCachedDataManager {
	
	public StaticCachedDataManager(DataManager manager) {
		super(manager);
	}
	public StaticCachedDataManager(){
		super(null);
	}
	/**
	 * <pre>
	 *    &lt;... type=&quot;com.ifeng.common.dm.StaticCachedDataManager&quot;&quot;&gt;
	 *      &lt;data-manager ... 内部datamanager的配置/&gt;
	 *    &lt;/...&gt;
	 * </pre>
	 */
	public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
		this.data = Collections.synchronizedMap(new HashMap());
		return super.config(configRoot, parent, configEle);
	}

	
	private synchronized Map getData() throws DataManagerException {
		if (this.data.size() == 0) {
			this.data = Collections.synchronizedMap(new HashMap());
			QueryResult allResult = this.getManager().query(null,null);
			int size = allResult.getRowCount();
			for (int i = 0; i < size; i++) {
				Object obj = allResult.getData(i);
				this.data.put(getId(obj), obj);
			}
		}
		return this.data;
	}

	public Object getById(Object id) throws DataManagerException {
		getData();
		return super.getById(id);
	}

	public Object getBufferedById(Object id) throws DataManagerException {
		getData();
		return super.getBufferedById(id); 
	}
}
