package com.ifeng.common.remoting.server.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 集中的对外RMI接口
 */
public interface RMIAnyCall extends Remote  {
    /**
     * 执行任意指定的请求。
     * @param objName 服务端对象名称
     * @param methodId 请求方法的ID，按照MethodSignatureUtils.getMethodId生成
     * @param args 请求参数
     * @return 返回值
     * @throws RemoteException 只是为了符合RMI的规定。实际上异常是通过返回值传递的
     */
    Object call(String objName, String methodId, Object[] args)
            throws RemoteException;
}
