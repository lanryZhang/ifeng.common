package com.ifeng.common.dm;

import java.util.Map;


/**
 * <title>AbstractCachedDataManager </title>
 * 
 * <pre>
 * 基于内存Map的缓存DataManager,支持ID查询时使用缓存，淘汰策略由子类实现。
 * </pre>
 * 
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public abstract class AbstractCachedDataManager extends
		AbstractReadOnlyBufferedDataManager {

	public Map data;

	private static final Object NON_EXIST = new Object();

	public AbstractCachedDataManager(DataManager manager) {
		super(manager);
	}
	
	public synchronized Object getById(Object id) throws DataManagerException {
		Object result = this.data.get(id);
		if (result != null) { // including NON_EXIST
			return result == NON_EXIST ? null : result;
		}
		result = getManager().getById(id);
		this.data.put(id, result == null ? NON_EXIST : result);
		return result;
	}

	public synchronized Object getBufferedById(Object id)
			throws DataManagerException {
		Object result = this.data.get(id);
        return result == NON_EXIST ? null : result;
	}
	public void invalidate(Object id) throws DataManagerException {
		this.data.remove(id);
	}

	public void invalidateAll() throws DataManagerException {
		this.data.clear();
	}

	public void set(Object id, Object value) throws DataManagerException {
		this.data.put(id, value);
	}
}
