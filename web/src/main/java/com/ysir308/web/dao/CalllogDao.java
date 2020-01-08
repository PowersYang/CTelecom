package com.ysir308.web.dao;

import com.ysir308.web.bean.Calllog;

import java.util.List;
import java.util.Map;

public interface CalllogDao {
    List<Calllog> queryMonthDatas(Map<String, Object> paramMap);
}