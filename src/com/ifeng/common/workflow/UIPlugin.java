package com.ifeng.common.workflow;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.XmlLoader;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;

/**
 * <title>UIPlugin </title>
 * 
 * <pre>
 *  工作流特定的plugin，挂起当前流程并等待外部执行一个UI(Engine处理)。
 *   配置方式如下：
 *  &ltconfig type="com.ifeng.common.workflow.UIPlugin" role="超级管理员" next-activity="test"/&gt
 * </pre>
 * 
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class UIPlugin extends AbstLogicPlugin implements Configurable{

	private String nextActivity;

	private String role;

	public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
		this.role = XmlLoader.getAttribute(configEle, "role");
		this.nextActivity = XmlLoader.getAttribute(configEle, "next-activity");
		return this;
	}

	public Object execute(Object object){
		ProcessContext context = (ProcessContext) object;
		context.setState(ProcessContext.SUSPENDED);
		context.setRole(this.role);
		context.setNextActivity(this.nextActivity);
		return Boolean.TRUE;
	}
}
