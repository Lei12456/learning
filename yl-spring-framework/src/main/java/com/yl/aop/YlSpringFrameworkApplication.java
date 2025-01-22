package com.yl.aop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "com.yl")
public class YlSpringFrameworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(YlSpringFrameworkApplication.class, args);
    }

}
