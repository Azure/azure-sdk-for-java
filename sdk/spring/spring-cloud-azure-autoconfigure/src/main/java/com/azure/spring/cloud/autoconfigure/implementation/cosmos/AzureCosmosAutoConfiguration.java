// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.cosmos;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosPropertiesConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Cosmos DB support.
 *
 * @since 4.0.0
 */
@Import({
    AzureCosmosPropertiesConfiguration.class,
    CosmosClientConfiguration.class
})
@ConditionalOnClass(CosmosClientBuilder.class)
public class AzureCosmosAutoConfiguration extends AzureServiceConfigurationBase {

    protected AzureCosmosAutoConfiguration(AzureGlobalProperties azureProperties) {
        super(azureProperties);
    }
}
