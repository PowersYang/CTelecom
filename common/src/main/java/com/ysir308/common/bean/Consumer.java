package com.ysir308.common.bean;

import java.io.Closeable;

public interface Consumer extends Closeable {

    /**
     * 消费数据
     */
    public void consume();
}
