package com.ifeng.common.remoting.client;


import com.ifeng.common.misc.Logger;
/**
 * 继承AbstractInvokeHandler，提供对客户端对象调用的封装
 */
class NormalInvokeHandler extends AbstractInvokeHandler {

	private static final Logger log = Logger
			.getLogger(NormalInvokeHandler.class);

	public NormalInvokeHandler(Client context, ServiceObjInfo info) {
		super(context, info);
	}

	public NormalInvokeHandler(AbstractInvokeHandler org) {
		super(org);
	}

	protected AnyCall getAnyCall(String objName) {
		return super.client.getAnyCall(objName);
	}
}
