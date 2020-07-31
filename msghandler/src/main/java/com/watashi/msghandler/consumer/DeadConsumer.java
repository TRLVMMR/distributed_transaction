package com.watashi.msghandler.consumer;

import com.watashi.api.MQConstants;
import com.watashi.msghandler.config.RabbitConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DeadConsumer {

    @RabbitListener(queues = MQConstants.DEAD_QUEUE)
    public void processMessage(String content, Message message) {
        // 监听死信队列的消息
        System.out.println("content = " + content);
    }
}
