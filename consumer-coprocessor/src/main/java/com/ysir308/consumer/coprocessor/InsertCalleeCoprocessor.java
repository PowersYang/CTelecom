package com.ysir308.consumer.coprocessor;

import com.ysir308.common.bean.BaseDao;
import com.ysir308.common.constant.Names;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * 使用协处理器保存被叫用户的数据
 * <p>
 * 协处理器的类
 * 1、创建类
 * 2、让表知道协处理类（和表进行关联）
 * 3、将项目打成jar包发布到hbase中（关联的项目也要打包），并且分发到其它机器
 */
public class InsertCalleeCoprocessor extends BaseRegionObserver {

    /**
     * 保存主叫用户数据之后，由HBase自动保存被叫用户数据
     *
     * @param e
     * @param put        主叫用户的数据
     * @param edit
     * @param durability
     * @throws IOException
     */
    @Override
    public void postPut(ObserverContext<RegionCoprocessorEnvironment> e, Put put, WALEdit edit, Durability durability) throws IOException {

        Table table = e.getEnvironment().getTable(TableName.valueOf(Names.TABLE.getValue()));

        // 主叫用户信息
        // rowkey格式为 分区号_主叫用户_时间_被叫用户_时长_flag
        String rowkey = Bytes.toString(put.getRow());
        String[] values = rowkey.split("_");

        // 根据主叫用户信息获取被叫用户信息
        String call1 = values[1];
        String call2 = values[3];
        String calltime = values[2];
        String duration = values[4];
        String flg = values[5];

        if ("1".equals(flg)) {
            // 只有主叫用户保存后才需要触发保存被叫用户
            String calleeRowkey = CoprocessorDao.getRegionNum(call2, calltime) + "_" + call2 + "_" + calltime + "_" + call1 + "_" + duration + "_0";
            Put calleePut = new Put(Bytes.toBytes(calleeRowkey));
            byte[] calleeFamily = Bytes.toBytes(Names.CF_CALLEE.getValue());
            calleePut.addColumn(calleeFamily, Bytes.toBytes("call1"), Bytes.toBytes(call2));
            calleePut.addColumn(calleeFamily, Bytes.toBytes("call2"), Bytes.toBytes(call1));
            calleePut.addColumn(calleeFamily, Bytes.toBytes("calltime"), Bytes.toBytes(calltime));
            calleePut.addColumn(calleeFamily, Bytes.toBytes("duration"), Bytes.toBytes(duration));
            calleePut.addColumn(calleeFamily, Bytes.toBytes("flg"), Bytes.toBytes("0"));

            table.put(calleePut);

            table.close();
        }
    }

    private static class CoprocessorDao extends BaseDao {
        public static int getRegionNum(String tel, String time) {
            return genRegionNum(tel, time);
        }
    }
}