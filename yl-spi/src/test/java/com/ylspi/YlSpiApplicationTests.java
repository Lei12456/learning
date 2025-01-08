package com.ylspi;

import com.ylspi.service.SpiService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ServiceLoader;

@SpringBootTest
class YlSpiApplicationTests {

    @Test
    void contextLoads() {
        ServiceLoader<SpiService> spiServices = ServiceLoader.load(SpiService.class);
        for (SpiService spiService : spiServices) {
            spiService.execute();
        }
    }

}
