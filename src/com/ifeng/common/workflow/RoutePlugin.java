package com.ifeng.common.workflow;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.XmlLoader;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;

/**
 * <title>RoutePlugin </title>
 * 
 * <pre>
 * 工作流特定的plugin，主要的能力为转到下一个activity。
 * 配置方式如下：
 *  &ltconfig type="com.ifeng.common.workflow.RoutePlugin" next-activity="test"/&gt
 * </pre>
 * 
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class RoutePlugin extends AbstLogicPlugin  implements Configurable {

    private String nextActivity;
    
    public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
        this.nextActivity = XmlLoader.getAttribute(configEle, "next-activity");
        return this;
    }

    public Object execute(Object object) {
        ProcessContext context = (ProcessContext)object;
        context.setNextActivity(this.nextActivity);
        return Boolean.TRUE;
    }
}

