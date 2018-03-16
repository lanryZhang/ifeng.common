package com.ifeng.common.dm;

/**
 * <title>DataManagerException </title>
 * 
 * <pre>数据管理统一异常，因为持久化的异常较多，且均为阻塞级的，所以需要包装一个
 * 统一的异常。非持久化可以服用此异常。
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public class DataManagerException extends Exception {

	private static final long serialVersionUID = 8798271034139479207L;

	public DataManagerException() {
        super();
    }

    public DataManagerException(String arg0) {
        super(arg0);
    }

    public DataManagerException(Throwable arg0) {
        super(arg0);
    }

    public DataManagerException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
    
    /**
     * 生成一个表示找不到对象的DataManagerException。
     */
    public static DataManagerException notFound() {
        return new DataManagerException("messages.OBJECT_NOTFOUND");
    }

}
