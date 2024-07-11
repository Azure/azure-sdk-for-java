// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.cosmos;

import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosPropertiesWithConnectionDetailsClassWithConnectionDetailsBeanConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosPropertiesWithConnectionDetailsClassWithoutConnectionDetailsBeanConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosPropertiesWithoutConnectionDetailsClassConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Cosmos DB support.
 *
 * @since 4.0.0
 */
@Import({
    AzureCosmosPropertiesWithConnectionDetailsClassWithConnectionDetailsBeanConfiguration.class,
    AzureCosmosPropertiesWithConnectionDetailsClassWithoutConnectionDetailsBeanConfiguration.class,
    AzureCosmosPropertiesWithoutConnectionDetailsClassConfiguration.class,
    CosmosClientConfiguration.class
})
@EnableConfigurationProperties
public class AzureCosmosAutoConfiguration {

}
