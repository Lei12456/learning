package com.yl.designpattern.chain;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import sun.misc.Request;

import javax.xml.ws.Response;

@Component
@Order(3)
public class CheckRuleHandler extends Abstracthandler{
    @Override
    void doFilter(Request filterRequest, Response response) {
        System.out.println("校验规则");
    }
}
