package com.sihai.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.sihai.springbootinit.constant.BiMqConstant;

import java.util.HashMap;
import java.util.Map;

import static com.sihai.springbootinit.constant.Mq.AiChatMqConstant.*;


/**
 * @author sihai
 * CreateTime 2023/6/24 16:08
 * 用于BI项目,创建测试测序用到的交换机和队列 (仅执行一次)
 */
public class AiMqInit {
    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            // 设置 rabbitmq 对应的信息
            factory.setHost(BiMqConstant.MQ_HOST);
            factory.setUsername(BiMqConstant.MQ_USERNAME);
            factory.setPassword(BiMqConstant.MQ_PASSWORD);

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            String aiQuestionExchangeName = AI_EXCHANGE_NAME;
            channel.exchangeDeclare(aiQuestionExchangeName, AI_DIRECT_EXCHANGE,true);

            // 创建ai对话队列
            String aiAssistantQueueName = AI_QUEUE;
            Map<String,Object> aiAssistantMqp = new HashMap<>();
            // ai对话队列绑定Ai对话死信交换机
            aiAssistantMqp.put("x-dead-letter-exchange", AI_DLX_EXCHANGE_NAME);
            aiAssistantMqp.put("x-dead-letter-routing-key", AI_DLX_ROUTING_KEY);
            channel.queueDeclare(aiAssistantQueueName,true,false,false,aiAssistantMqp);
            channel.queueBind(aiAssistantQueueName, AI_EXCHANGE_NAME,AI_ROUTING_KEY);

            //创建死信队列和死信交换机
            //创建死信队列
            channel.queueDeclare(AI_DLX_QUEUE_NAME,true,false,false,null);
            //创建死信交换机
            channel.exchangeDeclare(AI_DLX_EXCHANGE_NAME,AI_DIRECT_EXCHANGE);

            channel.queueBind(AI_DLX_QUEUE_NAME,AI_DLX_EXCHANGE_NAME,AI_DLX_ROUTING_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
