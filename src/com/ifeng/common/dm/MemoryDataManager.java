package com.ifeng.common.dm;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.dm.persist.WholeQueryResult;
import com.ifeng.common.misc.BeanTools;
import com.ifeng.common.misc.XmlLoader;

/**
 * <title>MemoryDataManager </title>
 * 
 * <pre>DataManager内存实现，可以在测试时使用。
 * 可以通过配置的方式注入数据。
 * 配置方式为:
 *  &lt;  ... type="com.ifeng.common.dm.MemoryDataManager" bean-class="bean类型"
 *       [id-field-name="id字段名"] [autoid="true|false"] &gt;
 *     &lt;data.../&gt;
 *     &lt;data.../&gt;
 *     ...
 *  &lt;/...&gt;
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */



public class MemoryDataManager extends AbstractDataManager implements Configurable {
    private static Random random = new Random();
    private List data = Collections.synchronizedList(new ArrayList());
    private String idFieldName = "id";
    private boolean autoId = true;

    /**
     * for config
     */
    public MemoryDataManager() {
        // for config
    }
    
    public MemoryDataManager(Class beanClass) {
        super(beanClass);
    }
    
    public MemoryDataManager(Class beanClass, String idFieldName, boolean autoId) {
        super(beanClass);
        this.idFieldName = idFieldName;
        this.autoId = autoId;
    }

    /**
     * 配置配置。
     * <pre>
     * &lt;  ... type="com.ifeng.common.dm.MemoryDataManager" bean-class="bean类型"
     *    [id-field-name="id字段名"] [autoid="true|false"] &gt;
     *   &lt;data.../&gt;
     *   &lt;data.../&gt;
     *   ...
     * &lt;/...&gt;
     * </pre>
     * @see com.ifeng.common.conf.Configurable#config(com.ifeng.common.conf.ConfigRoot, java.lang.Object, org.w3c.dom.Element)
     */
    public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
        setBeanClass(XmlLoader.getAttributeAsClass(configEle, "bean-class"));
        this.idFieldName = XmlLoader.getAttribute(configEle, "id-field-name", "id");
        this.autoId = XmlLoader.getAttributeAsBoolean(configEle, "autoid", true);
        this.data.addAll(configRoot.createChildObjects(this, configEle, "data",
                getBeanClass()));
        return this;
    }

    public void setIdFieldName(String idFieldName) {
        this.idFieldName = idFieldName;
    }
    
    public void setAutoId(boolean autoId) {
        this.autoId = autoId;
    }

    protected void addRawObj(Object obj) {
        this.data.add(obj);
    }

    protected Object doAdd(Object obj, Map params) throws DataManagerException {
        Object newId;
        if (this.autoId) {
            newId = new Long(random.nextLong());
            try {
				BeanTools.setProperty(obj, this.idFieldName, newId);
			} catch (Exception e) {
				throw new DataManagerException(e);
			}
        } else {
            newId = getId(obj);
        }
        data.add(obj);
        return newId;
    }

    protected void doDelete(Object obj, Map params) throws DataManagerException {
        Object idVal = getId(obj);
        for (int i = 0; i < data.size(); i++) {
            if (getId(data.get(i)).equals(idVal)) {
                data.remove(i);
                break;
            }
        }
    }

    protected void doModify(Object obj, String[] fields, Map params)
            throws DataManagerException {
        //这是一个暂时实现，并没有考虑是否要真的修改某些字段。
        doDelete(obj, params);
        doAdd(obj, params);
    }

    public QueryResult query(Map obj, Map params) throws DataManagerException {
        if (obj == null) {
            // 没有查询条件，返回所有数据
            return new WholeQueryResult(data);
        } else {
            List result = new ArrayList();
            // 遍历所有数据，找出符合条件的数据
            for (int i = 0; i < data.size(); i++) {
                Object item = data.get(i);
                boolean testOk = true;
                for (Iterator it = obj.entrySet().iterator(); it.hasNext();) {
                    Map.Entry entry = (Map.Entry)it.next();
                    String name = (String)entry.getKey();
                    QueryField qf = (QueryField)entry.getValue();
                    Object field;
                    try {
                    	field = BeanTools.getProperty(item, name);
                    } catch (Exception e) {
                        throw new DataManagerException(e);
                    }
                    // 注意：某些qf可能不支持testObject
                    if (!qf.testObject(field)) {
                        testOk = false;
                        break;
                    }
                }
                if (testOk) {
                    result.add(item);
                }
            }
            return new WholeQueryResult(result);
        }
    }

    public Object queryById(Object id) throws DataManagerException {
        Object result = getById(id);
        if (result == null) {
            throw DataManagerException.notFound();
        }
        return result;
    }

    public Object getById(Object id) throws DataManagerException {
        try {
            for (int i = 0; i < data.size(); i++) {
                if (id.equals(getId(data.get(i)))) {
                    return data.get(i);
                }
            }
            return null;
        } catch (Exception e) {
            throw new DataManagerException(e);
        }
    }

    public String getIdFieldName() throws DataManagerException {
        return this.idFieldName;
    }
    
    public Object getId(Object obj) throws DataManagerException {
    	try {
            return BeanTools.getProperty(obj, getIdFieldName());
        } catch (Exception e) {
            throw new DataManagerException(e);
        }
    }

	protected Object doDeepAdd(Object obj, Map params) throws DataManagerException {
		throw new UnsupportedOperationException("not impl.");
	}

	protected void doDeepModify(Object obj, String[] fields, Map params)
			throws DataManagerException {
		throw new UnsupportedOperationException("not impl.");
	}

}
