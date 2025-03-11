package com.yl.collection;

import java.util.ArrayList;
import java.util.List;

public class ArrayListNotThreadSafeExample {
    // 创建一个 ArrayList 对象
    private static final List<Integer> list = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        // 定义线程数量
        int threadCount = 10;
        // 创建线程数组
        Thread[] threads = new Thread[threadCount];

        // 创建并启动多个线程，每个线程向 ArrayList 中添加 1000 个元素
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    list.add(j);
                }
            });
            threads[i].start();
        }

        // 等待所有线程执行完毕
        for (Thread thread : threads) {
            thread.join();
        }

        // 预期的元素数量应该是 10 * 1000 = 10000
        int expectedSize = threadCount * 1000;
        // 实际的元素数量
        int actualSize = list.size();

        // 输出预期和实际的元素数量
        System.out.println("Expected size: " + expectedSize);
        System.out.println("Actual size: " + actualSize);

        // 如果实际数量不等于预期数量，说明 ArrayList 不是线程安全的
        if (actualSize != expectedSize) {
            System.out.println("ArrayList is not thread-safe.");
        }

        new ArrayListNotThreadSafeExample().getList();
    }

    public static void getList() {
        int[] hash = new int[26];

        int i = hash['y' - 'a'];
        System.out.printf(String.valueOf(i));
    }
}