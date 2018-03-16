package com.ifeng.common.dm;

import java.util.Map;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.BeanTools;

/**
 * <title>AbstractDataManagerDecorator </title>
 * 
 * <pre>
 * 提供DataManager的修饰能力,基于修饰可以扩展和复合datamanager的不同实现。
 * </pre>
 * 
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public abstract class AbstractDataManagerDecorator implements DataManager,
		Configurable {

	private DataManager manager;

	private String idFieldName;

	protected AbstractDataManagerDecorator() {
		// for config
	}

	protected AbstractDataManagerDecorator(DataManager manager) {
		setManager(manager);
	}

	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		setManager((DataManager) configRoot.createChildObject(this, configEle,
				"data-manager", true));
		return this;
	}

	public DataManager getManager() {
		return manager;
	}

	public void setManager(DataManager manager) {
		this.manager = manager;
	}

	public int hashCode() {
		return manager.hashCode();
	}

	public String toString() {
		return manager.toString();
	}

	public Object add(Object obj, Map params) throws DataManagerException {
		return manager.add(obj, params);
	}

	public Object deepAdd(Object obj, Map params) throws DataManagerException {
		return manager.deepAdd(obj, params);
	}

	public void deepModify(Object obj, String[] fields, Map params)
			throws DataManagerException {
		manager.deepModify(obj, fields, params);

	}

	public void delete(Object obj, Map params) throws DataManagerException {
		manager.delete(obj, params);
	}

	public Object getById(Object id) throws DataManagerException {
		return manager.getById(id);
	}

	public Object getId(Object object) throws DataManagerException {
		try {
			return BeanTools.getProperty(object, getIdFieldName());
		} catch (Exception e) {
			throw new DataManagerException(e);
		}
	}

	public String getIdFieldName() throws DataManagerException {
		if (this.idFieldName == null) {
			this.idFieldName = manager.getIdFieldName();
		}
		return this.idFieldName;
	}

	public void modify(Object obj, String[] fields, Map params)
			throws DataManagerException {
		manager.modify(obj, fields, params);

	}

	public QueryResult query(Map obj, Map params) throws DataManagerException {
		return manager.query(obj, params);
	}

	public Object queryById(Object id) throws DataManagerException {
		return manager.queryById(id);
	}
}
