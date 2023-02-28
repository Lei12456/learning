package com.yl.designpattern.singleton;

/**
 * @description 饿汉式
 * @author yanglei
 * @date 2023/2/22 18:51
 */
public class HungrySingleton {
    //不让别人new
    private HungrySingleton(){}

    //创建静态的实例，意味着只有第一次使用的时候才会创建
    private static HungrySingleton intance = new HungrySingleton();

    private static HungrySingleton getInstance(){
        return intance;
    }

}
