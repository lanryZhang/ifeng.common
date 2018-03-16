package com.ifeng.common.remoting.server.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.ifeng.common.misc.Logger;
import com.ifeng.common.remoting.server.ServerRMIImpl;

public class RMIAnyCallImpl extends UnicastRemoteObject implements RMIAnyCall{
	private static final long serialVersionUID = -4781843806335466925L;

	private static final Logger log = Logger.getLogger(RMIAnyCallImpl.class);
    
    private transient final ServerRMIImpl manager;

    public RMIAnyCallImpl(ServerRMIImpl impl) throws RemoteException {
        this.manager = impl;
    }

    public Object call(String objName, String methodId, Object[] args)
            throws RemoteException {
        if (log.isDebugEnabled()) {
            log.debug("RMI Call: objName=" + objName + " methodId=" + methodId
                    + " args.length=" + (args == null ? 0 : args.length));
        }
        Object result = this.manager.callInternal(objName, methodId, args);
        if (log.isDebugEnabled()) {
            log.debug("RMI Call result type="
                    + (result == null ? "null" : result.getClass().getName()));
        }
        return result;
    }
}
