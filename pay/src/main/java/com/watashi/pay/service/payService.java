package com.watashi.pay.service;


import com.rabbitmq.client.Channel;
import com.watashi.api.MQConstants;
import com.watashi.api.MQMessage;
import com.watashi.api.Pay;
import com.watashi.pay.mapper.PayMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class payService {

    @Autowired
    PayMapper payMapper;

    @RabbitListener(queues = {MQConstants.ORDER_QUEUE})
    public void pay(MQMessage mqMessage, Channel channel, Message message) throws IOException {
        log.info("收到消息" + mqMessage);
//        System.out.println("Message = " + message);
//        System.out.println("str = " + str);
        try {
            Pay pay = new Pay().setOrderId(Integer.valueOf(mqMessage.getMsgId()));
            // 数据表中有order_id的唯一索引，由此保证消息的幂等性
            payMapper.save(pay);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }catch (IOException e){
            log.error(e.getMessage());
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }

    }
}
