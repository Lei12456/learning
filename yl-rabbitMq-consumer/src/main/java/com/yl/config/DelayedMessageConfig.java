package com.yl.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;

/**
 * @description 配置类声明延迟交换机
 * @author yanglei
 * @date 2023/4/8 20:28
 */
//@Configuration
public class DelayedMessageConfig {

    /**
     * @description 延迟交换机声明
     * @author yanglei
     * @date 2023/4/8 19:22
     * @return DirectExchange
     */
    @Bean
    public DirectExchange delayedExchange(){
        return ExchangeBuilder.directExchange("delay.direct")
                .delayed()
                .autoDelete()
                .durable(true)
                .build();
    }
    /**
     * @description 延时队列声明
     * @author yanglei
     * @date 2023/4/8 19:22
     * @return Queue
     */
    @Bean
    public Queue delayedQueue(){
        return QueueBuilder.durable("delay.queue")
                .ttl(10000) //设置过期时间
                .deadLetterExchange("delay.direct")//绑定死信交换机
                .deadLetterRoutingKey("delay")//添加路由key
                .build();
    }
    /**
     * @description 声明延时队列和延时交换机的绑定关系
     * @author yanglei
     * @date 2023/4/8 19:23
     * @return Binding
     */
    @Bean
    public Binding ttlBinding(){
        return BindingBuilder
                .bind(delayedQueue())
                .to(delayedExchange())
                .with("delay");
    }

}

