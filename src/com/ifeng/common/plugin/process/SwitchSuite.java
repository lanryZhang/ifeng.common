package com.ifeng.common.plugin.process;



import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.plugin.core.abst.AbstMapSuite;
import com.ifeng.common.plugin.core.itf.IntfPlugin;

public class SwitchSuite extends AbstMapSuite {

	private IntfPlugin switchConditionPlugin;

	private IntfPlugin defaultPlugin;
/**
 * 在方法中执行了"switch"的过程：
 * 1 通过switchConditionPlugin.execute(context)获得传入的context的映射出来的key，即为Object key
 * 2 如果stepModulesMap中键的集合包含该key(在底层使用equals和hashCode方法作为判断依据)，则执行stepModulesMap中key对应的IntfPlugin.execute(context)
 * 3 如果stepModulesMap中键的集合不包含该key，则执行defaultPlugin.execute(context)。defaultPlugin == null时直接返回null。
 * 
 * 注意:在SwitchSuite中stepModulesMap和switchConditionPlugin必须配置。defaultPlugin可以不配置。
 */
	@Override
	public Object doExecute(Object context) {
		Object key = switchConditionPlugin.execute(context);
		if (stepModulesMap.containsKey(key)) {
			return stepModulesMap.get(key).execute(context);
		} else {
			if (defaultPlugin == null) {
				return null;
			} else {
				return defaultPlugin.execute(context);
			}
		}
	}

	private static final String CONDITIONTAG = "switchCondition";
	
	private static final String DEFAULTTAG = "default";
/**
 * 配置	switchCondition和default两个子标签的对象
 * 
 */
	public void configSub(ConfigRoot configRoot, Object parent, Element configEle) {
		switchConditionPlugin = (IntfPlugin)configRoot.createChildObject(parent, configEle, CONDITIONTAG); 
		if(switchConditionPlugin==null){
			throw new java.lang.IllegalArgumentException("'" + CONDITIONTAG + "' required.");
		}
		defaultPlugin = (IntfPlugin)configRoot.createChildObject(parent, configEle, DEFAULTTAG);		
	}



}
