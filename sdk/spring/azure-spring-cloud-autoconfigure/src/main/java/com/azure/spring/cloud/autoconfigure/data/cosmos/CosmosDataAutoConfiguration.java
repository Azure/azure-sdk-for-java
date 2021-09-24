// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.data.cosmos;

import com.azure.spring.cloud.autoconfigure.cosmos.AzureCosmosProperties;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;

/**
 * Auto Configure Cosmos properties and connection policy.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ CosmosTemplate.class })
@ConditionalOnExpression("${spring.cloud.azure.cosmos.enabled:true} "
                             + "and !T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.cosmos.uri:}') "
                             + "and !T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.cosmos.database:}')")
public class CosmosDataAutoConfiguration extends AbstractCosmosConfiguration {

    private final AzureCosmosProperties cosmosProperties;

    @Autowired(required = false)
    private ResponseDiagnosticsProcessor responseDiagnosticsProcessor;

    public CosmosDataAutoConfiguration(AzureCosmosProperties cosmosProperties) {
        this.cosmosProperties = cosmosProperties;
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
