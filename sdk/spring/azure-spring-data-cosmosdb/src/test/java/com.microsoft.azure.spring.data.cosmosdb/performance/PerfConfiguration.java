// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.performance;

import com.microsoft.azure.spring.data.cosmosdb.config.AbstractCosmosConfiguration;
import com.microsoft.azure.spring.data.cosmosdb.config.CosmosDBConfig;
import com.microsoft.azure.spring.data.cosmosdb.performance.utils.Constants;
import com.microsoft.azure.spring.data.cosmosdb.repository.config.EnableCosmosRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {"classpath:application.properties"})
@EnableCosmosRepositories
public class PerfConfiguration extends AbstractCosmosConfiguration {
    @Value("${cosmosdb.uri:}")
    private String cosmosDbUri;

    @Value("${cosmosdb.key:}")
    private String cosmosDbKey;

    @Bean
    public CosmosDBConfig getConfig() {
        return CosmosDBConfig.builder(cosmosDbUri, cosmosDbKey, Constants.PERF_DATABASE_NAME).build();
    }
}
