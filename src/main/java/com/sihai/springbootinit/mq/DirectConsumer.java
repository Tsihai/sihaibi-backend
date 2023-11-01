package com.sihai.springbootinit.mq;

import com.rabbitmq.client.*;

/**
 * 直接交换机 消费者
 */
public class DirectConsumer {

  private static final String EXCHANGE_NAME = "direct_exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    // 声明交换机类型
    channel.exchangeDeclare(EXCHANGE_NAME, "direct");

      // 创建消息队列
      String queueName1 = "sihai-1";
      // 声明队列 参数：队列名称、持久队列、仅限于此连接、自动删除队列、构造参数
      channel.queueDeclare(queueName1, true, false, false, null);
      // 绑定代码：队列名称、交换机名称、绑定规则(用于绑定的路由密钥)
      channel.queueBind(queueName1, EXCHANGE_NAME, "sihai-1");

      // 创建消息队列
      String queueName2 = "sihai-2";
      // 声明队列 参数：队列名称、持久队列、仅限于此连接、自动删除队列、构造参数
      channel.queueDeclare(queueName2, true, false, false, null);
      // 绑定代码：队列名称、交换机名称、绑定规则(用于绑定的路由密钥)
      channel.queueBind(queueName2, EXCHANGE_NAME, "sihai-2");

    
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    // 回调
    DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [sihai1] Received '" +
            delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };

      DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");
          System.out.println(" [sihai2] Received '" +
                  delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
      };
      
      // 消费队列
    channel.basicConsume(queueName1, true, deliverCallback1, consumerTag -> { });
    channel.basicConsume(queueName2, true, deliverCallback2, consumerTag -> { });
  }
}
