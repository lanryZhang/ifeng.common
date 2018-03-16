package com.ifeng.common.dm.persist.exception;
/**
 * <title>后台数据库数据约束校验异常</title>
 * 
 * <pre>后台数据库数据约束校验异常.<br>
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public class ConstraintViolationException extends PersistException {
    private static final long serialVersionUID = 3762530135533760825L;

    public ConstraintViolationException() {
        super();
    }
    /**
     * @param message
     */
    public ConstraintViolationException(String message) {
        super(message);
    }
    /**
     * @param message
     * @param cause
     */
    public ConstraintViolationException(String message, Throwable cause) {
        super(message, cause);
    }
    /**
     * @param cause
     */
    public ConstraintViolationException(Throwable cause) {
        super(cause);
    }
}
