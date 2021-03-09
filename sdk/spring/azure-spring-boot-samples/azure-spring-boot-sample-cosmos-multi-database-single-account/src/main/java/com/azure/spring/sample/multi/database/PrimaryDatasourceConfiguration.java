// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.sample.multi.database;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PrimaryDatasourceConfiguration {

    private static final String PRIMARY_DATABASE1 = "primary_database1";
    private static final String PRIMARY_DATABASE2 = "primary_database2";

    @Bean
    public CosmosProperties primary() {
        return new CosmosProperties();
    }

    @Bean
    public CosmosClientBuilder primaryClientBuilder(CosmosProperties primaryProperties) {
        return new CosmosClientBuilder()
            .key(primaryProperties.getKey())
            .endpoint(primaryProperties.getUri());
    }

    @EnableReactiveCosmosRepositories(basePackages = "com.azure.spring.sample.multi.database.database1",
        reactiveCosmosTemplateRef = "primaryDatabase1Template")
    public class PrimaryDatabase1Configuration extends AbstractCosmosConfiguration {

        @Bean
        public ReactiveCosmosTemplate primaryDatabase1Template(CosmosAsyncClient cosmosAsyncClient,
                                                              CosmosConfig cosmosConfig,
                                                              MappingCosmosConverter mappingCosmosConverter) {
            return new ReactiveCosmosTemplate(cosmosAsyncClient, PRIMARY_DATABASE1, cosmosConfig, mappingCosmosConverter);
        }

        @Override
        protected String getDatabaseName() {
            return PRIMARY_DATABASE1;
        }
    }

    @EnableReactiveCosmosRepositories(basePackages = "com.azure.spring.sample.multi.database.database2",
        reactiveCosmosTemplateRef = "primaryDatabase2Template")
    public class PrimaryDatabase2Configuration extends AbstractCosmosConfiguration {

        @Bean
        public ReactiveCosmosTemplate primaryDatabase2Template(CosmosAsyncClient cosmosAsyncClient,
                                                              CosmosConfig cosmosConfig,
                                                              MappingCosmosConverter mappingCosmosConverter) {
            return new ReactiveCosmosTemplate(cosmosAsyncClient, PRIMARY_DATABASE2, cosmosConfig, mappingCosmosConverter);
        }

        @Override
        protected String getDatabaseName() {
            return PRIMARY_DATABASE2;
        }
    }
}
