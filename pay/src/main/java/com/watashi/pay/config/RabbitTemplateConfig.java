package com.watashi.pay.config;


import org.springframework.amqp.rabbit.config.AbstractRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class RabbitTemplateConfig implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof AbstractRabbitListenerContainerFactory) {
            AbstractRabbitListenerContainerFactory listenerContainerFactory = (SimpleRabbitListenerContainerFactory) bean;
            // 配置序列化器
            listenerContainerFactory.setMessageConverter(new Jackson2JsonMessageConverter());
            //            listenerContainerFactory.setMessageConverter();
        }
            return bean;
    }

}
