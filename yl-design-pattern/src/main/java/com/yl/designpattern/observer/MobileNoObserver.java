package com.yl.designpattern.observer;

public class MobileNoObserver implements Observer{
    @Override
    public void doEvent() {
        System.out.println("发送短信消息");
    }
}
