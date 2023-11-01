package com.sihai.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

/**
 * 多消费者
 */
public class MultiConsumer {

  private static final String TASK_QUEUE_NAME = "multi_queue";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    final Connection connection = factory.newConnection();

    // 创建多个消费者，便于快速验证队列模型的工作机制
      for (int i = 0; i < 2; i++) {

          final Channel channel = connection.createChannel();

          channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
          System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

          // 控制单个消费者的处理任务积压数，每个消费者最多同时处理1个任务
          channel.basicQos(1);
          

          // 定义了如何处理消息
          int finalI = i;
          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
              String message = new String(delivery.getBody(), "UTF-8");


              try {
                  System.out.println(" [x] Received '" + "编号" + finalI + ":" + message + "'");
                  // 20s 后处理工作,模拟机器处理能力有限
                  Thread.sleep(20000);
                  // 指定确认消息被消费，
                  // multiple: 指是否批量确认历史消息直到当前这一条
                  channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

              } catch (InterruptedException e) {
                  e.printStackTrace();
                  // 指定拒绝消息
                  // requeue: 是否重新入队
                  channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
              } finally {
                  System.out.println(" [x] Done");
                  channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
              }
          };
          // 开启消费监听
          // autoAck: 如果服务器应考虑消息一旦传递就已确认，则为true; 如果服务器不需要自动确认，则为false
          channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> { });
          
      }
    
  }

}
