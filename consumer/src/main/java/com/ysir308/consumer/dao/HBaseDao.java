package com.ysir308.consumer.dao;

import com.ysir308.common.bean.BaseDao;
import com.ysir308.common.constant.Names;
import com.ysir308.common.constant.ValueConstant;
import com.ysir308.consumer.bean.Calllog;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        createTableXX(Names.TABLE.getValue(), "com.ysir308.consumer.coprocessor.InsertCalleeCoprocessor", ValueConstant.REGION_COUNT, Names.CF_CALLER.getValue(), Names.CF_CALLEE.getValue());

        end();
    }

    /**
     * 插入对象
     *
     * @param calllog
     * @throws IOException
     */
    public void insertData(Calllog calllog) throws Exception {
        String rowkey = genRegionNum(calllog.getCall1(), calllog.getCalltime()) + "_" + calllog.getCall1() + "_" +
                calllog.getCalltime() + "_" + calllog.getCall2() + "_" + calllog.getDuration();

        calllog.setRowkey(rowkey);
        putData(calllog);
    }


    /**
     * 插入数据
     *
     * @param value
     */
    public void insertDatas(String value) throws IOException {
        // 将通话日志保存到HBase表中
        // 1、获取通话日志数据
        String[] values = value.split("\t");
        String call1 = values[0];
        String call2 = values[1];
        String calltime = values[2];
        String duration = values[3];

        // 2、创建数据对象
        // rowkey的设计
        // 1)长度原则：最大值为64KB，推荐长度为10 ~ 100个字节（最好是8的倍数，能短则短，如果太长会影响性能）
        // 2)唯一性原则
        // 3)散列原则：
        //      3-1、盐值散列：不能使用时间戳直接作为rowkey，可以在rowKey前面增加随机数
        //      3-2、字符串反转：时间戳反转和电话号码反转用的多
        //      3-3、计算分区号

        // rowKey = regionNum + call1 + time + call2 + duration
        // 主叫用户
        String rowkey = genRegionNum(call1, calltime) + "_" + call1 + "_" + calltime + "_" + call2 + "_" + duration + "_1";
        Put put = new Put(Bytes.toBytes(rowkey));
        byte[] family = Bytes.toBytes(Names.CF_CALLER.getValue());
        put.addColumn(family, Bytes.toBytes("call1"), Bytes.toBytes(call1));
        put.addColumn(family, Bytes.toBytes("call2"), Bytes.toBytes(call2));
        put.addColumn(family, Bytes.toBytes("calltime"), Bytes.toBytes(calltime));
        put.addColumn(family, Bytes.toBytes("duration"), Bytes.toBytes(duration));
        put.addColumn(family, Bytes.toBytes("flg"), Bytes.toBytes("1"));

        // 被叫用户
//        String calleeRowkey = genRegionNum(call2, calltime) + "_" + call2 + "_" + calltime + "_" + call1 + "_" + duration + "_0";
//        Put calleePut = new Put(Bytes.toBytes(calleeRowkey));
//        byte[] calleeFamily = Bytes.toBytes(Names.CF_CALLEE.getValue());
//        calleePut.addColumn(calleeFamily, Bytes.toBytes("call1"), Bytes.toBytes(call2));
//        calleePut.addColumn(calleeFamily, Bytes.toBytes("call2"), Bytes.toBytes(call1));
//        calleePut.addColumn(calleeFamily, Bytes.toBytes("calltime"), Bytes.toBytes(calltime));
//        calleePut.addColumn(calleeFamily, Bytes.toBytes("duration"), Bytes.toBytes(duration));
//        calleePut.addColumn(calleeFamily, Bytes.toBytes("flg"), Bytes.toBytes("0"));

        // 3、保存数据
        List<Put> puts = new ArrayList<>();
        puts.add(put);
//        puts.add(calleePut);

        putData(Names.TABLE.getValue(), puts);
    }
}
