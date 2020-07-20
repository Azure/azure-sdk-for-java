// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.multidatasource;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosProperties;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 */
@Configuration
@PropertySource("classpath:application.properties")
public class DatabaseConfiguration extends AbstractCosmosConfiguration {

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

    @EnableReactiveCosmosRepositories(basePackages = "com.azure.cosmos.multidatasource.primarydatasource")
    public class PrimaryDataSourceConfiguration {
        @Autowired
        @Qualifier("primaryDataSourceConfiguration")
        CosmosProperties properties;
        @Bean
        public CosmosConfig cosmosConfig() {
            CosmosConfig cosmosConfig = CosmosConfig.builder()
                .cosmosClientBuilder(new CosmosClientBuilder()
                    .key(properties.getKey()).endpoint(properties.getUri()))
                .database(properties.getDatabase())
                .enableQueryMetrics(properties.isQueryMetricsEnabled())
                .build();
            return cosmosConfig;
        }

    }

    @EnableReactiveCosmosRepositories(basePackages = "com.azure.cosmos.multidatasource.secondarydatasource", reactiveCosmosTemplateRef = "secondaryReactiveCosmosTemplate")
    public class SecondaryDataSourceConfiguration {
        @Autowired
        @Qualifier("secondaryDataSourceConfiguration")
        CosmosProperties properties;
        @Bean
        public ReactiveCosmosTemplate secondaryReactiveCosmosTemplate(MappingCosmosConverter mappingCosmosConverter) {
            CosmosConfig cosmosConfig = CosmosConfig.builder()
                .cosmosClientBuilder(new CosmosClientBuilder()
                    .key(properties.getKey()).endpoint(properties.getUri()))
                .database(properties.getDatabase())
                .enableQueryMetrics(properties.isQueryMetricsEnabled())
                .build();

            return new ReactiveCosmosTemplate(new CosmosFactory(cosmosConfig), mappingCosmosConverter, cosmosConfig.getDatabase());
        }
    }
}
