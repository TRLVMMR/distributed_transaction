package com.watashi.msghandler.config;

import com.watashi.api.MQConstants;
import com.watashi.msghandler.mqwork.Coordinator;
import com.watashi.msghandler.mqwork.RedisCoordinator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RabbitTemplateConfig {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    RabbitCallbackConfig rabbitCallbackConfig;

    boolean returnFlag = false;

    @Bean
    public RabbitTemplate customRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // 设置消息格式为json
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        // mandatory 必须设置为true，ReturnCallback才会调用
        rabbitTemplate.setMandatory(true);

        // 若交换机不存在，rabiitmq会调用returnCallback方法。若交换机存在，则会调用ConfirmCallback方法
        //消息发送到RabbitMQ交换器，但无相应Exchange时的回调
        rabbitTemplate.setConfirmCallback(rabbitCallbackConfig);
        rabbitTemplate.setReturnCallback(rabbitCallbackConfig);

//
//        /** confirm的超时时间*/
//        rabbitTemplate.waitForConfirms(MQConstants.TIME_GAP);

        return rabbitTemplate;
    }

}