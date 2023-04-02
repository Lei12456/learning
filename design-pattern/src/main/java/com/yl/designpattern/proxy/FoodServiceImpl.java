package com.yl.designpattern.proxy;

public class FoodServiceImpl implements FoodService{

    @Override
    public Food makgeChicken() {
        return new Food();
    }

    @Override
    public Food makeNoodle() {
        return new Food();
    }
}
