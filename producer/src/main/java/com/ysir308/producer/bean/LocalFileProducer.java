package com.ysir308.producer.bean;

import com.ysir308.common.bean.DataIn;
import com.ysir308.common.bean.DataOut;
import com.ysir308.common.bean.Producer;
import com.ysir308.common.util.DateUtil;
import com.ysir308.common.util.NumberUtil;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;

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

            while (flag) {
                // 从通讯录中随机查找两个电话号码（主叫和被叫）
                int call1Index = new Random().nextInt(contacts.size());
                int call2Index;

                do {
                    call2Index = new Random().nextInt(contacts.size());
                } while (call1Index == call2Index);

                Contact call1 = contacts.get(call1Index);
                Contact call2 = contacts.get(call2Index);

                // 生成随机的通话时间
                String startDate = "20180101000000";
                String endDate = "20190101000000";


                long startTime = DateUtil.parse(startDate, "yyyyMMddHHmmss").getTime();
                long endTime = DateUtil.parse(endDate, "yyyyMMddHHmmss").getTime();

                // 通话时间
                long callTime = (long) (startTime + (endTime - startTime) * Math.random());

                // 通话时间字符串
                String callTimeString = DateUtil.format(new Date(callTime), "yyyyMMddHHmmss");

                // 生成随机的通话时长
                // 不够四位前面自动补零0012，0388，1122， 0234
                String duration = NumberUtil.format(new Random().nextInt(3000), 4);

                // 生成通话记录
                CallLog callLog = new CallLog(call1.getTel(), call2.getTel(), callTimeString, duration);

                System.out.println(callLog);
                // 将通话记录刷写到数据文件中
                out.write(callLog);

                Thread.sleep(500);
            }
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
