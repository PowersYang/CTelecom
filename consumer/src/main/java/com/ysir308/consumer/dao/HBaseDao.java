package com.ysir308.consumer.dao;

import com.ysir308.common.bean.BaseDao;
import com.ysir308.common.constant.Names;

import java.io.IOException;

/**
 * HBase的访问对象
 */
public class HBaseDao extends BaseDao {

    /**
     * 初始化
     */
    public void init() throws IOException {
        start();

        createNamespaceNX(Names.NAMESPACE.getValue());
        createTableXX(Names.TABLE.getValue(), Names.CF_CALLER.getValue());

        end();
    }

    /**
     * 插入数据
     * @param value
     */
    public void insertDatas(String value) {

    }
}
