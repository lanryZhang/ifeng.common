package com.ifeng.common.remoting.client;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import com.ifeng.common.misc.Logger;
import com.ifeng.common.misc.MethodSignatureUtils;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * CGLib的MethodInterceptor的实现，用于远程调用。
 * CGLib用这个MethodInterceptor生成远程调用的客户端Proxy对象。
 * 所有的远程调用均通过这个Proxy对象，每个方法调用由这个AbstractInvokeHandler 处理，转发给具体的远程调用实现的AnyCall
 * 
 * @author jinmy
 */
abstract class AbstractInvokeHandler implements MethodInterceptor {
	private static final Logger log = Logger
			.getLogger(AbstractInvokeHandler.class);

	/**
	 * 服务器对象信息结构
	 */
	protected final ServiceObjInfo info;

	/**
	 * 远程调用类的方法Id集合
	 */
	private Set remoteMethods;

	protected final Client client;

	/**
	 * 根据另一个InvokeHandler创建一个新的InvokeHandler
	 */
	public AbstractInvokeHandler(AbstractInvokeHandler org) {
		this.client = org.client;
		this.info = org.info;
		this.remoteMethods = org.remoteMethods;
	}

	/**
	 * 创建一个新的InvokeHandler
	 * 
	 * @param context
	 *            客户端上下文
	 * @param info
	 *            服务器端对象信息结构(刚刚创建，stub还没有生成)
	 */
	public AbstractInvokeHandler(Client client, ServiceObjInfo info) {
		this.client = client;
		this.info = info;
		this.remoteMethods = new HashSet();
		Method[] methods = info.getRemoteClass().getMethods();
		for (int i = 0; i < methods.length; i++) {
			this.remoteMethods.add(MethodSignatureUtils.getMethodId(methods[i]));
		}
	}

	public ServiceObjInfo getInfo() {
		return this.info;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.cglib.proxy.MethodInterceptor#intercept(java.lang.Object,
	 *      java.lang.reflect.Method, java.lang.Object[],
	 *      net.sf.cglib.proxy.MethodProxy)
	 */
	public Object intercept(Object object, Method method, Object[] args,
			MethodProxy proxy) throws Throwable {
		String methodId = MethodSignatureUtils.getMethodId(method);
		if (this.remoteMethods.contains(methodId)) {
			// 是一个远程method
			return dispatch(methodId, args);
		} else {
			// 缺省的其它方法调用
			return proxy.invokeSuper(object, args);
		}
	}

	/**
	 * 实际执行一个远程调用。一个框架方法，调用几个抽象方法
	 */
	protected Object dispatch(String methodId, Object[] args) throws Throwable {
		Object result = null;
		try {
			AnyCall any = getAnyCall( this.info.getObjName() );
			result = any.call( this.info.getObjName(), methodId, args);
		} catch (Exception e) {
			// 避免抛出非声明的exception
			if (e instanceof RuntimeException) {
				throw e;
			}
			// 如果是内部抛出的其它异常，需要用RemoteRuntimeException包装
			throw new RuntimeException(e);
		}
		return result;
	}

	/**
	 * 得到AnyCall，子类可以有不同的策略。
	 */
	protected abstract AnyCall getAnyCall(String objName);

}
