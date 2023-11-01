package com.sihai.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * ttl 消费者
 */
public class TtlConsumer {

    private final static String QUEUE_NAME = "ttl-queue";

    public static void main(String[] argv) throws Exception {
        // 创建连接
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        
        // 创建频道
        Channel channel = connection.createChannel();
        // 给队列设置生存时间
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-message-ttl", 5000);
        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, args);
        
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // 定义了如何处理消息
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            // 获取消息
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            // 输出消息
            System.out.println(" [x] Received '" + message + "'");
        };
        // 消费消息，会持续阻塞
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
}
