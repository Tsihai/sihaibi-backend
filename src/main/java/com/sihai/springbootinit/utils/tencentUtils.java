package com.sihai.springbootinit.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class tencentUtils implements InitializingBean {

    @Value("${tencent.secretId}")
    public String secretId;

    @Value("${tencent.secretKey}")
    public String secretKey;

    public static String SECRET_ID;
    public static String SECRET_KEY;

    @Override
    public void afterPropertiesSet() throws Exception {
        SECRET_ID = this.secretId;
        SECRET_KEY = this.secretKey;
    }
}
