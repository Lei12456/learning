package com.yl.designpattern.observer.eventbus;

public class EnentBusTest {

    public static void main(String[] args) {
        EventListener eventListener = new EventListener();
        NotifyEvent notifyEvent = new NotifyEvent();
        notifyEvent.setImNo("1111");
        notifyEvent.setMobileNo("22222");
        notifyEvent.setEmailNo("33333");
        eventListener.handle(notifyEvent);
    }
}
