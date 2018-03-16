package com.ifeng.common.remoting.client;

/**
 * <title> 客户端访问接口</title>
 * 
 * <pre>
 * 客户端访问接口，获取服务调用对象
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public interface Client {
	public static final String ANY_SERVER="ANY_SERVER";
	/**
     * 根据名字和接口类解析某个静态注册的服务或对象
     * @param objName 服务对象的名字，必须与服务器端注册的名字一致
     * @param remoteClass 对象的远程接口类(或对象类)。如果是null，则返回
     *   一个服务端对象的副本，而不对其做任何remote调用
     * @return 要解析的对象
     */
    public Object getServiceObj(String objName, Class remoteClass);
    
    public AnyCall getAnyCall(String objName);
}
