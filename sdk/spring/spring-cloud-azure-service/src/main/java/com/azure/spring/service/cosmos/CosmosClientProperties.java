// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.cosmos;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.models.CosmosPermissionProperties;
import com.azure.spring.core.aware.RetryAware;
import com.azure.spring.core.aware.authentication.KeyAware;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.retry.RetryProperties;

import java.util.List;

/**
 * Configuration properties for Cosmos database, consistency, telemetry, connection, query metrics and diagnostics.
 */
public interface CosmosClientProperties extends AzureProperties, KeyAware {

    String getEndpoint();

    String getResourceToken();

    Boolean getClientTelemetryEnabled();

    Boolean getEndpointDiscoveryEnabled();

    Boolean getConnectionSharingAcrossClientsEnabled();

    Boolean getContentResponseOnWriteEnabled();

    Boolean getMultipleWriteRegionsEnabled();

    Boolean getSessionCapturingOverrideEnabled();

    Boolean getReadRequestsFallbackEnabled();

    List<CosmosPermissionProperties> getPermissions();

    List<String> getPreferredRegions();

    GatewayConnectionConfig getGatewayConnection();

    DirectConnectionConfig getDirectConnection();

    ConsistencyLevel getConsistencyLevel();

    String getDatabase();

    ConnectionMode getConnectionMode();

    ThrottlingRetryOptions getThrottlingRetryOptions();

    @Override
    default RetryAware.Retry getRetry() {
        return new RetryProperties();
    }
}
