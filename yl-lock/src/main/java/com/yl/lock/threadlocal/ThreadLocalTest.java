package com.yl.lock.threadlocal;

public class ThreadLocalTest {
    ThreadLocal<Long> longLocal = new ThreadLocal<Long>();
    ThreadLocal<String> stringLocal = new ThreadLocal<String>();

    public void set() {
        longLocal.set(1L);
        stringLocal.set(Thread.currentThread().getName());
    }

    public long getLong() {
        return longLocal.get();
    }

    public String getString() {
        return stringLocal.get();
    }

    public static void main(String[] args) throws InterruptedException {
        new ThreadLocalTest().parentChildThread();
    }

    public void parentChildThread(){
        stringLocal.set("Shared Data");

        //子线程
        new Thread(() -> {
            System.out.println("Child Thread:"+ stringLocal.get());
        }).start();

    }
}