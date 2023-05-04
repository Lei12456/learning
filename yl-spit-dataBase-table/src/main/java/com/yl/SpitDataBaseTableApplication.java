package com.yl;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAutoConfiguration
@EnableTransactionManagement
@ComponentScan("com.yl")
public class SpitDataBaseTableApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpitDataBaseTableApplication.class);
    }
}