package com.ifeng.common.dm.persist;

import java.util.List;

import com.ifeng.common.dm.QueryResult;

public class QueryResultWrapper extends ObjectWrapper {

    private static final long serialVersionUID = 3977016232383755577L;

    public QueryResultWrapper(List lazyCollections, QueryResult queryResult) {
        super(lazyCollections, queryResult);
    }

    public QueryResult getQueryResult() {
        return (QueryResult)super.getObject();
    }
    
    public void setQueryResult(QueryResult queryResult) {
        super.setObject(queryResult);
    }
    
}
