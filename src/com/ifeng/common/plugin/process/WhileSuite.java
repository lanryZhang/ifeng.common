package com.ifeng.common.plugin.process;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.plugin.core.abst.AbstSuite;

/**
 * AbstSuite的实现类，在doExecute方法中，实现了“循环”逻辑，即：
 * 循环执行任务集中的第一个任务，只要返回值为isSuccess定义的“真”，则执行任务集中的第二个任务。跳出循环时返回“真”
 * @author yudf
 *
 */
public class WhileSuite extends AbstSuite{
	
	
	public Object doExecute(Object context){
		while(this.isSuccess(executeStep(0, context))){
			executeStep(1, context);
		}
		return Boolean.TRUE;
		
	}

	private static final String[] ACCEPTED_TAGS = { "condition", "on-true" };	
	
	/* (non-Javadoc)
	 * @see com.ifeng.common.plugin.core.abst.AbstSuite#configSubPlugin(com.ifeng.common.conf.ConfigRoot, org.w3c.dom.Element)
	 */
	protected void configSubPlugin(ConfigRoot configRoot, Element itemEle) {
        String acceptedTag;
        int modules = getModuleSize();
        if (modules < ACCEPTED_TAGS.length) {
            acceptedTag = ACCEPTED_TAGS[modules];
        } else {
            throw new java.lang.IllegalArgumentException("Extra subelement");
        }
        
        if (acceptedTag.equals(itemEle.getTagName())) {
            super.configSubPlugin(configRoot, itemEle);
        } else {
            throw new java.lang.IllegalArgumentException("'" + acceptedTag + "' required.");
        }
    }
}
