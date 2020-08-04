// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;
/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 */

import com.azure.core.credential.AzureKeyCredential;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
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
import org.springframework.lang.Nullable;

@Configuration
@EnableConfigurationProperties(CosmosProperties.class)
@EnableReactiveCosmosRepositories
@PropertySource("classpath:application.properties")
public class UserRepositoryConfiguration extends AbstractCosmosConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryConfiguration.class);

    @Autowired
    private CosmosProperties properties;

    private AzureKeyCredential azureKeyCredential;

    @Bean
    public CosmosClientBuilder cosmosClientBuilder() {
        this.azureKeyCredential = new AzureKeyCredential(properties.getKey());
        return new CosmosClientBuilder().credential(azureKeyCredential);
    }

    @Bean
    public CosmosConfig cosmosConfig() {
        return CosmosConfig.builder()
                           .responseDiagnosticsProcessor(new ResponseDiagnosticsProcessorImplementation())
                           .enableQueryMetrics(properties.isQueryMetricsEnabled())
                           .build();
    }

    public void switchToSecondaryKey() {
        this.azureKeyCredential.update(properties.getSecondaryKey());
    }

    public void switchToPrimaryKey() {
        this.azureKeyCredential.update(properties.getKey());
    }

    public void switchKey(String key) {
        this.azureKeyCredential.update(key);
    }

    @Override
    protected String getDatabaseName() {
        return "testdb";
    }

    private static class ResponseDiagnosticsProcessorImplementation implements ResponseDiagnosticsProcessor {

        @Override
        public void processResponseDiagnostics(@Nullable ResponseDiagnostics responseDiagnostics) {
            logger.info("Response Diagnostics {}", responseDiagnostics);
        }
    }
}
