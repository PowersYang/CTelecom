package com.ysir308.web.controller;

import com.ysir308.web.service.CalllogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class CalllogController {

    @Autowired
    private CalllogService calllogService;

    @RequestMapping("/query")
    public String query() {

        return "query";
    }

    @ResponseBody
    @RequestMapping("/view")
    public Object view() {
        Map<String, String> dataMap = new HashMap<>();

        dataMap.put("username", "张三");
        dataMap.put("age", "20");

        return dataMap;
    }


}
