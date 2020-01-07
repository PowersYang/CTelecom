package com.ysir308.web.service.impl;

import com.ysir308.web.dao.CalllogDao;
import com.ysir308.web.service.CalllogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalllogServiceImpl implements CalllogService {
    @Autowired
    private CalllogDao calllogDao;

}
