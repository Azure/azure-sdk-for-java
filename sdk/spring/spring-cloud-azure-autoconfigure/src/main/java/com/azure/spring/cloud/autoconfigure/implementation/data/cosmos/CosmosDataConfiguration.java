// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.data.cosmos;

import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosProperties;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

@ConditionalOnClass(CosmosTemplate.class)
@ConditionalOnBean(AzureCosmosProperties.class)
public class CosmosDataConfiguration extends AbstractCosmosConfiguration {

    private final AzureCosmosProperties cosmosProperties;
    private final ResponseDiagnosticsProcessor responseDiagnosticsProcessor;

    CosmosDataConfiguration(AzureCosmosProperties cosmosProperties,
                            @Autowired(required = false) ResponseDiagnosticsProcessor responseDiagnosticsProcessor) {
        this.cosmosProperties = cosmosProperties;
        this.responseDiagnosticsProcessor = responseDiagnosticsProcessor;
    }

    @Override
    protected String getDatabaseName() {
        return cosmosProperties.getDatabase();
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
