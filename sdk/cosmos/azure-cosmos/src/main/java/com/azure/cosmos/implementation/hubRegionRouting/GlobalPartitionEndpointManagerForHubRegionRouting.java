// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.hubRegionRouting;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.PartitionKeyRangeWrapper;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages per-partition hub region URI caching for single-master accounts.
 *
 * When a partition encounters repeated 404/1002 (ReadSessionNotAvailable), the SDK
 * discovers the hub region through a 403/3 discovery chain and caches the hub URI.
 * Future requests for the same partition route directly to the cached hub (warm path).
 *
 * Feature-flag gated via COSMOS.HUB_REGION_PROCESSING_ENABLED (default: false).
 */
public class GlobalPartitionEndpointManagerForHubRegionRouting {

    private static final Logger logger = LoggerFactory.getLogger(GlobalPartitionEndpointManagerForHubRegionRouting.class);

    private final ConcurrentHashMap<PartitionKeyRangeWrapper, RegionalRoutingContext> partitionKeyRangeToHubRegionRoutingContext;
    private final GlobalEndpointManager globalEndpointManager;
    private final AtomicBoolean isHubRegionProcessingEnabled;

    public GlobalPartitionEndpointManagerForHubRegionRouting(
        GlobalEndpointManager globalEndpointManager) {

        this.globalEndpointManager = globalEndpointManager;
        this.partitionKeyRangeToHubRegionRoutingContext = new ConcurrentHashMap<>();
        this.isHubRegionProcessingEnabled = new AtomicBoolean(Configs.isHubRegionProcessingEnabled());
    }

    /**
     * Check whether hub region routing is active (feature flag enabled + single master account).
     */
    public boolean isHubRegionRoutingActive(RxDocumentServiceRequest request) {
        if (!this.isHubRegionProcessingEnabled.get()) {
            return false;
        }

        if (request == null) {
            return false;
        }

        // Hub region routing only applies to single-master accounts
        if (this.globalEndpointManager.canUseMultipleWriteLocations(request)) {
            return false;
        }

        return isDocumentRequest(request);
    }

    /**
     * Try to route the request to a previously cached hub region for this partition.
     * Returns true if a cached hub was found and routing was overridden.
     */
    public boolean tryRouteToCachedHubRegion(RxDocumentServiceRequest request) {
        if (!isHubRegionRoutingActive(request)) {
            return false;
        }

        PartitionKeyRangeWrapper wrapper = getPartitionKeyRangeWrapper(request);
        if (wrapper == null) {
            return false;
        }

        RegionalRoutingContext cachedHubContext = this.partitionKeyRangeToHubRegionRoutingContext.get(wrapper);
        if (cachedHubContext != null) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "Routing request to cached hub region {} for partition {}",
                    cachedHubContext.getGatewayRegionalEndpoint(),
                    wrapper.getPartitionKeyRange().getId());
            }
            request.requestContext.routeToLocation(cachedHubContext);
            return true;
        }

        return false;
    }

    /**
     * Cache the hub region URI for a partition after a successful hub region discovery.
     */
    public void cacheHubRegionForPartition(RxDocumentServiceRequest request) {
        if (!isHubRegionRoutingActive(request)) {
            return;
        }

        PartitionKeyRangeWrapper wrapper = getPartitionKeyRangeWrapper(request);
        if (wrapper == null) {
            return;
        }

        RegionalRoutingContext routingContext = request.requestContext.regionalRoutingContextToRoute;
        if (routingContext == null) {
            return;
        }

        RegionalRoutingContext existing = this.partitionKeyRangeToHubRegionRoutingContext.putIfAbsent(wrapper, routingContext);
        if (existing == null) {
            logger.info(
                "Cached hub region {} for partition {} in collection {}",
                routingContext.getGatewayRegionalEndpoint(),
                wrapper.getPartitionKeyRange().getId(),
                wrapper.getCollectionResourceId());
        }
    }

    /**
     * Invalidate the hub region cache entry for a partition.
     * Called when the cached hub region becomes unavailable.
     */
    public void invalidateCachedHubRegion(RxDocumentServiceRequest request) {
        if (request == null) {
            return;
        }

        PartitionKeyRangeWrapper wrapper = getPartitionKeyRangeWrapper(request);
        if (wrapper == null) {
            return;
        }

        RegionalRoutingContext removed = this.partitionKeyRangeToHubRegionRoutingContext.remove(wrapper);
        if (removed != null) {
            logger.info(
                "Invalidated cached hub region for partition {} in collection {}",
                wrapper.getPartitionKeyRange().getId(),
                wrapper.getCollectionResourceId());
        }
    }

    /**
     * Check if there is a cached hub region for the given partition.
     */
    public boolean hasCachedHubRegion(RxDocumentServiceRequest request) {
        if (!isHubRegionRoutingActive(request)) {
            return false;
        }

        PartitionKeyRangeWrapper wrapper = getPartitionKeyRangeWrapper(request);
        if (wrapper == null) {
            return false;
        }

        return this.partitionKeyRangeToHubRegionRoutingContext.containsKey(wrapper);
    }

    /**
     * Clear all cached hub regions. Called on account topology changes.
     */
    public void clear() {
        this.partitionKeyRangeToHubRegionRoutingContext.clear();
    }

    /**
     * Refresh the feature flag state (for testing).
     */
    public void resetHubRegionProcessingEnabled(boolean enabled) {
        this.isHubRegionProcessingEnabled.set(enabled);
        if (!enabled) {
            this.clear();
        }
    }

    public boolean isHubRegionProcessingEnabled() {
        return this.isHubRegionProcessingEnabled.get();
    }

    int getCacheSize() {
        return this.partitionKeyRangeToHubRegionRoutingContext.size();
    }

    private PartitionKeyRangeWrapper getPartitionKeyRangeWrapper(RxDocumentServiceRequest request) {
        if (request.requestContext == null) {
            return null;
        }

        PartitionKeyRange partitionKeyRange = request.requestContext.resolvedPartitionKeyRange;
        String resolvedCollectionRid = request.requestContext.resolvedCollectionRid;

        if (partitionKeyRange == null || StringUtils.isEmpty(resolvedCollectionRid)) {
            return null;
        }

        return new PartitionKeyRangeWrapper(partitionKeyRange, resolvedCollectionRid);
    }

    private boolean isDocumentRequest(RxDocumentServiceRequest request) {
        if (request.getResourceType() != ResourceType.Document) {
            return false;
        }

        if (request.getOperationType() == OperationType.QueryPlan) {
            return false;
        }

        return true;
    }
}
