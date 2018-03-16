package com.ifeng.common.dm;

import java.util.List;
/**
 * <title>QueryField </title>
 * 
 * <pre>用于表示查询对象中一个字段的条件
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public interface QueryField {
    /**
     * 得到相应字段的查询条件(SQL/OQL/HQL通用)。
     * 如果条件为空，应当返回null，而不是空字符串
     * @param fieldName 字段的名字
     * @param ql 查询语句，将本query field的查询语句附加到后面
     * @return true 如果有查询条件；false 如果没有查询条件
     */
    public boolean getQL(String fieldName, StringBuffer ql);
    
    /**
     * 得到所需的From子句的内容
     * @param prefix 对象的前缀 (查询的数据对象的别名)
     * @param ql 查询语句，将From子句的内容附加到后面。包含所需的逗号或join等
     * @return true 如果有内容；false如果没有内容
     */
    public boolean getFromClause(String prefix, StringBuffer ql);
    
    /**
     * 测试一个对象是否满足query条件。一般只是用于测试环境下
     * @param obj 测试对象
     * @return true 如果满足条件。不抛出异常，所有异常均返回false
     */
    public boolean testObject(Object obj);

    /**
     * 得到查询条件中'?'所对应的参数列表
     * @param list 参数列表，将本query field的参数附加到后面
     */
    public void getParameters(List list);
    
}
