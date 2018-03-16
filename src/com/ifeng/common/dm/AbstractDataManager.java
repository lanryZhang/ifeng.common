package com.ifeng.common.dm;

import java.util.Map;

import com.ifeng.common.dm.persist.HQuery;
import com.ifeng.common.misc.Logger;


/**
 * <title>AbstractDataManager </title>
 * 
 * <pre>数据管理抽象类，在本类中扩展校验等共有能力。
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public abstract class AbstractDataManager implements DataManager {
    private static final Logger log = Logger.getLogger(AbstractDataManager.class);

    /**
     * 数据操作的对象类。所有数据操作方法doXXX将得到这个类型的参数.
     */
    private Class beanClass;

    /**
     * for config.
     */
    public AbstractDataManager() {
        // for config
    }
    
    /**
     * @param beanClass 数据操作的对象类。所有数据操作方法doXXX将得到这个类型的参数
     */
    public AbstractDataManager(Class beanClass) {
        this.beanClass = beanClass;
    }
    
    public Class getBeanClass() {
        return beanClass;
    }
    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }
    
    /**
     * 检查validation结果.
     */
    protected Object checkValidation(Object obj, Object orgObj)
            throws DataManagerException {
        if (obj instanceof DataManagerValidationError) {
            throw new DataManagerException(((DataManagerValidationError)obj)
                    .toString());
        }
        return obj != null ? obj : orgObj;
    }

  
    public Object checkValidAdd(Object obj, Map params) throws DataManagerException{
        return null;
    }
    public Object add(Object obj, Map params) throws DataManagerException {
        try {
            if (obj == null) {
                throw new DataManagerException("Exception adding a null object");
            }
            obj = checkValidation(checkValidAdd(obj, params), obj);
            obj = ensureBeanClass(obj);
            return doAdd(obj, params);
        } catch (DataManagerException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    
    public Object deepAdd(Object obj, Map params) throws DataManagerException {
        try {
            if (obj == null) {
                throw new DataManagerException("Exception adding a null object");
            }
            obj = checkValidation(checkValidAdd(obj, params), obj);
            obj = ensureBeanClass(obj);
            return doDeepAdd(obj, params);
        } catch (DataManagerException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 子类实现，真正的add.
     * @param obj 是一个beanClass类型的对象
     * @param params
     * @return 新创建对象的id
     */
    protected abstract Object doDeepAdd(Object obj, Map params) throws DataManagerException;

    /**
     * 子类实现，真正的add.
     * @param obj 是一个beanClass类型的对象
     * @param params
     * @return 新创建对象的id
     */
    protected abstract Object doAdd(Object obj, Map params) throws DataManagerException;

    public Object checkValidDelete(Object obj, Map params)
            throws DataManagerException {
        return null;
    }

    public void delete(Object obj, Map params) throws DataManagerException {
        try {
            if (obj == null) {
                throw new DataManagerException("Exception deleting a null object");
            }
            obj = checkValidation(checkValidDelete(obj, params), obj);
            obj = ensureBeanClass(obj);
            doDelete(obj, params);
        } catch (DataManagerException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 子类实现，删除一个对象.
     * @param obj 是beanClass类型的对象
     * @param params
     */
    protected abstract void doDelete(Object obj, Map params) throws DataManagerException;

    public Object checkValidModify(Map obj, Map params)
            throws DataManagerException {
        return null;
    }

    public Object checkValidModify(Object obj, String[] fields, Map params)
            throws DataManagerException {
        return null;
    }

    public void modify(Map obj, Map params) throws DataManagerException {
        try {
            if (obj == null) {
                throw new DataManagerException(
                        "Exception modifying a null object");
            }
            String[] fields = new String[obj.keySet().size()];
            obj.keySet().toArray(fields);
            Object obj1 = checkValidation(checkValidModify(obj, params), obj);
            obj1 = ensureBeanClass(obj1);
            doModify(obj1, fields, params);
        } catch (DataManagerException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    
    
    
    public void modify(Object obj, String[] fields, Map params)
            throws DataManagerException {
        try {
            if (obj == null) {
                throw new DataManagerException(
                        "Exception modifying a null object");
            }
            obj = checkValidation(checkValidModify(obj, fields, params), obj);
            obj = ensureBeanClass(obj);
            doModify(obj, fields, params);
        } catch (DataManagerException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

   
    public void deepModify(Object obj, String[] fields, Map params)
			throws DataManagerException {
		try {
			if (obj == null) {
				throw new DataManagerException(
						"Exception modifying a null object");
			}
			obj = checkValidation(checkValidModify(obj, fields, params), obj);
			obj = ensureBeanClass(obj);
			doDeepModify(obj, fields, params);
		} catch (DataManagerException e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}
    
    /**
	 * 子类实现：修改一个对象.
	 * 
	 * @param obj
	 *            beanClass类型的对象
	 * @param fields
	 *            需要修改的字段列表
	 * @param params
	 */
    protected abstract void doDeepModify(Object obj, String[] fields, Map params)
			throws DataManagerException;

	/**
	 * 子类实现：修改一个对象.
	 * 
	 * @param obj
	 *            beanClass类型的对象
	 * @param fields
	 *            需要修改的字段列表
	 * @param params
	 */
    protected abstract void doModify(Object obj, String[] fields, Map params) 
            throws DataManagerException;

    /**
     * 因为子类可能不需要实现这个方法，这里提供一个缺省的实现.
     */
    public QueryResult query(HQuery hql) throws DataManagerException {
        return null;
    }

    /**
     * 保证对象是一个支持的对象.
     * 子类可override(需要调用super)，以提供对象转换功能
     * @param obj 数据对象
     * @return bean 可支持的对象
     * @throws DataManagerException 对象无法转换到支持的类型
     */
    protected Object ensureBeanClass(Object obj) throws DataManagerException {
        if (this.beanClass.isInstance(obj)) {
            return obj;
        } else {
            throw new DataManagerException("Invalid type of class: " + obj.getClass()
                    + ". expected: " + this.beanClass);
        }
    }

    /**
     * bulk SQL-style update，留待子类实现
     * @param toSet 要修改的属性-值
     * @param condition 查询条件
     * @return 更新行数
     * @throws DataManagerException 错误
     */
    public int bulkModify(Map toSet, Map condition)
            throws DataManagerException{
    	return 0;
    }
}
