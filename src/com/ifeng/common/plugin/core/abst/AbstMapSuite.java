package com.ifeng.common.plugin.core.abst;

import java.util.Map;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.plugin.core.itf.IntfPlugin;

/**
 * AbstLogicPlugin的抽象类，在AbstLogicPlugin的基础上，增加了Map<Object,IntfPlugin>
 * stepModulesMap属性作为"键值对"类型的任务集。 提供了执行stepModulesMap中单个任务的方法 public Object
 * executeStep(Object keyObject,Object context)。
 * 同时使用doExecute方法作为execute方法的具体工作方法。
 * 
 * @author yudf
 * 
 */
public abstract class AbstMapSuite extends AbstLogicPlugin implements Configurable {

	protected Map<Object, IntfPlugin> stepModulesMap;

	public Object execute(Object context) {
		return doExecute(context);
	}

	public abstract Object doExecute(Object context);

	public Object executeStep(Object keyObject, Object context) {
		IntfPlugin intfPlugin = stepModulesMap.get(keyObject);
		if (intfPlugin == null) {
			return Boolean.FALSE;
		}
		return intfPlugin.execute(context);
	}

	public int getModulesMapSize() {
		return this.stepModulesMap.size();
	}

	/**
	 * 通过配置文件生成stepModulesMap并为其赋值。具体的配置文件生成stepModulesMap中对象的过程由configSubPlugin方法实现
	 */
	public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
		stepModulesMap = (Map)configRoot.createChildObject(parent, configEle, "stepModulesMap");
		if(stepModulesMap==null){
			System.out.println("AbstMapSuite: config: stepModulesMap is null");
			throw new java.lang.IllegalArgumentException("'stepModulesMap' required.");
		}
		configSub( configRoot,  parent,  configEle);
		return this;
	}
	/**
	 * 通过配置文件为其他属性生成对象。该方法用于子类扩展时覆盖。
	 */	
	protected void configSub(ConfigRoot configRoot, Object parent, Element configEle){
		System.out.println("AbstMapSuite: config: configSub in super");
	}

	/** getter and setter **/
	public Map<Object, IntfPlugin> getStepModulesMap() {
		return stepModulesMap;
	}

	public void setStepModulesMap(Map<Object, IntfPlugin> stepModulesMap) {
		this.stepModulesMap = stepModulesMap;
	}
	/** getter and setter **/

}
