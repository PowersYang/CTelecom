package com.ysir308.common.bean;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

import java.io.IOException;
import java.util.Arrays;

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
     * @param name
     * @param families
     * @throws IOException
     */
    protected void createTableXX(String name, String... families) throws IOException {
        Admin admin = getAdmin();

        TableName tableName = TableName.valueOf(name);

        if (admin.tableExists(tableName)) {
            deleteTable(name);
        }

        createTable(name, families);

    }

    private void createTable(String tableName, String... families) throws IOException {
        Admin admin = getAdmin();
        HTableDescriptor descriptors = new HTableDescriptor(tableName);

        if (families == null || families.length == 0) {
            families = new String[1];
            families[0] = "info";
        }

        for (String family : families) {
            HColumnDescriptor columnDescriptor= new HColumnDescriptor(family);
            descriptors.addFamily(columnDescriptor);
        }

        admin.createTable(descriptors);
    }

    protected void deleteTable(String name) throws IOException{
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
