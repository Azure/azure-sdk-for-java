// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.multidatasource;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.data.cosmos.CosmosProperties;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 */
@Configuration
public class PrimaryDatasourceConfiguration {

    private static final String DATABASE1 = "primary_database1";
    private static final String DATABASE2 = "primary_database2";

    @Bean
    @ConfigurationProperties(prefix = "azure.cosmos.primary")
    public CosmosProperties primary() {
        return new CosmosProperties();
    }

    @Bean
    public CosmosClientBuilder primaryClientBuilder(@Qualifier("primary") CosmosProperties primaryProperties) {
        return new CosmosClientBuilder()
            .key(primaryProperties.getKey())
            .endpoint(primaryProperties.getUri());
    }

    @EnableReactiveCosmosRepositories(basePackages = "com.azure.cosmos.multidatasource.primary.database1")
    public class DataBase1Configuration extends AbstractCosmosConfiguration {

        @Override
        protected String getDatabaseName() {
            return DATABASE1;
        }
    }

    @EnableReactiveCosmosRepositories(basePackages = "com.azure.cosmos.multidatasource.primary.database2",
                                      reactiveCosmosTemplateRef = "primaryDatabase2Template")
    public class Database2Configuration {

        @Bean
        public ReactiveCosmosTemplate primaryDatabase2Template(CosmosAsyncClient cosmosAsyncClient,
                                                               CosmosConfig cosmosConfig,
                                                               MappingCosmosConverter mappingCosmosConverter) {
            return new ReactiveCosmosTemplate(cosmosAsyncClient, DATABASE2, cosmosConfig, mappingCosmosConverter);
        }
    }
}

