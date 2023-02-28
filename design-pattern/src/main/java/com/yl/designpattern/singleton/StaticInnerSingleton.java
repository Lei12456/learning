package com.yl.designpattern.singleton;

/**
 * @description 嵌套类 ，静态内部类方式
 * @author yanglei
 * @date 2023/2/22 18:55
 */
public class StaticInnerSingleton {
    private StaticInnerSingleton(){}

    private static class Inner {
        private static StaticInnerSingleton intance = new StaticInnerSingleton();
    }

    public static StaticInnerSingleton getInstance(){
       return Inner.intance;
    }
}
