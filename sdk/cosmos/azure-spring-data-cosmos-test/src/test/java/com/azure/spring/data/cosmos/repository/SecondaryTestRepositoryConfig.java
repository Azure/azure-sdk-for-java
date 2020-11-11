// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.StringUtils;

/**
 * Secondary Database Account
 */
@Configuration
@PropertySource(value = {"classpath:application.properties"})
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
    public CosmosClientBuilder secondaryCosmosClientBuilder() {
        return new CosmosClientBuilder()
            .key(cosmosDbKey)
            .endpoint(cosmosDbUri)
            .contentResponseOnWriteEnabled(true);
    }

    @Bean("secondaryCosmosAsyncClient")
    public CosmosAsyncClient getCosmosAsyncClient(CosmosClientBuilder secondaryCosmosClientBuilder) {
        return CosmosFactory.createCosmosAsyncClient(secondaryCosmosClientBuilder);
    }

    /**
     * First database for this account
     */
    @EnableReactiveCosmosRepositories(reactiveCosmosTemplateRef = "secondaryReactiveCosmosTemplate")
    public class SecondaryDataSourceConfiguration {
        @Bean
        public ReactiveCosmosTemplate secondaryReactiveCosmosTemplate(@Qualifier("secondaryCosmosAsyncClient") CosmosAsyncClient client, MappingCosmosConverter mappingCosmosConverter) {

            CosmosConfig config =  CosmosConfig.builder()
                .enableQueryMetrics(queryMetricsEnabled)
                .build();

            return new ReactiveCosmosTemplate(new CosmosFactory(client, getFirstDatabase()), config, mappingCosmosConverter);
        }
    }

    /**
     * Second database for this account
     */
    @EnableReactiveCosmosRepositories(reactiveCosmosTemplateRef = "secondaryReactiveCosmosTemplate1")
    public class SecondaryDataSourceConfiguration1 {
        @Bean
        public ReactiveCosmosTemplate secondaryReactiveCosmosTemplate1(@Qualifier("secondaryCosmosAsyncClient") CosmosAsyncClient client, MappingCosmosConverter mappingCosmosConverter) {

            CosmosConfig config =  CosmosConfig.builder()
                .enableQueryMetrics(queryMetricsEnabled)
                .build();

            return new ReactiveCosmosTemplate(new CosmosFactory(client, getSecondDatabase()), config, mappingCosmosConverter);
        }
    }

    private String getFirstDatabase() {
        return StringUtils.hasText(this.database) ? this.database : "test_db_1";
    }

    private String getSecondDatabase() {
        return "test_db_2";
    }

}
