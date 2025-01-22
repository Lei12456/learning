package com.yl.aop.proxy.impl;

import com.yl.aop.proxy.IJdkProxyService;
import org.springframework.stereotype.Component;

@Component
public class IJdkProxyServiceImpl implements IJdkProxyService {

    @Override
    public void doMethod1() {
        System.out.println("doMethod1");
    }

    @Override
    public String doMethod2() {
        System.out.println("doMethod2");
        return "doMethod2";
    }

    @Override
    public String doMethod3() throws Exception {
        System.out.println("doMethod3");
        return "doMethod3";
    }


}
