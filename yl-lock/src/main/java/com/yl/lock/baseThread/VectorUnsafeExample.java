package com.yl.lock.baseThread;

import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName VectorUnsafeExample.java
 * @Description 相对安全的集合类
 * @createTime 2023年11月09日 11:13:00
 */
public class VectorUnsafeExample {

    private static Vector<Integer> vector = new Vector<>();

    public static void main(String[] args) {
        VectorUnsafeExample vectorUnsafeExample = new VectorUnsafeExample();
        //vectorUnsafeExample.unSafe();
        vectorUnsafeExample.safe();
    }

    private void unSafe() {
        while (true) {
            for (int i = 0; i < 100; i++) {
                vector.add(i);
            }
            ExecutorService executorService = Executors.newCachedThreadPool();

            //线程一删除
            executorService.execute(() -> {
                for(int i = 0;i < vector.size(); i++){
                    vector.remove(i);
                }
            });
            //线程二获取
            executorService.execute(() -> {
                for(int i = 0;i < vector.size(); i++){
                    vector.get(i);
                }
            });
            //结果:数据越界
        }
    }

    private void safe() {
        ReentrantLock reentrantLock = new ReentrantLock();

        while (true) {
            for (int i = 0; i < 100; i++) {
                vector.add(i);
            }
            ExecutorService executorService = Executors.newCachedThreadPool();

            //线程一删除
            executorService.execute(() -> {
                try {
                    reentrantLock.lock();
                    for(int i = 0;i < vector.size(); i++){
                        vector.remove(i);
                    }
                }finally {
                    reentrantLock.unlock();
                }

            });
            //线程二获取
            executorService.execute(() -> {
                try {
                    reentrantLock.lock();
                    for(int i = 0;i < vector.size(); i++){
                        vector.get(i);
                    }
                }finally {
                    reentrantLock.unlock();
                }
            });
            //结果:数据越界
        }
    }
}
