// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * This class represents response diagnostic statistics associated with a request to Azure Cosmos DB
 */
public final class CosmosResponseDiagnostics {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosResponseDiagnostics.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
     *
     * @return Response Diagnostic String
     */
    @Override
    public String toString() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this.clientSideRequestStatistics);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error while parsing diagnostics " + e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Retrieves latency related to the completion of the request
     *
     * @return request completion latency
     */
    public Duration getRequestLatency() {
        return this.clientSideRequestStatistics.getRequestLatency();
    }
}
