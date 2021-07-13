// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.FeedResponseDiagnostics;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class represents response diagnostic statistics associated with a request to Azure Cosmos DB
 */
public final class CosmosDiagnostics {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosDiagnostics.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String COSMOS_DIAGNOSTICS_KEY = "cosmosDiagnostics";

    private ClientSideRequestStatistics clientSideRequestStatistics;
    private FeedResponseDiagnostics feedResponseDiagnostics;
    private AtomicBoolean diagnosticsCapturedInPagedFlux = new AtomicBoolean(false);

    static final String USER_AGENT = Utils.getUserAgent();
    static final String USER_AGENT_KEY = "userAgent";

    CosmosDiagnostics(DiagnosticsClientContext diagnosticsClientContext) {
        this.clientSideRequestStatistics = new ClientSideRequestStatistics(diagnosticsClientContext);
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
        fillCosmosDiagnostics(null, stringBuilder);
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

    /**
     * Regions contacted for this request
     *
     * @return set of regions contacted for this request
     */
    @Beta(value = Beta.SinceVersion.V4_9_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Set<URI> getRegionsContacted() {
        if (this.feedResponseDiagnostics != null) {
            return null;
        }
        return this.clientSideRequestStatistics.getRegionsContacted();
    }

    FeedResponseDiagnostics getFeedResponseDiagnostics() {
        return feedResponseDiagnostics;
    }

    void fillCosmosDiagnostics(ObjectNode parentNode, StringBuilder stringBuilder) {
        if (this.feedResponseDiagnostics != null) {
            if (parentNode != null) {
                parentNode.put(USER_AGENT_KEY, USER_AGENT);
                parentNode.putPOJO(COSMOS_DIAGNOSTICS_KEY, feedResponseDiagnostics);
            }

            if (stringBuilder != null) {
                stringBuilder.append(USER_AGENT_KEY + "=").append(USER_AGENT).append(System.lineSeparator());
                stringBuilder.append(feedResponseDiagnostics);
            }
        } else {
            if (parentNode != null) {
                parentNode.putPOJO(COSMOS_DIAGNOSTICS_KEY, clientSideRequestStatistics);
            }

            if (stringBuilder != null) {
                try {
                    stringBuilder.append(OBJECT_MAPPER.writeValueAsString(this.clientSideRequestStatistics));
                } catch (JsonProcessingException e) {
                    LOGGER.error("Error while parsing diagnostics ", e);
                }
            }
        }
    }

    void setFeedResponseDiagnostics(FeedResponseDiagnostics feedResponseDiagnostics) {
        this.feedResponseDiagnostics = feedResponseDiagnostics;
    }

    private AtomicBoolean isDiagnosticsCapturedInPagedFlux(){
        return this.diagnosticsCapturedInPagedFlux;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////

    static {
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.setCosmosDiagnosticsAccessor(
            new ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor() {
                @Override
                public FeedResponseDiagnostics getFeedResponseDiagnostics(CosmosDiagnostics cosmosDiagnostics) {
                    if (cosmosDiagnostics != null) {
                        return cosmosDiagnostics.getFeedResponseDiagnostics();
                    }

                    return null;
                }

                @Override
                public AtomicBoolean isDiagnosticsCapturedInPagedFlux(CosmosDiagnostics cosmosDiagnostics) {
                    return cosmosDiagnostics.isDiagnosticsCapturedInPagedFlux();
                }
            });
    }
}
