// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.multidatasource;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.CosmosProperties;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.ResponseDiagnostics;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 */
@Configuration
public class SecondaryDatasourceConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecondaryDatasourceConfiguration.class);
    public static final String DATABASE3 = "secondary_database3";
    public static final String DATABASE4 = "secondary_database4";

    @Bean
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

    @EnableCosmosRepositories(basePackages = "com.azure.cosmos.multidatasource.secondary.database3",
                              cosmosTemplateRef  = "secondaryDatabase3Template")
    public class Database3Configuration {
        @Bean
        public CosmosTemplate secondaryDatabase3Template(@Qualifier("secondaryCosmosClient") CosmosAsyncClient client,
                                                         @Qualifier("secondaryCosmosConfig") CosmosConfig cosmosConfig,
                                                         MappingCosmosConverter mappingCosmosConverter) {
            return new CosmosTemplate(client, DATABASE3, cosmosConfig, mappingCosmosConverter);
        }
    }
    @EnableCosmosRepositories(basePackages = "com.azure.cosmos.multidatasource.secondary.database4",
                              cosmosTemplateRef  = "secondaryDatabase4Template")
    public class Database4Configuration {
        @Bean
        public CosmosTemplate secondaryDatabase4Template(@Qualifier("secondaryCosmosClient") CosmosAsyncClient client,
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

