package com.yl.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {

    @Bean("simpleAsyncThreadPool")
    public TaskExecutor simpleAsyncThreadPool(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数,默认为1
        executor.setCorePoolSize(8);
        // 设置最大线程数，默认为Integer.MAX_VALUE
        executor.setMaxPoolSize(16);
        // 设置线程池维护线程所允许的空闲时间（秒），默认为60s
        executor.setKeepAliveSeconds(60);
        // 设置默认线程名称
        executor.setThreadNamePrefix("simpleAsyncThreadPool-");
        /*
        设置拒绝策略
        AbortPolicy:直接抛出java.util.concurrent.RejectedExecutionException异常
        CallerRunsPolicy:主线程直接执行该任务，执行完之后尝试添加下一个任务到线程池中，可以有效降低向线程池内添加任务的速度
        DiscardOldestPolicy:抛弃旧的任务、暂不支持；会导致被丢弃的任务无法再次被执行
        DiscardPolicy:抛弃当前任务、暂不支持；会导致被丢弃的任务无法再次被执行
         */
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 执行初始化
        executor.initialize();
        return  executor;
    }
    @Bean("simpleThreadPool")
    public ThreadPoolExecutor simpleThreadPool(){
        return new ThreadPoolExecutor(
                8,
                16,
                60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(16),
                new ThreadPoolExecutor.CallerRunsPolicy());

    }

}
