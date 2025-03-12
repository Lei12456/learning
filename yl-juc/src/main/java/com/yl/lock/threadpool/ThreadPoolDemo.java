package com.yl.lock.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ThreadPoolDemo {
    public static void main(String[] args) {
        // 1. 创建固定大小为5的线程池（5个收银员）
        ExecutorService executor = Executors.newFixedThreadPool(5);

        // 2. 提交10个任务（10个顾客要结账）
        for (int i = 1; i <= 10; i++) {
            final int taskId = i;
            Future<?> future
                    = executor.submit(() -> {
                // 模拟业务处理
                System.out.println(Thread.currentThread().getName()
                        + " 正在处理任务 " + taskId);
                try {
                    // 模拟耗时操作
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(">>> 任务 " + taskId + " 处理完成");
            });// 3. 优雅关闭线程池
            executor.execute(() -> {
                // 模拟业务处理
                System.out.println(Thread.currentThread().getName()
                        + " 正在处理任务 " + taskId);
                try {
                    // 模拟耗时操作
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(">>> 任务 " + taskId + " 处理完成");
            });
        }
        executor.shutdownNow();
    // 停止接收新任务
        try {
            // 等待所有任务完成，最多等待1分钟
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // 强制终止
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}