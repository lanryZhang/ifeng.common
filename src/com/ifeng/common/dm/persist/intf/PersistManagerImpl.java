package com.ifeng.common.dm.persist.intf;

import java.util.List;

import com.ifeng.common.dm.persist.HQuery;
import com.ifeng.common.dm.persist.ObjectWrapper;
import com.ifeng.common.dm.persist.QueryResultWrapper;
import com.ifeng.common.dm.persist.exception.PersistException;

public interface PersistManagerImpl {
	/**
     * 打开一个ORM会话。
     * @return 可使用的session。使用完后必须关闭 @see #closeSession()
     * @throws PersistException 错误
     */
    public PersistSession openSession() throws PersistException;
    
    /**
     * 关闭一个ORM会话
     * @param session 会话@link #openSession()打开的
     * @throws PersistException 错误
     */
    public void closeSession(PersistSession session) throws PersistException;
    
    /**
     * 开始一个会话的事务
     * @param session 会话@link #openSession()打开的
     * @throws PersistException 错误
     */
    public void beginTransaction(PersistSession session) throws PersistException;
    
    /**
     * 提交一个会话的事务
     * @param session 会话@link #openSession()打开的
     * @throws PersistException 错误
     */
    public void commitTransaction(PersistSession session) throws PersistException;

    /**
     * 创建一个新的对象
     * @param session 会话@link #openSession()打开的
     * @param obj 创建之前为VO对象
     * @return id
     * @throws PersistException 错误
     */
    public Object add(PersistSession session, Object obj) throws PersistException;

    /**
     * "深"创建一个新的对象
     * @param session 会话@link #openSession()打开的
     * @param obj 创建之前为VO对象
     * @return id
     * @throws PersistException 错误
     */
    public Object deepAdd(PersistSession session, Object obj) throws PersistException;

    /**
     * 删除一个对象
     * @param session 会话@link #openSession()打开的
     * @param obj 对象
     * @throws PersistException 错误
     */
    public void delete(PersistSession session, Object obj) throws PersistException;

    /**
     * 修改一个对象
     * @param session 会话@link #openSession()打开的
     * @param obj 对象
     * @param fields 所要修改的字段集合。如果为空，修改所有的字段
     * @throws PersistException 错误
     */
    public void modify(PersistSession session, Object obj, String[] fields)
            throws PersistException;
    
    /**
     * "深"修改一个对象，类似deepClone
     * @param session 会话@link #openSession()打开的
     * @param obj 对象
     * @param fields 所要修改的字段集合。如果为空，修改所有的字段
     * @throws PersistException 错误
     */
    public void deepModify(PersistSession session, Object obj, String[] fields)
            throws PersistException;

    /**
     * 根据hql查询对应的对象
     * @param session 会话@link #openSession()打开的
     * @param hql 封装的HQL对象
     * @return 符合条件的对象列表，如果没有符合条件的对象，返回空列表(非null)
     * @throws PersistException 错误
     */
    public QueryResultWrapper query(PersistSession session, HQuery hql)
            throws PersistException;

    /**
     * @param hql 封装的HQL对象
     * @return 符合条件的记录（对象）数
     * @throws PersistException 错误
     */
    public int getResultSize(PersistSession session, HQuery hql) throws PersistException;

    /**
     * 根据id查询对应的对象。如果没找到则抛错ObjectNotFoundException
     * @param session 会话@link #openSession()打开的
     * @param clazz 类；用于多态时为父类，返回子类
     * @param id identifier
     * @return Object 符合条件的对象。
     * @throws PersistException 错误 (没找到抛ObjectNotFoundException)
     */
    public ObjectWrapper queryById(PersistSession session, Object id,
            Class clazz) throws PersistException;

    /**
     * 根据id查询对应的对象
     * @param session 会话@link #openSession()打开的
     * @param clazz 类；用于多态时为父类，返回子类
     * @param id identifier
     * @return Object 符合条件的对象，用ObjectWrapper包装。如果没找到，
     *          返回包装了null对象的ObjectWrapper
     * @throws PersistException 错误
     */
    public ObjectWrapper getById(PersistSession session, Object id,
            Class clazz) throws PersistException;

    /**
     * 调入lazy collection
     * @param session 会话@link #openSession()打开的
     * @param collection 一个LazyCollection
     * @return 新调入的collection，用ObjectWrapper包装
     * @throws PersistException 错误
     */
    public ObjectWrapper loadLazyCollection(PersistSession session,
            LazyCollection collection) throws PersistException;
    
    /**
     * @param session 会话@link #openSession()打开的
     * @param clazz 数据类型类
     * @return id字段的名称
     * @throws PersistException 错误
     */
    public String getIdFieldName(PersistSession session, Class clazz)
            throws PersistException;
    
   
    /**
     * 根据hql批量处理
     * @param session 会话@link #openSession()打开的
     * @param hql 
     * @param paralist 参数
     * @return
     * @throws PersistException
     */
	public int bulkModify(PersistSession session, String hql, List paralist)
	throws PersistException;
}
