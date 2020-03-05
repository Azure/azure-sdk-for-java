// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.ReplicationPolicy;

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

    private GlobalEndpointManager globalEndpointManager;

    public GatewayServiceConfigurationReader(GlobalEndpointManager globalEndpointManager) {
        this.globalEndpointManager = globalEndpointManager;
    }

    public ReplicationPolicy getUserReplicationPolicy() {
        return BridgeInternal.getReplicationPolicy(this.globalEndpointManager.getLatestDatabaseAccount());
    }

    public ReplicationPolicy getSystemReplicationPolicy() {
        return BridgeInternal.getSystemReplicationPolicy(this.globalEndpointManager.getLatestDatabaseAccount());
    }

    public ConsistencyLevel getDefaultConsistencyLevel() {
        return BridgeInternal.getConsistencyPolicy(this.globalEndpointManager.getLatestDatabaseAccount()).getDefaultConsistencyLevel();
    }

    public Map<String, Object> getQueryEngineConfiguration() {
        return BridgeInternal.getQueryEngineConfiuration(this.globalEndpointManager.getLatestDatabaseAccount());
    }
}
