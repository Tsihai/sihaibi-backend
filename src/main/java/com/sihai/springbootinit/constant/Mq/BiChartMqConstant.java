package com.sihai.springbootinit.constant.Mq;

public interface BiChartMqConstant {


    /**
     * Bi 交换机
     */
    String BI_EXCHANGE_NAME = "bi_exchange";
    String BI_QUEUE = "bi_queue";
    String BI_ROUTING_KEY = "bi_routingKey";
    String BI_DIRECT_EXCHANGE = "direct";

    /**
     * 死信队列交换机
     */
    String BI_DLX_EXCHANGE_NAME = "bi-dlx-exchange";

    /**
     * 死信队列
     */
    String BI_DLX_QUEUE_NAME = "bi_dlx_queue";

    /**
     * 死信队列路由键
     */
    String BI_DLX_ROUTING_KEY = "bi_dlx_routingKey";


}
