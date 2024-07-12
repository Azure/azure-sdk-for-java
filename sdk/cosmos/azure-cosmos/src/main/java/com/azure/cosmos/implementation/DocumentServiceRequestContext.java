// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.StoreResult;
import com.azure.cosmos.implementation.directconnectivity.TimeoutHelper;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.guava25.collect.ImmutableSet;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class DocumentServiceRequestContext implements Cloneable {
    public volatile boolean forceAddressRefresh;
    public volatile boolean forceRefreshAddressCache;
    public volatile RequestChargeTracker requestChargeTracker;
    public volatile TimeoutHelper timeoutHelper;
    public volatile String resolvedCollectionRid;
    public volatile ISessionToken sessionToken;
    public volatile long quorumSelectedLSN;
    public volatile long globalCommittedSelectedLSN;
    public volatile StoreResponse globalStrongWriteResponse;
    public volatile ConsistencyLevel originalRequestConsistencyLevel;
    public volatile PartitionKeyRange resolvedPartitionKeyRange;
    public volatile Integer regionIndex;
    public volatile Boolean usePreferredLocations;
    public volatile Integer locationIndexToRoute;
    public volatile URI locationEndpointToRoute;
    public volatile boolean performedBackgroundAddressRefresh;
    public volatile boolean performLocalRefreshOnGoneException;
    public volatile List<String> storeResponses;
    public volatile StoreResult quorumSelectedStoreResponse;
    public volatile PartitionKeyInternal effectivePartitionKey;
    public volatile CosmosDiagnostics cosmosDiagnostics;
    public volatile String resourcePhysicalAddress;
    public volatile String throughputControlCycleId;
    public volatile boolean replicaAddressValidationEnabled = Configs.isReplicaAddressValidationEnabled();
    private final Set<Uri> failedEndpoints = ConcurrentHashMap.newKeySet();
    private CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig;
    private AtomicBoolean isRequestCancelledOnTimeout = null;
    private volatile List<String> excludeRegions;
    private volatile Set<String> keywordIdentifiers;
    private volatile long approximateBloomFilterInsertionCount;
    private final Set<String> sessionTokenEvaluationResults = ConcurrentHashMap.newKeySet();

    // For cancelled rntbd requests, track the response as OperationCancelledException which later will be used to populate the cosmosDiagnostics
    public final Map<String, CosmosException> rntbdCancelledRequestMap = new ConcurrentHashMap<>();

    public DocumentServiceRequestContext() {}

    /**
     * Sets routing directive for GlobalEndpointManager to resolve the request
     *  to endpoint based on location index.
     *
     * @param locationIndex Index of the location to which the request should be routed.
     * @param usePreferredLocations Use preferred locations to route request.
     */
    public void routeToLocation(int locationIndex, boolean usePreferredLocations) {
        this.locationIndexToRoute = locationIndex;
        this.usePreferredLocations = usePreferredLocations;
        this.locationEndpointToRoute = null;
    }

    /**
     * Sets location-based routing directive for GlobalEndpointManager to resolve
     * the request to given locationEndpoint.
     *
     * @param locationEndpoint Location endpoint to which the request should be routed.
     */
    public void routeToLocation(URI locationEndpoint) {
        this.locationEndpointToRoute = locationEndpoint;
        this.locationIndexToRoute = null;
        this.usePreferredLocations = null;
    }

    /**
     * Clears location-based routing directive
     */
    public void clearRouteToLocation() {
        this.locationIndexToRoute = null;
        this.locationEndpointToRoute = null;
        this.usePreferredLocations = null;
    }

    public Set<Uri> getFailedEndpoints() {
        return this.failedEndpoints;
    }

    public void addToFailedEndpoints(Exception exception, Uri address) {

        if (exception instanceof CosmosException) {
            CosmosException cosmosException = (CosmosException) exception;

            // Tracking the failed endpoints, so during retry, we can prioritize other replicas (replicas have not been tried on)
            // If the exception eventually cause a forceRefresh gateway addresses, during that time, we are going to officially mark
            // the replica as unhealthy.
            // We started by only track 410 exceptions, but can add other exceptions based on the feedback and observations
            if (Exceptions.isGone(cosmosException)) {
                this.failedEndpoints.add(address);
            }
        }
    }

    @Override
    public DocumentServiceRequestContext clone() {
        DocumentServiceRequestContext context = new DocumentServiceRequestContext();
        context.forceAddressRefresh = this.forceAddressRefresh;
        context.forceRefreshAddressCache = this.forceRefreshAddressCache;
        context.requestChargeTracker = this.requestChargeTracker;
        context.timeoutHelper = this.timeoutHelper;
        context.resolvedCollectionRid = this.resolvedCollectionRid;
        context.sessionToken = this.sessionToken;
        context.quorumSelectedLSN = this.quorumSelectedLSN;
        context.globalCommittedSelectedLSN = this.globalCommittedSelectedLSN;
        context.globalStrongWriteResponse = this.globalStrongWriteResponse;
        context.originalRequestConsistencyLevel = this.originalRequestConsistencyLevel;
        context.resolvedPartitionKeyRange = this.resolvedPartitionKeyRange;
        context.regionIndex = this.regionIndex;
        context.usePreferredLocations = this.usePreferredLocations;
        context.locationIndexToRoute = this.locationIndexToRoute;
        context.locationEndpointToRoute = this.locationEndpointToRoute;
        context.performLocalRefreshOnGoneException = this.performLocalRefreshOnGoneException;
        context.effectivePartitionKey = this.effectivePartitionKey;
        context.performedBackgroundAddressRefresh = this.performedBackgroundAddressRefresh;
        context.cosmosDiagnostics = this.cosmosDiagnostics;
        context.resourcePhysicalAddress = this.resourcePhysicalAddress;
        context.throughputControlCycleId = this.throughputControlCycleId;
        context.replicaAddressValidationEnabled = this.replicaAddressValidationEnabled;
        context.endToEndOperationLatencyPolicyConfig = this.endToEndOperationLatencyPolicyConfig;
        return context;
    }

    public CosmosEndToEndOperationLatencyPolicyConfig getEndToEndOperationLatencyPolicyConfig() {
        return endToEndOperationLatencyPolicyConfig;
    }

    public void setEndToEndOperationLatencyPolicyConfig(CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig) {
        this.endToEndOperationLatencyPolicyConfig = endToEndOperationLatencyPolicyConfig;
    }

    public void setIsRequestCancelledOnTimeout(AtomicBoolean isRequestCancelledOnTimeout) {
        this.isRequestCancelledOnTimeout = isRequestCancelledOnTimeout;
    }

    public AtomicBoolean isRequestCancelledOnTimeout() {
        return this.isRequestCancelledOnTimeout;
    }

    public List<String> getExcludeRegions() {
        return this.excludeRegions;
    }

    public void setExcludeRegions(List<String> excludeRegions) {
        this.excludeRegions = excludeRegions;
    }

    public void setKeywordIdentifiers(Set<String> keywordIdentifiers) {
        this.keywordIdentifiers = keywordIdentifiers;
    }

    public Set<String> getKeywordIdentifiers() {
        return keywordIdentifiers;
    }

    public long getApproximateBloomFilterInsertionCount() {
        return approximateBloomFilterInsertionCount;
    }

    public void setApproximateBloomFilterInsertionCount(long approximateBloomFilterInsertionCount) {
        this.approximateBloomFilterInsertionCount = approximateBloomFilterInsertionCount;
    }

    public Set<String> getSessionTokenEvaluationResults() {
        return sessionTokenEvaluationResults;
    }
}

