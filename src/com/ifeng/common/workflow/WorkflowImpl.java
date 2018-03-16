package com.ifeng.common.workflow;

import java.util.Map;

import com.ifeng.common.dm.DataManager;
import com.ifeng.common.dm.DataManagerException;
import com.ifeng.common.misc.Logger;

/**

  @author :chenyong
  @version 1.0
  @date 2012-3-29
 */

public class WorkflowImpl extends AbstractWorkflow {
	private static final Logger log = Logger.getLogger(WorkflowImpl.class);
	public WorkflowImpl(){
		super();
	}

	public WorkflowImpl(Map processDefinitionMap, DataManager dm) {
		    super(processDefinitionMap,dm);
	}

	@Override
	public void continueProcess(ProcessContext processContext) throws WorkflowException,DataManagerException {
		dm.modify(processContext, null, null);
		
	}

	@Override
	public long startProcess(ProcessContext processContext) throws WorkflowException, DataManagerException {
		long retId=0;
		Object objId = dm.add(processContext, null);
		retId = Long.parseLong(objId.toString());		
		return retId;
	}

	@Override
	public boolean afterRunProcess(ProcessContext processContext)
			throws WorkflowException {
		boolean ret = true;
		try {
			if (processContext.getState() == ProcessContext.SUSPENDED) {
				if (log.isDebugEnabled()) {
					log.debug("A activeity is SUSPENDED，ProcessContext：" + processContext);
				}
				dm.modify(processContext, null, null);
			} else if (null == processContext.getNextActivity()) {
				if (log.isDebugEnabled()) {
					log.debug("A activeity is processed，ProcessContext：" + processContext);
				}
				dm.delete(processContext, null);
				ret = false;
			} else {
				throw new WorkflowException("A activeity is processed ,but it's not nomal exit！ProcessContext："
						+ processContext);
			}
		} catch (Exception ex) {
			throw new WorkflowException("A activeity is processed,but catch a error when save state！ProcessContext："
					+ processContext, ex);
		}
		return ret;
	}

}
