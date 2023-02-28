package com.yl.designpattern.observer.eventbus;

import com.google.common.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class EventListener {

    @Subscribe
    public void handle(NotifyEvent notifyEvent){
        System.out.println("发送IM消息" + notifyEvent.getImNo());
        System.out.println("发送移动端消息" + notifyEvent.getMobileNo());
        System.out.println("发送邮件消息" + notifyEvent.getEmailNo());

        Vector<Object> objects = new Vector<Object>();
        Iterator<Object> iterator = objects.iterator();

        List<Object> objectsList = new ArrayList<Object>();
        List<String> stringList = new ArrayList<String>();
    }

}
