package com.yl.designpattern.singleton;

/**
 * @description 懒汉式
 * @author yanglei
 * @date 2023/2/22 18:51
 */
public class SlobSingleton {
    private SlobSingleton(){
    }
    //
    private static volatile SlobSingleton intance = null;

    private static SlobSingleton getInstance(){
        if (intance == null){
            synchronized (SingletonDemo.class){
                if (intance == null){
                    intance = new SlobSingleton();
                }
            }
        }
        return intance;
    }
}
