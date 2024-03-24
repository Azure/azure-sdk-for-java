// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
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

    public void init() {

    }

    @Override
    public boolean tryMarkRegionAsUnavailableForPartitionKeyRange(RxDocumentServiceRequest request) {

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

            isFailureThresholdBreached.set(partitionKeyRangeFailoverInfoAsVal.isFailureThresholdBreachedForLocation(request));

            if (isFailureThresholdBreached.get()) {

                UnmodifiableList<URI> applicableEndpoints = request.isReadOnly() ?
                    this.globalEndpointManager.getApplicableReadEndpoints(request.requestContext.getExcludeRegions()) :
                    this.globalEndpointManager.getApplicableWriteEndpoints(request.requestContext.getExcludeRegions());

                isFailoverPossible.set(
                    partitionKeyRangeFailoverInfoAsVal.areLocationsAvailableForPartitionKeyRange(applicableEndpoints));
            }

            return partitionKeyRangeFailoverInfoAsVal;
        });

        // set to true if and only if failure threshold exceeded for the region
        // and if failover is possible
        // a failover is only possible when there are available regions left to fail over to
        if (isFailoverPossible.get()) {
            return true;
        }

        // no regions to fail over to
        this.partitionKeyRangeToFailoverInfo.remove(partitionKeyRange);
        return false;
    }

    @Override
    public boolean tryBookmarkRegionSuccessForPartitionKeyRange(RxDocumentServiceRequest request) {

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

                partitionKeyRangeFailoverInfoAsVal.bookmarkSuccess(succeededLocation);
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

                LocationLevelMetrics locationLevelMetrics
                    = partitionLevelFailoverInfo.partitionLevelFailureMetadata.get(locationWithUndeterminedAvailability);

                if (locationLevelMetrics.partitionScopedRegionUnavailabilityStatus.get() == PartitionScopedRegionUnavailabilityStatus.FreshUnavailable) {
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

    private Flux<Object> updateStaleLocationInfo() {
        return Mono.just(1).repeat().delayElements(Duration.ofSeconds(60)).flatMap(ignore -> {

            for (Map.Entry<PartitionKeyRange, PartitionLevelFailoverInfo> pkRangeToFailoverInfo : this.partitionKeyRangeToFailoverInfo.entrySet()) {

                PartitionLevelFailoverInfo partitionLevelFailoverInfo = pkRangeToFailoverInfo.getValue();

                for (Map.Entry<URI, LocationLevelMetrics> locationToLocationLevelMetrics : partitionLevelFailoverInfo.partitionLevelFailureMetadata.entrySet()) {

                    LocationLevelMetrics locationLevelMetrics = locationToLocationLevelMetrics.getValue();
                    locationLevelMetrics.handleSuccess(false);
                }
            }

            return Mono.empty();
        });
    }

    static class PartitionLevelFailoverInfo {

        private final ConcurrentHashMap<URI, LocationLevelMetrics> partitionLevelFailureMetadata;

        PartitionLevelFailoverInfo() {
            this.partitionLevelFailureMetadata = new ConcurrentHashMap<>();
        }

        public boolean isFailureThresholdBreachedForLocation(RxDocumentServiceRequest request) {

            AtomicBoolean isFailureThresholdBreached = new AtomicBoolean(false);

            if (request.locationLevelCircuitBreakerRequestContext == null) {
                return false;
            }

            if (!request.locationLevelCircuitBreakerRequestContext.getFailuresForAllLocations().isEmpty()) {

                ConcurrentHashMap<URI, ConcurrentHashMap<ErrorKey, Integer>> failuresForAllLocations
                    = request.locationLevelCircuitBreakerRequestContext.getFailuresForAllLocations();

                for (Map.Entry<URI, ConcurrentHashMap<ErrorKey, Integer>> failuresPerLocation : failuresForAllLocations.entrySet()) {

                    URI location = failuresPerLocation.getKey();
                    ConcurrentHashMap<ErrorKey, Integer> errorCounts = failuresPerLocation.getValue();

                    this.partitionLevelFailureMetadata.compute(location, (locationAsKey, locationLevelMetricsAsVal) -> {

                        if (locationLevelMetricsAsVal == null) {
                            locationLevelMetricsAsVal = new LocationLevelMetrics();
                        }

                        for (Map.Entry<ErrorKey, Integer> countForError : errorCounts.entrySet()) {
                            locationLevelMetricsAsVal.handleFailure(countForError.getValue());
                        }

                        isFailureThresholdBreached.set(locationLevelMetricsAsVal.isFailureThresholdBreached());
                        return locationLevelMetricsAsVal;
                    });
                }
            }

            return isFailureThresholdBreached.get();
        }

        public void bookmarkSuccess(URI succeededLocation) {
            this.partitionLevelFailureMetadata.compute(succeededLocation, (locationAsKey, locationLevelMetricsAsVal) -> {

                if (locationLevelMetricsAsVal == null) {
                    return new LocationLevelMetrics();
                }

                locationLevelMetricsAsVal.handleSuccess(false);
                return locationLevelMetricsAsVal;
            });
        }

        public boolean areLocationsAvailableForPartitionKeyRange(List<URI> availableLocationsAtAccountLevel) {

            for (URI availableLocation : availableLocationsAtAccountLevel) {
                if (!this.partitionLevelFailureMetadata.containsKey(availableLocation)) {
                    return true;
                } else {
                    LocationLevelMetrics locationLevelMetrics = this.partitionLevelFailureMetadata.get(availableLocation);

                    if (locationLevelMetrics.isRegionAvailableToProcessRequests()) {
                        return true;
                    }
                }
            }

            Instant mostStaleUnavailableTimeAcrossRegions = Instant.MAX;
            LocationLevelMetrics locationLevelFailureMetadataForMostStaleLocation = null;

            // find region with most 'stale' unavailability
            for (Map.Entry<URI, LocationLevelMetrics> uriToLocationLevelFailureMetadata : this.partitionLevelFailureMetadata.entrySet()) {
                LocationLevelMetrics locationLevelMetrics = uriToLocationLevelFailureMetadata.getValue();

                if (locationLevelMetrics.isRegionAvailableToProcessRequests()) {
                    return true;
                }

                Instant unavailableSinceSnapshot = locationLevelMetrics.unavailableSince.get();

                if (mostStaleUnavailableTimeAcrossRegions.isAfter(unavailableSinceSnapshot)) {
                    mostStaleUnavailableTimeAcrossRegions = unavailableSinceSnapshot;
                    locationLevelFailureMetadataForMostStaleLocation = locationLevelMetrics;
                }
            }

            if (locationLevelFailureMetadataForMostStaleLocation != null) {
                locationLevelFailureMetadataForMostStaleLocation.handleSuccess(true);
                return true;
            }

            return false;
        }
    }

    private static class LocationLevelMetrics {
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicReference<Instant> unavailableSince = new AtomicReference<>(Instant.MAX);
        private final AtomicReference<PartitionScopedRegionUnavailabilityStatus> partitionScopedRegionUnavailabilityStatus = new AtomicReference<>(PartitionScopedRegionUnavailabilityStatus.Available);
        private final AtomicBoolean isFailureThresholdBreached = new AtomicBoolean(false);

        public void handleSuccess(boolean forceStateChange) {

            PartitionScopedRegionUnavailabilityStatus currentStatusSnapshot = this.partitionScopedRegionUnavailabilityStatus.get();

            double allowedFailureRatio = getAllowedFailureRatioByStatus(currentStatusSnapshot);

            switch (currentStatusSnapshot) {
                case Available:
                    if (!forceStateChange) {
                        if (failureCount.get() > 0) {
                            failureCount.decrementAndGet();
                        }
                    }
                    break;
                case StaleUnavailable:
                    if (!forceStateChange) {
                        if (successCount.get() < 10) {
                            successCount.incrementAndGet();
                        } else {
                            if ((double) failureCount.get() / (double) successCount.get() < allowedFailureRatio) {
                                this.setHealthStatus(PartitionScopedRegionUnavailabilityStatus.Available);
                            }
                        }
                    }
                    break;
                case FreshUnavailable:
                    if (!forceStateChange) {
                        if (Duration.between(this.unavailableSince.get(), Instant.now()).compareTo(Duration.ofSeconds(120)) == 1) {
                            this.setHealthStatus(PartitionScopedRegionUnavailabilityStatus.StaleUnavailable);
                        }
                    } else {
                        this.setHealthStatus(PartitionScopedRegionUnavailabilityStatus.StaleUnavailable);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unsupported health status: " + currentStatusSnapshot);
            }
        }

        public void handleFailure(int errorCount) {

            PartitionScopedRegionUnavailabilityStatus currentStatusSnapshot = this.partitionScopedRegionUnavailabilityStatus.get();

            int allowedFailureCount = getAllowedFailureCountByStatus(currentStatusSnapshot);

            switch (currentStatusSnapshot) {
                case Available:
                    if (failureCount.get() < allowedFailureCount) {
                        failureCount.addAndGet(errorCount);
                    } else {
                        this.setHealthStatus(PartitionScopedRegionUnavailabilityStatus.FreshUnavailable);
                    }
                case StaleUnavailable:
                    if (failureCount.get() < allowedFailureCount) {
                        failureCount.addAndGet(errorCount);
                    } else {
                        this.setHealthStatus(PartitionScopedRegionUnavailabilityStatus.FreshUnavailable);
                    }
                default:
                    throw new IllegalStateException("Unsupported health status: " + currentStatusSnapshot);
            }
        }

        public void setHealthStatus(PartitionScopedRegionUnavailabilityStatus status) {
            this.partitionScopedRegionUnavailabilityStatus.updateAndGet(previousStatus -> {

                PartitionScopedRegionUnavailabilityStatus newStatus;

                switch (status) {
                    case Available:
                        if (previousStatus == PartitionScopedRegionUnavailabilityStatus.StaleUnavailable) {
                            this.failureCount.set(0);
                            this.successCount.set(0);
                            this.unavailableSince.set(Instant.MAX);
                            this.isFailureThresholdBreached.set(false);
                        }
                        newStatus = status;
                        break;
                    case FreshUnavailable:
                        if (previousStatus == PartitionScopedRegionUnavailabilityStatus.Available) {
                            this.failureCount.set(0);
                            this.successCount.set(0);
                            this.unavailableSince.set(Instant.now());
                            this.isFailureThresholdBreached.set(true);
                        }
                        newStatus = status;
                        break;
                    case StaleUnavailable:
                        this.failureCount.set(0);
                        this.successCount.set(0);
                        this.unavailableSince.set(Instant.MAX);
                        this.isFailureThresholdBreached.set(false);
                        newStatus = status;
                        break;
                    default:
                        throw new IllegalStateException("Unsupported health status: " + status);
                }

                return newStatus;
            });
        }

        private static double getAllowedFailureRatioByStatus(PartitionScopedRegionUnavailabilityStatus status) {
            switch (status) {
                case Available:
                    return 0.3d;
                case StaleUnavailable:
                    return 0.1d;
                default:
                    throw new IllegalStateException("Unsupported health status: " + status);
            }
        }

        private static int getAllowedFailureCountByStatus(PartitionScopedRegionUnavailabilityStatus status) {
            switch (status) {
                case Available:
                    return 10;
                case StaleUnavailable:
                    return 5;
                default:
                    throw new IllegalStateException("Unsupported health status: " + status);
            }
        }

        public boolean isFailureThresholdBreached() {
            return this.isFailureThresholdBreached.get();
        }

        public boolean isRegionAvailableToProcessRequests() {
            return this.partitionScopedRegionUnavailabilityStatus.get() == PartitionScopedRegionUnavailabilityStatus.Available ||
                this.partitionScopedRegionUnavailabilityStatus.get() == PartitionScopedRegionUnavailabilityStatus.StaleUnavailable;
        }

        public boolean isRegionUnavailableToProcessRequest() {
            return this.partitionScopedRegionUnavailabilityStatus.get() == PartitionScopedRegionUnavailabilityStatus.FreshUnavailable;

        }
    }

    enum PartitionScopedRegionUnavailabilityStatus {
        Available(100),
        FreshUnavailable(200),
        StaleUnavailable(300);

        private int priority;

        PartitionScopedRegionUnavailabilityStatus(int priority) {
            this.priority = priority;
        }
    }
}
