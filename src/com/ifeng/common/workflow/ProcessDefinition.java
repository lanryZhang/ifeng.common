package com.ifeng.common.workflow;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.XmlLoader;

/**
 * <title>ProcessDefinition </title>
 * 
 * <pre>
 * 	工作流的流程定义,一个流程由多个Activity组成,Activity的数据结构参见ActivityDefinition. 
 * 	配置方式如下：
 * &ltconfig name="test" type="com.ifeng.common.workflow.ProcessDefinition"&gt
 *     &lttitle&gt申请开通&lt/title&gt
 *     &ltdescription&gt申请开通(此流程启动)&lt/description&gt
 *  
 *     &ltactivity type="com.ifeng.common.workflow.ActivityDefinition" name="QualificationResult"&gt
 *         &lttitle&gt判断审核结果&lt/title&gt
 *         &ltdescription&gt判断审核结果·&lt/description&gt
 *         &ltplugin type="com.ifeng.common.plugin.process.ConditionSuite"&gt
 *             &ltcondition&gt......&lt/condition&gt
 *             &lton-true&gt
 *                 ....
 *             &lt/on-true&gt
 *         &lt/plugin&gt
 *	   &lt/activity&gt
 *  
 *     &ltactivity 
 *  	   .....
 *     &lt/activity&gt
 * &ltconfig&gt
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public class ProcessDefinition implements Configurable {

    /** key: activity name(String), value: Activity */
    private Map activities = new HashMap();

    private String name;

    private String title;

    private String description;

    private String firstActivityName;

    public ProcessDefinition() {
        // for config
    }

    public ProcessDefinition(Map activities, String name, String title,
            String description, String firstActivityName) {
        this.activities.putAll(activities);
        this.name = name;
        this.title = title;
        this.description = description;
        this.firstActivityName = firstActivityName;
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

        // 配置activity
        List activitiesList = configRoot.createChildObjects(this, configEle,
                "activity", ActivityDefinition.class);
        for (Iterator it = activitiesList.iterator(); it.hasNext();) {
            ActivityDefinition activity = (ActivityDefinition)it.next();
            this.activities.put(activity.getName(), activity);
            if (this.firstActivityName == null) {
                this.firstActivityName = activity.getName();
            }
        }
        if (this.firstActivityName == null) {
            throw new ConfigException(configEle,
                    "At least one activity required");
        }
        return this;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getFirstActivityName() {
        return firstActivityName;
    }

    /**
     * 得到所有活动定义。
     * @return 活动定义的不可修改的副本
     */
    public Map getActivities() {
        return UnmodifiableMap.decorate(this.activities);
    }

    /**
     * 根据活动名得到活动定义。
     * @param activityName
     * @return 对应名字的活动定义。如果不存在这个名字的活动，返回null。
     */
    public ActivityDefinition getActivity(String activityName) {
        return (ActivityDefinition)this.activities.get(activityName);
    }

}
