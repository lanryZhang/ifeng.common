package com.ifeng.common.dm;

import java.util.Map;

/**
 * <title>DataManager 数据管理接口 </title>
 * 
 * <pre>关于方法形式的统一说明：
 * obj参数：表示一个对象数据(bean)
 *      一般来说，如果对应的是O/R mapping的对象，则必须有一个id字段
 *      (id字段的名字可以配置)
 * param参数：提供的额外参数。
 * 除此之外，还有如下几个参数：见里面的PARAM_xxx的定义
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */


public interface DataManager {
	 /**
     * query时的排序字段列表
     */
    public static final String PARAM_ORDERBY_FIELDS = "dm-sort-fields";
    
    /**
     * 创建一个新的对象。
     * 如果此对象中包含的子对象为已经存在的对象，会自动更新子对象
	 *
     * @param obj 新的对象的数据
     * @param params 参数
     * @return 新创建的id
     * @throws DataManagerException 错误
     */
    public Object deepAdd(Object obj, Map params) throws DataManagerException;
    
    /**
     * 创建一个新的对象。
     * @param obj 新的对象的数据
     * @param params 参数
     * @return 新创建的id
     * @throws DataManagerException 错误
     */
    public Object add(Object obj, Map params) throws DataManagerException;

    /**
     * 删除一个对象。
     * @param obj 对象数据 (只要id字段有效即可)
     * @param params 参数
     * @throws DataManagerException 错误
     */
    public void delete(Object obj, Map params) throws DataManagerException;

    /**
     * 另一种修改对象的方法。
     * 对修改对象进行深层遍历修改，因此此方法会同时影响到对象中包含的字对象
     * @param obj 对象的新的数据
     * @param fields 所要修改的字段集合，如果为null，表示修改所有的字段
     * @param params 参数
     * @throws DataManagerException 错误
     */
    public void deepModify(Object obj, String[] fields, Map params)
            throws DataManagerException;
    
    /**
     * 另一种修改对象的方法。
     * @param obj 对象的新的数据
     * @param fields 所要修改的字段集合，如果为null，表示修改所有的字段
     * @param params 参数
     * @throws DataManagerException 错误
     */
    public void modify(Object obj, String[] fields, Map params)
            throws DataManagerException;
    
    /**
     * 当参数均为空时，查询所有的对象。
     * @param obj 对象查询模板，如果为null，则查询所有对象
     *    key为字段名称  value为QueryField类型的实例
     * @param params 参数  可以有PARAM_ORDERBY_FIELDS，指定排序字段
     * @return 符合条件的对象列表；如果没有符合条件的对象，返回空列表(非null)
     * @throws DataManagerException 错误
     */
    public QueryResult query(Map obj, Map params) throws DataManagerException;
    
    /**
     * 根据id查询对应的对象。
     * @param id 对象id属性值
     * @return 查询出来的对象；如果没有这个对象，抛出错误
     * @throws DataManagerException 错误(如果没有所需查询的对象，也抛错误)
     */
    public Object queryById(Object id) throws DataManagerException;

    /**
     * 根据id查询对应的对象。
     * @param id 对象id属性值
     * @return 查询出来的对象；如果没有这个对象，返回null
     * @throws DataManagerException 错误
     */
    public Object getById(Object id) throws DataManagerException;
    
    /**
     * 返回id字段的名称
     * @return id字段的名称
     * @throws DataManagerException 错误(如果没有所需查询的对象，也抛错误)
     */
    public String getIdFieldName() throws DataManagerException;
    
    /**
     * 返回一个对象的id
     * @param object 需要返回id的对象
     * @return 对象的id
     * @throws DataManagerException 错误(如果没有所需查询的对象，也抛错误)
     */
    public Object getId(Object object) throws DataManagerException;
}
