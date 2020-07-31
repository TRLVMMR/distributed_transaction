package com.watashi.msghandler.config;

import com.watashi.msghandler.mqwork.Coordinator;
import com.watashi.msghandler.mqwork.RedisCoordinator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@Configuration
@Slf4j
public class RabbitCallbackConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {

        log.info("confirm回调，ack={} correlationData={} cause={}", ack, correlationData, cause);
        String msgId = correlationData.getId();
        if(ack){
            log.info("消息已正确投递到队列, correlationData:{}", correlationData);
            //清除重发缓存
//                String dbCoordinatior = ((CompleteCorrelationData)correlationData).getCoordinator();
            Coordinator coordinator = (Coordinator)applicationContext.getBean(RedisCoordinator.class);
            coordinator.setMsgSuccess(msgId);
        }else{
            log.error("消息投递至交换机失败,业务号:{}，原因:{}",correlationData.getId(),cause);
        }
    }

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        String messageId = message.getMessageProperties().getMessageId();

        log.error("return回调，没有找到任何匹配的队列！message id:{},replyCode{},replyText:{},"
                + "exchange:{},routingKey{}", messageId, replyCode, replyText, exchange, routingKey);
//        returnFlag = true;
    }
}
