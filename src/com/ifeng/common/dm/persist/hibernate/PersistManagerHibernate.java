package com.ifeng.common.dm.persist.hibernate;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.EntityMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.UnresolvableObjectException;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.CollectionKey;
import org.hibernate.engine.CollectionSnapshot;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.hql.ast.QueryTranslatorImpl;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.dm.QueryResult;
import com.ifeng.common.dm.persist.HQuery;
import com.ifeng.common.dm.persist.ObjectWrapper;
import com.ifeng.common.dm.persist.QueryResultWrapper;
import com.ifeng.common.dm.persist.WholeQueryResult;
import com.ifeng.common.dm.persist.exception.ConstraintViolationException;
import com.ifeng.common.dm.persist.exception.ObjectNotFoundException;
import com.ifeng.common.dm.persist.exception.PersistException;
import com.ifeng.common.dm.persist.intf.LazyCollection;
import com.ifeng.common.dm.persist.intf.PersistManager;
import com.ifeng.common.dm.persist.intf.PersistManagerImpl;
import com.ifeng.common.dm.persist.intf.PersistSession;
import com.ifeng.common.misc.BeanTools;
import com.ifeng.common.misc.Logger;
/**
 * <title>PersistManagerHibernate </title>
 * 
 * <pre>PersistManager Hibernate实现类.<br>
 * 实现对Hibernate的封装；与具体beanClass和idField无关；完成对象数据增删改和SQL/HQL查询.
 * 
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class PersistManagerHibernate implements PersistManagerImpl,
		Configurable {

	private static final String SESSION_VARIABLE_TRANSACTION_COUNT = "TRANSACTION_COUNT";

	private static final String SESSION_VARIABLE_TRANSACTION = "TRANSACTION";

	private static final Logger log = Logger
			.getLogger(PersistManagerHibernate.class);

	/**
	 * public允许在配置中生成这个对象
	 */
	public PersistManagerHibernate() {
	}

	/**
	 * Hibernate session 的wrapper. 注意其内部字段，必须都是Serializable
	 * 而且必须是static，否则this$0会出问题
	 * 
	 * 为能够在外部直接使用 hibernate session 进行一些比较复杂的操作（如在修改对象时级联修改深层持久化对象）， 将此 class
	 * 的访问权限由 默认 改为 public ， 以便在外部通过类型强制转换得到本类的对象，然后得到 hibernate session。
	 */
	public static class SessionWrapper implements PersistSession {
		private static final long serialVersionUID = 3760562005274342196L;

		String sessionId;

		public SessionWrapper() throws HibernateException {
			this.sessionId = HibernateSession.open();
		}

		public Session getSession() {
			return HibernateSession.getSession(this.sessionId);
		}

		public void close() throws HibernateException {
			HibernateSession.close(this.sessionId);
		}

		public String toString() {
			return this.sessionId;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ifeng.common.dm.persist.PersistManager#openSession()
	 */
	public PersistSession openSession() throws PersistException {
		try {
			return new SessionWrapper();
		} catch (HibernateException e) {
			throw new PersistException("Exception opening Session: "
					+ e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ifeng.common.dm.persist.PersistManager#closeSession(com.ifeng.common.dm.persist.PersistSession)
	 */
	public void closeSession(PersistSession session) throws PersistException {
		try {
			try {
				String sessionId = ((SessionWrapper) session).sessionId;
				// 强制rollback未完成的transaction
				Transaction transaction = (Transaction) HibernateSession
						.getSessionVariable(sessionId,
								SESSION_VARIABLE_TRANSACTION);
				if (transaction != null) {
					if (log.isInfoEnabled()) {
						log
								.info("Force rollback a transaction when session closed");
					}
					// rollbackTransaction会忽略已经commit或rollback的transaction
					rollbackTransaction(transaction);
				}
			} catch (Throwable e) {
				// 忽略，让session能够正常关闭
			}
			((SessionWrapper) session).close();
		} catch (HibernateException e) {
			throw new PersistException("Exception closing Session: "
					+ e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ifeng.common.dm.persist.PersistManager#beginTransaction(com.ifeng.common.dm.persist.PersistSession)
	 */
	public void beginTransaction(PersistSession session)
			throws PersistException {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Starting transaction in this thread");
			}
			String sessionId = ((SessionWrapper) session).sessionId;
			Transaction transaction = (Transaction) HibernateSession
					.getSessionVariable(sessionId, SESSION_VARIABLE_TRANSACTION);
			Integer transactionCount;
			if (transaction != null) {
				transactionCount = (Integer) HibernateSession
						.getSessionVariable(sessionId,
								SESSION_VARIABLE_TRANSACTION_COUNT);
				transactionCount = new Integer(transactionCount.intValue() + 1);
			} else {
				transactionCount = new Integer(1);
				transaction = ((SessionWrapper) session).getSession()
						.beginTransaction();
				HibernateSession.setSessionVariable(sessionId,
						SESSION_VARIABLE_TRANSACTION, transaction);
			}
			HibernateSession.setSessionVariable(sessionId,
					SESSION_VARIABLE_TRANSACTION_COUNT, transactionCount);
		} catch (HibernateException e) {
			throw new PersistException("Exception begining Transaction: "
					+ e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ifeng.common.dm.persist.PersistManager#commitTransaction(com.ifeng.common.dm.persist.PersistSession)
	 */
	public void commitTransaction(PersistSession session)
			throws PersistException {
		String sessionId = ((SessionWrapper) session).sessionId;
		Transaction transaction = (Transaction) HibernateSession
				.getSessionVariable(sessionId, SESSION_VARIABLE_TRANSACTION);
		if (transaction == null) {
			throw new PersistException("No transaction to commit");
		}
		try {
			Integer transactionCount = (Integer) HibernateSession
					.getSessionVariable(sessionId,
							SESSION_VARIABLE_TRANSACTION_COUNT);
			if (transactionCount.intValue() <= 1) {
				// 已经达到嵌套的最上层，执行实际的commit
				if (!transaction.wasCommitted() && !transaction.wasRolledBack()) {
					if (log.isDebugEnabled()) {
						log.debug("Commiting transaction of this thread");
					}
					transaction.commit();
				}
				HibernateSession.setSessionVariable(sessionId,
						SESSION_VARIABLE_TRANSACTION, null);
			} else {
				// 没有达到最上层，只是将计数减1
				HibernateSession.setSessionVariable(sessionId,
						SESSION_VARIABLE_TRANSACTION_COUNT, new Integer(
								transactionCount.intValue() - 1));
			}
		} catch (HibernateException e) {
			try {
				rollbackTransaction(transaction);
			} finally {
				HibernateSession.setSessionVariable(sessionId,
						SESSION_VARIABLE_TRANSACTION, null);
			}
			if (e instanceof org.hibernate.exception.ConstraintViolationException) {
				// 转换异常
				throw new ConstraintViolationException("Exception commiting: "
						+ e.getMessage(), e);
			}
			throw new PersistException(
					"Exception commiting: " + e.getMessage(), e);
		}
	}

	private void rollbackTransaction(Transaction transaction)
			throws PersistException {
		try {
			if (transaction != null && !transaction.wasCommitted()
					&& !transaction.wasRolledBack()) {
				transaction.rollback();
			}
		} catch (HibernateException e) {
			throw new PersistException("Exception rollbacking: "
					+ e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ifeng.common.dm.persist.PersistManager#add(com.ifeng.common.dm.persist.PersistSession,
	 *      java.lang.Object)
	 */
	public Object add(PersistSession session, Object obj)
			throws PersistException {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Adding a object of " + obj.getClass().getName()
						+ " in Session " + session);
			}
			return ((SessionWrapper) session).getSession().save(obj);
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			throw new ConstraintViolationException(e);
		} catch (HibernateException e) {
			throw new PersistException("Exception adding object: "
					+ e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ifeng.common.dm.persist.PersistManager#delete(com.ifeng.common.dm.persist.PersistSession,
	 *      java.lang.Object)
	 */
	public void delete(PersistSession session, Object obj)
			throws PersistException {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Deleting a object of " + obj.getClass().getName()
						+ " in Session " + session);
			}
			Session innerSession = ((SessionWrapper) session).getSession();
			Object old = getPO(innerSession, obj);
			if (old == null) {
				throw new ObjectNotFoundException(
						"messages.OBJECT_HASBEENDELETED");
			}
			innerSession.delete(old);
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			throw new ConstraintViolationException(e);
		} catch (HibernateException e) {
			throw new PersistException("Exception deleting object: "
					+ e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ifeng.common.dm.persist.PersistManager#modify(com.ifeng.common.dm.persist.PersistSession,
	 *      java.lang.Object, java.lang.String[])
	 */
	public void modify(PersistSession session, Object obj, String[] fields)
			throws PersistException {
		try {
			Session innerSession = ((SessionWrapper) session).getSession();
			if (log.isDebugEnabled()) {
				log.debug("Modifying a object of " + obj.getClass().getName()
						+ " in Session " + session);
			}

			Object po = getPO(innerSession, obj);
			if (po != null) {
				copyFieldsToPO(innerSession, po, obj, fields);
				// 不用特殊的操作，对PO进行了修改，就相当于对持久状态进行了修改
			} else {
				log.warn("modify: Original object not found (add it): " + obj);
				((SessionWrapper) session).getSession().save(obj);
			}
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			throw new ConstraintViolationException(e);
		} catch (PersistException e) {
			throw e;
		} catch (Exception e) {
			throw new PersistException("Exception modifying object: "
					+ e.getMessage(), e);
		}
	}

	/**
	 * 使用 BeanTools 进行属性的简单复制，不能适应 map 中映射的属性没有对应私有变量的情况，
	 * 
	 * @param session
	 * @param po
	 * @param obj
	 * @param fields
	 * @throws Exception
	 */
	private void copyFieldsToPO(Session session, Object po, Object obj,
			String[] fields) throws Exception {
		if (fields == null) {
			EntityPersister persister = ((SessionImplementor) session)
					.getEntityPersister(null, po);
			fields = persister.getPropertyNames();
		}
		for (int i = 0; i < fields.length; i++) {
			Object value = BeanTools.getProperty(obj, fields[i]);
			BeanTools.setProperty(po, fields[i], value);
		}
	}

	/**
	 * 得到一个id相同的Persist Object。如果没找到，返回null
	 */
	private Object getPO(Session session, Object obj) throws PersistException {
		try {
			Class objClass = obj.getClass();
			Serializable id = HibernateSession.getSessionFactory()
					.getClassMetadata(objClass).getIdentifier(obj,
							EntityMode.POJO);
			if (id == null) {
				return null;
			}
			return session.get(objClass, id);
		} catch (HibernateException e) {
			throw new PersistException("Exception loadPO: " + e.getMessage(), e);
		}
	}

	public QueryResultWrapper query(PersistSession session, HQuery hql)
			throws PersistException {
		try {
			// 生成HQL字符串
			StringBuffer queryStr = new StringBuffer(hql.getQueryString());
			if (hql.getOrderby() != null) {
				queryStr.append(" ").append(hql.getOrderby());
			}
			if (hql.getGroupby() != null) {
				queryStr.append(" ").append(hql.getGroupby());
			}
			Session innerSession = ((SessionWrapper) session).getSession();
			Query query = innerSession.createQuery(queryStr.toString());
			if (hql.getParalist() != null) {
				List list = hql.getParalist();
				for (int i = 0; i < list.size(); i++) {
					query.setParameter(i, list.get(i));
				}
			}
			if (hql.getPageStartNo() != 0) {
				int pageno = hql.getPageStartNo();
				int perpagesize = hql.getPerPageSize();
				query.setFirstResult((pageno - 1) * perpagesize);
				query.setMaxResults(perpagesize);
			}
			if (log.isDebugEnabled()) {
				log.debug("Querying objects by HQL in Session " + session);
			}
			List qlist = query.list();
			// 转换查询结果
			PersistentObjectConvertor convertor = new PersistentObjectConvertor(
					innerSession);
			QueryResult queryResult = new WholeQueryResult(convertor
					.convertCollectionItems(qlist));
			return new QueryResultWrapper(convertor.lazyCollections,
					queryResult);
		} catch (HibernateException e) {
			throw new PersistException("Exception querying object: "
					+ e.getMessage(), e);
		}
	}

	/**
	 * 将一个“普通”的select查询语句转换成count查询语句。
	 * <p>
	 * 比如： 将 &quot;select p from objP p where p.f=1&quot;<br>
	 * 或 &quot;select p.name from objP p wherr p.f=1&quot;<br>
	 * 转换为以下语句--&gt;<br>
	 * &quot;select count(*) from objP p where p.f=1&quot;<br>
	 * <br>
	 * 将 &quot;select p,q from objP p, objQ q wherr p.f=q.f&quot;<br>
	 * 或 &quot;select p.name,q.name from objP p, objQ q wherr p.f=q.f&quot;<br>
	 * 转换为以下语句--&gt;<br>
	 * &quot;select count(*) from objP p, objQ q wherr p.f=q.f&quot;<br>
	 * 
	 * @param queryStr
	 *            一个select语句
	 * @return 新的select count(*)查询语句
	 */
	private String convertToCountQueryString(String queryStr)
			throws PersistException {
		QueryTranslatorImpl queryTranslator = new QueryTranslatorImpl(
				queryStr,
				Collections.EMPTY_MAP,
				(org.hibernate.engine.SessionFactoryImplementor) HibernateSession
						.getSessionFactory());

		queryTranslator.compile(Collections.EMPTY_MAP, false);

		return "select count(*) from (" + queryTranslator.getSQLString()
				+ ") rwxcommonxtempt";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ifeng.common.dm.persist.PersistManager#getResultSize(com.ifeng.common.dm.persist.PersistSession,
	 *      com.ifeng.common.dm.persist.HQuery)
	 */
	public int getResultSize(PersistSession session, HQuery hql)
			throws PersistException {
		StringBuffer queryStr = new StringBuffer(hql.getQueryString());
		String newQueryStr = convertToCountQueryString(queryStr.toString());
		if (log.isDebugEnabled()) {
			log.debug("count query string:" + newQueryStr);
		}

		try {
			Session innerSession = ((SessionWrapper) session).getSession();
			Query countQuery = innerSession.createSQLQuery(newQueryStr)
					.addScalar("count(*)", Hibernate.INTEGER);
			if (hql.getParalist() != null) {
				List list = hql.getParalist();
				for (int i = 0; i < list.size(); i++) {
					countQuery.setParameter(i, list.get(i));
				}
			}
			List list = countQuery.list();
			return Integer.valueOf(list.get(0).toString()).intValue();
		} catch (HibernateException e) {
			throw new PersistException("Exception querying result size: "
					+ e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ifeng.common.dm.persist.PersistManager#queryById(com.ifeng.common.dm.persist.PersistSession,
	 *      java.lang.Object, java.lang.Class)
	 */
	public ObjectWrapper queryById(PersistSession session, Object id,
			Class clazz) throws PersistException {
		if (log.isDebugEnabled()) {
			log.debug("Querying a object of " + clazz.getName() + " with id "
					+ id + " in Session " + session);
		}
		Session innerSession = ((SessionWrapper) session).getSession();
		Object result = null;
		try {
			result = innerSession.load(clazz, (Serializable) id);
			if (log.isDebugEnabled()) {
				log.debug("Querying a object of " + clazz.getName() + " with id "
						+ id + " in Session " + session + " class name :" +result.getClass().getName());
			}
		} catch (UnresolvableObjectException e) {
			throw new ObjectNotFoundException("messages.OBJECT_HASBEENDELETED");
		} catch (HibernateException e) {
			throw new PersistException("Exception querying object by id: "
					+ e.getMessage(), e);
		}
		PersistentObjectConvertor convertor = new PersistentObjectConvertor(
				innerSession);
		result = convertor.convertObject(result);
		return new ObjectWrapper(convertor.lazyCollections, result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ifeng.common.dm.persist.PersistManager#getById(com.ifeng.common.dm.persist.PersistSession,
	 *      java.lang.Object, java.lang.Class)
	 */
	public ObjectWrapper getById(PersistSession session, Object id, Class clazz)
			throws PersistException {
		if (log.isDebugEnabled()) {
			log.debug("Getting a object of " + clazz.getName() + " with id "
					+ id + " in Session " + session);
		}
		Session innerSession = ((SessionWrapper) session).getSession();
		Object result = null;
		try {
			result = innerSession.get(clazz, (Serializable) id);
		} catch (HibernateException e) {
			throw new PersistException("Exception getting object by id: "
					+ e.getMessage(), e);
		}
		PersistentObjectConvertor convertor = new PersistentObjectConvertor(
				innerSession);
		result = convertor.convertObject(result);
		return new ObjectWrapper(convertor.lazyCollections, result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ifeng.common.conf.Configurable#config(com.ifeng.common.conf.ConfigRoot,
	 *      java.lang.Object, org.w3c.dom.Element)
	 */
	public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
		HibernateConfig.config(configRoot, configEle);
		return this;
	}
	public ObjectWrapper loadLazyCollection(PersistSession session,
            LazyCollection collection) throws PersistException {
        SessionImplementor innerSession = (SessionImplementor)((SessionWrapper)session)
                .getSession();
        if (log.isDebugEnabled()) {
            log.debug("Load a lazy collection in Session " + session);
        }
        try {
        	LazyCollectionWrapper collWrapper = (LazyCollectionWrapper)collection;
            if (collWrapper.isConverted()) {
                log.warn("the collection has already loaded");
                return new ObjectWrapper(Collections.EMPTY_LIST, collWrapper
                        .getOrgCollection());
            }
            PersistentCollection orgPersistColl = collWrapper
                    .getOrgPersistentCollection();
            Object poOwner = getPO(innerSession, collWrapper.getOwner());
            CollectionPersister collPersister = innerSession.getFactory()
                    .getCollectionPersister(collWrapper.getType().getRole());
            CollectionSnapshot snapshot = orgPersistColl
                    .getCollectionSnapshot();
            CollectionKey collKey = new CollectionKey(collPersister, snapshot
                    .getKey(), EntityMode.POJO);
            orgPersistColl = innerSession.getPersistenceContext()
                    .getCollection(collKey);
            innerSession.initializeCollection(orgPersistColl, false);

            EntityPersister persister = innerSession.getEntityPersister(null,
                    poOwner);
            persister.setPropertyValue(poOwner, collWrapper.getLocation(),
                    orgPersistColl, EntityMode.POJO);
            PersistentObjectConvertor convertor = new PersistentObjectConvertor(
                    innerSession);
            Collection result = convertor.convertCollection(collWrapper
                    .getType(), orgPersistColl, null, 0);
            // 这里脱离了ODMGCollection
            return new ObjectWrapper(convertor.lazyCollections, result);
        } catch (HibernateException e) {
            throw new PersistException(
                    "Exception while load lazy collection: ", e);
        }
    }
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ifeng.common.dm.persist.PersistManager#getIdFieldName(com.ifeng.common.dm.persist.PersistSession,
	 *      java.lang.Class)
	 */
	public String getIdFieldName(PersistSession session, Class clazz)
			throws PersistException {
		try {
			SessionFactory sf = HibernateSession.getSessionFactory();
			return sf.getClassMetadata(clazz).getIdentifierPropertyName();
		} catch (HibernateException e) {
			throw new PersistException("Exception while getIdFieldName: ", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ifeng.common.dm.persist.PersistManager#deepAdd(com.ifeng.common.dm.persist.PersistSession,
	 *      java.lang.Object)
	 */
	public Object deepAdd(PersistSession session, Object obj)
			throws PersistException {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Deep adding a object of " + obj.getClass().getName()
						+ " in Session " + session);
			}
			Session innerSession = ((SessionWrapper) session).getSession();
			DTO2POConvertor convertor = new DTO2POConvertor(innerSession);
			obj = convertor.convertObject2Po(obj, null);
			return ((SessionWrapper) session).getSession().save(obj);
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			throw new ConstraintViolationException(e);
		} catch (HibernateException e) {
			throw new PersistException("Exception deep adding object: "
					+ e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ifeng.common.dm.persist.PersistManager#deepModify(com.ifeng.common.dm.persist.PersistSession,
	 *      java.lang.Object, java.lang.String[])
	 */
	public void deepModify(PersistSession session, Object obj, String[] fields)
			throws PersistException {
		try {
			Session innerSession = ((SessionWrapper) session).getSession();
			if (log.isDebugEnabled()) {
				log.debug("Deep modifying a object of "
						+ obj.getClass().getName() + " in Session " + session);
			}

			Object po = getPO(innerSession, obj);
			if (po != null) {
				// 转换查询结果
				DTO2POConvertor convertor = new DTO2POConvertor(innerSession);
				convertor.convertObject2Po(obj, fields);
			} else {
				DTO2POConvertor convertor = new DTO2POConvertor(innerSession);
				obj = convertor.convertObject2Po(obj, fields);
				log
						.warn("deep modify: Original object not found (deep add it): "
								+ obj);
				((SessionWrapper) session).getSession().save(obj);
			}
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			throw new ConstraintViolationException(e);
		} catch (PersistException e) {
			throw e;
		} catch (Exception e) {
			throw new PersistException("Exception deep modifying object: "
					+ e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ifeng.common.dm.persist.PersistManager#bulkModify(com.ifeng.common.dm.persist.PersistSession,
	 *      java.lang.String, java.util.List)
	 */
	public int bulkModify(PersistSession session, String hql, List paralist)
			throws PersistException {
		try {
			Session innerSession = ((SessionWrapper) session).getSession();
			Query query = innerSession.createQuery(hql);
			if (paralist != null) {
				for (int i = 0; i < paralist.size(); i++) {
					query.setParameter(i, paralist.get(i));
				}
			}
			if (log.isDebugEnabled()) {
				log.debug("bulk modify objects in Session " + session);
			}
			return query.executeUpdate();
		} catch (HibernateException e) {
			throw new PersistException("Exception bulk update object: "
					+ e.getMessage(), e);
		}
	}
}
