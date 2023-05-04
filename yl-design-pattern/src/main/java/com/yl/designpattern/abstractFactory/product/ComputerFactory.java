package com.yl.designpattern.abstractFactory.product;

import com.yl.designpattern.abstractFactory.factory.Cpu;
import com.yl.designpattern.abstractFactory.factory.HardDisk;
import com.yl.designpattern.abstractFactory.factory.MainBoard;

/**
 * @description 针对于产品族 cpu 硬盘 主板
 *              配置对应的电脑工厂
 * @author yanglei
 * @date 2023/2/22 18:39
 */
public interface ComputerFactory {
    /**
     * @description 制作cpu
     * @author yanglei
     * @date 2023/2/22 18:41
     * @return Cpu
     */
    Cpu makeCpu();

    /**
     * @description 制作主板
     * @author yanglei
     * @date 2023/2/22 18:41
     * @return Cpu
     */
    MainBoard makeMainBoard();


    /**
     * @description 制作硬盘
     * @author yanglei
     * @date 2023/2/22 18:41
     * @return Cpu
     */
    HardDisk makeHardDisk();



}
