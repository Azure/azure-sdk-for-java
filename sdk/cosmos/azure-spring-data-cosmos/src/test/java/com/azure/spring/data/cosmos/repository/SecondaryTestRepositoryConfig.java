// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.StringUtils;

@Configuration
@PropertySource(value = {"classpath:application.properties"})
@EnableCosmosRepositories
@EnableReactiveCosmosRepositories(reactiveCosmosTemplateRef = "secondaryReactiveCosmosTemplate")
public class SecondaryTestRepositoryConfig {
    @Value("${cosmos.secondary.uri:}")
    private String cosmosDbUri;

    @Value("${cosmos.secondary.key:}")
    private String cosmosDbKey;

    @Value("${cosmos.secondary.database:}")
    private String database;

    @Value("${cosmos.secondary.queryMetricsEnabled}")
    private boolean queryMetricsEnabled;

    @Bean
    public ReactiveCosmosTemplate secondaryReactiveCosmosTemplate(MappingCosmosConverter mappingCosmosConverter) {
        final String dbName = StringUtils.hasText(this.database) ? this.database : TestConstants.SECONDARY_DB_NAME;
        CosmosConfig config =  CosmosConfig.builder()
                                            .cosmosClientBuilder(new CosmosClientBuilder()
                                                .key(cosmosDbKey)
                                                .endpoint(cosmosDbUri)
                                                .contentResponseOnWriteEnabled(true))
                                            .database(dbName)
                                            .enableQueryMetrics(queryMetricsEnabled)
                                            .build();
        return new ReactiveCosmosTemplate(new CosmosFactory(config), mappingCosmosConverter, config.getDatabase());
    }
}
