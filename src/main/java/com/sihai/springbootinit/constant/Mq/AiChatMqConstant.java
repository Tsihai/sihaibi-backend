package com.sihai.springbootinit.constant.Mq;

public interface AiChatMqConstant {


    /**
     * AI 问答
     */
    String AI_EXCHANGE_NAME = "ai_exchange";
    String AI_QUEUE = "ai_queue";
    String AI_ROUTING_KEY = "ai_routingKey";
    String AI_DIRECT_EXCHANGE = "direct";

    /**
     * AI对话死信队列交换机
     */
    String AI_DLX_EXCHANGE_NAME = "ai-dlx-exchange";

    /**
     * AI对话死信队列
     */
    String AI_DLX_QUEUE_NAME = "ai_dlx_queue";

    /**
     * AI对话死信队列路由键
     */
    String AI_DLX_ROUTING_KEY = "ai_dlx_routingKey";


}
