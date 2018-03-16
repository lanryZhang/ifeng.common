package com.ifeng.common.plugin.process;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.plugin.core.abst.AbstSuite;
/**
 * AbstSuite的实现类，在doExecute方法中，实现了“遍历”逻辑，即：
 * 遍历任务集中的任务，返回执行成功的任务号列表。
 * @author yudf
 *
 */


public class ForEachSuite extends AbstSuite{
	public Object doExecute(Object context){
		List<Integer> sucessList = new ArrayList<Integer>();
		for(int i=0;i<this.stepModules.size();i++){
			if(this.isSuccess(this.executeStep(i, context))){
				sucessList.add(i);
			}
		}
		return sucessList;
	}
	
	private static final String ACCEPTED_TAGS =  "foreach-plugin" ;
	
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
