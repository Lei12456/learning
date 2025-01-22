package com.yl.aop;

import com.yl.aop.proxy.IJdkProxyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

@SpringBootTest
public class YlSpringAopApplicationTests {
    @Autowired
    private IJdkProxyService iJdkProxyService;

    @Test
    void contextLoads() {
        iJdkProxyService.doMethod1();
    }

}
