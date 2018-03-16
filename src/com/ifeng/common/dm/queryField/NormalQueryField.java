package com.ifeng.common.dm.queryField;

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import com.ifeng.common.dm.QueryField;
/**
 * <title>NormalQueryField </title>
 * 
 * <pre>表示普通的查询字段。具体怎么“普通”，取决于DataManager的实现。
 * 一般是在非null时直接匹配(等于)，null时不匹配
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class NormalQueryField implements QueryField {
    private Object value;

    public NormalQueryField(Object value) {
        this.value = value;
    }
    /**
     * @return Returns the value.
     */
    public Object getValue() {
        return value;
    }
    /**
     * @param value
     *            The value to set.
     */
    public void setValue(Object value) {
        this.value = value;
    }
    
    public boolean getQL(String fieldName, StringBuffer ql) {
        if (this.value == null) {
            return false;
        }
        ql.append(fieldName).append("=?");
        return true;
    }
    
    public void getParameters(List list) {
        if (this.value != null) {
            list.add(this.value);
        }
    }
    
    public boolean testObject(Object obj) {
        return this.value == null || obj.equals(this.value);
    }
    
    public boolean getFromClause(String prefix, StringBuffer ql) {
        return false;
    }
    
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof NormalQueryField) {
            return ObjectUtils.equals(this.value, ((NormalQueryField)obj).value);
        }
        return false;
    }
    
    public int hashCode() {
        return this.value == null ? 0 : this.value.hashCode();
    }
    
}
