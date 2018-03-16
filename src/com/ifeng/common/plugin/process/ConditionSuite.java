package com.ifeng.common.plugin.process;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.plugin.core.abst.AbstSuite;

/**
 * AbstSuite的实现类，在doExecute方法中，实现了“条件选择”逻辑，即：
 * 执行任务集中的第一个任务:若是返回isSuccess方法定义的“真”，则执行第二个任务，将其结果作为返回值；若是返回isSuccess方法定义的“假”，则执行第三个任务，将其结果作为返回值
 * 
 * @author yudf
 * 
 */
public class ConditionSuite extends AbstSuite {
	public Object doExecute(Object context) {
		if (this.isSuccess(this.executeStep(0, context))) {
			return this.executeStep(1, context);
		} else {
			return this.executeStep(2, context);
		}
	}

	private static final String[] ACCEPTED_TAGS = { "condition", "on-true",
			"on-false" };
	
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
