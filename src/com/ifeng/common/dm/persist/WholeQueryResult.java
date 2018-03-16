package com.ifeng.common.dm.persist;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.ifeng.common.dm.QueryResult;

/**
 * <title>WholeQueryResult </title>
 * 
 * <pre>用于一次得到所有结果的QueryResult，一般适用于数据量较小的查询结果
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public class WholeQueryResult implements QueryResult, Serializable {
    private static final long serialVersionUID = 3906653002961269049L;

    public WholeQueryResult(List result) {
        this.result = result;
    }

 
    public int getRowCount() {
        return result.size();
    }

   
    public List getData(int start, int end) {
        if (end > result.size()) {
            return Collections.EMPTY_LIST;
        } else {
            return result.subList(start, end);
        }
    }

    public Object getData(int index) {
        if (index >= 0 && index < result.size()) {
            return result.get(index);
        }
        return null;
    }

    private List result;

}

