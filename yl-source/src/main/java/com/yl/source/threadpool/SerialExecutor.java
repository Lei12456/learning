package com.yl.source.threadpool;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;

/**
 * 串行执行器：把提交的任务排队后逐个执行。
 * <p>
 * 该类的核心思想是：
 * <ol>
 *     <li>外部可并发提交任务</li>
 *     <li>内部维护 FIFO 队列</li>
 *     <li>任意时刻只允许一个任务处于执行态</li>
 * </ol>
 * 典型用途：保证同一业务维度下任务顺序执行（例如单用户事件流）。
 * </p>
 */
public class SerialExecutor implements Executor {
    //任务队列
    private final Queue<Runnable> tasks = new ArrayDeque<>();

    //真正的任务执行器
    private final Executor executor;

    private Runnable active;
    //初始化
    public SerialExecutor(Executor executor) {
        this.executor = executor;
    }

    @Override
    public synchronized void execute(Runnable command) {
        tasks.offer(() -> {
            try {
                command.run();
            } finally {
                scheduleNext();
            }
        });

        // 队列从空到非空时，启动调度
        if (active == null) {
            scheduleNext();
        }
    }

    protected synchronized void scheduleNext(){
        if ((active = tasks.poll()) != null){
            executor.execute(active);
        }
    }
}
