package com.padingpading.we_com.controller;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 热云配置
 */
@Component
@Data
public class GdtConfig {
    
    /**
     * 热云安卓app key
     */
    @Value("${re.yun.android.app.key:}")
    private String androidAppKey;
    
    /**
     * 热云IOS app key
     */
    @Value("${re.yun.ios.app.key:}")
    private String iosAppKey;
    
    /**
     * 热云事件url
     */
    @Value("${re.yun.event.url.app.key:https://log.trackingio.com/receive/tkio/event}")
    private String eventUrl;
    
    /**
     * 热云用户充值url
     */
    @Value("${re.yun.event.url.app.key:https://log.trackingio.com/receive/tkio/payment}")
    private String payment;
    
    
    /**
     * 默认不推送
     */
    @Value("${re.yun.switch:false}")
    private Boolean sendSwitch;
}
