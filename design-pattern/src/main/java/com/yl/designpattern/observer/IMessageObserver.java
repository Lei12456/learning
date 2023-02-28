package com.yl.designpattern.observer;

public class IMessageObserver implements Observer{
    @Override
    public void doEvent() {
        System.out.println("发送im消息");
    }
}
