package com.yl.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.jdbc.DataSourceHealthContributorAutoConfiguration;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceHealthConfig extends DataSourceHealthContributorAutoConfiguration {


    @Value("${spring.datasource.dbcp2.validation-query:select 1}")
    private String defaultQuery;


    public DataSourceHealthConfig(ObjectProvider<DataSourcePoolMetadataProvider> metadataProviders) {
        super(metadataProviders);
    }
}
