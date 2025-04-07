package com.yl.redis.config;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Configuration
public class CanalConfig {

    @Value("${canal.server.host}")
    private String canalHost;

    @Value("${canal.server.port}")
    private int canalPort;

    @Value("${canal.destination}")
    private String canalDestination;

    @Value("${canal.username}")
    private String canalUsername;

    @Value("${canal.password}")
    private String canalPassword;

    @Bean
    public CanalConnector canalConnector() {
        // 创建 Canal 连接器
        return CanalConnectors.newSingleConnector(new InetSocketAddress(canalHost, canalPort), canalDestination, "", "");

    }
}
