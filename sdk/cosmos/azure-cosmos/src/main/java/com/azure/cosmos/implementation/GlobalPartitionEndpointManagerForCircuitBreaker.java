// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class GlobalPartitionEndpointManagerForCircuitBreaker implements IGlobalPartitionEndpointManager {

    private static final Logger logger = LoggerFactory.getLogger(GlobalPartitionEndpointManagerForCircuitBreaker.class);

    private final GlobalEndpointManager globalEndpointManager;
    private final ConcurrentHashMap<PartitionKeyRange, PartitionLevelFailoverInfo> partitionKeyRangeToFailoverInfo;

    public GlobalPartitionEndpointManagerForCircuitBreaker(GlobalEndpointManager globalEndpointManager) {
        this.partitionKeyRangeToFailoverInfo = new ConcurrentHashMap<>();
        this.globalEndpointManager = globalEndpointManager;
    }

    @Override
    public boolean tryMarkPartitionKeyRangeAsUnavailable(RxDocumentServiceRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("request cannot be null!");
        }

        if (request.requestContext == null) {

            if (logger.isDebugEnabled()) {
                logger.warn("requestContext is null!");
            }

            return false;
        }

        PartitionKeyRange partitionKeyRange = request.requestContext.resolvedPartitionKeyRange;
        URI failedLocation = request.requestContext.locationEndpointToRoute;

        if (partitionKeyRange == null) {
            return false;
        }

        AtomicBoolean isFailoverPossible = new AtomicBoolean(true);
        AtomicBoolean isFailureThresholdBreached = new AtomicBoolean(false);

        this.partitionKeyRangeToFailoverInfo.compute(partitionKeyRange, (partitionKeyRangeAsKey, partitionKeyRangeFailoverInfoAsVal) -> {

            if (partitionKeyRangeFailoverInfoAsVal == null) {
                partitionKeyRangeFailoverInfoAsVal = new PartitionLevelFailoverInfo();
            }

            isFailureThresholdBreached.set(partitionKeyRangeFailoverInfoAsVal.isFailureThresholdBreachedForLocation(failedLocation, request.isReadOnlyRequest()));

            if (isFailureThresholdBreached.get()) {

                UnmodifiableList<URI> applicableEndpoints = request.isReadOnly() ?
                    this.globalEndpointManager.getApplicableReadEndpoints(request.requestContext.getExcludeRegions()) :
                    this.globalEndpointManager.getApplicableWriteEndpoints(request.requestContext.getExcludeRegions());

                isFailoverPossible.set(partitionKeyRangeFailoverInfoAsVal
                    .tryMoveNextLocation(applicableEndpoints, failedLocation, request.isReadOnlyRequest()));
            }

            return partitionKeyRangeFailoverInfoAsVal;
        });

        // set to true if and only if failure threshold exceeded for the region
        // and if failover is possible
        // a failover is only possible when there are available regions left to failover to
        if (isFailoverPossible.get()) {
            return true;
        }

        this.partitionKeyRangeToFailoverInfo.remove(partitionKeyRange);
        return false;
    }

    @Override
    public boolean tryMarkPartitionKeyRangeAsAvailable(RxDocumentServiceRequest request) {
        return false;
    }

    @Override
    public boolean tryAddPartitionKeyRangeLevelOverride(RxDocumentServiceRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("request cannot be null!");
        }

        if (request.requestContext == null) {

            if (logger.isDebugEnabled()) {
                logger.warn("requestContext is null!");
            }

            return false;
        }

        PartitionKeyRange partitionKeyRange = request.requestContext.resolvedPartitionKeyRange;

        if (partitionKeyRange == null) {
            return false;
        }

        if (this.partitionKeyRangeToFailoverInfo.containsKey(partitionKeyRange)) {

            // is it possible for this instance to go stale?
            PartitionLevelFailoverInfo partitionLevelFailoverInfo = this.partitionKeyRangeToFailoverInfo.get(partitionKeyRange);

            // it could be possible that this currentLocationSnapshot is stale since the ConcurrentHashMap.get
            // thread won over ConcurrentHashMap.compute (can mark a location as failed), in that case, the request
            // could hit possible unavailability issues again
            URI currentLocationSnapshot = partitionLevelFailoverInfo.current;

            if (logger.isDebugEnabled()) {
                logger.debug("Moving request to location : {}", currentLocationSnapshot.getPath());
            }

            request.requestContext.routeToLocation(currentLocationSnapshot);

            return true;
        }

        return false;
    }

    // what is the point of an inner class?
    // at a high-level, the below class needs:
    //  1. consecutive failure count tracker
    //  2. unavailable since
    //  3. regions unavailable in
    //  4. failure type
    static class PartitionLevelFailoverInfo {

        private final ConcurrentHashMap<URI, PartitionLevelFailureMetadata> partitionLevelFailureMetadata;
        private final Set<FailedLocation> failedLocations = ConcurrentHashMap.newKeySet();
        // points to the current location a request will be routed to
        private URI current;

        PartitionLevelFailoverInfo() {
            this.partitionLevelFailureMetadata = new ConcurrentHashMap<>();
        }

        // bookmark failure
        // method purpose:
        // 1. increment consecutive failure count
        // 2. if failure count crosses threshold for
        public boolean isFailureThresholdBreachedForLocation(URI failedLocation, boolean isReadRequest) {

            AtomicBoolean isFailureThresholdBreached = new AtomicBoolean(false);

            this.partitionLevelFailureMetadata.compute(failedLocation, (locationAsKey, partitionLevelFailureMetadataAsVal) -> {

                if (partitionLevelFailureMetadataAsVal == null) {
                    partitionLevelFailureMetadataAsVal = new PartitionLevelFailureMetadata();
                }

                // todo : make threshold for marking a location as failed more comprehensive

                if (isReadRequest) {
                    if (partitionLevelFailureMetadataAsVal.consecutiveFailureCountForReads.incrementAndGet() > 5) {
                        partitionLevelFailureMetadataAsVal.unavailableForReadsSince.set(Instant.now());
                        this.current = failedLocation;
                        isFailureThresholdBreached.set(true);
                    }
                } else {
                    if (partitionLevelFailureMetadataAsVal.consecutiveFailureCountForWrites.incrementAndGet() > 5) {
                        partitionLevelFailureMetadataAsVal.unavailableForWritesSince.set(Instant.now());
                        this.current = failedLocation;
                        isFailureThresholdBreached.set(true);
                    }
                }

                return partitionLevelFailureMetadataAsVal;
            });

            return isFailureThresholdBreached.get();
        }

        // bookmark success
        public void bookmarkSuccess(URI succeededLocation) {
            this.partitionLevelFailureMetadata.compute(succeededLocation, (locationAsKey, partitionLevelFailureMetadataAsVal) -> {

                if (partitionLevelFailureMetadataAsVal == null) {
                    return new PartitionLevelFailureMetadata();
                }

                if (partitionLevelFailureMetadataAsVal.consecutiveFailureCountForReads.get() > 1) {
                    partitionLevelFailureMetadataAsVal.consecutiveFailureCountForReads.decrementAndGet();
                }

                return partitionLevelFailureMetadataAsVal;
            });
        }

        // method purpose - choose the next possible region for this partition
        // 1. if current == failedLocation - try using a different location
        //      a) iterate through the list of read / write locations
        //      b) if location in iteration loop not part of failedLocations, then assign location to current
        // 2. if current != failedLocation - a different thread has updated it
        // 3.
        public boolean tryMoveNextLocation(List<URI> locations, URI failedLocation, boolean isReadRequest) {
            for (URI location : locations) {
                if (failedLocation == this.current) {
                    continue;
                }

                // failedLocation != current
                if (!this.failedLocations.contains(failedLocation)) {

                    this.failedLocations.add(new FailedLocation(failedLocation, isReadRequest));
                    this.current = location;

                    return true;
                }
            }

            return false;
        }

        public boolean tryMarkLocationAsAvailable(URI previouslyFailedLocation) {
            return false;
        }
    }

    static class PartitionLevelFailureMetadata {
        private final AtomicInteger consecutiveFailureCountForWrites = new AtomicInteger();
        private final AtomicInteger consecutiveFailureCountForReads = new AtomicInteger();
        private final AtomicReference<Instant> unavailableForWritesSince = new AtomicReference<>(Instant.MIN);
        private final AtomicReference<Instant> unavailableForReadsSince = new AtomicReference<>(Instant.MIN);
    }

    static class FailedLocation {
        private final URI location;
        private final boolean isRead;

        FailedLocation(URI location, boolean isRead) {
            this.location = location;
            this.isRead = isRead;
        }
    }
}
