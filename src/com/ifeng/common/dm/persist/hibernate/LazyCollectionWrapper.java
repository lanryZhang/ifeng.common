package com.ifeng.common.dm.persist.hibernate;

import java.util.Collection;
import java.util.Iterator;

import org.hibernate.collection.PersistentCollection;
import org.hibernate.type.CollectionType;

import com.ifeng.common.dm.persist.intf.LazyCollection;
import com.ifeng.common.dm.persist.intf.LazyCollectionHandler;

/**
 * <title>LazyCollectioWrapper </title>
 * 
 * <pre>用户保存和传递查询结果，避免数据库连接关闭以后无法查询ResultSet里的数据.<br>
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class LazyCollectionWrapper implements LazyCollection {
    private static final long serialVersionUID = 4050760507239510581L;
    PersistentCollection orgPersistentCollection;
    Collection orgCollection;
    Object owner;
    int location;
    private transient LazyCollectionHandler handler;
    boolean converted;
    CollectionType type;
    
    public LazyCollectionWrapper(CollectionType type,
            PersistentCollection org, Object owner, int location) {
        this.type = type;
        this.owner = owner;
        this.orgPersistentCollection = org;
        this.orgCollection = (Collection)org.getValue();
        this.location = location;
    }
    
    public boolean add(Object o) {
        throw new UnsupportedOperationException("I'm read only");
    }
    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException("I'm read only");
    }
    public void clear() {
        throw new UnsupportedOperationException("I'm read only");
    }
    public boolean contains(Object o) {
        read();
        return orgCollection.contains(o);
    }
    public boolean containsAll(Collection c) {
        read();
        return orgCollection.containsAll(c);
    }
    public boolean equals(Object obj) {
        read();
        return orgCollection.equals(obj);
    }
    public int hashCode() {
        read();
        return orgCollection.hashCode();
    }
    public boolean isEmpty() {
        read();
        return orgCollection.isEmpty();
    }
    public Iterator iterator() {
        read();
        return orgCollection.iterator();
    }
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("I'm read only");
    }
    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException("I'm read only");
    }
    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException("I'm read only");
    }
    public int size() {
        read();
        return orgCollection.size();
    }
    public Object[] toArray() {
        read();
        return orgCollection.toArray();
    }
    public Object[] toArray(Object[] a) {
        read();
        return orgCollection.toArray(a);
    }
    public String toString() {
        read();
        return orgCollection.toString();
    }
    private synchronized void read() {
        if (this.handler != null) {
            this.orgCollection = handler.handle(this);
            this.converted = true;
        }
    }

    public synchronized void setHandler(LazyCollectionHandler handler) {
        this.handler = handler;
    }
    
    public int getLocation() {
        return location;
    }
    public Collection getOrgCollection() {
        return orgCollection;
    }
    public PersistentCollection getOrgPersistentCollection() {
        return orgPersistentCollection;
    }
    public Object getOwner() {
        return owner;
    }
    public boolean isConverted() {
        return converted;
    }
    public CollectionType getType() {
        return type;
    }
}
