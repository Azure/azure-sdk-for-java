// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.FeedResponseDiagnostics;
import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * This class represents response diagnostic statistics associated with a request to Azure Cosmos DB
 */
public final class CosmosDiagnostics {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosDiagnostics.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ClientSideRequestStatistics clientSideRequestStatistics;
    private FeedResponseDiagnostics feedResponseDiagnostics;

    static final String USER_AGENT = Utils.getUserAgent();

    CosmosDiagnostics() {
        this.clientSideRequestStatistics = new ClientSideRequestStatistics();
    }

    CosmosDiagnostics(FeedResponseDiagnostics feedResponseDiagnostics) {
        this.feedResponseDiagnostics = feedResponseDiagnostics;
    }

    ClientSideRequestStatistics clientSideRequestStatistics() {
        return clientSideRequestStatistics;
    }

    CosmosDiagnostics clientSideRequestStatistics(ClientSideRequestStatistics clientSideRequestStatistics) {
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
        StringBuilder stringBuilder = new StringBuilder();
        if (this.feedResponseDiagnostics != null) {
            stringBuilder.append("userAgent=").append(USER_AGENT).append(System.lineSeparator());
            stringBuilder.append(feedResponseDiagnostics);
        } else {
            try {
                stringBuilder.append(OBJECT_MAPPER.writeValueAsString(this.clientSideRequestStatistics));
            } catch (JsonProcessingException e) {
                LOGGER.error("Error while parsing diagnostics " + e);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Retrieves duration related to the completion of the request.
     * This represents end to end duration of an operation including all the retries.
     * This is meant for point operation only, for query please use toString() to get full query diagnostics.
     *
     * @return request completion duration
     */
    public Duration getDuration() {
        if (this.feedResponseDiagnostics != null) {
            return null;
        }

        return this.clientSideRequestStatistics.getDuration();
    }

    FeedResponseDiagnostics getFeedResponseDiagnostics() {
        return feedResponseDiagnostics;
    }
}
