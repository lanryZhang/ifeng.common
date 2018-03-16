package com.ifeng.common.workflow;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.XmlLoader;
import com.ifeng.common.plugin.core.itf.IntfPlugin;

/**
 * <title>ActivityDefinition </title>
 * 
 * <pre>
 * 		工作流的活动定义，由IntfPlugin构成. 
 * 		你可以使用Plugin来配置整个活动的过程，必要的时候可以使用RoutePlugin来完成活动的跳转。
 * 	使用UIPlugin 来完成与ui挂起过程的交互。
 * 	具体的配置方式如下：
 * &ltconfig type="com.ifeng.common.workflow.ActivityDefinition" name="QualificationResult"&gt
 *     &lttitle&gt判断审核结果&lt/title&gt
 *     &ltdescription&gt判断审核结果·&lt/description&gt
 *     &ltplugin type="com.ifeng.common.plugin.process.ConditionSuite"&gt
 *       &ltcondition&gt......&lt/condition&gt
 *       &lton-true&gt
 *         ....
 *       &lt/on-true&gt
 *     &lt/plugin&gt
 *  &lt/config&gt
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class ActivityDefinition implements Configurable {

    private IntfPlugin plugin;

    private String name;

    private String title;

    private String description;

    public ActivityDefinition() {
    }

    public ActivityDefinition(IntfPlugin plugin, String name, String title,
            String description) {
        this.plugin = plugin;
        this.name = name;
        this.title = title;
        this.description = description;
    }

    public Object config(ConfigRoot configRoot, Object parent, Element configEle)
            throws ConfigException {
        this.name = XmlLoader.getAttribute(configEle, "name");
        Element descriptionEle = XmlLoader.getChildElement(configEle, null,
                "description");
        if (descriptionEle != null) {
            this.description = XmlLoader.getElementText(descriptionEle);
        } else {
            this.description = "";
        }
        Element titleEle = XmlLoader.getChildElement(configEle, null, "title");
        if (titleEle != null) {
            this.title = XmlLoader.getElementText(titleEle);
        } else {
            throw new ConfigException(configEle, "'title' subelement required");
        }
        
        this.plugin = (IntfPlugin)configRoot.createChildObject(this, configEle,"plugin");
        return this;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public IntfPlugin getPlugin() {
        return plugin;
    }

    public String getTitle() {
        return title;
    }

}
