// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.data.cosmos;

import com.azure.spring.data.cosmos.config.CosmosConfig;

/**
 * Callback interface that can be implemented by beans wishing to customize the {@code CosmosConfigBuilder}
 * whilst retaining default auto-configuration.
 */
@FunctionalInterface
public interface CosmosConfigBuilderCustomizer {
    /**
     * Customize the {@code CosmosConfigBuilder}.
     * @param cosmosConfigBuilder The {@code CosmosConfigBuilder} to customize.
     */
    void customize(CosmosConfig.CosmosConfigBuilder cosmosConfigBuilder);
}
