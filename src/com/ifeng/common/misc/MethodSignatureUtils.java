package com.ifeng.common.misc;

import java.lang.reflect.Method;

/**
 * 于生成一个Method的signature。这个signature加上method名字可以唯一地表示
 * 一个类中的method。这样有利于对method进行索引和搜索
 * 它生成的signature只是能达到唯一表示的目的，而并不与标准signature完全相同
 * 做了一些优化，比如不表示返回值，省去了括号等
 * @author jinmy
 */
public final class MethodSignatureUtils {
    
    private MethodSignatureUtils() {
        // utility class
    }
    
    /**
     * 得到一个Method的唯一标识，是method名字加上"#"加上method signature
     * @param method
     * @return Method ID.
     */
    public static String getMethodId(Method method) {
        return getMethodId(method.getName(), method.getParameterTypes());
    }
    
    public static String getMethodId(String methodName, Class[] paramTypes) {
        StringBuffer sb = new StringBuffer();
        sb.append(methodName).append('#');
        getSignature(paramTypes, sb);
        return sb.toString();
    }
    
    /**
     * Compute the JVM method descriptor for the method.
     */
    public static String getSignature(Method method) {
        StringBuffer sb = new StringBuffer();
        getSignature(method, sb);
        return sb.toString();
    }
    
    private static void getSignature(Method method, StringBuffer sb) {
        getSignature(method.getParameterTypes(), sb);
    }

    private static void getSignature(Class[] paramTypes, StringBuffer sb) {
        for (int j = 0; j < paramTypes.length; j++) {
            sb.append(getSignature(paramTypes[j]));
        }
    }
    
    /**
     * Compute the JVM signature for the class.
     * 做了一些优化，比如java/lang/String表示为s
     */
    public static String getSignature(Class clazz) {
        StringBuffer sb = new StringBuffer();
        getSignature(clazz, sb);
        return sb.toString();
    }

    private static void getSignature(Class clazz, StringBuffer sb) {
        if (clazz.isArray()) {
            getArraySignature(clazz, sb);
        } else if (clazz.isPrimitive()) {
            getPrimitiveSignature(clazz, sb);
        } else if (clazz == String.class) {
            sb.append('s');
        } else {
            sb.append('L').append(clazz.getName().replace('.', '/')).append(';');
        }
    }

    /**
     * primitive类型的字符串标识
     */
    private static void getPrimitiveSignature(Class clazz, StringBuffer sb) {
        if (clazz == Integer.TYPE) {
            sb.append('I');
        } else if (clazz == Byte.TYPE) {
            sb.append('B');
        } else if (clazz == Long.TYPE) {
            sb.append('J');
        } else if (clazz == Float.TYPE) {
            sb.append('F');
        } else if (clazz == Double.TYPE) {
            sb.append('D');
        } else if (clazz == Short.TYPE) {
            sb.append('S');
        } else if (clazz == Character.TYPE) {
            sb.append('C');
        } else if (clazz == Boolean.TYPE) {
            sb.append('Z');
        } else if (clazz == Void.TYPE) {
            sb.append('V'); // 只是返回类型
        }
    }

    private static void getArraySignature(Class clazz, StringBuffer sb) {
        Class cl = clazz;
        while (cl.isArray()) {
            sb.append('[');
            cl = cl.getComponentType();
        }
        sb.append(getSignature(cl));
    }

}
