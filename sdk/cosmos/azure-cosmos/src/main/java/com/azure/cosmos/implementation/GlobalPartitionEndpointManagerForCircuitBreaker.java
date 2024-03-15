// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class GlobalPartitionEndpointManagerForCircuitBreaker implements IGlobalPartitionEndpointManager {

    private static final Logger logger = LoggerFactory.getLogger(GlobalPartitionEndpointManagerForCircuitBreaker.class);

    private final ConcurrentHashMap<PartitionKeyRange, PartitionLevelFailoverInfoForCircuitBreaker> partitionKeyRangeToFailoverInfo;

    public GlobalPartitionEndpointManagerForCircuitBreaker() {
        this.partitionKeyRangeToFailoverInfo = new ConcurrentHashMap<>();
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

        PartitionLevelFailoverInfoForCircuitBreaker partitionLevelFailoverInfo = this.partitionKeyRangeToFailoverInfo.compute(partitionKeyRange, (partitionKeyRangeAsKey, partitionKeyRangeFailoverInfoAsVal) -> {

            if (partitionKeyRangeFailoverInfoAsVal == null) {
                partitionKeyRangeFailoverInfoAsVal = new PartitionLevelFailoverInfoForCircuitBreaker();
            }

            return partitionKeyRangeFailoverInfoAsVal;
        });

        if (partitionLevelFailoverInfo.tryMoveNextLocation(new HashSet<>(), failedLocation)) {
            return true;
        }

        this.partitionKeyRangeToFailoverInfo.remove(partitionKeyRange);
        return false;
    }

    @Override
    public boolean tryMarkPartitionKeyRangeAsAvailable(RxDocumentServiceRequest request) {
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

        PartitionLevelFailoverInfoForCircuitBreaker partitionLevelFailoverInfo = this.partitionKeyRangeToFailoverInfo.compute(partitionKeyRange, (partitionKeyRangeAsKey, partitionKeyRangeFailoverInfoAsVal) -> {

            if (partitionKeyRangeFailoverInfoAsVal == null) {
                partitionKeyRangeFailoverInfoAsVal = new PartitionLevelFailoverInfoForCircuitBreaker();
            }

            return partitionKeyRangeFailoverInfoAsVal;
        });

        partitionLevelFailoverInfo.bookmarkFailure(failedLocation);

        if (partitionLevelFailoverInfo.tryMoveNextLocation(new HashSet<>(), failedLocation)) {
            return true;
        }

        this.partitionKeyRangeToFailoverInfo.remove(partitionKeyRange);
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
            PartitionLevelFailoverInfoForCircuitBreaker partitionLevelFailoverInfoForCircuitBreaker = this.partitionKeyRangeToFailoverInfo.get(partitionKeyRange);

            URI current = partitionLevelFailoverInfoForCircuitBreaker.current;

            if (logger.isDebugEnabled()) {
                logger.debug("Moving request to location : {}", current.getPath());
            }

            request.requestContext.routeToLocation(current);

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
    static class PartitionLevelFailoverInfoForCircuitBreaker {

        private final ConcurrentHashMap<URI, PartitionLevelFailureMetadata> partitionLevelFailureMetadata;
        private final Set<URI> failedLocations = ConcurrentHashMap.newKeySet();
        private final Object failedRegionLock = new Object();
        // points to the current location a request will be routed to
        private URI current;

        PartitionLevelFailoverInfoForCircuitBreaker() {
            this.partitionLevelFailureMetadata = new ConcurrentHashMap<>();
        }

        // bookmark failure
        public void bookmarkFailure(URI failedLocation) {
            this.partitionLevelFailureMetadata.compute(failedLocation, (locationAsKey, partitionLevelFailureMetadataAsVal) -> {

                if (partitionLevelFailureMetadataAsVal == null) {
                    partitionLevelFailureMetadataAsVal = new PartitionLevelFailureMetadata();
                }

                // todo : make threshold for marking a location as failed more comprehensive
                if (partitionLevelFailureMetadataAsVal.consecutiveFailureCount.incrementAndGet() > 5) {
                    partitionLevelFailureMetadataAsVal.unavailableSince.set(Instant.now());
                    this.failedLocations.add(failedLocation);
                }

                return partitionLevelFailureMetadataAsVal;
            });
        }

        // bookmark success
        public void bookmarkSuccess(URI succeededLocation) {
            this.partitionLevelFailureMetadata.compute(succeededLocation, (locationAsKey, partitionLevelFailureMetadataAsVal) -> {

                if (partitionLevelFailureMetadataAsVal == null) {
                    return new PartitionLevelFailureMetadata();
                }

                if (partitionLevelFailureMetadataAsVal.consecutiveFailureCount.get() > 1) {
                    partitionLevelFailureMetadataAsVal.consecutiveFailureCount.decrementAndGet();
                }

                return partitionLevelFailureMetadataAsVal;
            });
        }

        // method purpose - choose the next possible region for this partition
        public boolean tryMoveNextLocation(Set<URI> locations, URI failedLocation) {

            if (partitionLevelFailureMetadata.get().consecutiveFailureCount.incrementAndGet() < 5) {
                return false;
            }

            if (failedLocation != this.current) {
                // a different thread has moved it to the next location
                return true;
            }

            synchronized (failedRegionLock) {

                if (failedLocation != this.current) {
                    // a different thread has moved it to the next location
                    return true;
                }

                for (URI location : locations) {

                    if (this.current == location) {
                        continue;
                    }

                    if (this.failedLocations.contains(location)) {
                        continue;
                    }

                    this.failedLocations.add(failedLocation);
                    this.current = location;
                    return true;
                }
            }

            return false;
        }

        public boolean tryMarkLocationAsAvailable(URI previouslyFailedLocation) {

        }
    }

    static class PartitionLevelFailureMetadata {
        private final AtomicInteger consecutiveFailureCount = new AtomicInteger();
        private final AtomicReference<Instant> unavailableSince = new AtomicReference<>(Instant.now());
    }
}
