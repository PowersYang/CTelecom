package com.ysir308.cache;

import com.ysir308.common.util.JDBCUtil;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 启动缓存客户端，向Redis增加缓存数据
 */
public class Bootstrap {
    public static void main(String[] args) {


        // 读取MySQL数据
        Map<String, Integer> dateMap = new HashMap<>();
        Map<String, Integer> userMap = new HashMap<>();

        // 读取用户时间数据
        Connection connection = null;
        PreparedStatement pstat = null;
        ResultSet res = null;
        try {
            connection = JDBCUtil.getConnection();

            String queryUserSql = "select id, tel from ct_user";
            pstat = connection.prepareStatement(queryUserSql);
            res = pstat.executeQuery();
            while (res.next()) {
                Integer id = res.getInt(1);
                String tel = res.getString(2);
                userMap.put(tel, id);
            }

            res.close();

            String queryDateSql = "select id, year, month, day from ct_date";
            pstat = connection.prepareStatement(queryDateSql);
            res = pstat.executeQuery();
            while (res.next()) {
                Integer id = res.getInt(1);
                String year = res.getString(2);
                String month = res.getString(3);
                String day = res.getString(4);

                // 保持格式统一为20190101
                if (month.length() == 1) {
                    month = "0" + month;
                }

                if (day.length() == 1) {
                    day = "0" + day;
                }

                dateMap.put(year + month + day, id);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

            if (res != null) {
                try {
                    res.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (pstat != null) {
                try {
                    pstat.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }


        // 写入Redis
        Jedis jedis = new Jedis("localhost", 6379);
        for (String key : userMap.keySet()) {
            Integer value = userMap.get(key);
            jedis.hset("ct_user", key, value.toString());
        }

        for (String key : dateMap.keySet()) {
            Integer value = dateMap.get(key);
            jedis.hset("ct_date", key, value.toString());
        }


        jedis.close();

    }
}
