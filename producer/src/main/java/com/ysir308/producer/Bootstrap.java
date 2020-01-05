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

        if (args.length < 2) {
            System.out.println("系统参数不正确，请按照格式传递：java -jar Produce.jar path1 path2");
            System.exit(1);
        }

        // 构建生产者对象
        Producer producer = new LocalFileProducer();

        producer.setIn(new LocalFileDataIn(args[0]));
        producer.setOut(new LocalFileDataOut(args[1]));

        // 生产数据
        producer.produce();

        // 关闭生产者对象
        producer.close();
    }
}