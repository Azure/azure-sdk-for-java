// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;
/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 */
import com.azure.data.cosmos.CosmosKeyCredential;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosDBConfig;
import com.azure.spring.data.cosmos.core.ResponseDiagnostics;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.Nullable;


@Configuration
@EnableConfigurationProperties(CosmosDbProperties.class)
@EnableReactiveCosmosRepositories
@PropertySource("classpath:application.properties")
public class UserRepositoryConfiguration extends AbstractCosmosConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(AppConfiguration.class);

    @Autowired
    private CosmosDbProperties properties;

    private CosmosKeyCredential cosmosKeyCredential;

    @Bean
    public CosmosDBConfig cosmosDbConfig() {
        this.cosmosKeyCredential = new CosmosKeyCredential(properties.getKey());
        CosmosDBConfig cosmosDBConfig = CosmosDBConfig.builder(properties.getUri(), cosmosKeyCredential,
            properties.getDatabase()).build();
        cosmosDBConfig.setPopulateQueryMetrics(properties.isPopulateQueryMetrics());
        cosmosDBConfig.setResponseDiagnosticsProcessor(new ResponseDiagnosticsProcessorImplementation());
        return cosmosDBConfig;
    }

    public void switchToSecondaryKey() {
        this.cosmosKeyCredential.key(properties.getSecondaryKey());
    }

    public void switchToPrimaryKey() {
        this.cosmosKeyCredential.key(properties.getKey());
    }

    public void switchKey(String key) {
        this.cosmosKeyCredential.key(key);
    }

    private static class ResponseDiagnosticsProcessorImplementation implements ResponseDiagnosticsProcessor {

        @Override
        public void processResponseDiagnostics(@Nullable ResponseDiagnostics responseDiagnostics) {
            logger.info("Response Diagnostics {}", responseDiagnostics);
        }
    }
}
