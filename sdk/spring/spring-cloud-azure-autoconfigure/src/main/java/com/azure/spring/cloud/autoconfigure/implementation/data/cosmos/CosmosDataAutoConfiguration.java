// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.data.cosmos;

import com.azure.spring.cloud.autoconfigure.implementation.cosmos.AzureCosmosConnectionDetails;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosProperties;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Data Cosmos support.
 *
 * @since 4.0.0
 */
@Configuration(proxyBeanMethods = false)
@Conditional(CosmosDataAutoConfigurationCondition.class)
@Import(CosmosDataDiagnosticsConfiguration.class)
public class CosmosDataAutoConfiguration extends AbstractCosmosConfiguration {

    private final AzureCosmosProperties cosmosProperties;
    private final AzureCosmosConnectionDetails connectionDetails;
    private final ResponseDiagnosticsProcessor responseDiagnosticsProcessor;

    CosmosDataAutoConfiguration(AzureCosmosProperties cosmosProperties,
                                AzureCosmosConnectionDetails connectionDetails, // This bean is provided in AzureCosmosAutoConfiguration. When CosmosDataAutoConfigurationCondition matches, AzureCosmosAutoConfigurationCondition must matches.
                                @Autowired(required = false) ResponseDiagnosticsProcessor responseDiagnosticsProcessor) {
        this.cosmosProperties = cosmosProperties;
        this.connectionDetails = connectionDetails;
        this.responseDiagnosticsProcessor = responseDiagnosticsProcessor;
    }

    @Override
    protected String getDatabaseName() {
        return this.connectionDetails.getDatabase();
    }

    @Override
    public CosmosConfig cosmosConfig() {
        final CosmosConfig.CosmosConfigBuilder builder = CosmosConfig.builder();
        builder.enableQueryMetrics(cosmosProperties.isPopulateQueryMetrics());

        if (responseDiagnosticsProcessor != null) {
            builder.responseDiagnosticsProcessor(responseDiagnosticsProcessor);
        }

        return builder.build();
    }

}
