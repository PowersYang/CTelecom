package com.ysir308.common.constant;

import com.ysir308.common.bean.Val;

/**
 * 名称相关的常量
 */
public enum Names implements Val {

    NAMESPACE("ysir308");

    private String name;

    private Names(String name) {
        this.name = name;
    }

    @Override
    public void setValue(Object value) {
        this.name = (String) value;
    }

    @Override
    public Object getValue() {
        return null;
    }
}
