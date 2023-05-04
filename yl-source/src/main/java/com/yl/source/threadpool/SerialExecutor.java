package com.yl.source.threadpool;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;

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
    public void execute(Runnable command) {
        tasks.offer(new Runnable() {
            @Override
            public void run() {
                command.run();
            }
        });
    }

    protected synchronized void scheduleNext(){
        if ((active = tasks.poll()) != null){
            executor.execute(active);
        }
    }
}
