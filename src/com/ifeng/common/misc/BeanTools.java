package com.ifeng.common.misc;

import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.MappedPropertyDescriptor;
import org.apache.commons.beanutils.PropertyUtils;

/**
 * <title>BeanTools </title>
 * 
 * <pre>类似commons-beanutils的BeanUtils的工具类。 实现了一些扩展策略。 
 * 这个类不要有过多的依赖关系.
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public final class BeanTools {

	private BeanTools() {
		// utility class
	}

	/**
	 * 用于在property access之前或之后做一些事情
	 */
	public interface PropertyInterceptor {
		public void beforeGet(Object object, String name, Object key);

		public void afterGet(Object object, String name, Object key,
				Object value);
	}

	/**
	 * 从apache jarkata的BeanUtils修改而来。为了支持DynaBean与普通Bean的拷贝。
	 * 解决的问题主要是嵌套对象的情况。需要拷贝合适类型的对象。<br>
	 * 具体规则：<br>
	 * 如果orig的某个属性类型为DynaBean，则假定dest中存在一个同名的Bean属性，将这个
	 * DynaBean类型的属性的所有子属性赋给dest中同名的属性。 其它情况下，均执行普通的拷贝(没有deep) 不支持Map
	 * 
	 * @see PropertyUtils#copyProperties(java.lang.Object, java.lang.Object)
	 */
	public static void copyBeanProperties(Object dest, Object orig)
			throws IllegalAccessException, InvocationTargetException {

		// Validate existence of the specified beans
		if (dest == null) {
			throw new IllegalArgumentException("No destination bean specified");
		}
		if (orig == null) {
			throw new IllegalArgumentException("No origin bean specified");
		}

		// Copy the properties, converting as necessary
		if (orig instanceof DynaBean) {
			DynaProperty[] origDescriptors = ((DynaBean) orig).getDynaClass()
					.getDynaProperties();
			for (int i = 0; i < origDescriptors.length; i++) {
				String name = origDescriptors[i].getName();
				if (PropertyUtils.isWriteable(dest, name)) {
					Object value = ((DynaBean) orig).get(name);
					copyBeanProperty(dest, name, value);
				}
			}
		} else /* if (orig is a standard JavaBean) */{
			PropertyDescriptor[] origDescriptors = PropertyUtils
					.getPropertyDescriptors(orig);
			for (int i = 0; i < origDescriptors.length; i++) {
				String name = origDescriptors[i].getName();
				if ("class".equals(name)) {
					continue; // No point in trying to set an object's class
				}
				if (PropertyUtils.isReadable(orig, name)
						&& PropertyUtils.isWriteable(dest, name)) {
					try {
						Object value = PropertyUtils.getSimpleProperty(orig,
								name);
						copyBeanProperty(dest, name, value);
					} catch (NoSuchMethodException e) {
						// Should not happen
					}
				}
			}
		}

	}

	/**
	 * 拷贝Bean的属性。解决DynaBean嵌套对象属性的拷贝问题
	 * 
	 * @see #copyBeanProperties(Object, Object)
	 */
	public static void copyBeanProperty(Object bean, String name, Object value)
			throws IllegalAccessException, InvocationTargetException {

		try {
			if (value instanceof DynaBean) {
				Object targetProp = PropertyUtils.getSimpleProperty(bean, name);
				if (targetProp == null) {
					// 如果target中当前这个属性为空，则意味着：
					// a) bean可能是一个LaxDynaBean(或者其它可能的DynaBean)，将value直接赋给它
					// b) bean可能是一个普通bean，在这种情况下，value直接赋给它可能是对的，也可能
					// 会产生错误(类型不匹配)。应当通过恰当的程序，避免这种错误
					PropertyUtils.setSimpleProperty(bean, name, value);
				} else {
					// 拷贝子属性
					copyBeanProperties(targetProp, value);
				}
			} else {
				// 直接拷贝属性
				PropertyUtils.setSimpleProperty(bean, name, value);
			}
		} catch (NoSuchMethodException e) {
			throw new InvocationTargetException(e, "Can't set property " + name);
		}
	}

	/**
	 * 得到一个bean的某个property的类型
	 * 
	 * @param target
	 *            bean (可以是DynaBean)
	 * @param propName
	 *            属性名称
	 * @return 如果没有或出错，返回null
	 */
	public static Class getBeanPropertyClass(Object target, String propName) {
		// Calculate the target property type
		if (target instanceof DynaBean) {
			DynaClass dynaClass = ((DynaBean) target).getDynaClass();
			DynaProperty dynaProperty = dynaClass.getDynaProperty(propName);
			if (dynaProperty == null) {
				return null; // Skip this property setter
			}
			return dynaProperty.getType();
		} else {
			PropertyDescriptor descriptor = null;
			try {
				descriptor = PropertyUtils.getPropertyDescriptor(target,
						propName);
				if (descriptor == null) {
					return null; // Skip this property setter
				}
			} catch (Exception e) {
				return null; // Skip this property setter
			}
			return descriptor.getPropertyType();
		}
	}

	/**
	 * 判断一个属性是否是indexed属性。bean必须是一个标准Java bean
	 * 
	 * @see PropertyUtils#getPropertyDescriptor(java.lang.Object,
	 *      java.lang.String)
	 */
	public static boolean isIndexedProperty(Object bean, String name)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		return PropertyUtils.getPropertyDescriptor(bean, name) instanceof IndexedPropertyDescriptor;
	}

	/**
	 * 判断一个属性是否是mapped属性。bean必须是一个标准Java bean
	 * 
	 * @see PropertyUtils#getPropertyDescriptor(java.lang.Object,
	 *      java.lang.String)
	 */
	public static boolean isMappedProperty(Object bean, String name)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		return PropertyUtils.getPropertyDescriptor(bean, name) instanceof MappedPropertyDescriptor;
	}

	/**
	 * 找到一串字符中，在字符串中出现最左边的位置。如果一个都没找到，返回-1
	 */
	private static int minIndexOf(String name, char[] chars) {
		int result = -1;
		for (int i = 0; i < chars.length; i++) {
			int index = name.indexOf(chars[i]);
			if (index != -1 && (result == -1 || result > index)) {
				result = index;
			}
		}
		return result;
	}

	private static final char[] DELIM_CHARS = { PropertyUtils.NESTED_DELIM,
			PropertyUtils.MAPPED_DELIM, PropertyUtils.INDEXED_DELIM, };

	/**
	 * 从PropertyUtils里面拷贝的setNestedProperty，做了如下改变 1.
	 * 如果property名字路径中有null，如果value也是null，则直接返回，而不抛异常
	 * 例如，name为propa.propb，propa取出是null，而value也是null，则直接返回 2.
	 * 如果property名字路径中有null，而value不是null，则按照声明类型创建此对象 3.
	 * 否则，如果value是null，而且目标字段是primitive类型，则直接返回 4.
	 * 支持Indexed和Mapped语法直接嵌套的情况，甚至可以直接以[和(开头 而不像BeanUtils中限制必须有名字
	 * 
	 * @param bean
	 *            Bean object.
	 * @param name
	 *            Property name, maybe nestable (xxx.xxx).
	 * @param value
	 *            Property value.
	 */
	public static void setProperty(Object bean, String name, Object value)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, InstantiationException {
		setProperty(bean, name, value, true, null);
	}

	/**
	 * @see #setProperty(Object, String, Object)
	 * @param autoCreateInterim
	 *            是否自动创建中间路径上的对象
	 */
	public static void setProperty(Object bean, String name, Object value,
			boolean autoCreateInterim) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			InstantiationException {
		setProperty(bean, name, value, autoCreateInterim, null);
	}

	/**
	 * @see #setProperty(Object, String, Object, boolean)
	 * @param interceptor
	 *            对property的所有操作截取器
	 */
	public static void setProperty(Object bean, String name, Object value,
			boolean autoCreateInterim, PropertyInterceptor interceptor)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, InstantiationException {
		while (bean != null) {
			int delimPos = minIndexOf(name, DELIM_CHARS);
			if (delimPos < 0) {
				// 最后一级，设置完后退出
				if (value != null
						|| !getBeanPropertyClass(bean, name).isPrimitive()) {
					// 如果value是null，而且目标类型是primitive，则不设置
					setSimpleProperty(bean, name, value);
				}
				return;
			} else {
				char delim = name.charAt(delimPos);
				switch (delim) {
				case PropertyUtils.NESTED_DELIM:
					if (delimPos > 0) {
						bean = getOrCreateSimpleProperty(bean, name.substring(
								0, delimPos), autoCreateInterim
								&& value != null, interceptor);
					} // 否则，直接去掉这个NESTED_DELIM，等下一次循环
					break;
				case PropertyUtils.MAPPED_DELIM: {
					int startPos = delimPos;
					delimPos = name.indexOf(PropertyUtils.MAPPED_DELIM2,
							delimPos);
					String propName = name.substring(0, startPos);
					String key = name.substring(startPos + 1, delimPos);
					if (delimPos == name.length() - 1) {
						// 已经是最后一级了
						setMappedProperty(bean, propName, key, value,
								interceptor);
						return;
					} else {
						bean = getOrCreateMappedProperty(bean, propName, key,
								autoCreateInterim && value != null, interceptor);
					}
					break;
				}
				case PropertyUtils.INDEXED_DELIM: {
					int startPos = delimPos;
					delimPos = name.indexOf(PropertyUtils.INDEXED_DELIM2,
							delimPos);
					String propName = name.substring(0, startPos);
					// don't check exception
					int index = Integer.parseInt(name.substring(startPos + 1,
							delimPos));
					if (delimPos == name.length() - 1) {
						// 已经是最后一级了
						setIndexedProperty(bean, propName, index, value,
								interceptor);
						return;
					} else {
						bean = getOrCreateIndexedProperty(bean, propName,
								index, autoCreateInterim && value != null,
								interceptor);
					}
					break;
				}
				default:
					// nothing
				}
				name = name.substring(delimPos + 1);
			}
		}
	}

	/**
	 * 类似getSimpleProperty，如果得到的是null，则试图根据其声明类型创建一个
	 */
	public static Object getOrCreateSimpleProperty(Object bean,
			String propName, boolean create) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			InstantiationException {
		return getOrCreateSimpleProperty(bean, propName, create, null);
	}

	public static Object getOrCreateSimpleProperty(Object bean,
			String propName, boolean create, PropertyInterceptor interceptor)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, InstantiationException {
		Object nextBean = getSimpleProperty(bean, propName, interceptor);
		if (nextBean == null && create) {
			// 按照声明类型创建此对象
			// 如果这个对象不是concrete或没有缺省constructor，将抛出异常
			// bean不能是一个map，因为nextBean是null，无法知道具体类型，也将抛出异常
			nextBean = getBeanPropertyClass(bean, propName).newInstance();
			setSimpleProperty(bean, propName, nextBean);
		}
		return nextBean;
	}

	/**
	 * 类似getIndexedProperty，如果得到的是null，则试图根据其声明类型创建一个
	 */
	public static Object getOrCreateIndexedProperty(Object bean,
			String propName, int index, boolean create)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, InstantiationException {
		return getOrCreateIndexedProperty(bean, propName, index, create, null);
	}

	public static Object getOrCreateIndexedProperty(Object bean,
			String propName, int index, boolean create,
			PropertyInterceptor interceptor) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			InstantiationException {
		Object nextBean;
		nextBean = getIndexedProperty(bean, propName, index, interceptor);
		if (nextBean == null && create) {
			// 见前面的注释
			nextBean = getBeanPropertyClass(bean, propName).newInstance();
			setIndexedProperty(bean, propName, index, nextBean, interceptor);
		}
		return nextBean;
	}

	/**
	 * 类似getMappedProperty，如果得到的是null，则试图根据其声明类型创建一个
	 */
	public static Object getOrCreateMappedProperty(Object bean,
			String propName, String key, boolean create)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, InstantiationException {
		return getOrCreateMappedProperty(bean, propName, key, create, null);
	}

	public static Object getOrCreateMappedProperty(Object bean,
			String propName, String key, boolean create,
			PropertyInterceptor interceptor) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			InstantiationException {
		Object nextBean = getMappedProperty(bean, propName, key, interceptor);
		if (nextBean == null && create) {
			// 见前面的注释
			nextBean = getBeanPropertyClass(bean, propName).newInstance();
			setMappedProperty(bean, propName, key, nextBean, interceptor);
		}
		return nextBean;
	}

	/**
	 * 支持Map的setSimpleProperty。 如果value是null，而且目标字段是primitive类型，则直接返回
	 */
	public static void setSimpleProperty(Object bean, String name, Object value)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		if (bean instanceof Map) {
			((Map) bean).put(name, value);
		} else {
			if (value != null
					|| !getBeanPropertyClass(bean, name).isPrimitive()) {
				// 如果value是null，而且目标类型是primitive，则不设置
				PropertyUtils.setSimpleProperty(bean, name, value);
			}
		}
	}

	/**
	 * 得到一个直接[]下标的值，以支持直接连接的()[]
	 */
	private static void setDirectIndexed(Object array, int index, Object value) {
		if (array instanceof List) {
			((List) array).set(index, value);
		} else {
			if (!array.getClass().isArray()) {
				throw new IllegalArgumentException("object can't index");
			}
			// 这里不支持primitive设null值
			Array.set(array, index, value);
		}
	}

	/**
	 * 修正的setIndexedProperty，支持直接嵌套和Map
	 */
	public static void setIndexedProperty(Object bean, String name, int index,
			Object value) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		setIndexedProperty(bean, name, index, value, null);
	}

	public static void setIndexedProperty(Object bean, String name, int index,
			Object value, PropertyInterceptor interceptor)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		if (name.length() == 0) {
			setDirectIndexed(bean, index, value);
		} else if (bean instanceof Map) {
			setDirectIndexed(((Map) bean).get(name), index, value);
		} else {
			if (interceptor != null) {
				// 如果有interceptor，就必须区分getIndexedProperty是否是先得到一个list
				// 或array，然后再访问的情况；和直接访问IndexedProperty的情况
				if (bean instanceof DynaBean || isIndexedProperty(bean, name)) {
					PropertyUtils.setIndexedProperty(bean, name, index, value);
				} else {
					Object bean1 = getSimpleProperty(bean, name, interceptor);
					setDirectIndexed(bean1, index, value);
				}
			} else {
				// 这里不支持primitive设null值
				PropertyUtils.setIndexedProperty(bean, name, index, value);
			}
		}
	}

	/**
	 * 修正的setMappedProperty，支持直接嵌套和Map
	 */
	public static void setMappedProperty(Object bean, String name1,
			String name2, Object value) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		setMappedProperty(bean, name1, name2, value, null);
	}

	public static void setMappedProperty(Object bean, String name1,
			String name2, Object value, PropertyInterceptor interceptor)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		if (name1.length() == 0) {
			// 直接把name2当作simple property的名字
			setSimpleProperty(bean, name2, value);
		} else if (bean instanceof Map) {
			setSimpleProperty(((Map) bean).get(name1), name2, value);
		} else {
			if (interceptor != null) {
				// 如果有interceptor，就必须区分setMappedProperty是否是先得到一个map
				// 再访问的情况；和直接访问MappedProperty的情况
				if (bean instanceof DynaBean || isMappedProperty(bean, name1)) {
					PropertyUtils.setMappedProperty(bean, name1, name2, value);
				} else {
					Object bean1 = getSimpleProperty(bean, name1, interceptor);
					setSimpleProperty(bean1, name2, value);
				}
			} else {
				// 这里不支持primitive设null值
				PropertyUtils.setMappedProperty(bean, name1, name2, value);
			}
		}
	}

	/**
	 * 从PropertyUtils里面拷贝的getNestedProperty，做了如下改变 1.
	 * 如果property名字路径中有null，则返回null，而不抛异常
	 * 例如，name为propa.propb，propa取出是null，则返回null 2.
	 * 支持Indexed和Mapped语法直接嵌套的情况，甚至可以直接以[和(开头 而不像BeanUtils中限制必须有名字
	 * 
	 * @param bean
	 *            Bean object.
	 * @param name
	 *            Property name, maybe ("xxx.xxx")
	 * @return Property value.
	 */
	public static Object getProperty(Object bean, String name)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		return getProperty(bean, name, null);
	}

	/**
	 * @see #getProperty(Object, String)
	 * @param interceptor
	 *            property操作截取器 (允许null)
	 */
	public static Object getProperty(Object bean, String name,
			PropertyInterceptor interceptor) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		// 任何一层对象如果是null，则直接返回null，而抛错
		while (bean != null && name.length() > 0) {
			int delimPos = minIndexOf(name, DELIM_CHARS);
			if (delimPos < 0) {
				bean = getSimpleProperty(bean, name, interceptor);
				break;
			} else {
				char delim = name.charAt(delimPos);
				switch (delim) {
				case PropertyUtils.NESTED_DELIM:
					if (delimPos > 0) {
						bean = getSimpleProperty(bean, name.substring(0,
								delimPos), interceptor);
					} // 否则，直接去掉这个NESTED_DELIM，等下一次循环
					break;
				case PropertyUtils.MAPPED_DELIM: {
					int startPos = delimPos;
					delimPos = name.indexOf(PropertyUtils.MAPPED_DELIM2,
							delimPos);
					bean = getMappedProperty(bean, name.substring(0, startPos),
							name.substring(startPos + 1, delimPos), interceptor);
					break;
				}
				case PropertyUtils.INDEXED_DELIM: {
					int startPos = delimPos;
					delimPos = name.indexOf(PropertyUtils.INDEXED_DELIM2,
							delimPos);
					bean = getIndexedProperty(bean,
							name.substring(0, startPos),
							// don't check exception
							Integer.parseInt(name.substring(startPos + 1,
									delimPos)), interceptor);
					break;
				}
				default:
					// nothing
				}
				name = name.substring(delimPos + 1);
			}
		}
		return bean;
	}

	/**
	 * 支持Map的getSimpleProperty
	 */
	public static Object getSimpleProperty(Object bean, String name)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		return getSimpleProperty(bean, name, null);
	}

	public static Object getSimpleProperty(Object bean, String name,
			PropertyInterceptor interceptor) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		if (interceptor != null) {
			interceptor.beforeGet(bean, name, null);
		}
		Object result = null;
		if (bean instanceof Map) {
			result = ((Map) bean).get(name);
		} else {
			result = PropertyUtils.getSimpleProperty(bean, name);
		}
		if (interceptor != null) {
			interceptor.afterGet(bean, name, null, result);
		}
		return result;
	}

	/**
	 * 得到一个直接[]下标的值，以支持直接连接的()[]
	 */
	private static Object getDirectIndexed(Object array, int index,
			PropertyInterceptor interceptor) {
		if (interceptor != null) {
			interceptor.beforeGet(array, null, new Integer(index));
		}
		Object result = null;
		if (array instanceof List) {
			result = ((List) array).get(index);
		} else {
			if (!array.getClass().isArray()) {
				throw new IllegalArgumentException("object can't index");
			}
			result = Array.get(array, index);
		}
		if (interceptor != null) {
			interceptor.afterGet(array, null, new Integer(index), result);
		}
		return result;
	}

	/**
	 * 修正的getIndexedProperty，支持直接嵌套和Map
	 */
	public static Object getIndexedProperty(Object bean, String name, int index)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		return getIndexedProperty(bean, name, index, null);
	}

	public static Object getIndexedProperty(Object bean, String name,
			int index, PropertyInterceptor interceptor)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		if (name.length() == 0) {
			return getDirectIndexed(bean, index, interceptor);
		}
		if (bean instanceof Map) {
			return getDirectIndexed(((Map) bean).get(name), index, interceptor);
		}
		if (interceptor != null) {
			// 如果有interceptor，就必须区分getIndexedProperty是否是先得到一个list
			// 或array，然后再访问的情况；和直接访问IndexedProperty的情况
			if (bean instanceof DynaBean || isIndexedProperty(bean, name)) {
				interceptor.beforeGet(bean, name, new Integer(index));
				Object result = PropertyUtils.getIndexedProperty(bean, name,
						index);
				interceptor.afterGet(bean, name, new Integer(index), result);
				return result;
			}
			Object bean1 = getSimpleProperty(bean, name, interceptor);
			return getDirectIndexed(bean1, index, interceptor);
		}
		return PropertyUtils.getIndexedProperty(bean, name, index);
	}

	/**
	 * 修正的getMappedProperty，支持直接嵌套和Map
	 */
	public static Object getMappedProperty(Object bean, String name1,
			String name2) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		return getMappedProperty(bean, name1, name2, null);
	}

	public static Object getMappedProperty(Object bean, String name1,
			String name2, PropertyInterceptor interceptor)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		if (name1.length() == 0) {
			// 直接把name2当作simple property的名字
			return getSimpleProperty(bean, name2, interceptor);
		}
		if (bean instanceof Map) {
			return getSimpleProperty(((Map) bean).get(name1), name2,
					interceptor);
		}
		if (interceptor != null) {
			// 如果有interceptor，就必须区分getMappedProperty是否是先得到一个map
			// 再访问的情况；和直接访问MappedProperty的情况
			if (bean instanceof DynaBean || isMappedProperty(bean, name1)) {
				interceptor.beforeGet(bean, name1, name2);
				Object result = PropertyUtils.getMappedProperty(bean, name1,
						name2);
				interceptor.afterGet(bean, name1, name2, result);
				return result;
			}
			Object bean1 = getSimpleProperty(bean, name1, interceptor);
			return getSimpleProperty(bean1, name2, interceptor);
		}
		return PropertyUtils.getMappedProperty(bean, name1, name2);
	}

}
