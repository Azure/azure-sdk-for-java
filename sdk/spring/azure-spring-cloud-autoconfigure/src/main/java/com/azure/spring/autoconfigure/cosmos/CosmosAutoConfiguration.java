// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.cosmos;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.autoconfigure.unity.AzureProperties;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.azure.spring.autoconfigure.unity.AzureProperties.AZURE_PROPERTY_BEAN_NAME;

/**
 * Auto Configure Cosmos properties and connection policy.
 */
@Configuration
@ConditionalOnClass({ CosmosAsyncClient.class, CosmosTemplate.class })
@ConditionalOnResource(resources = "classpath:cosmos.enable.config")
@EnableConfigurationProperties(CosmosProperties.class)
public class CosmosAutoConfiguration extends AbstractCosmosConfiguration {
    private final CosmosProperties cosmosProperties;
    private final AzureProperties azureProperties;


    public CosmosAutoConfiguration(CosmosProperties cosmosProperties,
                                   @Qualifier(AZURE_PROPERTY_BEAN_NAME) AzureProperties azureProperties) {
        this.cosmosProperties = cosmosProperties;
        this.azureProperties = azureProperties;
    }

    @Override
    protected String getDatabaseName() {
        return cosmosProperties.getDatabase();
    }

    @Bean
    public AzureKeyCredential azureKeyCredential() {
        return new AzureKeyCredential(cosmosProperties.getKey());
    }

    @Bean
    public CosmosClientBuilder cosmosClientBuilder(AzureKeyCredential azureKeyCredential) {
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder();
        cosmosClientBuilder.credential(azureKeyCredential)
                           .consistencyLevel(cosmosProperties.getConsistencyLevel())
                           .endpoint(cosmosProperties.getUri());
        if (ConnectionMode.GATEWAY == cosmosProperties.getConnectionMode()) {
            cosmosClientBuilder.gatewayMode();
        }
        return cosmosClientBuilder;
    }

    @Override
    public CosmosConfig cosmosConfig() {
        return CosmosConfig.builder()
                           .enableQueryMetrics(cosmosProperties.isPopulateQueryMetrics())
                           .responseDiagnosticsProcessor(cosmosProperties.getResponseDiagnosticsProcessor())
                           .build();
    }
}
