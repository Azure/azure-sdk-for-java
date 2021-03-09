// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.sample.multi.database;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.core.ResponseDiagnostics;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.annotation.Persistent;
import org.springframework.lang.Nullable;

@Configuration
public class SecondaryDatasourceConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecondaryDatasourceConfiguration.class);
    public static final String DATABASE3 = "secondary_database";
    public static final String DATABASE4 = "secondary_database2";

    @Bean
    @Qualifier("secondary")
    @ConfigurationProperties(prefix = "azure.cosmos.secondary")
    public CosmosProperties secondary() {
        return new CosmosProperties();
    }

    @Bean("secondaryCosmosClient")
    public CosmosAsyncClient getCosmosAsyncClient(@Qualifier("secondary") CosmosProperties secondaryProperties) {
        return CosmosFactory.createCosmosAsyncClient(new CosmosClientBuilder()
            .key(secondaryProperties.getKey())
            .endpoint(secondaryProperties.getUri()));
    }

    @Bean("secondaryCosmosConfig")
    public CosmosConfig getCosmosConfig() {
        return CosmosConfig.builder()
            .enableQueryMetrics(true)
            .responseDiagnosticsProcessor(new ResponseDiagnosticsProcessorImplementation())
            .build();
    }

    @EnableCosmosRepositories(cosmosTemplateRef  = "secondaryDatabaseTemplate")
    public class DatabaseConfiguration {
        @Bean
        public CosmosTemplate secondaryDatabaseTemplate(@Qualifier("secondaryCosmosClient") CosmosAsyncClient client,
                                                         @Qualifier("secondaryCosmosConfig") CosmosConfig cosmosConfig,
                                                         MappingCosmosConverter mappingCosmosConverter) {
            CosmosFactory cosmosFactory = new CosmosFactory(client, DATABASE3);

            final CosmosMappingContext mappingContext = new CosmosMappingContext();
            final MappingCosmosConverter cosmosConverter = new MappingCosmosConverter(mappingContext,
                null);
            CosmosTemplate cosmosTemplate = new CosmosTemplate(cosmosFactory, cosmosConfig, cosmosConverter);
            return cosmosTemplate;
        }
    }

    @EnableCosmosRepositories(cosmosTemplateRef  = "secondaryDatabase2Template")
    public class Database4Configuration {
        @Bean
        public CosmosTemplate secondaryDatabase2Template(@Qualifier("secondaryCosmosClient") CosmosAsyncClient client,
                                                         @Qualifier("secondaryCosmosConfig") CosmosConfig cosmosConfig,
                                                         MappingCosmosConverter mappingCosmosConverter) {
            return new CosmosTemplate(client, DATABASE4, cosmosConfig, mappingCosmosConverter);
        }
    }

    private static class ResponseDiagnosticsProcessorImplementation implements ResponseDiagnosticsProcessor {

        @Override
        public void processResponseDiagnostics(@Nullable ResponseDiagnostics responseDiagnostics) {
            LOGGER.info("Response Diagnostics {}", responseDiagnostics);
        }
    }
}

