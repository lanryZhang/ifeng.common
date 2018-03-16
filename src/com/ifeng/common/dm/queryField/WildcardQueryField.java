package com.ifeng.common.dm.queryField;



/**
 * <title>WildcardQueryField </title>
 * 
 * <pre>带通配符的查询字段。用'*'表示匹配任何子串
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class WildcardQueryField extends NormalQueryField {

    private boolean hasWildcard;
    
    public WildcardQueryField(Object value) {
        super(value);
        if (value != null) {
            if (value instanceof String) {
                String valueStr = (String)value;
                valueStr = valueStr.replace('*', '%').replace('?', '_');
                this.hasWildcard = valueStr.indexOf('%') >= 0
                        || valueStr.indexOf('_') >= 0;
                setValue(valueStr);
            } else {
                throw new IllegalArgumentException(
                        "WildcardQueryField requires the value is a string");
            }
        }
    }

    public boolean getQL(String fieldName, StringBuffer ql) {
        if (this.hasWildcard) {
            ql.append(fieldName).append(" like ?");
            return true;
        }
        return super.getQL(fieldName, ql);
    }
   
    public boolean testObject(Object obj) {
        return super.testObject(obj);
    }

}
