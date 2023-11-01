package com.sihai.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Dead Letter Exchanges 消费者
 */
public class DlxDirectConsumer {

  // 原本的业务交换机
  private static final String WORK_EXCHANGE_NAME = "direct2_exchange";
  // 死信交换机
  private static final String DEAD_EXCHANGE_NAME = "dlx-direct_exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    // 声明交换机类型
    channel.exchangeDeclare(WORK_EXCHANGE_NAME, "direct");


    // 指定死信队列参数
    Map<String, Object> args = new HashMap<>();
    // 设置死信交换机名称
    args.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
    // 设置死信要转发到哪个消息队列
    args.put("x-dead-letter-routing-key", "boos");
      // 创建消息队列
      String queueName1 = "sihai-queue";
      // 声明队列 参数：队列名称、持久队列、仅限于此连接、自动删除队列、构造参数
      channel.queueDeclare(queueName1, true, false, false, args);
      // 绑定代码：队列名称、交换机名称、绑定规则(用于绑定的路由密钥)
      channel.queueBind(queueName1, WORK_EXCHANGE_NAME, "sihai");

    // 指定死信队列参数
    Map<String, Object> args2 = new HashMap<>();
    // 设置死信交换机名称
    args2.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
    // 设置死信交换机路由键
    args2.put("x-dead-letter-routing-key", "waibao");
      // 创建消息队列
      String queueName2 = "jiayi-queue";
      // 声明队列 参数：队列名称、持久队列、仅限于此连接、自动删除队列、构造参数
      channel.queueDeclare(queueName2, true, false, false, args2);
      // 绑定代码：队列名称、交换机名称、绑定规则(用于绑定的路由密钥)
      channel.queueBind(queueName2, WORK_EXCHANGE_NAME, "jiayi");

    
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    // 回调
    DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        // 拒绝消息，就会发送给死信交换机，转发给 boos
        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
        System.out.println(" [sihai] Received '" +
            delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };
    DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [jiayi] Received '" +
            delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };
      
      // 消费队列
    channel.basicConsume(queueName1, false, deliverCallback1, consumerTag -> { });
    channel.basicConsume(queueName2, false, deliverCallback2, consumerTag -> { });
  }
}
