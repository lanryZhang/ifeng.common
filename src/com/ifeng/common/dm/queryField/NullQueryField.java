package com.ifeng.common.dm.queryField;

import java.util.List;

import com.ifeng.common.dm.QueryField;

/**
 * <title>NullQueryField </title>
 * 
 * <pre>用于判断一个字段是否为null的QueryField
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class NullQueryField implements QueryField {

    public NullQueryField() {
    }
    
    public boolean getQL(String fieldName, StringBuffer ql) {
        ql.append(fieldName).append(" is null ");
        return true;
    }

    public boolean getFromClause(String fieldName, StringBuffer ql) {
        return false;
    }

    public boolean testObject(Object obj) {
        return obj == null;
    }

    public void getParameters(List list) {
        // do nothing, no parameter
    }

    public boolean equals(Object obj) {
        return obj == this || obj instanceof NullQueryField;
    }
    
    public int hashCode() {
        return 0;
    }
    
}
