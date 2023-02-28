package com.yl.source.threadpool;

import java.util.concurrent.Executor;

public class DirectExecutor implements Executor {

    @Override
    public void execute(Runnable command) {
        command.run();//这里代表不是执行启动线程
        new Thread(command).start();//每个任务都用一个线程执行
    }
}
