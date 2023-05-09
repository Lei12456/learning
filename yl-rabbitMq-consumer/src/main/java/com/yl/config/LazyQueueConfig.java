package com.yl.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;

//@Configuration
public class LazyQueueConfig {

    /**
     * @description 惰性队列声明
     * @author yanglei
     * @date 2023/4/8 19:22
     * @return Queue
     */
    @Bean
    public Queue LazyQueue(){
        return QueueBuilder.durable("lazy.queue")
                .lazy()
                .build();
    }
    /**
     * @description 普通队列声明
     * @author yanglei
     * @date 2023/4/8 19:22
     * @return Queue
     */
    @Bean
    public Queue simpleQueue(){
        return QueueBuilder.durable("simple.queue")
                .build();
    }

}

