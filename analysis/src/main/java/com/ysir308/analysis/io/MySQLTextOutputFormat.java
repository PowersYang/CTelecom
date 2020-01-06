package com.ysir308.analysis.io;

import com.ysir308.common.util.JDBCUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * MySQL的格式化输出对象
 */
public class MySQLTextOutputFormat extends OutputFormat<Text, Text> {
    private FileOutputCommitter committer = null;

    protected static class MySQLRecordWriter extends RecordWriter<Text, Text> {

        private Connection connection = null;
        private Jedis jedis = null;


        public MySQLRecordWriter() {
            // 获取资源
            connection = JDBCUtil.getConnection();
            jedis = new Jedis("localhost", 6379);
        }

        /**
         * 输出数据
         *
         * @param key
         * @param value
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void write(Text key, Text value) throws IOException, InterruptedException {

            String[] values = value.toString().split("_");
            String sumCall = values[0];
            String sumDuration = values[1];

            PreparedStatement pstat = null;
            try {
                String insertql = "insert into ct_call (telid, dateid, sumcall, sumduration) values (?, ?, ?, ?)";
                pstat = connection.prepareStatement(insertql);

                String k = key.toString();
                String[] ks = k.split("_");

                String tel = ks[0];
                String date = ks[1];

                pstat.setInt(1, Integer.parseInt(jedis.hget("ct_user", tel)));
                pstat.setInt(2, Integer.parseInt(jedis.hget("ct_date", date)));
                pstat.setInt(3, Integer.parseInt(sumCall));
                pstat.setInt(4, Integer.parseInt(sumDuration));
                pstat.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (pstat != null) {
                    try {
                        pstat.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * 释放资源
         *
         * @param taskAttemptContext
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void close(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public RecordWriter<Text, Text> getRecordWriter(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        return new MySQLRecordWriter();
    }

    @Override
    public void checkOutputSpecs(JobContext jobContext) throws IOException, InterruptedException {

    }


    public static Path getOutputPath(JobContext job) {
        String name = job.getConfiguration().get(FileOutputFormat.OUTDIR);
        return name == null ? null : new Path(name);
    }

    @Override
    public OutputCommitter getOutputCommitter(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {

        if (committer == null) {
            Path output = getOutputPath(taskAttemptContext);
            committer = new FileOutputCommitter(output, taskAttemptContext);
        }

        return committer;
    }
}
