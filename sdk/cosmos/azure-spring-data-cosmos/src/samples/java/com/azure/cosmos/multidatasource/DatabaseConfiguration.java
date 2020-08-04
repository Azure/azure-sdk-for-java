// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.multidatasource;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosProperties;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.core.ResponseDiagnostics;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.mapping.EnableCosmosAuditing;
import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;
import org.springframework.lang.Nullable;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 */
@Configuration
@EnableCosmosAuditing
@PropertySource("classpath:application.properties")
public class DatabaseConfiguration extends AbstractCosmosConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfiguration.class);

    @Bean
    @ConfigurationProperties(prefix = "azure.cosmos.primary")
    public CosmosProperties primaryDataSourceConfiguration() {
        return new CosmosProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "azure.cosmos.secondary")
    public CosmosProperties secondaryDataSourceConfiguration() {
        return new CosmosProperties();
    }

    @Autowired
    @Qualifier("primaryDataSourceConfiguration")
    CosmosProperties primaryProperties;

    @Autowired
    @Qualifier("secondaryDataSourceConfiguration")
    CosmosProperties secondaryProperties;

    @Autowired(required = false)
    private IsNewAwareAuditingHandler cosmosAuditingHandler;

    @Bean
    public CosmosClientBuilder cosmosClientBuilder() {
        return new CosmosClientBuilder()
            .key(primaryProperties.getKey())
            .endpoint(primaryProperties.getUri());
    }

    @Bean
    public CosmosClientBuilder secondaryCosmosClientBuilder() {
        return new CosmosClientBuilder()
            .key(secondaryProperties.getKey())
            .endpoint(secondaryProperties.getUri());
    }
    // -------------------------First Cosmos Client for First Cosmos Account---------------------------
    @EnableReactiveCosmosRepositories(basePackages = "com.azure.cosmos.multidatasource.primarydatasource.first")
    public class PrimaryDataSourceConfiguration {

    }
    @EnableReactiveCosmosRepositories(basePackages = "com.azure.cosmos.multidatasource.primarydatasource.second", reactiveCosmosTemplateRef = "primaryReactiveCosmosTemplate")
    public class PrimaryDataSourceConfiguration2 {
        @Bean
        public ReactiveCosmosTemplate primaryReactiveCosmosTemplate(CosmosAsyncClient cosmosAsyncClient, CosmosConfig cosmosConfig, MappingCosmosConverter mappingCosmosConverter) {
            return new ReactiveCosmosTemplate(cosmosAsyncClient, "test1_2", cosmosConfig, mappingCosmosConverter, cosmosAuditingHandler);
        }
    }

    // -------------------------Second Cosmos Client for Secondary Cosmos Account---------------------------
    @Bean("secondaryCosmosAsyncClient")
    public CosmosAsyncClient getCosmosAsyncClient(CosmosClientBuilder secondaryCosmosClientBuilder) {
        return CosmosFactory.createCosmosAsyncClient(secondaryCosmosClientBuilder);
    }

    @Bean("secondaryCosmosConfig")
    public CosmosConfig getCosmosConfig() {
        return CosmosConfig.builder()
                           .enableQueryMetrics(true)
                           .responseDiagnosticsProcessor(new ResponseDiagnosticsProcessorImplementation())
                           .build();
    }

    @EnableReactiveCosmosRepositories(basePackages = "com.azure.cosmos.multidatasource.secondarydatasource.first", reactiveCosmosTemplateRef = "secondaryReactiveCosmosTemplate")
    public class SecondaryDataSourceConfiguration {
        @Bean
        public CosmosTemplate secondaryReactiveCosmosTemplate(@Qualifier("secondaryCosmosAsyncClient") CosmosAsyncClient client, @Qualifier("secondaryCosmosConfig") CosmosConfig cosmosConfig, MappingCosmosConverter mappingCosmosConverter) {
            return new CosmosTemplate(client, "test2_1", cosmosConfig, mappingCosmosConverter, cosmosAuditingHandler);
        }
    }
    @EnableReactiveCosmosRepositories(basePackages = "com.azure.cosmos.multidatasource.secondarydatasource.second", reactiveCosmosTemplateRef = "secondaryReactiveCosmosTemplate1")
    public class SecondaryDataSourceConfiguration1 {
        @Bean
        public CosmosTemplate secondaryReactiveCosmosTemplate1(@Qualifier("secondaryCosmosAsyncClient") CosmosAsyncClient client, @Qualifier("secondaryCosmosConfig") CosmosConfig cosmosConfig, MappingCosmosConverter mappingCosmosConverter) {
            return new CosmosTemplate(client, "test2_2", cosmosConfig, mappingCosmosConverter, cosmosAuditingHandler);
        }
    }

    private static class ResponseDiagnosticsProcessorImplementation implements ResponseDiagnosticsProcessor {

        @Override
        public void processResponseDiagnostics(@Nullable ResponseDiagnostics responseDiagnostics) {
            LOGGER.info("Response Diagnostics {}", responseDiagnostics);
        }
    }

    @Override
    protected String getDatabaseName() {
        return "test1_1";
    }
}
