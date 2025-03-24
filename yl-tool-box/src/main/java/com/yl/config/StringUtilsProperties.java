package com.yl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "string.utils")
public class StringUtilsProperties  {
    private boolean enableRandomString = true;

    public boolean isEnableRandomString() {
        return enableRandomString;
    }

    public void setEnableRandomString(boolean enableRandomString) {
        this.enableRandomString = enableRandomString;
    }
}
