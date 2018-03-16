package com.ifeng.common.dm.queryField;

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import com.ifeng.common.dm.QueryField;


/**
 * <title>RangeQueryField </title>
 * 
 * <pre>用于处理一个比较范围的的QueryField
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class RangeQueryField implements QueryField {
    private Object start;
    private Object end;

    public RangeQueryField(Object start, Object end) {
        this.start = start;
        this.end = end;
    }
    /**
     * @return Returns the end.
     */
    public Object getEnd() {
        return end;
    }
    /**
     * @param end
     *            The end to set.
     */
    public void setEnd(Object end) {
        this.end = end;
    }
    /**
     * @return Returns the start.
     */
    public Object getStart() {
        return start;
    }
    /**
     * @param start
     *            The start to set.
     */
    public void setStart(Object start) {
        this.start = start;
    }
    
    public boolean getQL(String fieldName, StringBuffer ql) {
        if (this.start == null) {
            if (this.end == null) {
                return false;
            } else {
                ql.append(fieldName).append("<=?");
                return true;
            }
        } else {
            if (this.end == null) {
                ql.append(fieldName).append(">=?");
            } else {
                ql.append(fieldName).append(" between ? and ?");
            }
            return true;
        }
    }
    
    public void getParameters(List list) {
        if (this.start != null) {
            list.add(this.start);
        }
        if (this.end != null) {
            list.add(this.end);
        }
    }
    public boolean testObject(Object obj) {
        return (this.start == null || ((Comparable)this.start).compareTo(obj) <= 0)
                && (this.end == null || ((Comparable)this.end).compareTo(obj) >= 0);
    }
    public boolean getFromClause(String prefix, StringBuffer ql) {
        return false;
    }
    
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof RangeQueryField) {
            return ObjectUtils.equals(this.start, ((RangeQueryField)obj).start)
                    && ObjectUtils.equals(this.end, ((RangeQueryField)obj).end);
        }
        return false;
    }
    
    public int hashCode() {
        return (this.start == null ? 0 : this.start.hashCode() * 5)
                + (this.end == null ? 0 : this.end.hashCode() * 17);
    }
    
}
