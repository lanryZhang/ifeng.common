package com.ifeng.common.dm.persist;

import java.io.Serializable;
import java.util.List;
/**
 * <title>HQuery </title>
 * 
 * <pre>HQL的语句封装类.
 * 该对象封装HQL的查询语句，参数集合，排序参数，分组参数，单页起始地址.
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class HQuery implements Serializable {
    private static final long serialVersionUID = 3832906563408310836L;
    /**
    * HQL查询语句
    */
    private String queryString;
    /**
    * 参数集合对象
    */
    private List paralist;
    /**
    * 排序字段
    */
    private String orderby;
    /**
    * 分组字段
    */
    private String groupby;
    /**
    * 分页起始查询地址
    */
    private int pageStartNo;
    /**
    * 每页大小
    */
    private int perPageSize;
    
    /**
    * 取得一个的query字符串
    * @return query字符串
    */
    public String getQueryString() {
        return queryString;
    }
    
    /**
    * 设置一个query查询字符串
    * @param queryString 查询字符串
    * 
    */
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }
    
    /**
    * 取得参数集合对象
    * @return 参数集合对象
    */
    public List getParalist() {
        return paralist;
    }
    
    /**
    * 设置参数集合对象
    * @param paralist 参数集合对象
    */
    public void setParalist(List paralist) {
        this.paralist = paralist;
    }

    /**
    * 取得排序字段
    * @return 排序字段
    */
    public String getOrderby() {
        return orderby;
    }
    
    /**
    * 设置排序字段
    * @param orderby 排序字段
    */
    public void setOrderby(String orderby) {
        this.orderby = orderby;
    }
    
    /**
    * 取得分组字段
    * @return 分组字段
    */
    public String getGroupby() {
        return groupby;
    }
    
    /**
    * 设置分组字段
    * @param groupby 分组字段
    */
    public void setGroupby(String groupby) {
        this.groupby = groupby;
    }
    
    /**
    * 取得页起始地址
    * @return 起始地址
    */
    public int getPageStartNo() {
        return pageStartNo;
    }
    
    /**
    * 设置页起始地址
    * @param pageStartNo 起始地址
    */
    public void setPageStartNo(int pageStartNo) {
        this.pageStartNo = pageStartNo;
    }
    
    /**
    * 取得每页大小
    * @return 每页大小
    */
    public int getPerPageSize() {
        return perPageSize;
    }
    
    /**
     * 设置每页大小.
     * @param perPageSize 每页大小
     */
    public void setPerPageSize(int perPageSize) {
        this.perPageSize = perPageSize;
    }
}

