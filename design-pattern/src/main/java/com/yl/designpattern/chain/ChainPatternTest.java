package com.yl.designpattern.chain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;

@Component
public class ChainPatternTest {

    @Autowired
    private List<Abstracthandler> abstracthandlerList;

    private Abstracthandler abstracthandler;

    @PostConstruct
    public void initializeChainFilter(){
        for (int i = 0; i < abstracthandlerList.size(); i++) {
            if (Objects.equals(i,0)){
                abstracthandler = abstracthandlerList.get(i);
            }else {
                Abstracthandler currentAbstractHandler = abstracthandlerList.get(i - 1);
                Abstracthandler nextAbstractHandler = abstracthandlerList.get(i);
                currentAbstractHandler.setNextHandler(nextAbstractHandler);
            }
        }
    }

}
