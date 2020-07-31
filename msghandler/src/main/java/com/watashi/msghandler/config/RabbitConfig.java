package com.watashi.msghandler.config;

import com.watashi.api.MQConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.HashMap;
import java.util.Map;

@Configuration
//@DependsOn("rabbitTemplateConfig")
public class RabbitConfig {

    @Bean
    Exchange orderExchange(){
        // 持久队列
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl",100*1000);//这个队列里面所有消息的过期时间
        arguments.put("x-dead-letter-exchange", MQConstants.DEAD_EXCHANGE);//消息死了交给那个交换机
        arguments.put("x-dead-letter-routing-key","dead");//死信发出去的路由键
        return new TopicExchange(MQConstants.ORDER_EXCHANGE,true,true,null);
    }

    @Bean
    Queue orderQueue(){
        // 持久队列
        return new Queue(MQConstants.ORDER_QUEUE,true,false,false, null);
    }

    @Bean
    Binding orderBinding(){
        // 绑定队列，路由键设置为队列名，实际上TopicExchange就跟DirectExchange一样了
        return new Binding(MQConstants.ORDER_QUEUE, Binding.DestinationType.QUEUE,MQConstants.ORDER_EXCHANGE,MQConstants.ORDER_QUEUE, null);
    }
    @Bean
    Exchange deadExange(){
        return new DirectExchange(MQConstants.DEAD_EXCHANGE,true,false,null);
    }

    @Bean
    Queue deadQueue(){
        return new Queue(MQConstants.DEAD_QUEUE,true,false,false,null);
    }

    @Bean
    Binding deadBinding(){
        return new Binding(MQConstants.DEAD_QUEUE, Binding.DestinationType.QUEUE,MQConstants.DEAD_EXCHANGE,"dead", null);
    }
}
