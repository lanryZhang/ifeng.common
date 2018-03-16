package com.ifeng.common.dm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.dm.persist.HQuery;
import com.ifeng.common.dm.persist.exception.PersistException;
import com.ifeng.common.dm.persist.intf.PersistManager;
import com.ifeng.common.dm.persist.intf.PersistManagerFactory;
import com.ifeng.common.misc.BeanTools;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.misc.XmlLoader;

/**
 * <title>持久化的DataManger数据管理类</title>
 * 
 * <pre>Description:
 * 1、作为DAO(Data Access Objects)实现对Hibernate等封装
 * 2、子类在增删改数据前要做预判断处理时，如名字、编号合法性检查，需重载父类方法
 * 3、子类在增删改数据时要实现自己的业务逻辑，一般需重载父类方法doXXX
 * 4、子类一般不需要重载add/delete/modify/query等方法，除非需要改变AbstractDataManager中的缺省实现
 * 5、这里实现的doAdd/doDelete/doModify/query等方法，缺省情况下，都是每个操作一个Session
 *    和一个Transaction。但由于PersistManager的Session和Transaction都是可以嵌套的，可以
 *    外加Session和Transaction的控制，来改变缺省行为。改变的方式有两种：
 *    a) 隐式(推荐使用)：使用DataManagerTransactionDecorator来包装子类对象，自动实现对每个
 *       数据访问方法的事务包装(即子类的每个操作自动被包装为一个Session和一个Transaction
 *    b) 显式(特殊情况下使用)：自己调用openSession/beginTransaction/commitTransaction/
 *       closeSession。调用的规则如下：
 *       try {
 *           openSession();
 *           beginTransaction();
 *           .....
 *           commitTransaction();
 *       [ 可选的异常处理，一般不要隐藏异常，而要原样抛出 ]
 *       } finally {
 *           closeSession();
 *       }
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class PersistDataManager extends AbstractDataManager implements Configurable {
    private static final Logger log = Logger.getLogger(PersistDataManager.class);
    private static final String ALWAYS_EQUAL = " 1=1 ";
    
    /**
     * 持久层管理对象实例，可供子类重载方法时使用
     */
    protected PersistManager persistManager;
    
    // filter params
    private String filter = ALWAYS_EQUAL;    
    private List filterParams;
    private String idFieldName;

    /**
     * used in configuration
     */
    public PersistDataManager() {
        // for config
    }
    
    public PersistDataManager(Class beanClass, PersistManager persistDM) {
        super(beanClass);
        this.persistManager = persistDM;
    }
    
    public PersistDataManager(Class beanClass, PersistManagerFactory factory) {
        super(beanClass);
        this.persistManager = factory.getInstance();
    }
    
    /**
     * <pre>
     * &lt;... type="com.ifeng.common.dm.PersistDataManager 
     *      bean-class="bean类型" [pm-factory="persist manager factory的配置名称"]
     *  /&gt;
     * persist-manager缺省使用PersistManagerFactory.ROLE的配置
     * </pre>
     */
    public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
    	
        setBeanClass(XmlLoader.getAttributeAsClass(configEle, "bean-class"));
        PersistManagerFactory factory = null; 
        String pmFactoryName = null;
        try{
    		pmFactoryName = XmlLoader.getAttribute(configEle, "pm-factory");
    		factory = (PersistManagerFactory)configRoot.getValue(pmFactoryName);
    	}catch(RuntimeException e){
    		//尝试另一种以子Element配置的格式
    		factory= (PersistManagerFactory)configRoot.createChildObject(this, configEle, "pm-factory", true);
    	}
        if (factory == null) {
            throw new ConfigException(configEle,
                    "Missing config of persistDM with config key: "
                            + pmFactoryName);
        }
        this.persistManager = factory.getInstance();
        return this;
    }
    
    /**
     * 创建一个会话。
     * <p>对于Hibernate，无需自己调用：
     * 用于查询时，openSession由query***调用；
     * 用于增删改时，openSession由beginTransaction调用</p>
     * @throws DataManagerException
     */
    protected void openSession() throws DataManagerException {
        try {
            persistManager.openSession();
        } catch (PersistException e) {
            log.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }
    
    /**
     * 关闭一个会话。
     * <p>用于查询时，未在query***方法结束后不立即关闭会话，可能存在关联对象lazy延迟加载的情况</p>
     * <p>需合理选择close时机，如查询关联对象已经被使用过了、所有增删改事务均已结束</p>
     * <p>可以参考SecurityManagerImpl、DMUIGenerator的处理</p>
     * @throws DataManagerException
     */
    protected void closeSession() throws DataManagerException {
        try {
            persistManager.closeSession();
        } catch (PersistException e) {
            log.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }
    
    /**
     * 用于增删改，开始一个会话的事务。
     * <p>每个增删改都有自己的事务</p>
     * @throws DataManagerException
     */
    protected void beginTransaction() throws DataManagerException {
        try {
            persistManager.beginTransaction();
        } catch (PersistException e) {
            log.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }
    
    /**
     * 用于增删改，结束一个会话的事务
     * @throws DataManagerException
     */
    protected void commitTransaction() throws DataManagerException {
        try {
            persistManager.commitTransaction();
        } catch (PersistException e) {
            log.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    protected Object doAdd(Object obj, Map params) throws DataManagerException {
        try {
            openSession();
            beginTransaction();
            // 调用save产生select语句获取id，insert由commit完成
            Object id = persistManager.add(obj);
            commitTransaction();
            return id;
        } catch (PersistException e) {
            log.error(e.getMessage(), e);
            throw new DataManagerException(e);
        } finally {
            closeSession();
        }
    }

    protected void doDelete(Object obj, Map params) throws DataManagerException {
        try {
            openSession();
            beginTransaction();
            persistManager.delete(obj);
            commitTransaction();
        } catch (PersistException e) {
            log.error(e.getMessage(), e);
            throw new DataManagerException(e);
        } finally {
            closeSession();
        }
    }

    protected void doModify(Object obj, String[] fields, Map params)
            throws DataManagerException {
        try {
            openSession();
            beginTransaction();
            persistManager.modify(obj, fields);
            commitTransaction();
        } catch (PersistException e) {
            log.error(e.getMessage(), e);
            throw new DataManagerException(e);
        } finally {
            closeSession();
        }
    }
   
    /**
     * 设置filter；子类实现
     */
    public synchronized void setFilter(String filter0, List filterParams0) {
        this.filter = filter0;
        this.filterParams = filterParams0;
        if (this.filter == null) {
            this.filter = ALWAYS_EQUAL;
        }
    }
    
    /**
     * 另一种接口方法
     * @param queryFilter
     */
    public synchronized void setFilter(Map queryFilter) {
        if (queryFilter == null) {
            setFilter(null, null);
        } else {
            this.filterParams = new ArrayList();
            StringBuffer sb = new StringBuffer("1=1");
            buildWhereClause(queryFilter, this.filterParams, sb);
            this.filter = sb.toString();
        }
    }
    
    /**
     * 构建一个where子句，开头可能以and开始，因此前面可能需要加上1=1
     * @param query
     * @param paraList 将parameter装入其中
     * @param sb 结果放到这里
     */
    private void buildWhereClause(Map query, List paraList, StringBuffer sb) {
        for (Iterator it = query.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry)it.next();
            String fieldName = (String)entry.getKey();
            Object value = entry.getValue();
            if (value instanceof QueryField) {
                QueryField qvalue = (QueryField)value;
                int pos = sb.length();
                if (qvalue.getQL("o." + fieldName, sb)) {
                    sb.insert(pos, " and ");
                    // add parameter values
                    qvalue.getParameters(paraList);
                }
            }
        }
    }
    
    /**
     * 构建一个modify where子句，不能用别名
     * @param query
     * @param paraList 将parameter装入其中
     * @param sb 结果放到这里
     */
    private void buildModifyWhereClause(Map query, List paraList, StringBuffer sb) {
        for (Iterator it = query.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry)it.next();
            String fieldName = (String)entry.getKey();
            Object value = entry.getValue();
            if (value instanceof QueryField) {
                QueryField qvalue = (QueryField)value;
                int pos = sb.length();
                if (qvalue.getQL(fieldName, sb)) {
                    sb.insert(pos, " and ");
                    // add parameter values
                    qvalue.getParameters(paraList);
                }
            }
        }
    }

    /**
     * 构造from object o后面的附加内容
     */
    private void buildFromClause(Map query, StringBuffer sb) {
        for (Iterator it = query.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry)it.next();
            Object value = entry.getValue();
            if (value instanceof QueryField) {
                ((QueryField)value).getFromClause("o.", sb);
            }
        }
    }

    public QueryResult query(Map obj, Map params)
            throws DataManagerException {
        //allow null parameter
        HQuery hquery = new HQuery();
        StringBuffer sb = new StringBuffer("select o from ");
        sb.append(getBeanClass().getName());
        sb.append(" o");
        if (obj != null) {
            buildFromClause(obj, sb);
        }
        sb.append(" where ");
        List paraList = new ArrayList();
        
        synchronized (this) { // avoid conflict with setFilter
            sb.append(this.filter);
            if (this.filterParams != null) {
                paraList.addAll(this.filterParams);
            }
        }
        
        if (obj != null) {
            buildWhereClause(obj, paraList, sb);
        }
        hquery.setQueryString(sb.toString());
        hquery.setParalist(paraList);
        if (params != null) {
            hquery.setOrderby((String)params.get(PARAM_ORDERBY_FIELDS));
        }
        return this.query(hquery);
    }

    public QueryResult query(HQuery hql) throws DataManagerException {
        try {
            openSession();
            if (log.isDebugEnabled()) {
                log.debug("query: " + hql.getQueryString());
            }
            return persistManager.query(hql);
        } catch (PersistException e) {
            log.error(e.getMessage(), e);
            throw new DataManagerException(e);
        } finally {
            closeSession();
        }
    }
    
    public int getResultSize(HQuery hql) throws DataManagerException {
        try {
            openSession();
            if (log.isDebugEnabled()) {
                log.debug("query: " + hql.getQueryString());
            }
            return persistManager.getResultSize(hql);
        } catch (PersistException e) {
            log.error(e.getMessage(), e);
            throw new DataManagerException(e);
        } finally {
            closeSession();
        }
    }

    public Object queryById(Object id) throws DataManagerException {
        try {
            if (id == null) {
                throw new DataManagerException("Exception querying by a null id");
            }
            openSession();
            return persistManager.queryById(id, getBeanClass());
        } catch (PersistException e) {
            log.error(e.getMessage(), e);
            throw new DataManagerException(e);
        } finally {
            closeSession();
        }
    }

    public Object getById(Object id) throws DataManagerException {
        try {
            if (id == null) {
                throw new DataManagerException("Exception querying by a null id");
            }
            openSession();
            return persistManager.getById(id, getBeanClass());
        } catch (PersistException e) {
            log.error(e.getMessage(), e);
            throw new DataManagerException(e);
        } finally {
            closeSession();
        }
    }

    /**
     * 构建一个set子句
     * @param toSet
     * @param paraList 将parameter装入其中
     * @param sb 结果放到这里
     */
    private void buildSetClause(Map toSet, List paraList, StringBuffer sb) {
        for (Iterator it = toSet.entrySet().iterator(); it.hasNext();){
            Map.Entry entry = (Map.Entry)it.next();
            String fieldName = (String)entry.getKey();
            Object value = entry.getValue();       
            paraList.add(value);
            sb.append(fieldName +"=?," );   
        }
        sb.deleteCharAt(sb.length()-1);
    }
    public synchronized String getIdFieldName() throws DataManagerException {
        if (this.idFieldName == null) {
            try {
                openSession();
                this.idFieldName = this.persistManager.getIdFieldName(getBeanClass());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new DataManagerException(e);
            } finally {
                closeSession();
            }
        }
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
		try {
            openSession();
            beginTransaction();
            // 调用save产生select语句获取id，insert由commit完成
            Object id = persistManager.deepAdd(obj);
            commitTransaction();
            return id;
        } catch (PersistException e) {
            log.error(e.getMessage(), e);
            throw new DataManagerException(e);
        } finally {
            closeSession();
        }
	}

	protected void doDeepModify(Object obj, String[] fields, Map params) throws DataManagerException {
		 try {
	            openSession();
	            beginTransaction();
	            persistManager.deepModify(obj, fields);
	            commitTransaction();
	        } catch (PersistException e) {
	            log.error(e.getMessage(), e);
	            throw new DataManagerException(e);
	        } finally {
	            closeSession();
	        }
	}
	
    public int bulkModify(Map toSet, Map condition)
	throws DataManagerException {       
        StringBuffer sb = new StringBuffer("update ");
        sb.append(getBeanClass().getName());
//        sb.append(" o ");
        List paraList = new ArrayList();
        sb.append(" set ");
        buildSetClause(toSet, paraList, sb);
             
        sb.append(" where ");
        sb.append(this.filter);      
        if (this.filterParams != null) {
            paraList.addAll(this.filterParams);
        }
        if (condition != null) {
        	buildModifyWhereClause(condition, paraList, sb);
        }
		
        try {
            openSession();  
            beginTransaction();
            if (log.isDebugEnabled()) {
                log.debug("query: " + sb);
            }
            int bulkNum = persistManager.bulkModify(sb.toString(), paraList);
            commitTransaction();
            return bulkNum;
        } catch (PersistException e) {
            log.error(e.getMessage(), e);
            throw new DataManagerException(e);
        } finally {
            closeSession();
        }
    }	
}
