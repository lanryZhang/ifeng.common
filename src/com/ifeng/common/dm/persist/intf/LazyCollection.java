package com.ifeng.common.dm.persist.intf;

import java.io.Serializable;
import java.util.Collection;


public interface LazyCollection extends Collection, Serializable {
	 /**
     * 设置处理LazyCollection的handler
     * @param handler 处理LazyCollection的Handler
     */
    public void setHandler(LazyCollectionHandler handler);
}
