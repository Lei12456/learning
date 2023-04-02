package com.yl.designpattern.proxy;

public class FoodProxyServiceImpl implements FoodService{
    //注入目标对象
    private FoodServiceImpl foodService = new FoodServiceImpl();

    @Override
    public Food makgeChicken() {
        //对目标方法做一些增强
        Food food = foodService.makgeChicken();
        return food;
    }

    @Override
    public Food makeNoodle() {
        return null;
    }
}
