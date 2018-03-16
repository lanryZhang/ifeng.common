package com.ifeng.common.dm;

import java.io.Serializable;
import java.util.List;

public interface QueryResult extends Serializable {
    /**
     * 得到查询结果的行数
     * 
     * @return 查询结果行数
     */
    int getRowCount();

    /**
     * 得到查询结果的数据
     * 
     * @param start
     *            起始行号(包含)
     * @param end
     *            结束行号(不包含)
     * @return 查询结果数据列表
     */
    List getData(int start, int end);
    
    /**
     * 得到某一行数据
     * @param index  行号
     * @return 该行的数据。如果没有，返回null
     */
    Object getData(int index);
}
