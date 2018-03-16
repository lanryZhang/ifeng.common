package com.ifeng.common.dm;

/**
 * <title>BufferedDataManager </title>
 * 
 * <pre>带有缓存的data manager,需要一定的机制维护数据一致性。
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public interface BufferedDataManager extends DataManager,DataManagerListener{
	/**
     * 得到已经buffer的数据。
     * @param id 数据对象的id
     * @return 已经buffer的数据。如果没有，返回null
     * @throws DataManagerException 错误
     */
    public Object getBufferedById(Object id) throws DataManagerException;
}
