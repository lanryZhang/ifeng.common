package com.ifeng.common.remoting.client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.w3c.dom.Element;

import com.ifeng.common.remoting.server.rmi.RMIAnyCall;
import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.dm.persist.intf.PersistManagerFactory;


/**
 * <title> Client的RMI方式实现</title>
 * 
 * <pre>
 * 用于代理客户端对象访问，本类实现了RMI注册訪問，并對AnyCall于RMIAnyCall進行了適配。
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 * @author <a href="mailto:liyi@ifeng.com">liyi</a>
 */
public class ClientRMIImpl extends AbstractClient implements Configurable{
	private String host = "localhost";
	private int port = 8889;

	/**
	 * 用于配置的情况
	 */
	public ClientRMIImpl() {
		super();
	}

	/**
	 * RMI的AnyCall适配器
	 */
	protected static class RMIAnyCallAdapter implements AnyCall {
		private RMIAnyCall rmiAnyCall;

		public RMIAnyCallAdapter(RMIAnyCall rmiAnyCall) {
			this.rmiAnyCall = rmiAnyCall;
		}

		public Object call(String objName, String methodId, Object[] args)
				throws Exception {
			return this.rmiAnyCall.call(objName, methodId, args);
		}
	}


	/**
	 * 獲取遠程訪問對象
	 */
	public AnyCall getAnyCall(String objName) {
		AnyCall call = null;
		try {
			/*
			 * 得到远程发布的服务 返回与指定 name 关联的远程对象的引用（一个stub）
			 */
			String url = String.format("rmi://%s:%d/%s", host, port, objName);
			RMIAnyCall obj = (RMIAnyCall) Naming.lookup(url); // 注：通过接口拿 
			call = new RMIAnyCallAdapter(obj);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
		return call;
	}

	/**
	 * 用于配置的情况
	 */
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		String host =  (String)configRoot.createChildObject(this, configEle, "host", true);
		int port =  (Integer)configRoot.createChildObject(this, configEle, "port", true);
		if(null != host && !"".equals(host)) {
			this.host = host;
		}
		if( port != 0) {
			this.port = port;
		}
		if( this.port == 0 ) {
			 throw  new ConfigException("The port couldn't be empty.");
		}
		return this;
	}
}
