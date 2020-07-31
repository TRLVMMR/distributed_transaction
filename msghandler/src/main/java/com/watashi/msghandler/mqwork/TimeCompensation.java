package com.watashi.msghandler.mqwork;

import com.watashi.api.MQConstants;
import com.watashi.api.MessageUtil;
import com.watashi.msghandler.service.ProviderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.watashi.api.MQMessage;
import java.io.IOException;

/**
 * 定时补偿,注意的是，消息的最终一致性方案，是会产生消息重复的问题的，因此需要消费者保证幂等性
 */
@Slf4j
@Component
public class TimeCompensation {


    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    Coordinator coordinator;

    @Autowired
    ProviderService providerService;

    public void setHalfMessage(MQMessage msg){
        coordinator.addMsgPrepare(msg);
    }

    public void rollback(MQMessage msg){
        coordinator.deleteMsgPrepare(msg);
    }

    //第10秒开始，之后每20秒执行一次
    @Scheduled(cron ="10/20 * * * * ?" )
    private void compensation(){
        log.info("定时补偿机制启动");
        coordinator.getMsgReady().forEach(item ->{
            // 重试次数+1
            log.info("正在进行"+ item.getMsgId() + "的补偿");
            MessageUtil.incrResendVal(item);
            sendMessage(item);
        });

    }

    public void sendMessage(MQMessage mqMessage){

        CorrelationData correlationData = new CorrelationData();
        correlationData.setId(mqMessage.getMsgId());
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setMessageId(mqMessage.getMsgId());
        Message message = new Message("i am message".getBytes(), messageProperties);
        correlationData.setReturnedMessage(message);
        // 路由键设置为queue的名称，一对一
        rabbitTemplate.convertAndSend(MQConstants.ORDER_EXCHANGE,MQConstants.ORDER_QUEUE,mqMessage,correlationData);
//        rabbitTemplate.convertAndSend(MQConstants.ORDER_EXCHANGE,MQConstants.ORDER_QUEUE,mqMessage);
    }

    /**
     * 注意这个方法，可能会在业务完成以前就去尝试重试。从而重试次数提前-1，
     * 解决方法可以使用时间戳进行过滤，个人考虑到这种请求是少数，没必要因为一颗老鼠屎而坏了一锅粥。因此没有检测，
     */
    @Scheduled(cron ="10/20 * * * * ?" )
    private void timingConfirm(){
        log.info("重试机制启动");
        coordinator.getMsgPrepare().forEach(item ->{
            log.info("正在进行" + item.getMsgId() + "的重试");
            MessageUtil.incrConfirmVal(item);
            confirm(item);
        });
    }

    public void confirm(MQMessage msg){
        // 通知生产者确认消息
        if (providerService.confirm(msg.getMsgId())) {
            log.info("已向确认确认成功" + msg.getMsgId());
            // 修改消息状态
            try {
                coordinator.setMsgReady(msg.getMsgId());
                // 通知生产者删除业务号
                providerService.deleteBusiness(msg.getMsgId());
                log.info("已通知提供者删除标记");
                // 发送消息到MQ
                sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
//            coordinator.deleteMsgPrepare(msg);
            log.info("提供者此业务未完成");
        }
    }
}
