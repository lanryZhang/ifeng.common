package com.ifeng.common.dm.persist;

import java.io.Serializable;
import java.util.List;

public class ObjectWrapper implements Serializable {
    private static final long serialVersionUID = 3690756206906257720L;
    private List lazyCollections;
    private Object object;
    
    public ObjectWrapper(List lazyCollections, Object object) {
        super();
        this.lazyCollections = lazyCollections;
        this.object = object;
    }
    
    
    public List getLazyCollections() {
        return lazyCollections;
    }
    public void setLazyCollections(List lazyCollections) {
        this.lazyCollections = lazyCollections;
    }
    public Object getObject() {
        return object;
    }
    public void setObject(Object object) {
        this.object = object;
    }
    
}

