package com.ifeng.common.dm;



/**
 * <title>DataManagerValidationError </title>
 * 
 * <pre>DM合法性检查Error。可以做为DataManager validxxx方法的返回值。
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */


public class DataManagerValidationError {

    /**
     * form widget域名。
     * Comment for <code>formFieldName</code>
     */
    private String formFieldName;
    
    /**
     * form widget域错误描述。
     * Comment for <code>formFieldErrorDesc</code>
     */
    private String formFieldErrorDesc;
    private String[] params;
    /**
     * 构造DM合法性检查Error
     * 
     * @param formFieldName
     *            non-null，form widget域名；
     *            null，不针对具体的widget域，将作为Exception抛出formFieldErrorDesc
     * @param formFieldErrorDesc
     *            form widget域错误描述，描述为FormsMessages.xml中的key值，显示对应的value，
     *            可用来描述通用的错误信息；若key未找到，则显示描述本身，可被用来描述专用的错误信息
     */
    public DataManagerValidationError(String formFieldName,
            String formFieldErrorDesc) {
        this(formFieldName, formFieldErrorDesc, new String[0]);
    }
    
    /**
     * 构造DM合法性检查Error。
     * 前两个参数见上面
     * @param params 如果formFieldErrorDesc中需要参数，由params来传递
     */
    public DataManagerValidationError(String formFieldName,
            String formFieldErrorDesc, String[] params) {
        this.formFieldName = formFieldName;
        this.formFieldErrorDesc = formFieldErrorDesc;
        this.params = params;
    }
    
    /**
     * @return Returns the formFieldErrorDesc.
     */
    public String getFormFieldErrorDesc() {
        return formFieldErrorDesc;
    }
    /**
     * @param formFieldErrorDesc The formFieldErrorDesc to set.
     */
    public void setFormFieldErrorDesc(String formFieldErrorDesc) {
        this.formFieldErrorDesc = formFieldErrorDesc;
    }
    /**
     * @return Returns the formFieldName.
     */
    public String getFormFieldName() {
        return formFieldName;
    }
    /**
     * @param formFieldName The formFieldName to set.
     */
    public void setFormFieldName(String formFieldName) {
        this.formFieldName = formFieldName;
    }
    
    public String[] getParams() {
        return params;
    }
    public void setParams(String[] params) {
        this.params = params;
    }
    
    public String toString() {
        return this.formFieldErrorDesc;
    }
}
