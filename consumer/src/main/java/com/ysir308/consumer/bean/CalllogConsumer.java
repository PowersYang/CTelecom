package com.ysir308.consumer.bean;

import com.ysir308.common.bean.Consumer;
import com.ysir308.common.constant.Names;
import com.ysir308.consumer.dao.HBaseDao;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

/**
 * 通话日志的消费对象
 */
public class CalllogConsumer implements Consumer {

    /**
     * 消费数据
     */
    @Override
    public void consume() {
        try {
            // 创建配置对象
            Properties prop = new Properties();
            prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("consumer.properties"));

            // 获取Flume采集的数据
            KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(prop);

            // 关注主题
            consumer.subscribe(Arrays.asList(Names.TOPIC.getValue()));

            // HBase访问对象
            HBaseDao dao = new HBaseDao();
            dao.init();

            // 消费数据
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println(record.value());
                    dao.insertDatas(record.value());
//                    Calllog log = new Calllog(record.value());
//                    dao.insertData(log);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {

    }
}
