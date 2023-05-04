package com.yl.lock.blockQueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @description 阻塞队列的定：
 *              当队列为空的时候，从对列中获取元素的操作将会被阻塞
 *              当队列是满的时候，往队列中添加元素的操作将会被阻塞
 *              空了消费者阻塞，满了生产者阻塞
 *              阻塞：wait() 唤醒：notify()
 * @author yanglei
 * @date 2023/1/19 18:12
 */
public class BlockingLockDemo {
    public static void main(String[] args) {
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(3);
        blockingQueue.add("a");
        blockingQueue.add("b");
        blockingQueue.add("c");

        blockingQueue.add("E");
        System.out.println(blockingQueue);
    }
}
