package com.ifeng.common.plugin.process;

import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;

/**
 * AbstLogicPlugin的实现类，在execte方法中返回注入的对象;默认是Boolean.True
 * @author chenyong
 */

public class RevertPlugin extends AbstLogicPlugin {
	
	private Object fireball = Boolean.TRUE;
	public Object getFireball() {
		return fireball;
	}
	public void setFireball(Object fireball) {
		this.fireball = fireball;
	}
	public Object execute(Object o) {
		return fireball;
	}

}
