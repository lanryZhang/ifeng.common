package com.ifeng.common.workflow;

/**
 * <title>WorkflowException </title>
 * 
 * <pre>工作流引擎的异常. 
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public class WorkflowException extends Exception {

	private static final long serialVersionUID = -751768944857454781L;

    public WorkflowException() {
        super();
    }

    /**
     * @param message
     */
    public WorkflowException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public WorkflowException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public WorkflowException(Throwable cause) {
        super(cause);
    }

}

