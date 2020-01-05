package com.ysir308.producer.io;

import com.ysir308.common.bean.DataOut;

import java.io.*;

/**
 * 本地文件数据输出
 */
public class LocalFileDataOut implements DataOut {

    private PrintWriter writer = null;

    public LocalFileDataOut(String path) {
        setPath(path);
    }

    @Override
    public void setPath(String path) {
        try {
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(Object data) throws Exception {
        write(data.toString());
    }

    /**
     * 将数据写入文件
     *
     * @param data
     * @throws Exception
     */
    @Override
    public void write(String data) throws Exception {
        writer.println(data);
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }
}
