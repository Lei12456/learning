package com.yl.collection;

import java.util.ArrayList;
import java.util.List;

public class ArrayListWithNullNotThreadSafeExample {
    // 定义一个静态的 ArrayList 用于存储元素
    private static final List<Integer> list = new ArrayList<>();
    // 每个线程添加元素的数量
    private static final int ADD_COUNT = 1000;
    // 线程数量
    private static final int THREAD_COUNT = 10;

    public static void main(String[] args) throws InterruptedException {
        // 创建线程数组
        Thread[] threads = new Thread[THREAD_COUNT];

        // 创建并启动多个线程
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < ADD_COUNT; j++) {
                    // 随机决定是否添加 null 元素，这里以 20% 的概率添加 null
                    if (Math.random() < 0.2) {
                        list.add(null);
                    } else {
                        list.add(j);
                    }
                }
            });
            threads[i].start();
        }

        // 等待所有线程执行完毕
        for (Thread thread : threads) {
            thread.join();
        }

        // 统计 null 元素的数量
        int nullCount = 0;
        for (Integer element : list) {
            if (element == null) {
                nullCount++;
            }
        }

        // 输出结果
        System.out.println("Total elements in the list: " + list.size());
        System.out.println("Number of null elements: " + nullCount);
        // 预期 null 元素的数量，大约是 20% 的添加总数
        int expectedNullCount = (int) (THREAD_COUNT * ADD_COUNT * 0.2);
        System.out.println("Expected number of null elements: " + expectedNullCount);

        // 由于 ArrayList 线程不安全，可能出现数据不一致等问题，导致 null 元素数量和元素总数可能不符合预期
        if (list.size() != THREAD_COUNT * ADD_COUNT || nullCount != expectedNullCount) {
            System.out.println("ArrayList is not thread-safe.");
        }
    }
}