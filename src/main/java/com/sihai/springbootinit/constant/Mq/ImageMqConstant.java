package com.sihai.springbootinit.constant.Mq;

/**
 * @author sihImage
 * CreateTime 2023/6/24 16:26
 * 应用到Image项目当中的mq常量
 */
public interface ImageMqConstant {

    /**
     * 普通交换机
     */
    String Image_EXCHANGE_NAME = "image_exchange";
    String Image_QUEUE = "image_queue";
    String Image_ROUTING_KEY = "image_routingKey";
    String Image_DIRECT_EXCHANGE = "direct";

    /**
     * 死信队列交换机
     */
    String Image_DLX_EXCHANGE_NAME = "image-dlx-exchange";

    /**
     * 死信队列
     */
    String Image_DLX_QUEUE_NAME = "image_dlx_queue";

    /**
     * 死信队列路由键
     */
    String Image_DLX_ROUTING_KEY = "image_dlx_routingKey";

}
