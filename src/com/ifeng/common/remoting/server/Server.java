package com.ifeng.common.remoting.server;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.misc.MethodSignatureUtils;
import com.ifeng.common.misc.XmlLoader;
/**
 * <title> RMI服务端代理抽象类</title>
 * 
 * <pre>
 * 真正实现了客户端对象代理访问
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 * @author <a href="mailto:liyi@ifeng.com">liyi</a>
 */
public abstract class Server implements Configurable{
	protected Serializable serverId;
	private static final Logger log = Logger.getLogger(Server.class);
	
	 // 服务对象map，key为服务对象名，value 为obj。包括动态服务对象
    protected Map<String,Object> serviceObjs = new HashMap<String, Object>();
    // key为服务对象名，value为methodMap
    protected Map<String,Map<String, Method>> objectMethodMaps = new HashMap<String, Map<String, Method>>();
    
    public static final String GETOBJECT = "##GETOBJECT";
    
	protected String serviceName;
	public Server(){
		
	}
	
	public Server(String serviceName){
		this.serviceName = serviceName;
	}
	
	public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
        this.serviceName = XmlLoader.getAttribute(configEle, "service-name");
        configServerObjects(configRoot, configEle);
        return this;
    }
	
	/**
	 * 远端服务(remoting)配置加载
	 */
	protected void configServerObjects(ConfigRoot configRoot, Element configEle) {
        for (Node node = configEle.getFirstChild(); node != null; node = node
                .getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element subConfigEle = (Element)node;
            String tag = subConfigEle.getTagName();
            if (tag.equals("service-object")) {
                String name = XmlLoader.getAttribute(subConfigEle, "name");
                String remoteClassName = subConfigEle
                        .getAttribute("remote-class");
                
                Object server = configRoot.createValueObject(this, subConfigEle,Object.class);
                Class remoteClass = server.getClass();
                if (remoteClassName.length() > 0) {
                    try {
                        remoteClass = Class.forName(remoteClassName);
                    } catch (ClassNotFoundException e) {
                        throw new ConfigException(subConfigEle,
                                "class not found: " + remoteClassName);
                    }
                }
                register(name, server, remoteClass);
            }
        }
    }
	/**
	 * 根据服务名，注册一个服务
	 */
	public void register(String serviceObjName, Object obj, Class clazz) {
        synchronized (this) {
            this.serviceObjs.put(serviceObjName, obj);
            this.objectMethodMaps.put(serviceObjName, getMethodMap(clazz));
        }
    }
	public void unregister(String serviceObjName) {
        synchronized (this) {
            this.serviceObjs.remove(serviceObjName);
            this.objectMethodMaps.remove(serviceObjName);
        }
	}  
	public final void start() {
        this.serverId = startInternal();
    }

    public final void stop() {
        stopInternal();
    }
    
    public Serializable getServerId() {
        return this.serverId;
    }
    
    /**
     * 子类实现。返回serverId。serverId时具体的远程调用实现中，用来标识一个特定
     * 服务器(进程)的对象。
     */
    protected abstract Serializable startInternal();
    
    /**
     * 子类实现。
     * 做一些stop时的清理工作。
     */
    protected abstract void stopInternal();
	
	private Map<String, Method> getMethodMap(Class clazz) {
        Map<String, Method> methodMap = new HashMap<String, Method>();
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            
            //使方法使用反射方式执行时跳过访问权限检查，以缩短执行时间（前面 getMethods() 得到的已经是 public 方法）
            method.setAccessible(true);
            
            String methodId = MethodSignatureUtils.getMethodId(method);
            methodMap.put(methodId, method);
        }
        // this.methodMapCache.put(clazz, methodMap);
        return methodMap;
    }
	/**
	 * 真正的代理调用，从注入信息获取到类、调用方法，通过method.invoke动态调用执行
	 */
	public Object callInternal(String objName, String methodId, Object[] args) {
        // 特殊方法
        if (methodId.equals(GETOBJECT)) {
            // 得到一个对象的副本
            return this.serviceObjs.get(objName);
        }

        // 先找到目标对象。这里不用同步this，因为只读，而且在读的过程中不会有并发修改
        Object obj = this.serviceObjs.get(objName);
        Map methodMap = (Map)this.objectMethodMaps.get(objName);
        if (obj == null) {
            throw new RuntimeException(
                    "Can't find object: " + objName);
        }
        if (methodMap == null) {
            throw new RuntimeException(
                    "Can't find methodMap: " + objName);
        }
        
        Method method = (Method)methodMap.get(methodId);
        if (method == null) {
            throw new RuntimeException(
                    "Can't find method: " + methodId);
        }
        try {
            return method.invoke(obj, args);
        } catch (InvocationTargetException e) {
            // 接口实现所产生的异常
            Throwable e1 = e.getCause();
            if (log.isInfoEnabled()) {
                log.info("Service method " + method + " throw exception: ", e1);
            } else {
                log.error("Service method " + method + " throw exception: " + e1);
            }
            return e;
        } catch (Throwable e) {
            // method invoke本身产生的错误
            if (log.isInfoEnabled()) {
                log.info("Error calling service method " + method, e);
            } else {
                log.error("Error calling service method " + method + " " + e);
            }
            throw new RuntimeException(e);
        }
    }
}
