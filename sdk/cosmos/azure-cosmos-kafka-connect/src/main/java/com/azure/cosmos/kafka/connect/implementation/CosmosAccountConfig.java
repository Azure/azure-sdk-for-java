// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class CosmosAccountConfig {
    private final String endpoint;
    private final CosmosAuthConfig cosmosAuthConfig;
    private final String applicationName;
    private final boolean useGatewayMode;
    private final List<String> preferredRegionsList;

    public CosmosAccountConfig(
        String endpoint,
        CosmosAuthConfig cosmosAuthConfig,
        String applicationName,
        boolean useGatewayMode,
        List<String> preferredRegionsList) {

        checkArgument(StringUtils.isNotEmpty(endpoint), "Argument 'endpoint' should not be null");
        checkNotNull(cosmosAuthConfig, "Argument 'cosmosAuthConfig' should not be null");

        this.endpoint = endpoint;
        this.cosmosAuthConfig = cosmosAuthConfig;
        this.applicationName = applicationName;
        this.useGatewayMode = useGatewayMode;
        this.preferredRegionsList = preferredRegionsList;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public CosmosAuthConfig getCosmosAuthConfig() {
        return cosmosAuthConfig;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public boolean isUseGatewayMode() {
        return useGatewayMode;
    }

    public List<String> getPreferredRegionsList() {
        return preferredRegionsList;
    }
}
