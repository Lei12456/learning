package com.yl.designpattern.strategy.config;

import com.yl.designpattern.strategy.service.IFileStrategy;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FileResolveStrategyConfig implements ApplicationContextAware {

    //将每个策略初始化到map中
    private Map<Integer, IFileStrategy> fileStrategyMap = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, IFileStrategy> map = applicationContext.getBeansOfType(IFileStrategy.class);
        map.values().forEach(strategy -> fileStrategyMap.put(strategy.getFileType(),strategy));
    }
}
