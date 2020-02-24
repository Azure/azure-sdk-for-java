// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import java.time.Duration;

/**
 * This class represents response diagnostic statistics associated with a request to Azure Cosmos DB
 */
public class CosmosResponseDiagnostics {

    private ClientSideRequestStatistics clientSideRequestStatistics;

    CosmosResponseDiagnostics() {
        this.clientSideRequestStatistics = new ClientSideRequestStatistics();
    }

    ClientSideRequestStatistics clientSideRequestStatistics() {
        return clientSideRequestStatistics;
    }

    CosmosResponseDiagnostics clientSideRequestStatistics(ClientSideRequestStatistics clientSideRequestStatistics) {
        this.clientSideRequestStatistics = clientSideRequestStatistics;
        return this;
    }

    /**
     * Retrieves Response Diagnostic String
     * @return Response Diagnostic String
     */
    @Override
    public String toString() {
        return this.clientSideRequestStatistics.toString();
    }

    /**
     * Retrieves latency related to the completion of the request
     * @return request completion latency
     */
    public Duration requestLatency() {
        return this.clientSideRequestStatistics.getRequestLatency();
    }
}
