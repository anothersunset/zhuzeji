package com.example.seclab.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmsGateway {
    public void send(String phone, String text) {
        // \u6253\u65e5\u5fd7\u6a21\u62df\uff1a\u5b9e\u9645\u9879\u76ee\u91cc\u8fd9\u5c31\u662f\u82b1\u94b1\u7684\u5730\u65b9
        log.info("[SMS] -> {} : {}", phone, text);
    }
}
