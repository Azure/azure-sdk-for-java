// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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

                isFailoverPossible.set(partitionKeyRangeFailoverInfoAsVal.areLocationsAvailableForPartitionKeyRange(
                    this,
                    applicableEndpoints,
                    request.isReadOnlyRequest()));
            }

            return partitionKeyRangeFailoverInfoAsVal;
        });

        // set to true if and only if failure threshold exceeded for the region
        // and if failover is possible
        // a failover is only possible when there are available regions left to fail over to
        if (isFailoverPossible.get()) {
            this.updateStaleLocationInfo(request);
            return true;
        }

        // no regions to fail over to
        this.partitionKeyRangeToFailoverInfo.remove(partitionKeyRange);
        return false;
    }

    @Override
    public boolean tryBookmarkPartitionKeyRangeSuccess(RxDocumentServiceRequest request) {

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

        URI succeededLocation = request.requestContext.locationEndpointToRoute;

        if (this.partitionKeyRangeToFailoverInfo.containsKey(partitionKeyRange)) {
            this.partitionKeyRangeToFailoverInfo.compute(partitionKeyRange, (partitionKeyRangeAsKey, partitionKeyRangeFailoverInfoAsVal) -> {

                if (partitionKeyRangeFailoverInfoAsVal == null) {
                    partitionKeyRangeFailoverInfoAsVal = new PartitionLevelFailoverInfo();
                }

                partitionKeyRangeFailoverInfoAsVal.bookmarkSuccess(succeededLocation, request.isReadOnlyRequest());
                return partitionKeyRangeFailoverInfoAsVal;
            });
        }

        return false;
    }

    @Override
    public boolean isRegionAvailableForPartitionKeyRange(RxDocumentServiceRequest request) {

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
            throw new IllegalStateException("requestContext.resolvedPartitionKeyRange cannot be null!");
        }

        URI locationWithUndeterminedAvailability = request.requestContext.locationEndpointToRoute;

        if (locationWithUndeterminedAvailability == null) {
            throw new IllegalStateException("requestContext.locationEndpointToRoute cannot be null!");
        }

        if (this.partitionKeyRangeToFailoverInfo.containsKey(partitionKeyRange)) {

            // is it possible for this instance to go stale?
            PartitionLevelFailoverInfo partitionLevelFailoverInfo = this.partitionKeyRangeToFailoverInfo.get(partitionKeyRange);

            if (partitionLevelFailoverInfo.partitionLevelFailureMetadata.containsKey(locationWithUndeterminedAvailability)) {

                LocationLevelFailureMetadata locationLevelFailureMetadata
                    = partitionLevelFailoverInfo.partitionLevelFailureMetadata.get(locationWithUndeterminedAvailability);

                if (locationLevelFailureMetadata.partitionKeyRangeUnavailabilityStatus.get() == PartitionKeyRangeUnavailabilityStatus.FreshUnavailable) {
                    return false;
                }
            }

            // there is no locationLevelFailureMetadata for locationWithUndeterminedAvailability
            // [or] locationWithUndeterminedAvailability is still available / is stale unavailable
            return true;
        }

        // there is no partitionLevelFailoverInfo for partitionKeyRange
        return true;
    }

    public void updateStaleLocationInfo(RxDocumentServiceRequest request) {
        Mono.delay(Duration.ofSeconds(60))
            .flatMap(ignore -> {
                Map<PartitionKeyRange, PartitionLevelFailoverInfo> partitionKeyRangeToFailoverInfo
                    = this.partitionKeyRangeToFailoverInfo;

                if (request.requestContext == null) {
                    return Mono.empty();
                }

                PartitionKeyRange partitionKeyRange = request.requestContext.resolvedPartitionKeyRange;

                if (partitionKeyRange == null) {
                    return Mono.empty();
                }

                URI unavailableLocation = request.requestContext.locationEndpointToRoute;

                if (unavailableLocation == null) {
                    return Mono.empty();
                }

                if (partitionKeyRangeToFailoverInfo.containsKey(partitionKeyRange)) {
                    PartitionLevelFailoverInfo partitionLevelFailoverInfo
                        = partitionKeyRangeToFailoverInfo.get(partitionKeyRange);
                    LocationLevelFailureMetadata locationLevelFailureMetadata
                        = partitionLevelFailoverInfo.partitionLevelFailureMetadata.get(unavailableLocation);

                    locationLevelFailureMetadata.partitionKeyRangeUnavailabilityStatus.set(PartitionKeyRangeUnavailabilityStatus.StaleUnavailable);
                }

                return Mono.empty();
            }).subscribeOn(CosmosSchedulers.COSMOS_PARALLEL)
            .subscribe();
    }

    // what is the point of an inner class?
    // at a high-level, the below class needs:
    //  1. consecutive failure count tracker
    //  2. unavailable since
    //  3. regions unavailable in
    //  4. failure type
    static class PartitionLevelFailoverInfo {

        private final ConcurrentHashMap<URI, LocationLevelFailureMetadata> partitionLevelFailureMetadata;
        // points to the current location a request will be routed to

        PartitionLevelFailoverInfo() {
            this.partitionLevelFailureMetadata = new ConcurrentHashMap<>();
        }

        // bookmark failure
        // method purpose:
        // 1. increment consecutive failure count
        // 2. if failure count crosses threshold for
        public boolean isFailureThresholdBreachedForLocation(URI failedLocation, boolean isReadRequest) {

            AtomicBoolean isFailureThresholdBreached = new AtomicBoolean(false);

            this.partitionLevelFailureMetadata.compute(failedLocation, (locationAsKey, locationLevelFailureMetadataAsVal) -> {

                if (locationLevelFailureMetadataAsVal == null) {
                    locationLevelFailureMetadataAsVal = new LocationLevelFailureMetadata();
                }

                // todo : make threshold for marking a location as failed more comprehensive

                if (isReadRequest) {
                    if (locationLevelFailureMetadataAsVal.consecutiveFailureCountForReads.incrementAndGet() > 5) {
                        locationLevelFailureMetadataAsVal.unavailableForReadsSince.set(Instant.now());
                        locationLevelFailureMetadataAsVal.partitionKeyRangeUnavailabilityStatus.set(PartitionKeyRangeUnavailabilityStatus.FreshUnavailable);
                        isFailureThresholdBreached.set(true);
                    }
                } else {
                    if (locationLevelFailureMetadataAsVal.consecutiveFailureCountForWrites.incrementAndGet() > 5) {
                        locationLevelFailureMetadataAsVal.unavailableForWritesSince.set(Instant.now());
                        locationLevelFailureMetadataAsVal.partitionKeyRangeUnavailabilityStatus.set(PartitionKeyRangeUnavailabilityStatus.FreshUnavailable);
                        isFailureThresholdBreached.set(true);
                    }
                }

                return locationLevelFailureMetadataAsVal;
            });

            return isFailureThresholdBreached.get();
        }

        // bookmark success
        public void bookmarkSuccess(URI succeededLocation, boolean isReadRequest) {
            this.partitionLevelFailureMetadata.compute(succeededLocation, (locationAsKey, locationLevelFailureMetadataAsVal) -> {

                if (locationLevelFailureMetadataAsVal == null) {
                    return new LocationLevelFailureMetadata();
                }

                if (isReadRequest) {
                    if (locationLevelFailureMetadataAsVal.consecutiveFailureCountForReads.get() > 1) {
                        switch (locationLevelFailureMetadataAsVal.partitionKeyRangeUnavailabilityStatus.get()) {
                            case StaleUnavailable:
                                locationLevelFailureMetadataAsVal = new LocationLevelFailureMetadata();
                            case Available:
                                locationLevelFailureMetadataAsVal.consecutiveFailureCountForReads.decrementAndGet();
                        }
                    }
                } else {
                    if (locationLevelFailureMetadataAsVal.consecutiveFailureCountForWrites.get() > 1) {
                        switch (locationLevelFailureMetadataAsVal.partitionKeyRangeUnavailabilityStatus.get()) {
                            case StaleUnavailable:
                                locationLevelFailureMetadataAsVal = new LocationLevelFailureMetadata();
                            case Available:
                                locationLevelFailureMetadataAsVal.consecutiveFailureCountForWrites.decrementAndGet();
                        }                    }
                }

                return locationLevelFailureMetadataAsVal;
            });
        }

        // method purpose - choose the next possible region for this partition
        // 1. if current == failedLocation - try using a different location
        //      a) iterate through the list of read / write locations
        //      b) if location in iteration loop not part of locationUnavailabilityInfos, then assign location to current
        // 2. if current != failedLocation - a different thread has updated it
        // 3.
        public boolean areLocationsAvailableForPartitionKeyRange(
            GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker,
            List<URI> availableLocationsAtAccountLevel,
            boolean isReadRequest) {

            for (URI availableLocation : availableLocationsAtAccountLevel) {
                if (!this.partitionLevelFailureMetadata.containsKey(availableLocation)) {
                    return true;
                } else {
                    LocationLevelFailureMetadata locationLevelFailureMetadata = this.partitionLevelFailureMetadata.get(availableLocation);

                    if (locationLevelFailureMetadata.partitionKeyRangeUnavailabilityStatus.get() == PartitionKeyRangeUnavailabilityStatus.Available) {
                        return true;
                    }
                }
            }

            Instant mostStaleUnavailableTimeAcrossRegions = Instant.MAX;
            LocationLevelFailureMetadata locationLevelFailureMetadataForMostStaleLocation = null;

            // find region with most 'stale' unavailability
            for (Map.Entry<URI, LocationLevelFailureMetadata> uriToLocationLevelFailureMetadata : this.partitionLevelFailureMetadata.entrySet()) {
                URI unavailableLocation = uriToLocationLevelFailureMetadata.getKey();
                LocationLevelFailureMetadata locationLevelFailureMetadata = uriToLocationLevelFailureMetadata.getValue();

                if (locationLevelFailureMetadata.partitionKeyRangeUnavailabilityStatus.get() == PartitionKeyRangeUnavailabilityStatus.Available) {
                    return true;
                }

                if (locationLevelFailureMetadata.partitionKeyRangeUnavailabilityStatus.get() == PartitionKeyRangeUnavailabilityStatus.StaleUnavailable) {
                    return true;
                }

                if (isReadRequest) {

                    Instant unavailableSince = locationLevelFailureMetadata.unavailableForReadsSince.get();

                    if (mostStaleUnavailableTimeAcrossRegions.isAfter(unavailableSince)) {
                        mostStaleUnavailableTimeAcrossRegions = unavailableSince;
                        locationLevelFailureMetadataForMostStaleLocation = locationLevelFailureMetadata;
                    }
                } else {

                    Instant unavailableSince = locationLevelFailureMetadata.unavailableForWritesSince.get();

                    if (mostStaleUnavailableTimeAcrossRegions.isAfter(locationLevelFailureMetadata.unavailableForWritesSince.get())) {
                        mostStaleUnavailableTimeAcrossRegions = unavailableSince;
                        locationLevelFailureMetadataForMostStaleLocation = locationLevelFailureMetadata;
                    }
                }
            }

            if (locationLevelFailureMetadataForMostStaleLocation != null) {
                locationLevelFailureMetadataForMostStaleLocation.partitionKeyRangeUnavailabilityStatus.set(PartitionKeyRangeUnavailabilityStatus.StaleUnavailable);
                return true;
            }

            return false;
        }
    }

    private static class LocationLevelFailureMetadata {
        private final AtomicInteger consecutiveFailureCountForWrites = new AtomicInteger();
        private final AtomicInteger consecutiveFailureCountForReads = new AtomicInteger();
        private final AtomicReference<Instant> unavailableForWritesSince = new AtomicReference<>(Instant.MIN);
        private final AtomicReference<Instant> unavailableForReadsSince = new AtomicReference<>(Instant.MIN);
        private final AtomicReference<PartitionKeyRangeUnavailabilityStatus> partitionKeyRangeUnavailabilityStatus = new AtomicReference<>(PartitionKeyRangeUnavailabilityStatus.Available);

    }

    enum PartitionKeyRangeUnavailabilityStatus {
        Available(100),
        FreshUnavailable(200),
        StaleUnavailable(300);

        private int priority;

        PartitionKeyRangeUnavailabilityStatus(int priority) {
            this.priority = priority;
        }
    }
}
