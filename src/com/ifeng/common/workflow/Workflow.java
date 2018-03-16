package com.ifeng.common.workflow;

import java.util.List;
import java.util.Map;


/**
 * <title>Workflow </title>
 * 
 * <pre>工作流外部调用接口. 
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public interface Workflow {

    /**
     * 启动工作流.
     * @param processName 工作流名称
     * @param contextData 上下文业务数据。
     * @return 工作流实例id
     * @throws WorkflowException 启动异常
     */
    public long startProcess(String processName, Map contextData)
            throws WorkflowException;

    /**
     * 启动工作流！lockKey是该流程在工作流中的唯一标志，如果有相同的流程再一次进入工作流就要抛错。
     * 调用这个方法的程序要保证key的唯一性。（例如：前缀_数据库主键，一定要保证在工作流中的唯一性）
     * @param processName 工作流名称
     * @param contextData 上下文业务数据。
     * @param lockKey 流程锁的关键字。这个关键字的流程只能开始一次，如果工作流已经有一个了。
     * @return 工作流实例id
     * @throws WorkflowException 启动异常
     */
    public long startProcess(String processName, Map contextData, String lockKey)
            throws WorkflowException;

    /**
     * 通知挂起的工作流可以继续执行.
     * @param processInstanceId 工作流实例id
     * @param activityName 活动名称.
     * @param contextData 上下文数据.
     *        当contextData的key与ProcessContext中的key相同时为修改该key的值。
     *        当contextData中value为null是，将ProcessContext中相应的key/value对删除。
     * @throws WorkflowException 挂起异常
     */
    public void continueProcess(long processInstanceId, String activityName,
            Map contextData) throws WorkflowException;
    
    /**
     * 通知挂起的工作流可以继续执行.
     * @param processInstanceId 工作流实例id
     * @param processName 工作流名称
     * @param activityName 活动名称.
     * @param contextData 上下文数据.
     *        当contextData的key与ProcessContext中的key相同时为修改该key的值。
     *        当contextData中value为null是，将ProcessContext中相应的key/value对删除。
     * @throws WorkflowException 挂起异常
     */
    public void continueProcess(long processInstanceId, String processName, 
            String activityName, Map contextData) throws WorkflowException;
	public boolean runReadyProcessContext(ProcessContext processContext)
			throws WorkflowException;
    /**
     * 查询当前用户的可以操作的活动.
     * 
     * @param roles 操作员拥有的角色. 如果为null，忽略角色的判断
     * @param activityName 活动名称. 如果为null，忽略活动名称的判断
     * @return 活动对象的集合. 元素为ProcessContext类型.
               通过ProcessContext能购得到当前活动名称、当前活动UI , 上下文数据等基础数据。
     * @throws WorkflowException  查询挂起的流程实例异常
     */
    public List queryProcessContext(String[] roles, String activityName)
            throws WorkflowException;

    /**
     * 得到当前工作流上下文.
     * @param processInstanceId 工作流id
     * @return 工作流程实例的上下文。
     * @throws WorkflowException 得到流程异常
     */
    public ProcessContext getProcessContext(long processInstanceId)
            throws WorkflowException;
	/**
	 * 查找所有就绪的流程实例。
	 * 
	 * @param second
	 *            查询就绪一定时间以后的活动。以秒为单位的时间段。
	 * @return 活动对象的集合， 元素为ProcessContext类型。
	 */
    public List queryReadyProcessContext(int second) throws WorkflowException;
    /**
	 * 得到一个流程定义对象.
	 * 
	 * @param processDefinitionName
	 *            工作流实例id
	 */
	public ProcessDefinition getProcessDefinition(String processDefinitionName);
}