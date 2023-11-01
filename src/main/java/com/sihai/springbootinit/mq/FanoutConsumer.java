package com.sihai.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

/**
 * 扇出消费者（fanout 交换机）
 */
public class FanoutConsumer {
  private static final String EXCHANGE_NAME = "fanout-exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    
    // 创建两个队列
    Channel channel1 = connection.createChannel();
    Channel channel2 = connection.createChannel();

    // 声明交换机名称
    channel1.exchangeDeclare(EXCHANGE_NAME, "fanout");
    
    // 创建队列，随机分配一个队列名称
    String queueName1 = "sihai-1";
    String queueName2 = "sihai-2";
    
    // 声明队列 参数：队列名称、持久队列、仅限于此连接、自动删除队列、构造参数
    channel1.queueDeclare(queueName1, true, false, false, null);
    // 绑定代码：队列名称、交换机名称、绑定规则(用于绑定的路由密钥)
    channel1.queueBind(queueName1, EXCHANGE_NAME, "");
    
    channel2.queueDeclare(queueName2, true, false, false, null);
    channel2.queueBind(queueName2, EXCHANGE_NAME, "");

    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    // 输出消息
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [x] Received '" + message + "'");
    };
    // 消费两个队列
    channel1.basicConsume(queueName1, true, deliverCallback, consumerTag -> { });
    channel2.basicConsume(queueName2, true, deliverCallback, consumerTag -> { });
  }
}
