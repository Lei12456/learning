package com.yl.redis.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")//服务停止后调用shutdwon
    public RedissonClient redissonClient()  {
        Config config = new Config();
        //集群模式
        //config.useClusterServers().addNodeAddress("localhost","localhost");
        //单节点配置
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        return Redisson.create(config);
    }
}
