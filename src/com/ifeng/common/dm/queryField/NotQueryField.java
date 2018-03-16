package com.ifeng.common.dm.queryField;

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import com.ifeng.common.dm.QueryField;

/**
 * <title>NotQueryField </title>
 * 
 * <pre>给另一个QueryField取反
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class NotQueryField implements QueryField {

    private QueryField original;

    public NotQueryField(QueryField original) {
        this.original = original;
    }
    
    public boolean getQL(String fieldName, StringBuffer ql) {
        StringBuffer sb = new StringBuffer();
        boolean result = this.original.getQL(fieldName, sb);
        if (result) {
            ql.append("not(").append(sb).append(')');
            return true;
        }
        return false;
    }

    public boolean testObject(Object obj) {
        return !this.original.testObject(obj);
    }

    public void getParameters(List list) {
        this.original.getParameters(list);
    }

    public boolean getFromClause(String prefix, StringBuffer ql) {
        return this.original.getFromClause(prefix, ql);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof NotQueryField) {
            return ObjectUtils.equals(this.original, ((NotQueryField)obj).original);
        }
        return false;
    }
    
    public int hashCode() {
        return this.original.hashCode(); 
    }
    
}
