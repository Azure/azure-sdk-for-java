// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.data.cosmos;

import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosPropertiesConfiguration;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Data Cosmos support.
 *
 * @since 4.0.0
 */
@Import({AzureCosmosPropertiesConfiguration.class,
    CosmosDataDiagnosticsConfiguration.class,
    CosmosDataConfiguration.class})
@ConditionalOnClass(AbstractCosmosConfiguration.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure.cosmos", name = "database")
public class CosmosDataAutoConfiguration {

}
