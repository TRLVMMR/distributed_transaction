package com.watashi.msghandler.contoller;

import com.watashi.api.MQConstants;
import com.watashi.api.MQMessage;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class TestController{

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/test/{id}")
    String test(@PathVariable String id) {
        MQMessage mqMessage = new MQMessage().setMsgId(id);
        rabbitTemplate.convertAndSend(MQConstants.ORDER_EXCHANGE,MQConstants.ORDER_QUEUE,mqMessage);
        CorrelationData correlationData = new CorrelationData();
        correlationData.setId(id);
        return "success";
    }

    @GetMapping("/send/{id}")
    String send(@PathVariable String id) {
        CorrelationData correlationData = new CorrelationData();
        correlationData.setId(id);
        MQMessage mqMessage = new MQMessage().setMsgId(id);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setMessageId(id);
        Message message = new Message("i am message".getBytes(), messageProperties);
//        Message message1 = new Message(mqMessage.toString().getBytes(),)
        correlationData.setReturnedMessage(message);

        rabbitTemplate.convertAndSend(MQConstants.ORDER_EXCHANGE,MQConstants.ORDER_QUEUE,mqMessage,correlationData);
//        rabbitTemplate.convertAndSend(MQConstants.ORDER_EXCHANGE,MQConstants.ORDER_QUEUE,correlationData);
        return "success";
    }
}
