package com.ysir308.common.constant;

import com.ysir308.common.bean.Val;

/**
 * 名称相关的常量
 */
public enum Names implements Val {

    NAMESPACE("ct"),
    TABLE("ct:calllog"),
    CF_CALLER("caller"),
    CF_CALLEE("callee"),
    CF_INFO("info"),
    TOPIC("calllog");

    private String name;

    private Names(String name) {
        this.name = name;
    }

    @Override
    public String getValue() {
        return name;
    }

    @Override
    public void setValue(Object value) {
        this.name = (String) value;
    }
}
