package com.ifeng.common.dm.persist.exception;
/**
 * <title>持久化统一的异常</title>
 * 
 * <pre>一般用在一个泛化场景，当需要一个特殊含义的异常时，需要定义一个子类.<br>
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class PersistException extends Exception {
    private static final long serialVersionUID = 3762530135533760825L;

    public PersistException() {
        super();
    }
    /**
     * @param message
     */
    public PersistException(String message) {
        super(message);
    }
    /**
     * @param message
     * @param cause
     */
    public PersistException(String message, Throwable cause) {
        super(message, cause);
    }
    /**
     * @param cause
     */
    public PersistException(Throwable cause) {
        super(cause);
    }
}
