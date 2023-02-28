package com.yl.designpattern.strategy.service;


import org.springframework.stereotype.Component;

@Component
public class BFileResolve implements IFileStrategy{
    @Override
    public Integer getFileType() {
        return 2;
    }

    @Override
    public void resolve(Object obj) {

    }
}
