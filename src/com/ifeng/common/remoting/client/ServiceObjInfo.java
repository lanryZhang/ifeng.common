package com.ifeng.common.remoting.client;

import java.io.Serializable;

/**
 * 服务器对象的客户端信息结构
 */
class ServiceObjInfo {
	/**
	 * 远程对象的客户端stub(proxy)。 创建ServiceObjInfo之后，必须调用setStub来设定它
	 */
	private Object stub;
	private String objName;
	private Class remoteClass;

	public ServiceObjInfo(String objName, Class remoteClass) {

		this.objName = objName;
		this.remoteClass = remoteClass;
	}

	public String getObjName() {
		return objName;
	}

	public void setObjName(String objName) {
		this.objName = objName;
	}

	public Class getRemoteClass() {
		return remoteClass;
	}

	public void setRemoteClass(Class remoteClass) {
		this.remoteClass = remoteClass;
	}

	public Object getStub() {
		return stub;
	}

	public void setStub(Object stub) {
		this.stub = stub;
	}

}