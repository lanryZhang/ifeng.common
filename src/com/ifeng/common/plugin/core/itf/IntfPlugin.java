package com.ifeng.common.plugin.core.itf;
/**
 * 该接口作为框架的顶层接口，提供了最宽松的约束，仅要求实现一个基于上下文Object o的业务方法execute
 * @author yudf
 *
 */
public interface IntfPlugin {
	
	public Object execute(Object o);

}
