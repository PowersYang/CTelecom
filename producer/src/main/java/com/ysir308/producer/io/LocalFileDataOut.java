package com.ysir308.producer.io;

import com.ysir308.common.bean.DataOut;

import java.io.IOException;

/**
 * 本地文件数据输出
 */
public class LocalFileDataOut implements DataOut {

    public LocalFileDataOut(String path) {
        setOut(path);
    }

    @Override
    public void setOut(String path) {

    }

    @Override
    public void close() throws IOException {

    }
}
