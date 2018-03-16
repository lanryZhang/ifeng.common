package com.ifeng.common.workflow;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ifeng.common.misc.Logger;


/**
 * <title>WorkflowEngine </title>
 * 
 * <pre>工作流引擎. 
 * 	1 启动轮询线程; 2 注册一个远程调用实例.
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class WorkflowEngine {
    private static final Logger log = Logger.getLogger(WorkflowEngine.class);

    private final int intDEFALUT = 60;
    
    private int waitSecond = intDEFALUT; // 线程轮循间隔时间（单位：秒）

    private int beforeSearchSECOND = intDEFALUT; // 查询多长时间前的数据（单位：秒）
    
    private int threadNum = intDEFALUT; // 每次轮询启动的最大线程数
    
    // 开始线程前首先判断流程ID是否在堆栈中，如果不存在则存入堆栈然后新建一个线程。线程处理完成之后从堆栈中删除。
    private Set threadSet = new HashSet(); // 存放线程标志（流程ID）的堆栈。

    private Workflow workflow;


    public WorkflowEngine(Workflow workflow, int waitSec, int bsSecond) {
        this.workflow = workflow;
        this.waitSecond = waitSec;
        this.beforeSearchSECOND = bsSecond;
    }
    
    public WorkflowEngine(Workflow workflow, int waitSec, 
            int bsSecond, int threadNum) {
        this.workflow = workflow;
        this.waitSecond = waitSec;
        this.beforeSearchSECOND = bsSecond;
        this.threadNum = threadNum;
    }
    
    /**
     * 启动工作流引擎。 1、启动守护线程。查询数据库中就绪的流程，新建工作线程执行。 2、设置消息监听。
     */
    public void start() {
        (new CycleThread(this)).start();
        if (log.isDebugEnabled()) {
            log.debug("WorkflowEngine is started！");
        }
    }


    public int getBeforeSearchSECOND() {
        return beforeSearchSECOND;
    }        
    public int getThreadNum() {
        return threadNum;
    }
    
    public int getWaitSecond() {
        return waitSecond;
    }

    public Set getThreadSet() {
        return threadSet;
    }
    
    

    
    
    /**
     * 轮循线程。定期查询就绪的流程，然后新建工作线程处理每一个流程。
     */
    public static class CycleThread extends Thread {
        private final int intKilo = 1000;

        private WorkflowEngine workflowEngine;

        public CycleThread(WorkflowEngine workflowEngine) {
            this.workflowEngine = workflowEngine;
        }

        /**
         * 轮询线程，定期查询代办事项，如果不在处理堆栈则执行。
         */
        public void run() {
            while (true) {
                try {                
                    // 等待
                    sleep(workflowEngine.getWaitSecond() * intKilo);
                    if (log.isDebugEnabled()) {
                        log.debug("CycleThread begin lookup the process which state is ready!");
                    }
                    // 处理流程。查询就绪的活动，新建线程处理流程。
                    List list = null;
                    try {
                        list = this.workflowEngine.getWorkflow()
                                .queryReadyProcessContext(
                                        workflowEngine.getBeforeSearchSECOND());
                    } catch (WorkflowException e) {
                        e.printStackTrace();
                    }
  
                    for (int i = 0; i < list.size() && i < (workflowEngine.getThreadNum()-workflowEngine.threadSet.size()); i++) {
                        ProcessContext pc = (ProcessContext)list.get(i);
                        // 判断是否在线程堆栈中。                        
                        if (!this.workflowEngine.getThreadSet().contains("" + pc.getId())) {
                            this.workflowEngine.getThreadSet().add("" + pc.getId());
                            new WorkThread(workflowEngine.getWorkflow(), pc, workflowEngine).start();
                        }
                        
                        if (log.isDebugEnabled()) {
                            log.debug("CycleThread start the process which state is ready!" + pc);
                            log.debug("The running thread num  = " + workflowEngine.getThreadSet().size());
                        }
                    }  
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 工作线程。
     * 
     */
    public static class WorkThread extends Thread {
        private Workflow workflow;

        private ProcessContext processContext;
        
        private WorkflowEngine workflowEngine;

        public WorkThread(Workflow workflow,
                ProcessContext processContext, WorkflowEngine workflowEngine) {
            this.workflow = workflow;
            this.processContext = processContext;
            this.workflowEngine = workflowEngine;
        }

        public void run() {
            try {
                // 调用工作流处理等待状态的流程。
            	workflow.runReadyProcessContext(processContext);
                // 在线程堆栈中删除标志。
                workflowEngine.getThreadSet().remove("" + processContext.getId());
            } catch (WorkflowException e) {
                e.printStackTrace();
            }
        }
    }

	public Workflow getWorkflow() {
		return workflow;
	}

}


