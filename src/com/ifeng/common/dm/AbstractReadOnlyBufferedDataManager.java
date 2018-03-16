package com.ifeng.common.dm;

import java.util.Map;
/**
 * <title>AbstractReadOnlyBufferedDataManager </title>
 * 
 * <pre>提供公共的只读（依赖缓存的只读）DataManager。
 * 具体的数据通过修饰模式由注入的内部DataManager实现读操作。
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public abstract class AbstractReadOnlyBufferedDataManager extends AbstractDataManagerDecorator implements BufferedDataManager {
	protected AbstractReadOnlyBufferedDataManager(DataManager manager) {
        super(manager);
        // super will call setManager
    }
	public Object add(Object obj, Map params) throws DataManagerException {
		throw new UnsupportedOperationException("Read only data manager");
	}
	public void delete(Object obj, Map params) throws DataManagerException {
		throw new UnsupportedOperationException("Read only data manager");
	}
	public void modify(Object obj, String[] fields, Map params) throws DataManagerException {
		throw new UnsupportedOperationException("Read only data manager");
	}
	public Object deepAdd(Object obj, Map params) throws DataManagerException {
		throw new UnsupportedOperationException("Read only data manager");
	}
	public void deepModify(Object obj, String[] fields, Map params) throws DataManagerException {
		throw new UnsupportedOperationException("Read only data manager");
		
	}

	/**
	 * 子类实现
	 */
	public abstract Object getById(Object id) throws DataManagerException;

	public Object queryById(Object id) throws DataManagerException {
		Object result = getById(id);
        if (result == null) {
            throw DataManagerException.notFound();
        }
        return result;
	}
}