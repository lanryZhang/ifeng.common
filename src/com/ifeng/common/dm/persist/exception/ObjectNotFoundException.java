package com.ifeng.common.dm.persist.exception;



/**
 * <title>没有对应的数据异常</title>
 * 
 * <pre>没有对应的数据异常时抛出此异常.<br>
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class ObjectNotFoundException extends PersistException {
    private static final long serialVersionUID = 3762530135533760825L;

    public ObjectNotFoundException() {
        super();
    }
    /**
     * @param message
     */
    public ObjectNotFoundException(String message) {
        super(message);
    }
    /**
     * @param message
     * @param cause
     */
    public ObjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    /**
     * @param cause
     */
    public ObjectNotFoundException(Throwable cause) {
        super(cause);
    }
}
