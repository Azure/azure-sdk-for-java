// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.FeedResponseDiagnostics;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * This class represents response diagnostic statistics associated with a request to Azure Cosmos DB
 */
public final class CosmosDiagnostics {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosDiagnostics.class);
    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String COSMOS_DIAGNOSTICS_KEY = "cosmosDiagnostics";

    private ClientSideRequestStatistics clientSideRequestStatistics;
    private FeedResponseDiagnostics feedResponseDiagnostics;

    private CosmosDiagnosticsContext diagnosticsContext;

    private double samplingRateSnapshot;
    private final AtomicBoolean diagnosticsCapturedInPagedFlux;
    static final String USER_AGENT_KEY = "userAgent";
    static final String SAMPLING_RATE_SNAPSHOT_KEY = "samplingRateSnapshot";

    CosmosDiagnostics(DiagnosticsClientContext diagnosticsClientContext) {
        this.diagnosticsCapturedInPagedFlux = new AtomicBoolean(false);
        this.clientSideRequestStatistics = new ClientSideRequestStatistics(diagnosticsClientContext);
        this.samplingRateSnapshot = 1;
    }

    CosmosDiagnostics(FeedResponseDiagnostics feedResponseDiagnostics) {
        this.diagnosticsCapturedInPagedFlux = new AtomicBoolean(false);
        this.feedResponseDiagnostics = feedResponseDiagnostics;
        this.samplingRateSnapshot = 1;
    }

    CosmosDiagnostics(CosmosDiagnostics toBeCloned) {
        if (toBeCloned.feedResponseDiagnostics != null) {
            this.feedResponseDiagnostics = new FeedResponseDiagnostics(toBeCloned.feedResponseDiagnostics);
        }

        if (toBeCloned.clientSideRequestStatistics != null) {
            this.clientSideRequestStatistics = new ClientSideRequestStatistics(toBeCloned.clientSideRequestStatistics);
        }

        this.diagnosticsCapturedInPagedFlux = new AtomicBoolean(toBeCloned.diagnosticsCapturedInPagedFlux.get());
        this.samplingRateSnapshot = toBeCloned.samplingRateSnapshot;
    }

    ClientSideRequestStatistics clientSideRequestStatistics() {
        return clientSideRequestStatistics;
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
     * Returns the associated CosmosDiagnosticsContext or null if not associated with any context yet.
     * @return the associated CosmosDiagnosticsContext or null if not associated with any context yet.
     */
    @JsonIgnore
    public CosmosDiagnosticsContext getDiagnosticsContext() {
        return this.diagnosticsContext;
    }

    void setDiagnosticsContext(CosmosDiagnosticsContext ctx) {
        checkNotNull(ctx, "Argument 'ctx' must not be null.");
        this.diagnosticsContext = ctx;
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

            Collection<ClientSideRequestStatistics> statistics =
                this.feedResponseDiagnostics.getClientSideRequestStatistics();
            if (statistics == null) {
                return Duration.ZERO;
            }

            Instant min = Instant.MAX;
            Instant max = Instant.MIN;
            for (ClientSideRequestStatistics s: statistics) {
                if (s.getRequestStartTimeUTC() != null &&
                    s.getRequestStartTimeUTC().isBefore(min)) {
                    min = s.getRequestStartTimeUTC();
                }

                if (s.getRequestEndTimeUTC() != null &&
                    s.getRequestEndTimeUTC().isAfter(max)) {
                    max = s.getRequestEndTimeUTC();
                }
            }

            if (max.isBefore(min)) {
                return null;
            }

            if (min == max) {
                return Duration.ZERO;
            }

            return Duration.between(min, max);
        }

        return this.clientSideRequestStatistics.getDuration();
    }

    /**
     * Regions contacted for this request
     *
     * @return set of regions contacted for this request
     */
    @Beta(value = Beta.SinceVersion.V4_9_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    @Deprecated
    public Set<URI> getRegionsContacted() {
        if (this.feedResponseDiagnostics != null) {
            return null;
        }
        return this.clientSideRequestStatistics.getLocationEndpointsContacted();
    }

    /**
     * Regions contacted for this request
     *
     * @return set of regions contacted for this request
     */
    public Set<String> getContactedRegionNames() {
        if (this.feedResponseDiagnostics != null) {
            Set<String> aggregatedRegionsContacted = Collections.synchronizedSet(new HashSet<>());

            if (this.clientSideRequestStatistics != null) {
                Set<String> temp = this.clientSideRequestStatistics.getContactedRegionNames();
                if (temp != null && temp.size() > 0) {
                    aggregatedRegionsContacted.addAll(temp);
                }
            }

            Collection<ClientSideRequestStatistics> clientStatisticCollection =
                this.feedResponseDiagnostics.getClientSideRequestStatistics();
            if (clientStatisticCollection != null) {
                for (ClientSideRequestStatistics clientStatistics : clientStatisticCollection) {
                    Set<String> temp = clientStatistics.getContactedRegionNames();
                    if (temp != null && temp.size() > 0) {
                        aggregatedRegionsContacted.addAll(temp);
                    }
                }
            }

            return aggregatedRegionsContacted;
        }
        return this.clientSideRequestStatistics.getContactedRegionNames();
    }

    /**
     * Gets the UserAgent header value used by the client issueing this operation
     * @return the UserAgent header value used for the client that issued this operation
     */
    public String getUserAgent() {
        if (this.feedResponseDiagnostics != null) {
            return this.feedResponseDiagnostics.getUserAgent();
        }

        return this.clientSideRequestStatistics.getUserAgent();
    }

    FeedResponseDiagnostics getFeedResponseDiagnostics() {
        return feedResponseDiagnostics;
    }

    /**
     * Retrieves payload size of the request in bytes
     * This is meant for point operation only, for query and feed operations the request payload is always 0.
     *
     * @return request payload size in bytes
     */
    int getRequestPayloadSizeInBytes() {
        if (this.feedResponseDiagnostics != null) {
            return 0;
        }

        return this.clientSideRequestStatistics.getRequestPayloadSizeInBytes();
    }

    /**
     * Retrieves payload size of the response in bytes
     *
     * @return response payload size in bytes
     */
    int getTotalResponsePayloadSizeInBytes() {
        if (this.feedResponseDiagnostics != null) {
            int totalResponsePayloadSizeInBytes = 0;

            Collection<ClientSideRequestStatistics> clientStatisticCollection =
                this.feedResponseDiagnostics.getClientSideRequestStatistics();
            if (clientStatisticCollection != null) {
                for (ClientSideRequestStatistics clientStatistics : clientStatisticCollection) {
                    totalResponsePayloadSizeInBytes += clientStatistics.getMaxResponsePayloadSizeInBytes();
                }
            }

            return totalResponsePayloadSizeInBytes;
        }

        return this.clientSideRequestStatistics.getMaxResponsePayloadSizeInBytes();
    }

    ClientSideRequestStatistics getClientSideRequestStatisticsRaw() {
        return this.clientSideRequestStatistics;
    }

    Collection<ClientSideRequestStatistics> getClientSideRequestStatistics() {
        if (this.feedResponseDiagnostics != null) {
            return this.feedResponseDiagnostics.getClientSideRequestStatistics();
        }

        return ImmutableList.of(this.clientSideRequestStatistics);
    }

    Collection<ClientSideRequestStatistics> getClientSideRequestStatisticsForQueryPipelineAggregations() {
        //Used only during aggregations like Aggregate/Orderby/Groupby which may contain clientSideStats in
        //feedResponseDiagnostics. So we need to add from both the places
        List<ClientSideRequestStatistics> combinedStatistics = new ArrayList<>();

        combinedStatistics
            .addAll(this.feedResponseDiagnostics.getClientSideRequestStatistics());
        if (this.clientSideRequestStatistics != null) {
            combinedStatistics.add(this.clientSideRequestStatistics);
        }

        return combinedStatistics;
    }

    double getSamplingRateSnapshot() {
        return this.samplingRateSnapshot;
    }

    void fillCosmosDiagnostics(ObjectNode parentNode, StringBuilder stringBuilder) {
        if (this.feedResponseDiagnostics != null) {
            feedResponseDiagnostics.setSamplingRateSnapshot(this.samplingRateSnapshot);
            if (parentNode != null) {
                parentNode.put(USER_AGENT_KEY, this.feedResponseDiagnostics.getUserAgent());
                parentNode.putPOJO(COSMOS_DIAGNOSTICS_KEY, feedResponseDiagnostics);
            }

            if (stringBuilder != null) {
                stringBuilder.append(feedResponseDiagnostics);
            }
        } else {
            clientSideRequestStatistics.setSamplingRateSnapshot(this.samplingRateSnapshot);
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

    void addClientSideDiagnosticsToFeed(Collection<ClientSideRequestStatistics> requestStatistics) {
        if (this.feedResponseDiagnostics == null || requestStatistics == null || requestStatistics.isEmpty()) {
            return;
        }

        this.feedResponseDiagnostics
            .addClientSideRequestStatistics(requestStatistics);
    }

    CosmosDiagnostics setSamplingRateSnapshot(double samplingRate) {
        this.samplingRateSnapshot = samplingRate;
        return this;
    }

    String getFirstContactedRegion() {
        return this.clientSideRequestStatistics.getFirstContactedRegion();
    }

    URI getFirstContactedLocationEndpoint() {
        return this.clientSideRequestStatistics.getFirstContactedLocationEndpoint();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.setCosmosDiagnosticsAccessor(
            new ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor() {
                @Override
                public FeedResponseDiagnostics getFeedResponseDiagnostics(CosmosDiagnostics cosmosDiagnostics) {
                    if (cosmosDiagnostics == null) {
                        return null;
                    }

                    return cosmosDiagnostics.getFeedResponseDiagnostics();
                }

                @Override
                public AtomicBoolean isDiagnosticsCapturedInPagedFlux(CosmosDiagnostics cosmosDiagnostics) {
                    if (cosmosDiagnostics == null) {
                        return null;
                    }

                    return cosmosDiagnostics.isDiagnosticsCapturedInPagedFlux();
                }

                @Override
                public Collection<ClientSideRequestStatistics> getClientSideRequestStatistics(CosmosDiagnostics cosmosDiagnostics) {
                    if (cosmosDiagnostics == null) {
                        return null;
                    }

                    return cosmosDiagnostics.getClientSideRequestStatistics();
                }

                @Override
                public Collection<ClientSideRequestStatistics> getClientSideRequestStatisticsForQueryPipelineAggregations(CosmosDiagnostics cosmosDiagnostics) {
                    if (cosmosDiagnostics == null) {
                        return new ArrayList<>();
                    }

                    return cosmosDiagnostics.getClientSideRequestStatisticsForQueryPipelineAggregations();
                }

                @Override
                public ClientSideRequestStatistics getClientSideRequestStatisticsRaw(CosmosDiagnostics cosmosDiagnostics) {
                    if (cosmosDiagnostics == null) {
                        return null;
                    }

                    return cosmosDiagnostics.getClientSideRequestStatisticsRaw();
                }

                @Override
                public int getTotalResponsePayloadSizeInBytes(CosmosDiagnostics cosmosDiagnostics) {
                    if (cosmosDiagnostics == null) {
                        return 0;
                    }

                    return cosmosDiagnostics.getTotalResponsePayloadSizeInBytes();
                }

                @Override
                public int getRequestPayloadSizeInBytes(CosmosDiagnostics cosmosDiagnostics) {
                    if (cosmosDiagnostics == null) {
                        return 0;
                    }

                    return cosmosDiagnostics.getRequestPayloadSizeInBytes();
                }

                @Override
                public void addClientSideDiagnosticsToFeed(CosmosDiagnostics cosmosDiagnostics,
                                                           Collection<ClientSideRequestStatistics> requestStatistics) {
                    if (cosmosDiagnostics == null) {
                        return;
                    }

                    cosmosDiagnostics
                        .addClientSideDiagnosticsToFeed(requestStatistics);
                }

                @Override
                public void setSamplingRateSnapshot(CosmosDiagnostics cosmosDiagnostics, double samplingRate) {
                    if (cosmosDiagnostics == null) {
                        return;
                    }

                    cosmosDiagnostics.setSamplingRateSnapshot(samplingRate);
                }

                @Override
                public CosmosDiagnostics create(DiagnosticsClientContext clientContext, double samplingRate) {
                    return new CosmosDiagnostics(clientContext).setSamplingRateSnapshot(samplingRate);
                }

                @Override
                public void recordAddressResolutionEnd(
                    RxDocumentServiceRequest request,
                    String identifier,
                    String errorMessage,
                    long transportRequestId) {
                    if (request.requestContext.cosmosDiagnostics == null) {
                        return;
                    }

                    request
                        .requestContext.cosmosDiagnostics
                        .clientSideRequestStatistics
                        .recordAddressResolutionEnd(
                            identifier,
                            errorMessage,
                            request.faultInjectionRequestContext.getFaultInjectionRuleId(transportRequestId),
                            request.faultInjectionRequestContext.getFaultInjectionRuleEvaluationResults(transportRequestId));
                }

                @Override
                public boolean isNotEmpty(CosmosDiagnostics cosmosDiagnostics) {
                    if (cosmosDiagnostics == null) {
                        return false;
                    }

                    if (cosmosDiagnostics.feedResponseDiagnostics != null) {
                        return true;
                    }

                    if (!cosmosDiagnostics.clientSideRequestStatistics.getResponseStatisticsList().isEmpty() ||
                        !cosmosDiagnostics.clientSideRequestStatistics.getAddressResolutionStatistics().isEmpty() ||
                        !cosmosDiagnostics.clientSideRequestStatistics.getGatewayStatisticsList().isEmpty()) {

                        return true;
                    }

                    return false;
                }

                @Override
                public void setDiagnosticsContext(CosmosDiagnostics cosmosDiagnostics, CosmosDiagnosticsContext ctx) {
                    if (cosmosDiagnostics == null) {
                        return;
                    }

                    cosmosDiagnostics.setDiagnosticsContext(ctx);
                }

                @Override
                public URI getFirstContactedLocationEndpoint(CosmosDiagnostics cosmosDiagnostics) {

                    if (cosmosDiagnostics == null) {
                        return null;
                    }

                    return cosmosDiagnostics.getFirstContactedLocationEndpoint();
                }

                @Override
                public void mergeMetadataDiagnosticContext(CosmosDiagnostics cosmosDiagnostics, MetadataDiagnosticsContext otherMetadataDiagnosticsContext) {

                    if (cosmosDiagnostics == null) {
                        return;
                    }

                    ClientSideRequestStatistics clientSideRequestStatistics = cosmosDiagnostics.clientSideRequestStatistics;

                    if (clientSideRequestStatistics != null) {
                        clientSideRequestStatistics.mergeMetadataDiagnosticsContext(otherMetadataDiagnosticsContext);
                    }
                }

                @Override
                public void mergeSerializationDiagnosticContext(CosmosDiagnostics cosmosDiagnostics, SerializationDiagnosticsContext otherSerializationDiagnosticsContext) {
                    if (cosmosDiagnostics == null) {
                        return;
                    }

                    ClientSideRequestStatistics clientSideRequestStatistics = cosmosDiagnostics.clientSideRequestStatistics;

                    if (clientSideRequestStatistics != null) {
                        clientSideRequestStatistics.mergeSerializationDiagnosticsContext(otherSerializationDiagnosticsContext);
                    }
                }
            });
    }

    static { initialize(); }
}
