package com.ifeng.common.plugin.core.abst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.plugin.core.itf.IntfPlugin;
/**
 * AbstLogicPlugin的抽象类，在AbstLogicPlugin的基础上，增加了List<IntfPlugin> stepModules属性作为任务集。
 * 提供了执行stepModules中单个任务的方法 public Object executeStep(int stepIndex,Object context)。
 * 同时使用doExecute方法作为execute方法的具体工作方法。
 * @author yudf
 *
 */
public abstract class AbstSuite extends AbstLogicPlugin implements Configurable{
	
	protected List<IntfPlugin> stepModules;
	
	public Object execute(Object context){
		return doExecute(context);
	}
	
	public abstract Object doExecute(Object context);
	
	public Object executeStep(int stepIndex,Object context){
		IntfPlugin intfPlugin = stepModules.get(stepIndex);
		if(intfPlugin==null){
			return Boolean.FALSE;
		}
		return intfPlugin.execute(context);
	}

	public List<IntfPlugin> getStepModules() {
		return stepModules;
	}

	public void setStepModules(List<IntfPlugin> stepModules) {
		this.stepModules = stepModules;
	}
	
/**
 * 通过配置文件生成stepModules并为其赋值。具体的配置文件生成stepModules中对象的过程由configSubPlugin方法实现
 */
	public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
        if (this.stepModules == null) {
            this.stepModules = new ArrayList();
        }
        for (Node node = configEle.getFirstChild(); node != null; node = node
                .getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element itemEle = (Element)node;
            configSubPlugin(configRoot, itemEle);
        }
        return this;
    }
	/**
     * 配置一个子plugin。此处提供一个默认的实现，将配置在该标签下的任何元素均装载为IntfPlugin类型对象加入stepModules中
     * 
     * 子类可以覆盖。
     */
    protected void configSubPlugin(ConfigRoot configRoot, Element itemEle) {
    	this.stepModules.add((IntfPlugin)configRoot.createValueObject(this, itemEle, IntfPlugin.class));
    }
    
    public int getModuleSize() {
        return this.stepModules.size();
    }
}
