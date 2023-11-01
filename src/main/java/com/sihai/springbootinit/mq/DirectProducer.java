package com.sihai.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.sihai.springbootinit.common.ErrorCode;
import com.sihai.springbootinit.exception.BusinessException;

import java.util.Scanner;

/**
 * 直接交换机 生产者
 */
public class DirectProducer {

  private static final String EXCHANGE_NAME = "direct_exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    try (Connection connection = factory.newConnection();
         Channel channel = connection.createChannel()) {
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

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

            channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message + " with routing:" + routingKey + "'");
        }

        
    }
  }
  //..
}
