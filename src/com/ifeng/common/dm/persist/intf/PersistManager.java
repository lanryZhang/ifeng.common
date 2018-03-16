package com.ifeng.common.dm.persist.intf;

import java.util.List;

import com.ifeng.common.dm.QueryResult;
import com.ifeng.common.dm.persist.HQuery;
import com.ifeng.common.dm.persist.exception.PersistException;


/**
 * <title>ORM持久层管理接口类 </title>
 * 
 * <pre>ORM持久层管理接口类。
 * 注意异常处理的规则：如果在一个session中，某个事务(一般情况不要在一个session中处理多个事务)
 * 的处理中间出错，正确的处理方式有如下两个：<br/>
 * 1. 不要对这个session进行后续的操作(即让这个异常中断处理)。<br/>
 * 或<br/>
 * 2. 如果(很少见的情况)确实需要在这个时候启动另一个事务，则必须做如下操作：
 *   pm.forceCloseSession();  // 强制关闭当前的session (如果确信当前的openSession
 *     // 是最外层的，也可以调用pm.closeSession()，但还是用forceCloseSession更可靠些)
 *   pm.openSession();      // 启动另一个session
 *   try {
 *     ...
 *   finally {
 *     pm.closeSession();
 *   }
 * 这种情况下，要注意，这种forceCloseSession，是否会和外层的session发生冲突。

 * 这里没有提供rollbackTransaction方法，是因为forceCloseSession(或最外层的
 * closeSession)会自动rollback当前的transaction，而且session的错误都是不可恢复的。
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public interface PersistManager {
	/**
     * 打开一个ORM会话。内部必须实现计数功能，可以嵌套打开和关闭。
     * 只有最外层的openSession才会真正打开一个session，内层的openSession只是将计数加一。
     * @throws PersistException 错误
     */
    public void openSession() throws PersistException;
    
    /**
     * 关闭一个ORM会话。内部必须实现计数功能，可以嵌套打开和关闭。
     * 最外层的closeSession才会产生实际的动作，内层的closeSession只是将计数减一。
     * 最外层的closeSession，如果有正在进行的transaction，则强制rollback
     * @throws PersistException 错误。在close一个计数已经为0的连接时，应当忽略而不抛错。
     */
    public void closeSession() throws PersistException;
    
    /**
     * 不管打开了多少次session，强制关闭session。如果有正在进行的transaction，则强制rollback
     * @throws PersistException 错误
     */
    public void forceCloseSession() throws PersistException;
    
    /**
     * 开始一个会话的事务。
     * @throws PersistException 错误
     */
    public void beginTransaction() throws PersistException;
    
    /**
     * 提交一个会话的事务。没有提供
     * @throws PersistException 错误
     */
    public void commitTransaction() throws PersistException;
    
    /**
     * 创建一个新的PO对象
     * @param obj 创建之前为VO对象，之后为PO对象
     * @return id
     * @throws PersistException 错误
     */
    public Object add(Object obj) throws PersistException;

    /**
     * "深"创建一个新的PO对象，对子对象为PO的会修改
     * @param obj 创建之前为VO对象，之后为PO对象
     * @return id
     * @throws PersistException 错误
     */
    public Object deepAdd(Object obj) throws PersistException;
    
    /**
     * 删除一个PO对象
     * @param obj PO对象，删除之后变为VO对象
     * @throws PersistException 错误
     */
    public void delete(Object obj) throws PersistException;

    /**
     * 修改一个对象
     * @param obj PO对象
     * @param fields 所要修改的字段集合。如果为空，修改所有的字段
     * @throws PersistException 错误
     */
    public void modify(Object obj, String[] fields) throws PersistException;
    
    /**
     * "深"修改一个对象，类似deepclone
     * @param obj PO对象
     * @param fields 所要修改的字段集合。如果为空，修改所有的字段
     * @throws PersistException 错误
     */
    public void deepModify(Object obj, String[] fields) throws PersistException;
    
    /**
     * 根据hql查询对应的对象
     * @param hql 封装的HQL对象
     * @return 符合条件的对象列表，如果没有符合条件的对象，返回空列表(非null)
     * @throws PersistException 错误
     */
    public QueryResult query(HQuery hql) throws PersistException;

    /**
     * @param hql 封装的HQL对象
     * @return 符合条件的记录个数
     * @throws PersistException 错误
     */
    public int getResultSize(HQuery hql) throws PersistException;

    /**
     * 根据id查询对应的对象
     * @param clazz 类；用于多态时为父类，返回子类
     * @param id identifier
     * @return Object 符合条件的对象。如果找不到，抛ObjectNotFoundException
     * @throws PersistException 错误 (当找不到对象时抛ObjectNotFoundException)
     */
    public Object queryById(Object id, Class clazz) throws PersistException;
    
    /**
     * 根据id得到对应的对象
     * @param clazz 类；用于多态时为父类，返回子类
     * @param id identifier
     * @return Object 符合条件的对象。如果找不到，返回null
     * @throws PersistException 错误
     */
    public Object getById(Object id, Class clazz) throws PersistException;
    
    /**
     * @param clazz 数据类型类
     * @return 返回id字段的名称
     * @throws PersistException 错误
     */
    public String getIdFieldName(Class clazz) throws PersistException;
    
    /**
     * 根据hql批量处理
     * @param session 会话@link #openSession()打开的
     * @param hql 
     * @param paralist 参数
     * @return
     * @throws PersistException
     */
	public int bulkModify(String hql, List paralist)
	throws PersistException;
    
}
