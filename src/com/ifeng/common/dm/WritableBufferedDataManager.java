package com.ifeng.common.dm;

import java.util.Map;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigRoot;
/**
 * <title>WritableBufferedDataManager </title>
 * 
 * <pre>可写的DataManager ，包装只读的DataManager.
 * 读操作使用包装的只读的BufferedDataManager,写操作使用backManager。
 * 
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class WritableBufferedDataManager extends AbstractDataManagerDecorator
		implements BufferedDataManager {

	private BufferedDataManager readManager;

	/**
	 * 一般backManager也应当是readManager的内部manager
     * @param backManager 写操作将delegate到backManager
     * @param readManager 读操作将delegate到readManager
	 */
	public WritableBufferedDataManager(DataManager backManager,
			AbstractReadOnlyBufferedDataManager readManager) {
		super(backManager);
		this.readManager = readManager;
	}

	public WritableBufferedDataManager() {
		super(null);
	}
	/**
	 * <pre>
	 *    &lt;... type=&quot;com.ifeng.common.dm.WritableBufferedDataManager&quot;&quot;&gt;
	 *      &lt;data-manager ... 内部提供写能力的datamanager /&gt;
	 *      &lt;read-manager ... 内部提供读能力的datamanager应该是一个BufferedDataManager /&gt;
	 *    &lt;/...&gt;
	 * </pre>
	 */
	public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
		setReadManager((BufferedDataManager) configRoot.createChildObject(this, configEle,
				"read-manager", true));
		return super.config(configRoot, parent, configEle);
	}
	public Object getBufferedById(Object id) throws DataManagerException {
		return readManager.getBufferedById(id);
	}

	public BufferedDataManager getReadManager() {
		return readManager;
	}

	public void setReadManager(BufferedDataManager readManager) {
		this.readManager = readManager;
	}

	public QueryResult query(Map obj, Map params) throws DataManagerException {
		return readManager.query(obj, params);
	}

	public Object queryById(Object id) throws DataManagerException {
		return readManager.queryById(id);
	}

	public Object getById(Object id) throws DataManagerException {
		return readManager.getById(id);
	}

	public void invalidate(Object id) throws DataManagerException {
		this.readManager.invalidate(id);
	}

	public void invalidateAll() throws DataManagerException {
		this.readManager.invalidateAll();
	}

	public void set(Object id, Object value) throws DataManagerException {
		this.readManager.set(id, value);
	}

	public Object add(Object obj, Map params) throws DataManagerException {
		Object id = getManager().add(obj, params);
		readManager.set(id, obj);
		return id;
	}

	public void delete(Object obj, Map params) throws DataManagerException {
		getManager().delete(obj, params);
		readManager.invalidate(getId(obj));
	}

	public void modify(Object obj, String[] fields, Map params)
			throws DataManagerException {
		getManager().modify(obj, fields, params);
		if (fields == null) {
			readManager.set(getId(obj), obj);
		} else {
			readManager.invalidate(getId(obj));
		}
	}

	public Object deepAdd(Object obj, Map params) throws DataManagerException {
		Object id = getManager().deepAdd(obj, params);
		readManager.set(id, obj);
		return id;
	}

	public void deepModify(Object obj, String[] fields, Map params)
			throws DataManagerException {
		getManager().deepModify(obj, fields, params);
		if (fields == null) {
			readManager.set(getId(obj), obj);
		} else {
			readManager.invalidate(getId(obj));
		}
	}
}
