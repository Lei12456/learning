package com.yl.redis.config;

import io.lettuce.core.metrics.CommandLatencyCollectorOptions;
import io.lettuce.core.resource.DefaultClientResources;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.ClassUtils;

@Configuration
public class RedisConfig {

    @Bean(destroyMethod = "shutdown")//服务停止后调用shutdwon
    public RedissonClient redissonClient()  {
        Config config = new Config();
        //集群模式
        //config.useClusterServers().addNodeAddress("localhost","localhost");
        //单节点配置
        config.useSingleServer().setAddress("redis://121.199.60.142:6379").setPassword("123456");
        return Redisson.create(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

//    @Bean(destroyMethod = "shutdown")
//    DefaultClientResources lettuceClientResources() {
//        if (ClassUtils.hasMethod(CommandLatencyCollectorOptions.class, "disabled")) {
//            return DefaultClientResources.builder()
//                    .commandLatencyCollectorOptions(CommandLatencyCollectorOptions.disabled())
//                    .build();
//        } else {
//            return DefaultClientResources.builder().build();
//        }
//    }
}
