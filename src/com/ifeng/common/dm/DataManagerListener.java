package com.ifeng.common.dm;

/**
 * <title>DataManagerListener </title>
 * 
 * <pre>BufferedDataManager的数据变化通知接口.
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public interface DataManagerListener {
	 /**
     * 通知一个数据已经变化，需要重新调入。具体是否调入，取决于具体的实现
     * @param id 数据对象的id。
     * @throws DataManagerException 错误
     */
	public void invalidate(Object id) throws DataManagerException;

    /**
     * 通知所有数据变化，需要重新调入。具体是否调入，取决于具体实现
     * @throws DataManagerException 错误
     */
    public void invalidateAll() throws DataManagerException;
    
    /**
     * 设置一个对象的值
     * @param id 数据对象的id
     * @param value 新的值。如果是null，则删除这个数据
     * @throws DataManagerException 错误
     */
    public void set(Object id, Object value) throws DataManagerException;
}
