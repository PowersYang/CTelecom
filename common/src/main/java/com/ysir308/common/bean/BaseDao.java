package com.ysir308.common.bean;

import com.ysir308.common.api.Column;
import com.ysir308.common.api.Rowkey;
import com.ysir308.common.api.TableRef;
import com.ysir308.common.constant.Names;
import com.ysir308.common.constant.ValueConstant;
import com.ysir308.common.util.DateUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 基础的数据访问对象
 */
public abstract class BaseDao {

    private ThreadLocal<Connection> connHolder = new ThreadLocal<>();
    private ThreadLocal<Admin> adminHolder = new ThreadLocal<>();

    protected void start() throws IOException {
        getConnection();
        getAdmin();
    }

    protected void end() throws IOException {
        Admin admin = getAdmin();
        if (admin != null) {
            admin.close();
            adminHolder.remove();
        }

        Connection conn = getConnection();
        if (conn != null) {
            conn.close();
            connHolder.remove();
        }

    }

    /**
     * 创建命名空间。如果命名空间存在，不创建；否则,创建新的。
     *
     * @param namespace
     */
    protected void createNamespaceNX(String namespace) throws IOException {
        Admin admin = getAdmin();

        try {
            admin.getNamespaceDescriptor(namespace);

        } catch (NamespaceNotFoundException e) {
            NamespaceDescriptor descriptor = NamespaceDescriptor.create(namespace).build();
            admin.createNamespace(descriptor);
        }
    }

    /**
     * 创建表。如果表存在，删除；否则，创建新的。
     *
     * @param name
     * @param families
     * @throws IOException
     */
    protected void createTableXX(String name, String... families) throws IOException {
        createTableXX(name, null, null, families);
    }

    protected void createTableXX(String name, String coprocessorClass, Integer regionCount, String... families) throws IOException {
        Admin admin = getAdmin();

        TableName tableName = TableName.valueOf(name);

        if (admin.tableExists(tableName)) {
            deleteTable(name);
        }

        createTable(name, coprocessorClass, regionCount, families);

    }

    private void createTable(String tableName, String coprocessorClass, Integer regionCount, String... families) throws IOException {
        Admin admin = getAdmin();
        HTableDescriptor descriptors = new HTableDescriptor(tableName);

        if (families == null || families.length == 0) {
            families = new String[1];
            families[0] = Names.CF_INFO.getValue();
        }

        for (String family : families) {
            HColumnDescriptor columnDescriptor = new HColumnDescriptor(family);
            descriptors.addFamily(columnDescriptor);
        }

        if (coprocessorClass != null && !"".equals(coprocessorClass)) {
            descriptors.addCoprocessor(coprocessorClass);
        }

        // 增加预分区
        if (regionCount == null || regionCount <= 1) {
            admin.createTable(descriptors);
        } else {
            // 分区键
            byte[][] splitKeys = genSplitKeys(regionCount);
            admin.createTable(descriptors, splitKeys);
        }
    }

    /**
     * 获取查询时的startrow和stoprow的集合
     *
     * @param tel
     * @param start
     * @param end
     * @return
     */
    protected List<String[]> getStartStopRowkeys(String tel, String start, String end) {
        List<String[]> rowkeyss = new ArrayList<>();

        String startTime = start.substring(0, 6);
        String endTime = end.substring(0, 6);

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(DateUtil.parse(startTime, "yyyyMM"));

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(DateUtil.parse(endTime, "yyyyMM"));

        while (startCal.getTimeInMillis() <= endCal.getTimeInMillis()) {

            // 当前循环的月份
            String nowTime = DateUtil.format(startCal.getTime(), "yyyyMM");

            int regionNum = genRegionNum(tel, nowTime);

            String startRow = regionNum + "_" + tel + "_" + nowTime;
            String stopRow = startRow + "|";

            String[] rowkey = {startRow, stopRow};
            rowkeyss.add(rowkey);

            // 月份加1
            startCal.add(Calendar.MONTH, 1);
        }

        return rowkeyss;
    }

    /**
     * 计算分区号
     *
     * @param tel
     * @param date
     * @return
     */
    protected static int genRegionNum(String tel, String date) {
        // 13988886666
        String usercode = tel.substring(tel.length() - 4);
        // 201901011212
        String yearMonth = date.substring(0, 6);

        int userCodeHash = usercode.hashCode();
        int yearMonthHash = yearMonth.hashCode();

        // CRC校验采用异或算法
        int crc = Math.abs(userCodeHash ^ yearMonthHash);

        // 取模
        int regionNum = crc % ValueConstant.REGION_COUNT;

        return regionNum;
    }

    /**
     * 生成分区键
     *
     * @param regionCount 分区数
     * @return
     */
    private byte[][] genSplitKeys(Integer regionCount) {
        // n个分区需要n-1个分区键
        int splitKeyCount = regionCount - 1;
        byte[][] bs = new byte[splitKeyCount][];

        List<byte[]> bsList = new ArrayList<>();

        for (int i = 0; i < splitKeyCount; i++) {
            String splitKey = i + "|";
            bsList.add(Bytes.toBytes(splitKey));
        }

        bsList.toArray(bs);

        return bs;
    }

    /**
     * 增加数据
     *
     * @param name
     * @param put
     */
    protected void putData(String name, Put put) throws IOException {
        // 获取表对象
        Connection conn = getConnection();
        Table table = conn.getTable(TableName.valueOf(name));

        // 增加数据
        table.put(put);

        // 关闭表
        table.close();
    }

    /**
     * 增加多条数据
     *
     * @param name
     * @param puts
     */
    protected void putData(String name, List<Put> puts) throws IOException {
        // 获取表对象
        Connection conn = getConnection();
        Table table = conn.getTable(TableName.valueOf(name));

        // 增加数据
        table.put(puts);

        // 关闭表
        table.close();
    }

    /**
     * 增加对象，自动封装数据并保存到hbase中去
     *
     * @param obj
     */
    protected void putData(Object obj) throws Exception {

        // 反射
        Class<?> clazz = obj.getClass();
        TableRef tableRef = clazz.getAnnotation(TableRef.class);
        String tableName = tableRef.value();

        Field[] fields = clazz.getDeclaredFields();
        String stringRowkey = "";
        for (Field field : fields) {
            Rowkey rowkey = field.getAnnotation(Rowkey.class);
            if (rowkey != null) {
                field.setAccessible(true);
                stringRowkey = (String) field.get(obj);
                break;
            }
        }

        // 获取表对象
        Connection conn = getConnection();
        Table table = conn.getTable(TableName.valueOf(tableName));
        Put put = new Put(Bytes.toBytes(stringRowkey));

        for (Field field : fields) {
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                String family = column.family();
                String colName = column.column();
                if (colName == null || "".equals(colName)) {
                    colName = field.getName();
                }

                field.setAccessible(true);
                String value = (String) field.get(obj);

                put.addColumn(Bytes.toBytes(family), Bytes.toBytes(colName), Bytes.toBytes(value));
            }
        }

        // 增加数据
        table.put(put);

        // 关闭表
        table.close();
    }

    /**
     * 删除表格
     *
     * @param name
     * @throws IOException
     */
    protected void deleteTable(String name) throws IOException {
        Admin admin = getAdmin();
        TableName tableName = TableName.valueOf(name);
        admin.disableTable(tableName);
        admin.deleteTable(tableName);
    }

    /**
     * 获取连接对象
     *
     * @return
     * @throws IOException
     */
    protected synchronized Connection getConnection() throws IOException {
        Connection conn = connHolder.get();
        if (conn == null) {
            Configuration conf = HBaseConfiguration.create();
            conn = ConnectionFactory.createConnection(conf);
            connHolder.set(conn);
        }

        return conn;
    }

    protected synchronized Admin getAdmin() throws IOException {
        Admin admin = adminHolder.get();
        if (admin == null) {
            admin = getConnection().getAdmin();
            adminHolder.set(admin);
        }

        return admin;
    }
}
