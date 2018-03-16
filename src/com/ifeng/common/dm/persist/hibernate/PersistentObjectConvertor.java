package com.ifeng.common.dm.persist.hibernate;

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

import org.hibernate.EntityMode;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.collection.PersistentMap;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.AbstractComponentType;
import org.hibernate.type.ArrayType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.MapType;
import org.hibernate.type.Type;

import com.ifeng.common.misc.Logger;
import com.ifeng.common.misc.Primitives;

/**
 * <title>PersistentObjectConvertor </title>
 * 
 * <pre>将一个PO包装为可以在网络上传输的对象. 主要是对PersistentCollection进行转换.<br>
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class PersistentObjectConvertor {

	private static final Logger log = Logger
			.getLogger(PersistentObjectConvertor.class);

	/**
	 * 记录已经转换过的对象，避免递规死循环
	 */
	private Map convertedObjs = new IdentityHashMap();

	private Map convertedCollections = new IdentityHashMap();

	public List lazyCollections = new ArrayList();

	private SessionImplementor session;

	public PersistentObjectConvertor(Session session) {
		this.session = (SessionImplementor) session;
	}

	public Collection convertCollection(CollectionType type,
			PersistentCollection collection, Object owner, int location) {
		Collection result = (Collection) this.convertedCollections
				.get(collection);
		if (result != null) {
			return result;
		}
		if (!collection.wasInitialized()) {
			result = new LazyCollectionWrapper(type, collection, owner,
					location);
			this.convertedCollections.put(collection, result);
			this.lazyCollections.add(result);
		} else {
			CollectionPersister persister;
			try {
				persister = this.session.getFactory().getCollectionPersister(
						type.getRole());
			} catch (MappingException e) {
				throw new RuntimeException(e);
			}
			Type elementType = persister.getElementType();
			Collection innerColl = (Collection) collection.getValue();
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

	public Map convertMap(MapType type, PersistentMap map, int location) {
		Map result = (Map) this.convertedCollections.get(map);
		if (result != null) {
			return result;
		}
		if (!map.wasInitialized()) {
			throw new ClassCastException("Don't support lazy map");
		} else {
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
			Map innerMap = (Map) map.getValue();
			result = new HashMap();
			for (Iterator it = innerMap.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				result.put(
						convertValue(null, keyType, entry.getKey(), location),
						convertValue(null, elementType, entry.getValue(),
								location));
			}
		}
		return result;
	}

	public List convertCollectionItems(Collection collection) {
		List result = new ArrayList(collection.size());
		this.convertedCollections.put(collection, result);
		for (Iterator it = collection.iterator(); it.hasNext();) {
			result.add(convertObject(it.next()));
		}
		return result;
	}

	/**
	 * @param obj
	 * @return 转换后的 dto 对象
	 */
	public Object convertObject(Object obj) {
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
					Array.set(newObj, i, convertObject(Array.get(obj, i)));
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
					newMap.put(entry.getKey(), entry.getValue());
				}
				newObj = newMap;
			} else {
				newObj = obj.getClass().newInstance();
				this.convertedObjs.put(obj, newObj);
				EntityPersister persister = this.session.getEntityPersister(
						null, obj);
				Type[] types = persister.getPropertyTypes();
				Object[] values = persister.getPropertyValues(obj,
						EntityMode.POJO);
				convertValues(newObj, types, values);
				persister.setIdentifier(newObj, persister.getIdentifier(obj,
						EntityMode.POJO), EntityMode.POJO);
				persister.setPropertyValues(newObj, values, EntityMode.POJO);
			}
			return newObj;
		} catch (Exception e) {
			// should not occur
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
			if (value instanceof PersistentMap) {
				// Map需要特殊处理
				return convertMap((MapType) type, (PersistentMap) value,
						location);
			} else if (value instanceof PersistentCollection) {
				return convertCollection((CollectionType) type,
						(PersistentCollection) value, owner, location);
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
			return convertObject(value);
		} else if (type.isAnyType()) {
			return convertObject(value);
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

}
