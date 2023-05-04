package com.yl.designpattern.strategy.service;


import org.springframework.stereotype.Component;

@Component
public class AFileResolve implements IFileStrategy{
    @Override
    public Integer getFileType() {
        return 1;
    }

    @Override
    public void resolve(Object obj) {

    }
}
