package com.ysir308.web.controller;

import com.ysir308.web.bean.Calllog;
import com.ysir308.web.service.CalllogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class CalllogController {

    @Autowired
    private CalllogService calllogService;

    @RequestMapping("/query")
    public String query() {

        return "query";
    }

    @RequestMapping("/view")
    public Object view(String tel, String calltime, Model model) {
        // 查询统计结果
        List<Calllog> logs = calllogService.queryMonthDatas(tel, calltime);
        model.addAttribute("calllogs", logs);
        return "view";
    }
}
