package com.yl.designpattern.observer.eventbus;

import com.google.common.eventbus.EventBus;

public class EventBusCenter{
    private static EventBus eventBus = new EventBus();
    
    private EventBusCenter(){
        
    }
    
    public static void register(Object obj){
        eventBus.register(obj);
    }

    public static void unRegister(Object obj){
        eventBus.unregister(obj);
    }

    public static void post(Object obj){
        eventBus.post(obj);
    }

}
