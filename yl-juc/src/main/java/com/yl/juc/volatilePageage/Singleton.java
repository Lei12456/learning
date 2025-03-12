package com.yl.juc.volatilePageage;

public class Singleton {

    private static volatile Singleton instance;

    private Singleton() {

    }

    //双重校验锁DCL
    public static Singleton getInstance(){
        if(instance == null){
            synchronized (Singleton.class){
                if(instance == null){
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }

}
