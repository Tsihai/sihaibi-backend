package com.sihai.springbootinit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Queue;

/**
 * 主类（项目启动入口） 
 */
// todo 如需开启 Redis，须移除 exclude 中的内容
//@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@SpringBootApplication
@MapperScan("com.sihai.springbootinit.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
    

}
