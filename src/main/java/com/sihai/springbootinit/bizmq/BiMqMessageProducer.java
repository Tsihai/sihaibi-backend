package com.sihai.springbootinit.bizmq;

import com.sihai.springbootinit.constant.BiMqConstant;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.sihai.springbootinit.constant.Mq.BiChartMqConstant.*;

/**
 * @author sihai
 * CreateTime 2023/6/24 15:53
 * BI项目 生产者
 */
@Component
public class BiMqMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     * @param message
     */
    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(BI_EXCHANGE_NAME,BI_ROUTING_KEY, message);
    }
}
