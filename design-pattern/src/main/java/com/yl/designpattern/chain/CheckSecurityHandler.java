package com.yl.designpattern.chain;


import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import sun.misc.Request;

import javax.xml.ws.Response;

@Component
@Order(2) //排序
public class CheckSecurityHandler extends Abstracthandler{

    @Override
    void doFilter(Request filterRequest, Response response) {
        System.out.println("安全认证校验");
    }
}
