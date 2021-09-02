// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.cosmos;

import com.azure.spring.cloud.autoconfigure.cosmos.AzureCosmosClientAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.cosmos.AzureCosmosProperties;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Auto Configure Cosmos properties and connection policy.
 */
@Configuration
@AutoConfigureAfter(AzureCosmosClientAutoConfiguration.class)
@ConditionalOnClass({ CosmosTemplate.class })
@ConditionalOnResource(resources = "classpath:cosmos.enable.config")
@EnableConfigurationProperties(AzureCosmosProperties.class)
public class CosmosDataAutoConfiguration extends AbstractCosmosConfiguration {

    private final AzureCosmosProperties cosmosProperties;

    public CosmosDataAutoConfiguration(AzureCosmosProperties cosmosProperties) {
        this.cosmosProperties = cosmosProperties;
    }

    @Override
    protected String getDatabaseName() {
        return cosmosProperties.getDatabase();
    }

    @Override
    public CosmosConfig cosmosConfig() {
        return CosmosConfig.builder()
                           .enableQueryMetrics(cosmosProperties.isPopulateQueryMetrics())
                           .responseDiagnosticsProcessor(cosmosProperties.getResponseDiagnosticsProcessor())
                           .build();
    }
}
