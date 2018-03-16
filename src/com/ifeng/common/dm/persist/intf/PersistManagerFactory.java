package com.ifeng.common.dm.persist.intf;

/**
 * <title>PersistManagerFactory </title>
 * 
 * <pre>
 * 持久对象PO管理Factory接口。
 * 不同的ORM可以有不同的Factory实现
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public interface PersistManagerFactory {
    
    /**
     * 创建或得到一个PersistManager实例
     * @return PersistManager
     */
    public PersistManager getInstance();
}
