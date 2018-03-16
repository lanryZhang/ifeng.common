package com.ifeng.common.remoting.client;

/**
 * 一个实现无关的AnyCall接口，每种不同的Client实现中，有不同的AnyCall实现。
 * @author jinmy
 */
interface AnyCall {
    /**
     * 进行一次方法调用。
     * @param objName 对象名称
     * @param methodId 方法Id
     * @param args 参数列表
     * @return 调用结果
     * @throws Exception 任何调用本身的异常
     */
    public Object call(String objName, String methodId, Object[] args)
            throws Exception;
}