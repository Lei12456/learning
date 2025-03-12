package com.yl.lock.synchronizedlock;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName SynchronizedObjectLock02.java
 * @Description
 * @createTime 2023年11月08日 15:56:00
 */
public class SynchronizedObjectLock02 implements Runnable{

    static SynchronizedObjectLock02 instance = new SynchronizedObjectLock02();

    @Override
    public void run() {
        System.out.println("我是线程" + Thread.currentThread().getName());
        synchronized (this){
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "结束");
        }
    }

    public static void main(String[] args) {
        Thread thread1 = new Thread(instance);
        Thread thread2 = new Thread(instance);
        thread1.start();
        thread2.start();
    }
}
