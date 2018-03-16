package com.ifeng.common.dm.persist.hibernate;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.ifeng.common.misc.IDGenerator;

/**
 * <title>Hibernate SessionFactory的封装</title>
 * 
 * <pre>Hibernate的SessionFactory的包装.<br>
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public final class HibernateSession {

    private static SessionFactory sessionFactory = HibernateConfig.getSessionFactory();
    
    // it is synchronized in methods
    private static Map sessionMap = new HashMap();
    
    private static IDGenerator sessionIDGenerator = new IDGenerator("HSES", false);
    private static IDGenerator objectIDGenerator = new IDGenerator("OBJ", false);
    private static Map sessionValueMap = new HashMap();

    private HibernateSession() {
        // 
    }
    
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    
    public static synchronized String open() throws HibernateException {
        Session session = sessionFactory
                .openSession();
        String sessionId = sessionIDGenerator.nextId();
        sessionMap.put(sessionId, session);
        return sessionId;
    }

    public static synchronized void close(String sessionId)
            throws HibernateException {
        Session session = (Session)sessionMap.get(sessionId);
        if (session != null) {
            session.close();
            sessionMap.remove(sessionId);
            sessionValueMap.remove(sessionId);
        } else {
            throw new RuntimeException("Can't find session: " + sessionId);
        }
    }

    public static synchronized String setSessionVariable(String sessionId,
            String valueId, Object value) {
        if (valueId == null) {
            valueId = objectIDGenerator.nextId();
        }
        Map valueMap = (Map)sessionValueMap.get(sessionId);
        if (valueMap == null) {
            valueMap = new HashMap();
            sessionValueMap.put(sessionId, valueMap);
        }
        valueMap.put(valueId, value);
        return valueId;
    }

    public static synchronized Object getSessionVariable(String sessionId,
            String valueId) {
        Map valueMap = (Map)sessionValueMap.get(sessionId);
        if (valueMap == null) {
            return null;
        }
        return valueMap.get(valueId);
    }

    public static synchronized Session getSession(String sessionId) {
        Session session = (Session)sessionMap.get(sessionId);
        if (session != null) {
            return session;
        } else {
            throw new RuntimeException("Can't find session: " + sessionId);
        }
    }

}
