package com.ylspi;

import com.ylspi.service.SpiService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Iterator;
import java.util.ServiceLoader;

@SpringBootTest
class YlSpiApplicationTests {

    @Test
    void contextLoads() {
        ServiceLoader<SpiService> spiServices = ServiceLoader.load(SpiService.class);
        Iterator<SpiService> iterator = spiServices.iterator();
        while (iterator.hasNext()) {
            SpiService spiService = iterator.next();
            spiService.execute();
        }
    }

}
