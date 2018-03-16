package com.ifeng.common.remoting.server.rmi;

public class RMIAnyCallImpl_Stub
 extends java.rmi.server.RemoteStub
 implements com.ifeng.common.remoting.server.rmi.RMIAnyCall, java.rmi.Remote
{
 private static final long serialVersionUID = 2;
 
 private static java.lang.reflect.Method $method_call_0;
 
 static {
	try {
	    $method_call_0 = com.ifeng.common.remoting.server.rmi.RMIAnyCall.class.getMethod("call", new java.lang.Class[] {java.lang.String.class, java.lang.String.class, java.lang.Object[].class});
	} catch (java.lang.NoSuchMethodException e) {
	    throw new java.lang.NoSuchMethodError(
		"stub class initialization failed");
	}
 }
 
 // constructors
 public RMIAnyCallImpl_Stub(java.rmi.server.RemoteRef ref) {
	super(ref);
 }
 
 // methods from remote interfaces
 
 // implementation of call(String, String, Object[])
 public java.lang.Object call(java.lang.String $param_String_1, java.lang.String $param_String_2, java.lang.Object[] $param_arrayOf_Object_3)
	throws java.rmi.RemoteException
 {
	try {
	    return ref.invoke(this, $method_call_0, new java.lang.Object[] {$param_String_1, $param_String_2, $param_arrayOf_Object_3}, 2749389479362523638L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
 }
}
