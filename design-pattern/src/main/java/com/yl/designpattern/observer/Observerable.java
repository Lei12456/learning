package com.yl.designpattern.observer;

import java.util.List;
import java.util.ArrayList;

public class Observerable {

    private List<Observer> observerList = new ArrayList<Observer>();

    private int state;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    //添加观察者
    public void addServer(Observer observer){
        observerList.add(observer);
    }

    //移除观察者
    public void removeObserver(Observer observer){
        observerList.remove(observer);
    }

    //通知
    public void notifyAllObservers(int state){
        if (state != 1){
            System.out.println("不是通知的状态");
        }
        for (Observer observer : observerList) {
            observer.doEvent();
        }
    }
}
