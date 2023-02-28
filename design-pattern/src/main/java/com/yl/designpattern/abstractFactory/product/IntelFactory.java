package com.yl.designpattern.abstractFactory.product;

import com.yl.designpattern.abstractFactory.factory.Cpu;
import com.yl.designpattern.abstractFactory.factory.HardDisk;
import com.yl.designpattern.abstractFactory.factory.MainBoard;

public class IntelFactory implements ComputerFactory{
    @Override
    public Cpu makeCpu() {
        return null;
    }

    @Override
    public MainBoard makeMainBoard() {
        return null;
    }

    @Override
    public HardDisk makeHardDisk() {
        return null;
    }
}
