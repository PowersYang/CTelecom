package com.ysir308.producer.bean;

import com.ysir308.common.bean.Data;

public class Contact extends Data {
    private String tel;
    private String name;

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setValue(Object value) {
        content = (String) value;
        String[] values = content.split("\t");
        setTel(values[0]);
        setName(values[1]);
    }

    @Override
    public String toString() {
        return "Contact[" + name + ", " + tel + "]";
    }
}
