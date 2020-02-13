// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.ReplicationPolicy;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

/**
 * This class will read the service configuration from the gateway.
 *
 * As .Net does code sharing between the SDK and GW there are two implementation to IServiceConfigurationReader
 * GatewayServiceConfigurationReader which is for SDK
 * DatabaseAccountConfigurationReader which is for GW
 * Some of the APIs are not relevant in SDK and due to that in .Net the SDK implementation one throws not-implemented.
 *
 * In java, as we don't do code sharing
 * and we got rid of the interface which is not needed and only implemented the methods in GatewayServiceConfigurationReader
 */
public class GatewayServiceConfigurationReader {

    private URI serviceEndpoint;
    private GlobalEndpointManager globalEndpointManager;

    public GatewayServiceConfigurationReader(URI serviceEndpoint, GlobalEndpointManager globalEndpointManager) {
        this.serviceEndpoint = serviceEndpoint;
        this.globalEndpointManager = globalEndpointManager;
        this.globalEndpointManager.getDatabaseAccountFromCache(this.serviceEndpoint).block();
    }

    public Mono<ReplicationPolicy> getUserReplicationPolicy() {
        return this.globalEndpointManager.getDatabaseAccountFromCache(this.serviceEndpoint).map(databaseAccount -> BridgeInternal.getReplicationPolicy(databaseAccount));
    }

    public Mono<ReplicationPolicy> getSystemReplicationPolicy() {
        return this.globalEndpointManager.getDatabaseAccountFromCache(this.serviceEndpoint).map(databaseAccount -> BridgeInternal.getSystemReplicationPolicy(databaseAccount));
    }

    public Mono<ConsistencyLevel> getDefaultConsistencyLevel() {
        return this.globalEndpointManager.getDatabaseAccountFromCache(this.serviceEndpoint).map(databaseAccount -> BridgeInternal.getConsistencyPolicy(databaseAccount).getDefaultConsistencyLevel());
    }

    public Mono<Map<String, Object>> getQueryEngineConfiguration() {
        return this.globalEndpointManager.getDatabaseAccountFromCache(this.serviceEndpoint).map(databaseAccount -> BridgeInternal.getQueryEngineConfiuration(databaseAccount));
    }
}
