package com.ifeng.common.plugin.process;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.plugin.core.abst.AbstSuite;


/**
 * AbstSuite的实现类，在doExecute方法中，实现了“与”逻辑，即：
 * 遍历执行任务集，只要有一个任务返回isSuccess方法定义的“假”，doExecute方法即返回FALSE;任务集中所有任务均返回isSuccess方法定义的“真”时，doExecute方法返回TRUE
 * @author yudf
 *
 */
public class AndSuite extends AbstSuite{
	
	public Object doExecute(Object context){
		for(int i=0;i<this.stepModules.size();i++){
			if(!this.isSuccess(this.executeStep(i, context))){
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}
	
	
	private static final String ACCEPTED_TAGS =  "and-plugin" ;

	/* (non-Javadoc)
	* @see com.ifeng.common.plugin.core.abst.AbstSuite#configSubPlugin(com.ifeng.common.conf.ConfigRoot, org.w3c.dom.Element)
	*/
	protected void configSubPlugin(ConfigRoot configRoot, Element itemEle) {

		if (ACCEPTED_TAGS.equals(itemEle.getTagName())) {
		    super.configSubPlugin(configRoot, itemEle);
		} else {
		    throw new java.lang.IllegalArgumentException("'" + ACCEPTED_TAGS + "' required.");
		}
	}
}
