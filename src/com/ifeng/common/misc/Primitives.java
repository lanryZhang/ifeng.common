package com.ifeng.common.misc;

import java.util.HashMap;
import java.util.Map;

/**
 * Java基本数据类型定义.<br>
 * 
 * @author jinmy
 */
public final class Primitives {

	public static final String TYPE_BOOLEAN = "boolean";

	public static final String TYPE_BYTE = "byte";

	public static final String TYPE_CHAR = "char";

	public static final String TYPE_DOUBLE = "double";

	public static final String TYPE_FLOAT = "float";

	public static final String TYPE_INT = "int";

	public static final String TYPE_LONG = "long";

	public static final String TYPE_SHORT = "short";

	private static final Map primitiveTypes;

	private static final Map primitiveTypesToClass;

	private static final Map primitiveClassToTypes;

	static {
		primitiveTypes = new HashMap();
		primitiveTypes.put(TYPE_BOOLEAN, Boolean.TYPE);
		primitiveTypes.put(TYPE_BYTE, Byte.TYPE);
		primitiveTypes.put(TYPE_CHAR, Character.TYPE);
		primitiveTypes.put(TYPE_DOUBLE, Double.TYPE);
		primitiveTypes.put(TYPE_FLOAT, Float.TYPE);
		primitiveTypes.put(TYPE_INT, Integer.TYPE);
		primitiveTypes.put(TYPE_LONG, Long.TYPE);
		primitiveTypes.put(TYPE_SHORT, Short.TYPE);

		primitiveTypesToClass = new HashMap();
		primitiveTypesToClass.put(Integer.TYPE, Integer.class);
		primitiveTypesToClass.put(Long.TYPE, Long.class);
		primitiveTypesToClass.put(Float.TYPE, Float.class);
		primitiveTypesToClass.put(Double.TYPE, Double.class);
		primitiveTypesToClass.put(Boolean.TYPE, Boolean.class);
		primitiveTypesToClass.put(Byte.TYPE, Byte.class);
		primitiveTypesToClass.put(Character.TYPE, Character.class);
		primitiveTypesToClass.put(Short.TYPE, Short.class);

		primitiveClassToTypes = new HashMap();
		primitiveClassToTypes.put(Integer.class, Integer.TYPE);
		primitiveClassToTypes.put(Long.class, Long.TYPE);
		primitiveClassToTypes.put(Float.class, Float.TYPE);
		primitiveClassToTypes.put(Double.class, Double.TYPE);
		primitiveClassToTypes.put(Boolean.class, Boolean.TYPE);
		primitiveClassToTypes.put(Byte.class, Byte.TYPE);
		primitiveClassToTypes.put(Character.class, Character.TYPE);
		primitiveClassToTypes.put(Short.class, Short.TYPE);

	}

	/**
	 * 根据指定的基本类型字符串获取其对应的基本类型Class对象.
	 * 
	 * @param primitiveType
	 *            基本类型
	 * @return 基本类型Class对象.
	 */
	public static Class getPrimitiveTypeClass(String primitiveType) {
		return (Class) primitiveTypes.get(primitiveType);
	}

	/**
	 * 根据指定的基本类型Class对象获取对应的数据类型的Class对象.比如根据Long.TYPE获取Long.class.
	 * 
	 * @param primitiveTypeClass
	 *            基本类型Class对象.
	 * @return 数据类型Class对象.
	 */
	public static Class getTypeClass(Class primitiveTypeClass) {
		return (Class) primitiveTypesToClass.get(primitiveTypeClass);
	}

	public static boolean isPrimitiveClass(Class clazz) {
		return primitiveClassToTypes.containsKey(clazz);
	}

	private Primitives() {
		// utility class
	}

}
