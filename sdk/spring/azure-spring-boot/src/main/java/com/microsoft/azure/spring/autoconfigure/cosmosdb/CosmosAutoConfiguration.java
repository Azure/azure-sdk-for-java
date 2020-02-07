/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.autoconfigure.cosmosdb;

import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.CosmosClient;
import com.microsoft.azure.spring.data.cosmosdb.config.AbstractCosmosConfiguration;
import com.microsoft.azure.spring.data.cosmosdb.config.CosmosDBConfig;
import com.microsoft.azure.spring.data.cosmosdb.core.CosmosTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({ CosmosClient.class, CosmosTemplate.class })
@ConditionalOnResource(resources = "classpath:cosmosdb.enable.config")
@EnableConfigurationProperties(CosmosDBProperties.class)
public class CosmosAutoConfiguration extends AbstractCosmosConfiguration {
    private final CosmosDBProperties properties;
    private final ConnectionPolicy policy;

    public CosmosAutoConfiguration(CosmosDBProperties properties,
                                   ObjectProvider<ConnectionPolicy> connectionPolicyObjectProvider) {
        this.properties = properties;
        this.policy = connectionPolicyObjectProvider.getIfAvailable();
        configConnectionPolicy(properties, policy);
    }

    @Bean
    public CosmosDBConfig cosmosDBConfig() {

        return CosmosDBConfig.builder(
                properties.getUri(), properties.getKey(), properties.getDatabase())
                             .consistencyLevel(properties.getConsistencyLevel())
                             .allowTelemetry(properties.isAllowTelemetry())
                             .connectionPolicy(properties.getConnectionPolicy())
                             .responseDiagnosticsProcessor(properties.getResponseDiagnosticsProcessor())
                             .populateQueryMetrics(properties.isPopulateQueryMetrics())
                             .build();
    }

    private void configConnectionPolicy(CosmosDBProperties properties, ConnectionPolicy connectionPolicy) {
        // This is a temp fix as CosmosDbFactory does not support loading ConnectionPolicy bean from context
        final ConnectionPolicy policy = connectionPolicy == null ? ConnectionPolicy.defaultPolicy() : connectionPolicy;

        properties.setConnectionPolicy(policy);
    }
}
