package com.ylspi.test;

import com.ylspi.service.SpiService;

import java.util.ServiceLoader;

public class SpiTest {

    public static void main(String[] args) {
        ServiceLoader<SpiService> spiServices = ServiceLoader.load(SpiService.class);
        for (SpiService spiService : spiServices) {
            spiService.execute();
        }
    }
}
