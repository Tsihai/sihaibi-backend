package com.sihai.springbootinit.utils;


import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 常量类，读取配置文件application.yml中的配置
 * 
 * 腾讯云对象存储参数
 * @author sihai
 */
@Component
public class FileUtils implements InitializingBean {
    
    @Value("${tencent.cos.secretId}")
    public String secretId;

    @Value("${tencent.cos.secretKey}")
    public String secretKey;

    @Value("${tencent.cos.buckerName}")
    public String buckerName;

    @Value("${tencent.cos.region}")
    public String region;

    @Value("${tencent.cos.url}")
    public String url;

    public static String SECRET_ID;
    public static String SECRET_KEY;
    public static String BUCKET_NAME;
    public static String REGION;
    public static String URL;


    @Override
    public void afterPropertiesSet() throws Exception {
        SECRET_ID = this.secretId;
        SECRET_KEY = this.secretKey;
        BUCKET_NAME = this.buckerName;
        REGION = this.region;
        URL = this.url;
    }

}
