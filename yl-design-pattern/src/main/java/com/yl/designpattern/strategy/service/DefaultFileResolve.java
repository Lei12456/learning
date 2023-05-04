package com.yl.designpattern.strategy.service;


import org.springframework.stereotype.Component;

@Component
public class DefaultFileResolve implements IFileStrategy{
    @Override
    public Integer getFileType() {
        return 0;
    }

    @Override
    public void resolve(Object obj) {
        System.out.println("这个是默认解析的方法");
    }
}
