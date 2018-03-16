package com.ifeng.common.dm.persist;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.ifeng.common.dm.QueryResult;
import com.ifeng.common.dm.persist.exception.PersistException;
import com.ifeng.common.dm.persist.intf.LazyCollection;
import com.ifeng.common.dm.persist.intf.LazyCollectionHandler;
import com.ifeng.common.dm.persist.intf.PersistManager;
import com.ifeng.common.dm.persist.intf.PersistManagerImpl;
import com.ifeng.common.dm.persist.intf.PersistSession;
import com.ifeng.common.misc.Logger;
/**
 * <title>ThreadLocalPersistManager </title>
 * 
 * <pre>实现一个从BasePersistManager到PersistManager的适配。
 * 通过将session放到threadlocal中
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class ThreadLocalPersistManager implements PersistManager {
    private static ThreadLocal localSession = new ThreadLocal();
    private static final Logger log = Logger.getLogger(ThreadLocalPersistManager.class);
    private PersistManagerImpl persistManagerImpl;

    public ThreadLocalPersistManager(PersistManagerImpl persistManagerImpl) {
        this.persistManagerImpl = persistManagerImpl;
    }
    
    /**
     * session信息，维护session的打开次数和嵌套事务
     */
    static class SessionInfo {
        private PersistSession session;
        private boolean autoOpenedByTransaction;
        private int count;
        SessionInfo(PersistSession session) {
            this.session = session;
            this.count = 0;
        }
        public String toString() {
            return session + "(" + count + ")";
        }
    }

    public void openSession() throws PersistException {
        SessionInfo s = (SessionInfo)localSession.get();
        // Open a new Session, if this Thread has none yet
        if (s == null) {
            if (log.isDebugEnabled()) {
                log.debug("Opening new Session");
            }
            s = new SessionInfo(this.persistManagerImpl.openSession());
            localSession.set(s);
            if (log.isDebugEnabled()) {
                log.debug("Session: " + s.toString());
            }
        }
        s.count++;
        if (log.isDebugEnabled()) {
            log.debug("Increase session open count to " + s.count);
        }
    }
    public void closeSession() throws PersistException {
        SessionInfo s = (SessionInfo)localSession.get();
        if (s != null) {
            s.count--;
            if (log.isDebugEnabled()) {
                log.debug("Decrease session open count to " + s.count);
            }
            if (s.count <= 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Closing Session: " + s.toString());
                }
                this.persistManagerImpl.closeSession(s.session);
                localSession.set(null);
            }
        }
    }

    public void forceCloseSession() throws PersistException {
        SessionInfo s = (SessionInfo)localSession.get();
        if (s != null) {
            if (log.isDebugEnabled()) {
                log.debug("Force close session");
            }
            if (s.count != 1) {
                log.warn("Someone forget to close session, count is: "
                        + (s.count - 1));
            }
            this.persistManagerImpl.closeSession(s.session);
            localSession.set(null);
        }
    }
    
    /**
     * 得到线程相关的 PersistSession 对象.
     * 
     * 为能够在外部直接使用 hibernate session 进行一些比较复杂的操作（如在修改对象时级联修改深层持久化对象），
     * 将此方法的访问权限由 protected 改为 public，
     * 以便在外部得到 PersistSession 后，再通过类型强制转换一步一步得到 hibernate session。
     * 
     * @return PersistSession
     * @throws PersistException
     */
    public PersistSession getCurrentSession() throws PersistException {
        SessionInfo s = (SessionInfo)localSession.get();
        if (s == null) {
            throw new PersistException("No session opened for current thread");
        }
        return s.session;
    }

    public void beginTransaction() throws PersistException {
        if (localSession.get() == null) {
            // 如果没打开过session，这里打开一个
            openSession();
            ((SessionInfo)localSession.get()).autoOpenedByTransaction = true;
        }
        this.persistManagerImpl.beginTransaction(getCurrentSession());
    }

    public void commitTransaction() throws PersistException {
        try {
            this.persistManagerImpl.commitTransaction(getCurrentSession());
        } finally {
            SessionInfo session = (SessionInfo)localSession.get();
            if (session != null && session.autoOpenedByTransaction) {
                closeSession();
            }
        }
    }

    public Object add(Object obj) throws PersistException {
        return this.persistManagerImpl.add(getCurrentSession(), obj);
    }

    public void delete(Object obj) throws PersistException {
        this.persistManagerImpl.delete(getCurrentSession(), obj);
    }

    public void modify(Object obj, String[] fields) throws PersistException {
        this.persistManagerImpl.modify(getCurrentSession(), obj, fields);
    }

    private LazyCollectionHandler lazyCollectionHandler = new LazyCollectionHandlerImpl();
    /**
     * 在客户端处理LazyCollection，触发服务器端调入一个LazyCollection
     */
    private class LazyCollectionHandlerImpl implements LazyCollectionHandler {
        public Collection handle(LazyCollection lazyCollection) {
            try {
                // 如果没有打开过session，这里打开一个
                openSession();
                try {
                    ObjectWrapper wrapper = persistManagerImpl.loadLazyCollection(
                            getCurrentSession(), lazyCollection);
                    processWrapper(wrapper);
                    return (Collection)wrapper.getObject();
                } finally {
                    closeSession();
                }
            } catch (PersistException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }
            
    public QueryResult query(HQuery hql) throws PersistException {
        QueryResultWrapper wrapper = this.persistManagerImpl.query(getCurrentSession(), hql);
        processWrapper(wrapper);
        return wrapper.getQueryResult();
    }

    public int getResultSize(HQuery hql) throws PersistException {
        return this.persistManagerImpl.getResultSize(getCurrentSession(), hql);
    }

    public Object queryById(Object id, Class clazz) throws PersistException {
        ObjectWrapper wrapper = this.persistManagerImpl.queryById(
                getCurrentSession(), id, clazz);
        processWrapper(wrapper);
        return wrapper.getObject();
    }

    public Object getById(Object id, Class clazz) throws PersistException {
        ObjectWrapper wrapper = this.persistManagerImpl.getById(
                getCurrentSession(), id, clazz);
        processWrapper(wrapper);
        return wrapper.getObject();
    }

    /**
     * @param wrapper
     */
    void processWrapper(ObjectWrapper wrapper) {
        List lazyCollections = wrapper.getLazyCollections(); 
        if (lazyCollections != null) {
            for (Iterator it = lazyCollections.iterator(); it.hasNext();) {
                LazyCollection lazyCollection = (LazyCollection)it.next();
                lazyCollection.setHandler(this.lazyCollectionHandler);
            }
        }
    }

    public String getIdFieldName(Class clazz) throws PersistException {
        return this.persistManagerImpl.getIdFieldName(getCurrentSession(), clazz);
    }

	public Object deepAdd(Object obj) throws PersistException {
		return this.persistManagerImpl.deepAdd(getCurrentSession(), obj);
	}

	public void deepModify(Object obj, String[] fields) throws PersistException {
		this.persistManagerImpl.deepModify(getCurrentSession(), obj, fields);
	}

	public int bulkModify(String hql, List paralist)
			throws PersistException {
		return this.persistManagerImpl.bulkModify(getCurrentSession(), hql, paralist);
	}

}


