// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.StoreResult;
import com.azure.cosmos.implementation.directconnectivity.TimeoutHelper;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public RetryContext retryContext;

    public DocumentServiceRequestContext() {
        retryContext = new RetryContext();
    }

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

    public void updateRetryContext(IRetryPolicy retryPolicy, boolean isGenericRetry) {
        if (isGenericRetry) {
            if (this.retryContext.directRetrySpecificStatusAndSubStatusCodes != null && this.retryContext.directRetrySpecificStatusAndSubStatusCodes.size() > 0) {
                for (int i = this.retryContext.directRetrySpecificStatusAndSubStatusCodes.size() - 1; i >= 0; i--) {
                    retryPolicy.incrementRetry();
                    retryPolicy.addStatusAndSubStatusCode(0, this.retryContext.directRetrySpecificStatusAndSubStatusCodes.get(i)[0],
                        this.retryContext.directRetrySpecificStatusAndSubStatusCodes.get(i)[1]);
                }
                this.retryContext.directRetrySpecificStatusAndSubStatusCodes.clear();
            }

            if (retryPolicy.getStatusAndSubStatusCodes() != null) {
                this.retryContext.genericRetrySpecificStatusAndSubStatusCodes = Collections.synchronizedList(new ArrayList<>(retryPolicy.getStatusAndSubStatusCodes()));
            } else {
                this.retryContext.genericRetrySpecificStatusAndSubStatusCodes = Collections.synchronizedList(new ArrayList<>());
            }
            this.retryContext.retryCount = retryPolicy.getRetryCount();
            this.retryContext.statusAndSubStatusCodes = retryPolicy.getStatusAndSubStatusCodes();
            if (this.retryContext.retryStartTime == null) {
                this.retryContext.retryStartTime = retryPolicy.getStartTime();
            }
            this.retryContext.retryEndTime = retryPolicy.getEndTime();
        } else {
            if (this.retryContext.genericRetrySpecificStatusAndSubStatusCodes != null && this.retryContext.genericRetrySpecificStatusAndSubStatusCodes.size() > 0) {
                for (int i = this.retryContext.genericRetrySpecificStatusAndSubStatusCodes.size() - 1; i >= 0; i--) {
                    retryPolicy.incrementRetry();
                    retryPolicy.addStatusAndSubStatusCode(0, this.retryContext.genericRetrySpecificStatusAndSubStatusCodes.get(i)[0],
                        this.retryContext.genericRetrySpecificStatusAndSubStatusCodes.get(i)[1]);
                }
                this.retryContext.genericRetrySpecificStatusAndSubStatusCodes.clear();
            }

            if (retryPolicy.getStatusAndSubStatusCodes() != null) {
                this.retryContext.directRetrySpecificStatusAndSubStatusCodes = Collections.synchronizedList(new ArrayList<>(retryPolicy.getStatusAndSubStatusCodes()));
            } else {
                this.retryContext.directRetrySpecificStatusAndSubStatusCodes = Collections.synchronizedList(new ArrayList<>());
            }
            this.retryContext.retryCount = retryPolicy.getRetryCount();
            this.retryContext.statusAndSubStatusCodes = retryPolicy.getStatusAndSubStatusCodes();
            if (this.retryContext.retryStartTime == null) {
                this.retryContext.retryStartTime = retryPolicy.getStartTime();
            }
            this.retryContext.retryEndTime = retryPolicy.getEndTime();
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
        context.retryContext = new RetryContext(this.retryContext);

        return context;
    }
}

