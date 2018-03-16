package com.ifeng.common.dm.persist.hibernate;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.AbstractComponentType;
import org.hibernate.type.ArrayType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.MapType;
import org.hibernate.type.Type;

import com.ifeng.common.dm.persist.exception.PersistException;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.misc.Primitives;

/**
 * <title>DTO2PO转换器</title>
 * 
 * <pre>通过此类将一个DTO转换为一个PO.<br>
 * PO=persistant object持久层对象
 * DTO=Data Transfer Object数据传输对象
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class DTO2POConvertor {

    private static final Logger log = Logger.getLogger(DTO2POConvertor.class);

    /**
     * 记录已经转换过的对象，避免递规死循环
     */
    private Map convertedObjs = new IdentityHashMap();

    private Map convertedCollections = new IdentityHashMap();

    List lazyCollections = new ArrayList();

    private SessionImplementor session;

    public DTO2POConvertor(Session session) {
        this.session = (SessionImplementor) session;
    }
   
    public Collection convertCollection(CollectionType type,
            Collection collection, Object owner, int location) {
        Collection result = (Collection) this.convertedCollections
                .get(collection);
        if (result != null) {
            return result;
        }
        if (collection instanceof LazyCollectionWrapper) {
            LazyCollectionWrapper tmp = (LazyCollectionWrapper) collection;
            return (Collection) tmp.getOrgPersistentCollection();
        } else {
            CollectionPersister persister;
            try {
                persister = this.session.getFactory().getCollectionPersister(
                        type.getRole());
            } catch (MappingException e) {
                throw new RuntimeException(e);
            }
            Type elementType = persister.getElementType();
            Collection innerColl = collection; // (Collection)collection.getValue();
            if (collection instanceof Set) {
                result = new HashSet(innerColl.size());
            } else {
                result = new ArrayList(innerColl.size());
            }
            for (Iterator it = innerColl.iterator(); it.hasNext();) {
                result.add(convertValue(null, elementType, it.next(),
                                location));
            }
        }
        return result;
    }

    private Object convertArray(ArrayType type, Object array, int location) {
        Collection result = (Collection) this.convertedCollections.get(array);
        if (result != null) {
            return result;
        }
        if (array == null) {
            return null;
        }
        Type elemType = type.getElementType(this.session.getFactory());
        Class elemClass = elemType.getReturnedClass();
        int length = Array.getLength(array);
        if (log.isDebugEnabled()) {
            log.debug("array length:" + length + ", array element type:"
                    + elemType + ", array element class:" + elemClass);
        }

        Object newObj = Array.newInstance(elemClass, length);
        for (int i = 0; i < length; i++) {
            Object elemValue = Array.get(array, i);
            if (log.isDebugEnabled()) {
                log.debug("convert array element, index:" + i + ", value:"
                        + elemValue);
            }
            Array.set(newObj, i, convertValue(null, elemType, elemValue,
                    location));

        }
        return newObj;
    }

    public Map convertMap(MapType type, Map map, int location) {
        Map result = (Map) this.convertedCollections.get(map);
        if (result != null) {
            return result;
        }
        CollectionPersister persister;
        try {
            persister = this.session.getFactory().getCollectionPersister(
                    type.getRole());
        } catch (MappingException e) {
            throw new RuntimeException(e);
        }
        Type keyType = persister.getKeyType();
        Type elementType = persister.getElementType();
        // map不是Collection接口，需要特别处理
        Map innerMap = map;
        result = new HashMap();
        for (Iterator it = innerMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            result.put(convertValue(null, keyType, entry.getKey(), location),
                            convertValue(null, elementType, entry.getValue(),
                                    location));
        }
        return result;
    }

    public List convertCollectionItems(Collection collection) {
        List result = new ArrayList(collection.size());
        this.convertedCollections.put(collection, result);
        for (Iterator it = collection.iterator(); it.hasNext();) {
            result.add(convertObject2Po(it.next()));
        }
        return result;
    }

    /**
     * @param obj DTO
     * @param fields
     * @return 转换后的 po 对象
     */
    public Object convertObject2Po(Object obj, String[] fields) {// 仅供外部调用
        if (obj == null) {
            return null;
        }
        Object newObj = this.convertedObjs.get(obj);
        if (newObj != null) {
            return newObj;
        }
        if (fields == null) {
            return convertObject2Po(obj);
        } 
        try {
            newObj = getPO(obj);
            if (newObj == null) {
                newObj = obj;
            }
            this.convertedObjs.put(obj, newObj);
            EntityPersister persister = this.session.getEntityPersister(null,
                    obj);
            Type[] types = persister.getPropertyTypes();
            String[] propertyNames = persister.getPropertyNames();

            for (int i = 0; i < fields.length; i++) {
                int index = ArrayUtils.indexOf(propertyNames, fields[i]);
                Object value = persister.getPropertyValue(obj, index,
                        EntityMode.POJO); 
                Type type = types[index];
                value = convertValue(newObj, type, value, index);
                persister.setPropertyValue(newObj, index, value,
                        EntityMode.POJO);

            }
            persister.setIdentifier(newObj, persister.getIdentifier(obj,
                    EntityMode.POJO), EntityMode.POJO);
            return newObj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param obj DTO
     *            
     * @return 转换后的 po 对象
     */
    private Object convertObject2Po(Object obj) {
        if (obj == null) {
            return null;
        }
        Object newObj = this.convertedObjs.get(obj);
        if (newObj != null) {
            return newObj;
        }
        try {
            if (obj.getClass().isArray()) {
                // 如果是数组，直接返回，这主要是为了处理HQL语句类似
                // "select t.f1,t.f2 from Table t" 时的情况
                int length = Array.getLength(obj);
                newObj = Array.newInstance(Object.class, length);
                for (int i = 0; i < length; i++) {
                    Array.set(newObj, i, convertObject2Po(Array.get(obj, i)));
                }
                this.convertedObjs.put(obj, newObj);
            } else if (Primitives.isPrimitiveClass(obj.getClass())) {
                // 基本类型直接返回，这主要是为了处理HQL语句类似
                // "select t.id from Table t" 时的情况
                newObj = obj;
                this.convertedObjs.put(obj, newObj);
            } else if (obj instanceof String) {
                // 基本类型直接返回，这主要是为了处理HQL语句类似
                // "select t.id from Table t" 时的情况
                newObj = obj;
                this.convertedObjs.put(obj, newObj);
            } else if (obj instanceof Map) {
                // 处理类似的查询：
                // String hql = "select new Map(p.id,p.template.templateName)
                // from " + TemplateExpression.class.getName()
                // + " p where p.id=1";
                Map newMap = new HashMap();
                Iterator it = ((Map) obj).entrySet().iterator();
                while (it.hasNext()) {
                    Entry entry = (Entry) it.next();
                    newMap.put(convertObject2Po(entry.getKey()),
                            convertObject2Po(entry.getValue())); 
                }
                newObj = newMap;
            } else {
                newObj = getPO(obj);
                if (newObj == null) {
                    newObj = obj; // BeanUtils.cloneBean(obj);
                }
                this.convertedObjs.put(obj, newObj);
                EntityPersister persister = this.session.getEntityPersister(
                        null, obj);
                Type[] types = persister.getPropertyTypes();
                Object[] values = persister.getPropertyValues(obj,
                        EntityMode.POJO);
                convertValues(newObj, types, values);
                persister.setPropertyValues(newObj, values, EntityMode.POJO);
                persister.setIdentifier(newObj, persister.getIdentifier(obj,
                        EntityMode.POJO), EntityMode.POJO);

            }
            return newObj;
        } catch (Exception e) {
            log.error("convertObject() ERROR! obj class:"
                    + obj.getClass().getName() + ", obj.toString():"
                    + obj.toString(), e);

            throw new RuntimeException(e);
        }
    }


    private void convertValues(Object owner, Type[] types, Object[] values) {
        for (int i = 0; i < types.length; i++) {
            if (values[i] != null) {
                values[i] = convertValue(owner, types[i], values[i], i);
            }
        }
    }

    /**
     * @param owner
     * @param type
     * @param value
     * @param location
     * @return 转换后的 dto 对象
     */
    private Object convertValue(Object owner, Type type, Object value,
            int location) {
        if (type.isCollectionType()) {
            if (value instanceof Map) {
                // Map需要特殊处理
                return convertMap((MapType) type, (Map) value, location);
            } else if (value instanceof Collection) {
                return convertCollection((CollectionType) type,
                        (Collection) value, owner, location);
            } else {
                if (type instanceof ArrayType) {
                    // 如果是数组
                    return convertArray((ArrayType) type, value, location);
                } else {
                    // 没有预料到的其他collection
                    if (log.isWarnEnabled()) {
                        log.warn("WARN! NOT supported collection type:"
                                + type.toString());
                    }
                    return value;
                }
            }
        } else if (type.isEntityType()) {
            return convertObject2Po(value);
        } else if (type.isAnyType()) {
            return convertObject2Po(value);
        } else if (type.isComponentType()) {
            return convertComponent((AbstractComponentType) type, value);
        } else {
            return value;
        }
    }

    private Object convertComponent(AbstractComponentType type, Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            Object newObj = obj.getClass().newInstance();
            Type[] types = type.getSubtypes();
            Object[] values = type.getPropertyValues(obj, EntityMode.POJO);
            convertValues(newObj, types, values);
            type.setPropertyValues(newObj, values, EntityMode.POJO);
            return newObj;
        } catch (Exception e) {
            // should not occur
            throw new RuntimeException(e);
        }
    }

    /**
     * 得到一个id相同的Persist Object。如果没找到，返回null
     */
    private Object getPO(Object obj) throws PersistException {
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
}
