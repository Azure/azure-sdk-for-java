// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.sample.multi.database;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.autoconfigure.cosmos.CosmosProperties;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class PrimaryDatasourceConfiguration {

    private static final String DATABASE1 = "primary_database1";

    @Bean
    @Primary
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

    @EnableReactiveCosmosRepositories(reactiveCosmosTemplateRef = "primaryDatabaseTemplate")
    public class Database2Configuration {

        @Bean
        public ReactiveCosmosTemplate primaryDatabaseTemplate(CosmosAsyncClient cosmosAsyncClient,
                                                               CosmosConfig cosmosConfig,
                                                               MappingCosmosConverter mappingCosmosConverter) {
            return new ReactiveCosmosTemplate(cosmosAsyncClient, DATABASE1, cosmosConfig, mappingCosmosConverter);
        }

    }
}
