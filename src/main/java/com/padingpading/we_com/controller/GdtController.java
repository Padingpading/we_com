package com.padingpading.we_com.controller;


import com.alibaba.fastjson.JSON;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.util.Map;

@RestController
@RequestMapping("")
public class GdtController {
    
    @GetMapping("/callback/gdt/feedback/callback")
    public String blank(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        // {"trace_id":["wx077v2testb23nk"],"ad_id":["2394499769"],"adgroup_id":["2400000070"],"wechat_unionid":["om8_q5rbT3a9J26auXH9sT257P-E"],"click_time":["1606022101"],"add_channel":["H5"],"qywx_corp_id":["wwad7cc050af02e211"],"campaign_id":["2400000068"]}
        System.out.println(JSON.toJSON(parameterMap));
        System.out.println("调用");
        return "调用成功";
    }
    
    @GetMapping("/response")
    public String auth(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        System.out.println(JSON.toJSON(parameterMap));
        System.out.println("调用");
        return "调用成功";
    }
    
    public static void main(String[] args) {
        String encode = URLEncoder.encode("http://mengxinagyun.free.idcfengye.com/callback/gdt/feedback/callback");
        System.out.println(encode);
        System.out.println(System.currentTimeMillis());
        String url  = "";
      //  https://developers.e.qq.com/oauth/authorize?client_id=1112013986&redirect_uri=http://mengxinagyun.free.idcfengye.com/callback/gdt/feedback/callback&account_type=ACCOUNT_TYPE_WECHAT
    }
}
