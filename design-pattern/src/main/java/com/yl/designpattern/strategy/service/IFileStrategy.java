package com.yl.designpattern.strategy.service;

public interface IFileStrategy {
    /**
     * @description 获取文件解析的类型
     * @author yanglei
     * @date 2022/12/21 16:09
     * @return Integer
     */
    Integer getFileType();

    /**
     * @description 具体的解析方法
     * @author yanglei
     * @date 2022/12/21 16:09
     * @param obj
     */
    void resolve(Object obj);
}
