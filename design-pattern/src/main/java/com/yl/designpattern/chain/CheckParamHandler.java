package com.yl.designpattern.chain;


import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import sun.misc.Request;

import javax.xml.ws.Response;

@Component
@Order(1) //排序最先校验
public class CheckParamHandler extends Abstracthandler{

    @Override
    void doFilter(Request filterRequest, Response response) {
        System.out.println("参数校验");
    }
}
