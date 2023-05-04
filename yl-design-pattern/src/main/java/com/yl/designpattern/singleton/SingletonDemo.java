package com.yl.designpattern.singleton;

public class SingletonDemo {
    private static volatile SingletonDemo instence = null;

    private SingletonDemo(){
        System.out.println("我是构造器");
    }
    //1 、 Double check lock 指令重排会有问题
    //2 、使用volatile禁止指令重排序
    private static SingletonDemo getInstence() {
        if (instence == null){
            synchronized (SingletonDemo.class) {
                if (instence == null) {
                    instence = new SingletonDemo();
                }
            }
        }
        return instence;
    }
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            new Thread(SingletonDemo::getInstence,String.valueOf(i)).start();
        }
    }
}
