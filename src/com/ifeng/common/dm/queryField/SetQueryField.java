package com.ifeng.common.dm.queryField;

import java.util.Arrays;
import java.util.List;

import com.ifeng.common.dm.QueryField;


/**
 * <title>SetQueryField </title>
 * 
 * <pre>用于一个匹配集合的QueryField
 * 长度为0的set和null是不同的。长度为0表示任何东西都不符合。
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class SetQueryField implements QueryField {

    private final Object[] set;
    public SetQueryField(Object[] set) {
        this.set = set == null ? null : (Object[])set.clone();
    }
    
    public boolean getQL(String fieldName, StringBuffer ql) {
        if (this.set != null) {
            if (this.set.length == 0) {
                ql.append("1=0");
                return true;
            }
            ql.append(fieldName).append(" in (");
            for (int i = 0; i < this.set.length; i++) {
                ql.append(i == this.set.length - 1 ? "?)" : "?,");
            }
            return true;
        }
        return false;
    }

    public boolean testObject(Object obj) {
        if (this.set != null) {
            for (int i = 0; i < this.set.length; i++) {
                if (this.set[i].equals(obj)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
    public void getParameters(List list) {
        if (this.set != null) {
            for (int i = 0; i < this.set.length; i++) {
                list.add(this.set[i]);
            }
        }
    }

    public boolean getFromClause(String prefix, StringBuffer ql) {
        return false;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SetQueryField) {
            return Arrays.equals(this.set, ((SetQueryField)obj).set);
        }
        return false;
    }
    
    public int hashCode() {
        return this.set == null ? 0 : this.set.length;
    }
    
}
