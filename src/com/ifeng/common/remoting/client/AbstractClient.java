package com.ifeng.common.remoting.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxy.Enhancer;
import com.ifeng.common.remoting.server.Server;
/**
 * <title> Client的RMI方式实现</title>
 * 
 * <pre>
 * 客户端抽象类，用于整合客户端调用的AOP方法拦截，并保持服务调用信息
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public abstract class AbstractClient implements Client{
	Map serviceObjs = Collections.synchronizedMap(new HashMap());
	 /**
     * 根据名字和接口类解析某个静态注册的服务或对象
     * @param serviceObjName 服务对象的名字，必须与服务器端注册的名字一致
     * @param remoteClass 对象的远程接口类(或对象类)
     * @return 要解析的对象
     * (no @-CheckStyle bug)throws RuntimeException (unchecked) 如果解析中发生异常
     */
    public synchronized Object getServiceObj(String serviceObjName,
            Class remoteClass) {
        ServiceObjInfo info = (ServiceObjInfo)this.serviceObjs
                .get(serviceObjName);
        if (info != null) {
            return info.getStub();
        }

        info = new ServiceObjInfo(serviceObjName, remoteClass);
        Object serviceObj;
        if (remoteClass == null) {
            try {
                // 没有指定remote class，也就是说没有任何调用需要远程调用
                // 直接得到服务器端的一个对象的拷贝
                AbstractInvokeHandler invokeHandler = new NormalInvokeHandler(
                        this, info); 
                serviceObj = invokeHandler.dispatch(Server.GETOBJECT, null);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        } else {
            serviceObj = Enhancer.create(info.getRemoteClass(),new NormalInvokeHandler(this, info));
        }
        info.setStub(serviceObj);
        this.serviceObjs.put(serviceObjName, info);
        return serviceObj;
    }
}
