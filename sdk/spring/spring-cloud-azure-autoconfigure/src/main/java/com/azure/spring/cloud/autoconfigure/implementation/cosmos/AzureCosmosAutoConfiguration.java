// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.cosmos;

import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosPropertiesConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
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
public class AzureCosmosAutoConfiguration {

}
