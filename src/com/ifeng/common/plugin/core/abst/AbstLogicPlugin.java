package com.ifeng.common.plugin.core.abst;

import com.ifeng.common.plugin.core.itf.IntfPlugin;

/**
 * IntfPlugin的抽象类，提供了进行isSuccess判断的功能。
 * @author yudf
 *
 */
public abstract class AbstLogicPlugin implements IntfPlugin{
	
	public boolean isSuccess(Object o){
		if(o instanceof Boolean){
			return ((Boolean) o).booleanValue();
		}else{
			return o!=null;
		}
	}
}
