package com.ifeng.common.dm.persist.intf;

import java.util.Collection;


public interface LazyCollectionHandler {
	 /**
     * 处理一个LazyCollection，返回一个可使用的Collection
     * @param lazyCollection 一个lazyCollection
     * @return 一个可是用的Collection
     */
    public Collection handle(LazyCollection lazyCollection);
}
