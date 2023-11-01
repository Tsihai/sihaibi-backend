package com.sihai.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.Scanner;

/**
 * Dead Letter Exchanges 生产者
 */
public class DlxDirectProducer {
    // 原本的业务交换机
    private static final String WORK_EXCHANGE_NAME = "direct2_exchange";
    // 死信交换机
  private static final String DEAD_EXCHANGE_NAME = "dlx-direct_exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    try (Connection connection = factory.newConnection();
         Channel channel = connection.createChannel()) {
        
        // 声明死信交换机
        channel.exchangeDeclare(DEAD_EXCHANGE_NAME, "direct");
        
        // 声明死信队列
        String queueName1 = "boos-dlx-queue";
        // 声明队列 参数：队列名称、持久队列、仅限于此连接、自动删除队列、构造参数
        channel.queueDeclare(queueName1, true, false, false, null);
        // 绑定代码：队列名称、交换机名称、绑定规则(用于绑定的路由密钥)
        channel.queueBind(queueName1, WORK_EXCHANGE_NAME, "boos");
        
        // 声明死信队列
        String queueName2 = "waibao-dlx-queue";
        // 声明队列 参数：队列名称、持久队列、仅限于此连接、自动删除队列、构造参数
        channel.queueDeclare(queueName2, true, false, false, null);
        // 绑定代码：队列名称、交换机名称、绑定规则(用于绑定的路由密钥)
        channel.queueBind(queueName2, WORK_EXCHANGE_NAME, "waibao");



        // 可以回调 boos 队列消息
        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [boos] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        // 消费队列
        channel.basicConsume(queueName1, false, deliverCallback1, consumerTag -> { });
        
        

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            // 拆分用户输入
            String userInput = scanner.nextLine();
            String[] strings = userInput.split(" ");
            // 如果无法拆分
            if (strings.length < 1) {
                continue;
            }
            // 用户输入信息
            String message = strings[0];
            // 获取路由键
            String routingKey = strings[1];

            channel.basicPublish(WORK_EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message + " with routing:" + routingKey + "'");
        }
        
        
        

        
    }
  }
  //..
}
