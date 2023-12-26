package com.sihai.springbootinit.bizmq;

import com.sihai.springbootinit.constant.BiMqConstant;
import com.sihai.springbootinit.constant.ImageMqConstant;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ImageMessageProducer {
    @Resource
    RabbitTemplate rabbitTemplate;
    public void sendMessage(String message){
        rabbitTemplate.convertAndSend(ImageMqConstant.Image_EXCHANGE_NAME,ImageMqConstant.Image_ROUTING_KEY,message);
    }
}
