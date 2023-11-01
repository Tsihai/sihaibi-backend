package com.sihai.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.sihai.springbootinit.common.ErrorCode;
import com.sihai.springbootinit.constant.BiMqConstant;
import com.sihai.springbootinit.exception.BusinessException;
import com.sihai.springbootinit.model.entity.Chart;
import com.sihai.springbootinit.model.enums.ChartStatusEnum;
import com.sihai.springbootinit.service.ChartService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Bi 消息失败消费者
 * @author sihai
 */
@Component
@Slf4j
public class BiMessageFailConsumer {

    @Resource
    private ChartService chartService;


    /**
     * 监听死信队列
     *
     * @param message
     * @param channel
     * @param dekivery
     */
    @SneakyThrows
    @RabbitListener(queues = {BiMqConstant.BI_DLX_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long dekivery) {
        // 接收到失败的信息，
        log.info("死信队列receiveMessage message{}", message);
        if (StringUtils.isBlank(message)) {
            channel.basicNack(dekivery, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息为空");
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if (chart == null) {
            channel.basicNack(dekivery, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图表不存在");
        }
        // 把图表标为失败
        chart.setChartStatus(ChartStatusEnum.FAILED.getValue());
        boolean updateById = chartService.updateById(chart);
        if (!updateById) {
            log.info("处理死信队列消息失败,失败图表id:{}", chart.getId());
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 确认消息
        channel.basicAck(dekivery, false);
    }
    
}
