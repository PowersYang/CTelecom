package com.ysir308.producer.bean;

import com.ysir308.common.bean.DataIn;
import com.ysir308.common.bean.DataOut;
import com.ysir308.common.bean.Producer;

import java.io.IOException;
import java.util.List;

/**
 * 本地数据文件生产者
 */
public class LocalFileProducer implements Producer {

    private DataIn in;
    private DataOut out;
    private volatile boolean flag = true;

    @Override
    public void setIn(DataIn in) {
        this.in = in;
    }

    @Override
    public void setOut(DataOut out) {
        this.out = out;
    }

    /**
     * 生产数据
     */
    @Override
    public void produce() {

        // 读取通讯录数据
        try {
            List<Contact> contacts = in.read(Contact.class);
            for (Contact contact : contacts) {
                System.out.println(contact);
            }
//            while (flag) {
                // 从通讯录中随机查找两个电话号码（主叫和被叫）

                // 生成随机的通话时间

                // 生成随机的通话时长

                // 生成通话记录

                // 将通话记录刷写到数据文件中
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void close() throws IOException {
        if (in != null) {
            in.close();
        }

        if (out != null) {
            out.close();
        }
    }
}
