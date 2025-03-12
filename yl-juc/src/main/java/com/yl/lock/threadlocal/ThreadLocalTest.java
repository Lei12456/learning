package com.yl.lock.threadlocal;

public class ThreadLocalTest {

    //创建一个ThreadLocal变量，用户存储每个线程的计数器
   private static final ThreadLocal<Integer> threadLocalCounter =  ThreadLocal.withInitial(() -> 0);

    public static void main(String[] args) {
        new Thread(new CounterTasklet(1)).start();
        /*
        结果，每个线程的
        Thread-1 count = 1
        Thread-1 count = 2
        Thread-1 count = 3
        Thread-1 count = 4
        Thread-1 count = 5
        Thread-1 count = 6
        Thread-1 count = 7
        Thread-1 count = 8
        Thread-1 count = 9
        Thread-1 count = 10
         */
    }
    //静态内部类
    public static class CounterTasklet implements Runnable {
        private final int threadId;

        public CounterTasklet(int threadId) {
            this.threadId = threadId;
        }

        @Override
        public void run() {
            //创建10个线程，每个线程增加自己的计数器
            for (int i = 0; i < 10; i++) {
                Integer count = threadLocalCounter.get();
                threadLocalCounter.set(count + 1);
                System.out.println("Thread-" + threadId + " count = " + threadLocalCounter.get());
            }
        }
    }


}
