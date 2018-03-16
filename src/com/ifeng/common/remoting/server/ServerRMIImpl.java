package com.ifeng.common.remoting.server;

import java.io.Serializable;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteStub;
import java.rmi.server.UnicastRemoteObject;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.remoting.server.rmi.RMIAnyCall;
import com.ifeng.common.remoting.server.rmi.RMIAnyCallImpl;

/**
 * <title> RMI服务端代理调用</title>
 * 
 * <pre>
 * 用于代理客户端对象访问，本类实现了RMI注册的创建与AnyCall对象的创建。
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 * @author <a href="mailto:liyi@ifeng.com">liyi</a>
 */
public class ServerRMIImpl extends Server {
	private static final Logger log = Logger.getLogger(ServerRMIImpl.class);
	private RMIAnyCall anyCall;

	private RemoteStub anyCallStub;
	private String host = "";
	private int port = 8889;

	public ServerRMIImpl() {
	}

	/**
	 * 用于配置的情况
	 */
	public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
		super.config(configRoot, parent, configEle);
		String host = (String) configRoot.createChildObject(this, configEle,
				"host", true);
		int port = (Integer) configRoot.createChildObject(this, configEle,
				"port", true);
		if(null != host && !"".equals(host)) {
			this.host = host;
		}
		if( port != 0) {
			this.port = port;
		}
		log.info(String.format( "Get configure params host %s, port %d.", host, port));
		initRMI();
		return this;
	}

	public ServerRMIImpl(String serviceName) {
		super(serviceName);
		initRMI();
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * 创建RMI注册器
	 */
	private Registry createRegistry() {
		Registry registry = null;
		try {
			registry = LocateRegistry.getRegistry(port);
			registry.list();
		} catch (final Exception e) {
			log.warn("Registry not exist, try to create it.");
			try {
				registry = LocateRegistry.createRegistry(port);
			} catch (final Exception ee) {
				throw new RuntimeException(ee);
			}
		}
		return registry;
	}

	/**
	 * RMI服务绑定
	 */
	public void bind() {
		Registry registry = createRegistry();
		try {
			registry.rebind(serviceName, this.anyCall);
			//registry.rebind(serviceName, this.anyCall);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	private void initRMI() {
		try {
			this.anyCall = new RMIAnyCallImpl(this);
			// TODO
			bind();
			// 由于上面new RMIAnyCall已经export了，为了得到stub，
			// 这里先unexport，再export
			UnicastRemoteObject.unexportObject(this.anyCall, true);
			this.anyCallStub = UnicastRemoteObject.exportObject(this.anyCall);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public Serializable startInternal() {
		return this.anyCallStub;
	}

	protected void stopInternal() {
	}
}
