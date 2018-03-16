package com.ifeng.common.workflow;

import java.util.Date;
import java.util.Map;

import com.ifeng.common.misc.CoderUtil;

/**
 * <title>ProcessContext </title>
 * 
 * <pre>
 *  工作流程的上下文数据。每个上下文完全体现一个工作流程实例的状态。
 * </pre>
 * 
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public class ProcessContext {

	// 表示流程状态的常量。
	public static final int READY = 1;

	public static final int RUN = 2;

	public static final int SUSPENDED = 3;

	/**
	 * 以下属性需要持久化。
	 */

	/**
	 * 工作流程上下文的标识。
	 */
	private long id;

	/**
	 * 流程定义的名字。
	 */
	private String processDefinitionName;

	/**
	 * 唯一实例关键字
	 */
	private String lockKey;

	/**
	 * 流程当前的状态。
	 */
	private int state = 1;

	/**
	 * 当前活动的名称。
	 */
	private String activity;

	/**
	 * 下一个活动的名称。
	 */
	private String nextActivity;

	/**
	 * 当前活动的角色。
	 */
	private String role;

	/**
	 * 流程开始时间。
	 */
	private Date processStartDate;

	/**
	 * 活动开始时间。
	 */
	private Date activityStartDate;

	/**
	 * 状态开始时间。
	 */
	private Date stateStartDate;

	// 存放上下文信息的map。
	private Map data;
	
	private String transportData;
	

	/**
	 * 以下属性不保存到数据库中，是从内存中得到的。
	 */

	/**
	 * 流程定义的标题。
	 */
	private String processTitle;

	/**
	 * 流程定义的描述。
	 */
	private String processDescription;

	/**
	 * 流程活动的标题。
	 */
	private String activityTile;

	/**
	 * 当前活动的描述。
	 */
	private String activityDescription;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getProcessDefinitionName() {
		return processDefinitionName;
	}

	public void setProcessDefinitionName(String processDefinitionName) {
		this.processDefinitionName = processDefinitionName;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public String getNextActivity() {
		return nextActivity;
	}

	public void setNextActivity(String nextActivity) {
		this.nextActivity = nextActivity;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Date getProcessStartDate() {
		return processStartDate;
	}

	public void setProcessStartDate(Date processStartDate) {
		this.processStartDate = processStartDate;
	}

	public Date getActivityStartDate() {
		return activityStartDate;
	}

	public void setActivityStartDate(Date activityStartDate) {
		this.activityStartDate = activityStartDate;
	}

	public Date getStateStartDate() {
		return stateStartDate;
	}

	public void setStateStartDate(Date stateStartDate) {
		this.stateStartDate = stateStartDate;
	}

	public Map getData() {
		return (Map)CoderUtil.string2Object(transportData);
	}

	public void setData(Map data) {
		this.data = data;
		String ts = CoderUtil.object2String(data);
		setTransportData(ts);
	}
	public String getTransportData() {
		return transportData;
		
	}

	public void setTransportData(String transformData) {		
		this.transportData = transformData;	
	}

	public String getProcessTitle() {
		return processTitle;
	}

	public String getProcessDescription() {
		return processDescription;
	}

	public String getActivityTile() {
		return activityTile;
	}

	public String getActivityDescription() {
		return activityDescription;
	}

	public String getLockKey() {
		return lockKey;
	}

	public void setLockKey(String lockKey) {
		this.lockKey = lockKey;
	}

	 /**
     * 判断当前工作流是否在流程定义文件中。
     * @param workflowImpl
     * @return 当前工作流是否在流程定义文件中。
     */
    public boolean isExist(Workflow workflow) {
        boolean result = false;
        if (null != workflow) {
            ProcessDefinition processDefinition = workflow
                    .getProcessDefinition(processDefinitionName);
            if (null != processDefinition) {
            	result = true;
            } 
        }        
        return result;  
    }
	/**
	 * 激活当前的工作流程。当前的活动在数据库中持久化的信息不全，需要从流程定义文件中补充信息。
	 * 
	 * @param workflowImpl
	 * @throws WorkflowException
	 */
	public void activation(Workflow workflow) throws WorkflowException {
		if (null != workflow) {
			ProcessDefinition processDefinition = workflow
					.getProcessDefinition(processDefinitionName);
			if (null != processDefinition) {
				this.processDefinitionName = processDefinition.getName();
				this.processTitle = processDefinition.getTitle();
				this.processDescription = processDefinition.getDescription();
				ActivityDefinition activityDefinition = processDefinition
						.getActivity(activity);
				this.activityTile = activityDefinition.getTitle();
				this.activityDescription = activityDefinition.getDescription();
			} else {
				throw new WorkflowException("Active processContext catch exception!Maybe "
						+ processDefinitionName + "process config file is not loaded");
			}
		}
	}
}
