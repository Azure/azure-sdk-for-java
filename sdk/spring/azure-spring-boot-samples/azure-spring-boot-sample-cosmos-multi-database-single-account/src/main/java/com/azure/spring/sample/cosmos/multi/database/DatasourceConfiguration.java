// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.sample.cosmos.multi.database;

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
public class DatasourceConfiguration {

    private static final String DATABASE1 = "database1";
    private static final String DATABASE2 = "database2";

    @Bean
    public CosmosProperties cosmosProperties() {
        return new CosmosProperties();
    }

    @Bean
    public CosmosClientBuilder primaryClientBuilder(CosmosProperties cosmosProperties) {
        return new CosmosClientBuilder()
            .key(cosmosProperties.getKey())
            .endpoint(cosmosProperties.getUri());
    }

    @EnableReactiveCosmosRepositories(basePackages = "com.azure.spring.sample.cosmos.multi.database.repository1",
        reactiveCosmosTemplateRef = "database1Template")
    public class Database1Configuration extends AbstractCosmosConfiguration {

        @Bean
        public ReactiveCosmosTemplate database1Template(CosmosAsyncClient cosmosAsyncClient,
                                                              CosmosConfig cosmosConfig,
                                                              MappingCosmosConverter mappingCosmosConverter) {
            return new ReactiveCosmosTemplate(cosmosAsyncClient, DATABASE1, cosmosConfig, mappingCosmosConverter);
        }

        @Override
        protected String getDatabaseName() {
            return DATABASE1;
        }
    }

    @EnableReactiveCosmosRepositories(basePackages = "com.azure.spring.sample.cosmos.multi.database.repository2",
        reactiveCosmosTemplateRef = "database2Template")
    public class Database2Configuration {

        @Bean
        public ReactiveCosmosTemplate database2Template(CosmosAsyncClient cosmosAsyncClient,
                                                              CosmosConfig cosmosConfig,
                                                              MappingCosmosConverter mappingCosmosConverter) {
            return new ReactiveCosmosTemplate(cosmosAsyncClient, DATABASE2, cosmosConfig, mappingCosmosConverter);
        }

    }
}
