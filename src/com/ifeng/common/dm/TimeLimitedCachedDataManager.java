package com.ifeng.common.dm;

import java.util.HashMap;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.misc.TimeLimitedMap;
import com.ifeng.common.misc.XmlLoader;

/**
 * <title>TimeLimitedCachedDataManager </title>
 * 
 * <pre>
 *  基于超时的DataManager,支持ID查询时使用缓存，超时淘汰策略。
 * </pre>
 * 
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public class TimeLimitedCachedDataManager extends AbstractCachedDataManager {


	public TimeLimitedCachedDataManager(DataManager manager, int time) {
		super(manager);
		data = new TimeLimitedMap(new HashMap(), time);
	}
	public TimeLimitedCachedDataManager(){
		super(null);
	}
	/**
	 * <pre>
	 *   &lt;... type=&quot;com.ifeng.common.dm.TimeLimitedCachedDataManager&quot; time-out=&quot;缓存时间（单位毫秒）&quot;&gt;
	 *     &lt;data-manager ... 内部datamanager的配置
	 *     /&gt;
	 *   &lt;/...&gt;
	 * </pre>
	 */
	public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
		data = new TimeLimitedMap(new HashMap(), (XmlLoader
				.getAttributeAsInteger(configEle, "time-out")));
		return super.config(configRoot, parent, configEle);
	}
}
