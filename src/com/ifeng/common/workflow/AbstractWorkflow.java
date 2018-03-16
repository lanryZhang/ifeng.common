package com.ifeng.common.workflow;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ifeng.common.dm.DataManager;
import com.ifeng.common.dm.DataManagerException;
import com.ifeng.common.dm.QueryResult;
import com.ifeng.common.dm.queryField.NormalQueryField;
import com.ifeng.common.dm.queryField.RangeQueryField;
import com.ifeng.common.misc.Logger;

public abstract class AbstractWorkflow implements Workflow {
	private static final Logger log = Logger.getLogger(AbstractWorkflow.class);

	public static final String LOCK_KEY = "_workflow_lock_key";

	/** key: String(process name), value: ProcessDefinition */
	// 保存流程定义文件
	protected Map processDefinitionMap;

	// 持久化ProcessContext的DM
	protected DataManager dm;

	public Map getProcessDefinitionMap() {
		return processDefinitionMap;
	}

	public void setProcessDefinitionMap(Map processDefinitionMap) {
		this.processDefinitionMap = processDefinitionMap;
	}
	public AbstractWorkflow(){
	}

	public AbstractWorkflow(Map processDefinitionMap,DataManager dm){
		this.processDefinitionMap = processDefinitionMap;
		this.dm = dm;
	}

	/*
	 * 新建一个工作流实例。 1 将状态改为就绪 2 然后保存到数据库
	 * 需要判断是否存在同样的lockKey
	 */
	public long startProcess(String processName, Map contextData, String lockKey)
			throws WorkflowException {
		try {
			//判断这个key是否已经存在
			Map query = new HashMap();
			query.put("lockKey", new NormalQueryField(lockKey));
			QueryResult queryResult = dm.query(query, null);
			if (queryResult.getRowCount() > 0) {
				throw new WorkflowException(
						"There's same lockkey process in engine.");
			}
			// 把这个key通过contextData传给ProcessContext。
			contextData.put(LOCK_KEY, lockKey);
			// 调用startProcess(processName, contextData)方法
			return startProcess(processName, contextData);
		} catch (DataManagerException e) {
			throw new WorkflowException(
					" Catch error for this lockKey: " + lockKey, e);
		}
	}

	/*
	 * 新建一个工作流实例。 1 将状态改为就绪 2 然后保存到数据库
	 */
	public long startProcess(String processName, Map contextData)
			throws WorkflowException {
		long retId = 0;
		Object pdObject = processDefinitionMap.get(processName);
		// 如果上层抛错，把得到的异常继续抛出去。
		if (pdObject instanceof Exception) {
			throw new WorkflowException("Get process activityDefinition " + processName + " catch a exception!",
					(Exception) pdObject);
		}
		if (null == pdObject) {
			throw new WorkflowException("ProcessDefinition " + processName + " not exist！(Maybe name is wrong)");
		} else {
			// new ProcessContext
			ProcessDefinition processDefinition = (ProcessDefinition) pdObject;
			ProcessContext pc = new ProcessContext();
			pc.setLockKey((String) contextData.get(LOCK_KEY));
			contextData.remove(LOCK_KEY);
			pc.setProcessDefinitionName(processName);
			pc.setState(ProcessContext.READY);
			pc.setActivity(processDefinition.getFirstActivityName());
			pc.setNextActivity(processDefinition.getFirstActivityName());
			pc.setProcessStartDate(Calendar.getInstance().getTime());
			pc.setActivityStartDate(Calendar.getInstance().getTime());
			pc.setStateStartDate(Calendar.getInstance().getTime());
			pc.setData(contextData);
            try {
				retId = startProcess(pc);
			} catch (DataManagerException e) {
				throw new WorkflowException(pc.getActivity()
						+ "Process activity error (maybe db error)。ProcessContext：" + pc, e);
			}
			// save into database
			
		}
		return retId;
	}
	
	public abstract long startProcess(ProcessContext processContext) throws  WorkflowException,DataManagerException;
	
	

	public ProcessContext getProcessContext(long processInstanceId)
			throws WorkflowException {
		ProcessContext processContext = null;
		try {
			processContext = (ProcessContext) dm.queryById(new Long(
					processInstanceId));
			processContext.activation(this);
		} catch (Exception e) {
			throw new WorkflowException("Cann't find id=" + processInstanceId
					+ "'s activity！", e);
		}
		return processContext;
	}

	/*
	 * 查询多个角色的已经被挂起活动的流程。 条件：角色是一个数组。activityName空时查所有的活动。
	 */
	public List queryProcessContext(String[] roles, String activityName)
			throws WorkflowException {
		List ret = new ArrayList();
		// 构造查询条件：状态、活动名。（条件中不包括角色，数据库中的角色是用“,”隔开的多个角色，角色在查询之后再过滤）
		Map query = new HashMap();
		query.put("state", new NormalQueryField(new Long(
				ProcessContext.SUSPENDED)));
		if (null != activityName) {
			query.put("activity", new NormalQueryField(activityName));
		}

		// 查询
		QueryResult queryResult = null;
		try {
			queryResult = dm.query(query, null);
		} catch (DataManagerException e) {
			throw new WorkflowException("Haven't process for this query:" + query, e);
		}

		// 循环查询结果：1、过滤角色条件；2、处理上下文；3、激活。
		if (queryResult != null) {
			List list = queryResult.getData(0, queryResult.getRowCount());
			for (Iterator it = list.iterator(); it.hasNext();) { // 循环每一条记录
				ProcessContext temp = (ProcessContext) it.next();
				// 对比数据库中的角色和参数中的角色。
				boolean isValid = false;
				String[] rolesDB = temp.getRole().split(",");
				for (int i = 0; i < rolesDB.length; i++) { // 循环数据库中的每一个角色
					if (rolesDB[i].trim().equals("all")) {
						isValid = true;
						break;
					}
					for (int j = 0; j < roles.length; j++) {
						if (rolesDB[i].trim().equals(roles[j].trim())) {
							isValid = true;
							break;
						}
					}
				}

				if (isValid) {
					temp.activation(this); // 激活
					ret.add(temp);
					if (log.isDebugEnabled()) {
						log.debug("Find a SUSPENDED process:: " + temp);
					}
				}
			}
		}
		return ret;
	}

	/*
	 * 挂起->就绪；然后持久化。SUSPENDED-->READY
	 */
	public void continueProcess(long processInstanceId, String activityName,
			Map contextData) throws WorkflowException {
		continueProcess(processInstanceId, null, activityName, contextData);
	}

	public void continueProcess(long processInstanceId, String processName,
			String activityName, Map contextData) throws WorkflowException {
		ProcessContext processContext = null;
		try {
			processContext = (ProcessContext) dm.queryById(new Long(
					processInstanceId));

			// 判断当前活动是否有效
			boolean activityFlag = false;
			if (null == processName) {
				if (activityName.equals(processContext.getActivity())) {
					activityFlag = true;
				}
			} else {
				if (processName.equals(processContext
						.getProcessDefinitionName())
						&& activityName.equals(processContext.getActivity())) {
					activityFlag = true;
				}
			}

			if (activityFlag) {
				processContext.setState(ProcessContext.READY);
				Map map = processContext.getData();
				if (null != contextData) {
					map.putAll(contextData);
				}
				continueProcess(processContext);
			} else {
				throw new WorkflowException(activityName
						+ "Process activity isn't current activity , make sure,pls.！ProcessContext：" + processContext);
			}
		} catch (DataManagerException e) {
			throw new WorkflowException(activityName
					+ "Process activity error (maybe db error)。ProcessContext：" + processContext, e);
		}
		if (log.isDebugEnabled()) {
			log.debug("The activity is from SUSPENDED to READY : " + processContext);
		}
	}
	
	public abstract void continueProcess(ProcessContext processContext) throws WorkflowException,DataManagerException;

	/**
	 * 查找所有就绪的流程实例。
	 * 
	 * @param second
	 *            查询就绪一定时间以后的活动。以秒为单位的时间段。
	 * @return 活动对象的集合， 元素为ProcessContext类型。
	 */
	public List queryReadyProcessContext(int second) throws WorkflowException {
		List ret = new ArrayList();
		final int intStart = 60 * 60 * 24 * 365;

		// 构造查询条件
		Calendar endTime = Calendar.getInstance();
		endTime.add(Calendar.SECOND, 0 - second);
		Calendar startTime = Calendar.getInstance();
		startTime.add(Calendar.SECOND, 0 - intStart);

		Map query = new HashMap();
		query.put("state", new NormalQueryField(new Long(ProcessContext.READY)));
		query.put("activityStartDate", new RangeQueryField(startTime, endTime));

		// 查询
		QueryResult queryResult = null;
		try {
			queryResult = dm.query(query, null);
		} catch (DataManagerException e) {
			throw new WorkflowException("Haven't process for this query:" + query, e);
		}

		// 激活
		if (queryResult != null) {
			List list = queryResult.getData(0, queryResult.getRowCount());
			for (Iterator it = list.iterator(); it.hasNext();) {
				ProcessContext temp = (ProcessContext) it.next();

				// 只激活当前引擎启动时读取的流程定义文件中包含的流程。
				if (temp.isExist(this)) {
					temp.activation(this);
					ret.add(temp);
					if (log.isDebugEnabled()) {
						log.debug("Find a ready process: " + temp);
					}
				}
			}
		}
		return ret;
	}

	/**
	 * READY->RUN->SUSPEND(END) 运行所有就绪的流程。
	 * 
	 * @param id
	 *            就绪流程id
	 * @return 流程是否在执行（true-还在执行；false-已经成功执行完了）
	 * @throws WorkflowException
	 */
	public boolean runReadyProcessContext(long id) throws WorkflowException {
		return runReadyProcessContext(getProcessContext(id));
	}

	/**
	 * READY->RUN->SUSPEND(END) 运行所有就绪的流程。 循环处理流程中的每一个活动，直到挂起或结束。 核心：
	 * 1.流程结束的标志是没有下一个活动。（在解析下一个活动前先把它赋为空） 2.循环处理结束的标志是下一个活动为空或是状态为挂起。
	 * 3.避免陷入死循化，解析到的下一个活动与本活动不能相同。
	 * 
	 * @param processContext
	 *            查询就绪一定时间以后的活动。以秒为单位的时间段。
	 * @return 流程是否在执行（true-还在执行；false-已经成功执行完了）
	 * @throws WorkflowException
	 */
	public boolean runReadyProcessContext(ProcessContext processContext)
			throws WorkflowException {
		ProcessDefinition processDefinition = (ProcessDefinition) processDefinitionMap.get(processContext.getProcessDefinitionName());
		while (processContext.getState() != ProcessContext.SUSPENDED
				&& null != processContext.getNextActivity()) {
			try {
				processContext.setNextActivity(null);
				processContext.setActivityStartDate(Calendar.getInstance()
						.getTime());
				processContext.setStateStartDate(Calendar.getInstance()
						.getTime());
				// 不同的Plugin定义，用不同的实现处理。								
				ActivityDefinition activityDefinition = processDefinition
						.getActivity(processContext.getActivity());
		               activityDefinition.getPlugin().execute(processContext);
				// 处理完当前的活动，进行到下一个活动。
				processContext.setActivity(processContext.getNextActivity());
			} catch (Exception ex) {
				throw new WorkflowException("process activity catch a exception！ProcessContext："
						+ processContext, ex);
			}
		}
		// 活动停止后的处理
		return afterRunProcess(processContext);
	}

	/**
	 * 循环处理完活动以后，判断：1、挂起--修改数据 2、结束--删除数据
	 * 
	 * @param processContext
	 * @return 流程是否在执行（true-还在执行；false-已经成功执行完了）
	 * @throws WorkflowException
	 */
	public abstract boolean afterRunProcess(ProcessContext processContext)
			throws WorkflowException;

	/**
	 * 处理数据库中所有就绪的流程。下面的守护线程会代替它。 1.查询到所有的就绪流程。 2.一级循环，处理每一个流程。
	 * 3.二级循环，处理流程中的每一个活动。
	 * 
	 * @param second
	 *            查询就绪一定时间以后的活动。以秒为单位的时间段。
	 */
	public void workOnReadyProcessContext(int second) throws WorkflowException {
		// 查询就绪态的活动
		List list = queryReadyProcessContext(second);
		// 处理所有找到的
		for (Iterator it = list.iterator(); it.hasNext();) {
			runReadyProcessContext((ProcessContext) it.next());
		}
	}

	/**
	 * 得到一个流程定义对象.
	 * 
	 * @param processDefinitionName
	 *            工作流实例id
	 */
	public ProcessDefinition getProcessDefinition(String processDefinitionName) {
		if (null == processDefinitionName || null == processDefinitionMap) {
			return null;
		} else {
			Object obj = processDefinitionMap.get(processDefinitionName);
			if (null == obj) {
				return null;
			} else {
				return (ProcessDefinition) obj;
			}
		}
	}
}
