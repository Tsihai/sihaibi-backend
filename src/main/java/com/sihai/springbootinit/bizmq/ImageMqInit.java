package com.sihai.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.sihai.springbootinit.constant.BiMqConstant;
import com.sihai.springbootinit.constant.Mq.ImageMqConstant;

import java.util.HashMap;
import java.util.Map;

import static com.sihai.springbootinit.constant.Mq.ImageMqConstant.*;

/*** 用于创建测试程序用到的交换机和队列（只用在程序启动前执行一次）*/
public class ImageMqInit {

    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            // 设置 rabbitmq 对应的信息
            factory.setHost(BiMqConstant.MQ_HOST);
            factory.setUsername(BiMqConstant.MQ_USERNAME);
            factory.setPassword(BiMqConstant.MQ_PASSWORD);

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            String biExchange = ImageMqConstant.Image_EXCHANGE_NAME;
            channel.exchangeDeclare(biExchange, ImageMqConstant.Image_DIRECT_EXCHANGE);

            // 创建Image分析队列
            String queueName = Image_QUEUE;
            Map<String,Object> map = new HashMap<>();

            // Image分析队列绑定死信交换机
            map.put("x-dead-letter-exchange", Image_DLX_EXCHANGE_NAME);
            map.put("x-dead-letter-routing-key", Image_DLX_ROUTING_KEY);
            channel.queueDeclare(queueName,true,false,false,map);
            channel.queueBind(queueName,ImageMqConstant.Image_EXCHANGE_NAME,Image_ROUTING_KEY);

            //创建死信队列和死信交换机
            //创建死信队列
            channel.queueDeclare(Image_DLX_QUEUE_NAME,true,false,false,null);
            //创建死信交换机
            channel.exchangeDeclare(Image_DLX_EXCHANGE_NAME,Image_DIRECT_EXCHANGE);

            channel.queueBind(Image_DLX_QUEUE_NAME,Image_DLX_EXCHANGE_NAME,Image_DLX_ROUTING_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
