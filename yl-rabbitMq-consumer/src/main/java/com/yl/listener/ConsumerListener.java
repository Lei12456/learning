package com.yl.listener;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ConsumerListener {

    @RabbitListener(queues = "simple.queue")
    public void listenSimpleQueue(String msg){
        System.out.println("消费者接收到了来自simpleQueue的消息：" + msg);
        //消费者逻辑代码发生异常
        //开始消费者失败重试
        System.out.println(1 / 0);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "dl.queue",durable = "true"),
            exchange = @Exchange(name = "dl.direct"),
            key = "dl"
            ))
    public void listenTTLQueue(String msg){
        System.out.println("消费者接收到了来自DeadLetterQueue的消息：" + msg);
    }

    /**
     * @description 延迟交换机声明
     * @author yanglei
     * @date 2023/4/8 20:26
     * @param msg
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "delay.queue",durable = "true"),
            exchange = @Exchange(name = "delay.direct",delayed = "true"),
            key = "delay"
    ))
    public void listenDelayedExchange(String msg){
        System.out.println("消费者接收到了来自delayedQueue的消息：" + msg);
    }
}
