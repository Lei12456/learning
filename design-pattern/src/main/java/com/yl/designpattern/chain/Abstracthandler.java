package com.yl.designpattern.chain;

import sun.misc.Request;

import javax.xml.ws.Response;

public abstract class Abstracthandler {
    /**
     * 责任链的下一个对象
     */
    private Abstracthandler nextHandler;

    /**
     * 设置责任链的下一个对象
     */
    public void setNextHandler(Abstracthandler nextHandler){
        this.nextHandler = nextHandler;
    }

    /**
     * 获取责任链的下一个对象
     */
    public Abstracthandler getNextHandler(){
         return nextHandler;
    }


    public void filter(Request request, Response response){
        doFilter(request,response);
        if (getNextHandler() != null){
            nextHandler.filter(request,response);
        }
    }

    abstract void doFilter(Request filterRequest , Response response);
}
