package com.yl.designpattern.adapter;

public class CockAdapter implements Duck{
    Cock cock;

    public CockAdapter(Cock cock){
        this.cock = cock;
    }

    @Override
    public void quack() {
        //适配鸡的咕咕叫
        cock.gobble();

    }

    @Override
    public void fly() {
        cock.fly();
    }

    public static void main(String[] args) {
        WildCock wildCock = new WildCock();
        Duck duck = new CockAdapter(wildCock);
        duck.quack();
    }
}
