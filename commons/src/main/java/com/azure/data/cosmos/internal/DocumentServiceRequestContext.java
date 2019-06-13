/*
 *
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.ClientSideRequestStatistics;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.PartitionKeyRange;
import com.azure.data.cosmos.directconnectivity.StoreResponse;
import com.azure.data.cosmos.directconnectivity.StoreResult;
import com.azure.data.cosmos.directconnectivity.TimeoutHelper;
import com.azure.data.cosmos.internal.routing.PartitionKeyInternal;

import java.net.URL;
import java.util.List;

public class DocumentServiceRequestContext implements Cloneable{
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
    public volatile URL locationEndpointToRoute;
    public volatile boolean performedBackgroundAddressRefresh;
    public volatile boolean performLocalRefreshOnGoneException;
    public volatile List<String> storeResponses;
    public volatile StoreResult quorumSelectedStoreResponse;
    public volatile PartitionKeyInternal effectivePartitionKey;
    public volatile ClientSideRequestStatistics clientSideRequestStatistics;

    /**
     * Sets routing directive for GlobalEndpointManager to resolve the request
     *  to endpoint based on location index.
     *
     * @param locationIndex Index of the location to which the request should be routed.
     * @param usePreferredLocations Use preferred locations to route request.
     */
    public void RouteToLocation(int locationIndex, boolean usePreferredLocations) {
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
    public void RouteToLocation(URL locationEndpoint) {
        this.locationEndpointToRoute = locationEndpoint;
        this.locationIndexToRoute = null;
        this.usePreferredLocations = null;
    }

    /**
     * Clears location-based routing directive
     */
    public void ClearRouteToLocation() {
        this.locationIndexToRoute = null;
        this.locationEndpointToRoute = null;
        this.usePreferredLocations = null;
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
        context.clientSideRequestStatistics = this.clientSideRequestStatistics;

        return context;
    }
}

