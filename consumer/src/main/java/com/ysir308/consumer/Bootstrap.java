package com.ysir308.consumer;

import com.ysir308.common.bean.Consumer;
import com.ysir308.consumer.bean.CalllogConsumer;

import java.io.IOException;

/**
 * 启动消费者
 * 使用kafka消费Flume采集的数据
 * 将数据存储到HBase中
 */
public class Bootstrap {
    public static void main(String[] args) throws IOException {
        Consumer consumer = new CalllogConsumer();

        consumer.consume();

        consumer.close();
    }
}
