package com.ifeng.common.dm.persist.hibernate;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.dm.persist.ThreadLocalPersistManager;
import com.ifeng.common.dm.persist.intf.PersistManager;
import com.ifeng.common.dm.persist.intf.PersistManagerFactory;
/**
 * <title>PersistManagerFactoryHibernateImpl </title>
 * 
 * <pre>持久对象PO管理Hibernate Factory实现。<br>
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class PersistManagerFactoryHibernateImpl
        implements PersistManagerFactory, Configurable {

    private static final PersistManagerHibernate _innerInstance 
        = new PersistManagerHibernate();
    private static final PersistManager _instance = new ThreadLocalPersistManager(
            _innerInstance);

    /**
     * 创建PersistManager实例
     * @return PersistManager
     */
    public PersistManager getInstance() {
        return _instance;
    }

    public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
        _innerInstance.config(configRoot, parent, configEle);
        return this;
    }
    
}

