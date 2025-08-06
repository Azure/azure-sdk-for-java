// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.util.List;
import java.util.Objects;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Configuration class for CosmosDB client cache entries.
 */
public class CosmosClientCacheConfig {
    private final String endpoint;
    private final CosmosAuthConfig authConfig;
    private final String applicationName;
    private final boolean useGatewayMode;
    private final List<String> preferredRegions;
    private final String context;

    public CosmosClientCacheConfig(String endpoint,
                                CosmosAuthConfig authConfig,
                                String applicationName,
                                boolean useGatewayMode,
                                List<String> preferredRegions,
                                String context) {
        checkArgument(StringUtils.isNotEmpty(endpoint), "Argument 'endpoint' must not be empty");
        checkNotNull(authConfig,  "Argument 'authConfig' must not be null");

        this.endpoint = endpoint;
        this.authConfig = authConfig;
        this.applicationName = applicationName;
        this.useGatewayMode = useGatewayMode;
        this.preferredRegions = preferredRegions;
        this.context = context;
    }

    @Override
    public String toString() {
        return String.format("%s|%s|%s|%s|%s|%s",
            endpoint,
            authConfig,
            applicationName != null ? applicationName : "",
            useGatewayMode,
            preferredRegions != null ? String.join(",", preferredRegions) : "",
            context != null ? context : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CosmosClientCacheConfig that = (CosmosClientCacheConfig) o;
        return useGatewayMode == that.useGatewayMode
            && Objects.equals(endpoint, that.endpoint)
            && Objects.equals(authConfig, that.authConfig)
            && Objects.equals(applicationName, that.applicationName)
            && Objects.equals(preferredRegions, that.preferredRegions)
            && Objects.equals(context, that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoint, authConfig, applicationName, useGatewayMode,
            preferredRegions, context);
    }
}
