package com.ysir308.producer;

import com.ysir308.common.bean.Producer;
import com.ysir308.producer.bean.LocalFileProducer;
import com.ysir308.producer.io.LocalFileDataIn;
import com.ysir308.producer.io.LocalFileDataOut;

import java.io.IOException;

/**
 * 启动对象
 */
public class Bootstrap {
    public static void main(String[] args) throws IOException {

        // 构建生产者对象
        Producer producer = new LocalFileProducer();

        producer.setIn(new LocalFileDataIn("/Users/ysir/contact.log"));
        producer.setOut(new LocalFileDataOut("call.log"));

        // 生产数据
        producer.produce();

        // 关闭生产者对象
        producer.close();
    }
}
