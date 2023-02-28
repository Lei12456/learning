package com.yl.designpattern.observer;

public class EmailObserver implements Observer{
    @Override
    public void doEvent() {
        System.out.println("发送邮件消息");
    }
}
