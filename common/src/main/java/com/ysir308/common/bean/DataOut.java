package com.ysir308.common.bean;

import java.io.Closeable;

public interface DataOut extends Closeable {

    public void setOut(String path);
}
