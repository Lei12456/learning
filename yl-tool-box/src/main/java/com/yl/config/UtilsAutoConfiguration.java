package com.yl.config;

import com.yl.utils.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StringUtilsProperties.class)
public class UtilsAutoConfiguration {

    private final StringUtilsProperties properties;

    public UtilsAutoConfiguration(StringUtilsProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnProperty(name = "string.utils.enable-random-string", havingValue = "true", matchIfMissing = true)
    public StringUtils stringUtils() {
        return new StringUtils();
    }
}