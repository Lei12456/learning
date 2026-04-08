package com.yl.source.aqs;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于 {@link ReentrantLock} + {@link Condition} 实现的有界缓冲区。
 * <p>
 * 这个类是 AQS 条件队列经典示例：
 * <ul>
 *     <li>缓冲区满时，生产者在 {@code notFull} 条件队列等待</li>
 *     <li>缓冲区空时，消费者在 {@code notEmpty} 条件队列等待</li>
 *     <li>通过 {@code signal()} 精确唤醒目标角色，避免无效唤醒</li>
 * </ul>
 * </p>
 */
public class BoundedBuffer {
    final Lock lock = new ReentrantLock();

    final Condition notFull = lock.newCondition();

    final Condition notEmpty = lock.newCondition();

    Object[] items = new Object[100];

    int putptr , takeptr, count;

    /**
     * 向缓冲区放入一个元素。
     *
     * @param x 待放入元素
     * @throws InterruptedException 等待过程中线程被中断
     */
    public void put(Object x) throws InterruptedException {
        lock.lock();
        try {
            // 使用 while 防止虚假唤醒（spurious wakeup）
            while (count == items.length) {
                notFull.await();
            }

            items[putptr] = x;
            if (++putptr == items.length) {
                putptr = 0;
            }
            count++;

            // 成功放入后，通知消费者可以取数据
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 从缓冲区取出一个元素。
     *
     * @return 取出的元素
     * @throws InterruptedException 等待过程中线程被中断
     */
    public Object take() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0) {
                notEmpty.await();
            }

            Object x = items[takeptr];
            items[takeptr] = null;
            if (++takeptr == items.length) {
                takeptr = 0;
            }
            count--;

            // 成功消费后，通知生产者可以继续放入
            notFull.signal();
            return x;
        } finally {
            lock.unlock();
        }
    }


}
