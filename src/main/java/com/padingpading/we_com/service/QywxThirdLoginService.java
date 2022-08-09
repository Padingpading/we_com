package com.padingpading.we_com.service;


import com.padingpading.we_com.model.entity.WechatCorpLogin;
import com.padingpading.we_com.task.QywxThirdAsync;
import com.padingpading.we_com.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class QywxThirdLoginService {

    @Autowired
    QywxThirdCacheService qywxThirdCacheService;

    @Autowired
    QywxThirdAsync qywxThirdAsync;

    @Autowired
    RedisUtils redisUtils;

    public WechatCorpLogin getAuthLogin(String companyId) throws IOException {
        return qywxThirdCacheService.getAuthLoginCache(companyId);
    }

    public WechatCorpLogin authLogin(String companyId){

        //开始清空之前数据
        WechatCorpLogin login = new WechatCorpLogin();
        login.setCompanyId(companyId);
        qywxThirdCacheService.setAuthLoginCache(login);

        qywxThirdAsync.authLoginTask(companyId);

        return login;

    }




}
