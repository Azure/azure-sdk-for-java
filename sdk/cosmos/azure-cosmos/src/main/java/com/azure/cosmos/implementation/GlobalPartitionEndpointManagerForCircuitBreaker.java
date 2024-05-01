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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class GlobalPartitionEndpointManagerForCircuitBreaker implements IGlobalPartitionEndpointManager {

    private static final Logger logger = LoggerFactory.getLogger(GlobalPartitionEndpointManagerForCircuitBreaker.class);

    private final GlobalEndpointManager globalEndpointManager;
    private final ConcurrentHashMap<PartitionKeyRange, PartitionLevelLocationUnavailabilityInfo> partitionKeyRangeToFailoverInfo;

    public GlobalPartitionEndpointManagerForCircuitBreaker(GlobalEndpointManager globalEndpointManager) {
        this.partitionKeyRangeToFailoverInfo = new ConcurrentHashMap<>();
        this.globalEndpointManager = globalEndpointManager;
    }

    public void init() {
        this.updateStaleLocationInfo().subscribeOn(CosmosSchedulers.PARTITION_AVAILABILITY_STALENESS_CHECK_SINGLE).subscribe();
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
                partitionKeyRangeFailoverInfoAsVal = new PartitionLevelLocationUnavailabilityInfo();
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

    public boolean tryMarkRegionAsUnavailableForPartitionKeyRange(RxDocumentServiceRequest request, URI failedLocation) {

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

        AtomicBoolean isFailoverPossible = new AtomicBoolean(true);
        AtomicBoolean isFailureThresholdBreached = new AtomicBoolean(false);

        this.partitionKeyRangeToFailoverInfo.compute(partitionKeyRange, (partitionKeyRangeAsKey, partitionKeyRangeFailoverInfoAsVal) -> {

            if (partitionKeyRangeFailoverInfoAsVal == null) {
                partitionKeyRangeFailoverInfoAsVal = new PartitionLevelLocationUnavailabilityInfo();
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
                    partitionKeyRangeFailoverInfoAsVal = new PartitionLevelLocationUnavailabilityInfo();
                }

                partitionKeyRangeFailoverInfoAsVal.bookmarkSuccess(succeededLocation);
                return partitionKeyRangeFailoverInfoAsVal;
            });
        }

        return false;
    }

    public List<URI> getUnavailableLocationsForPartition(PartitionKeyRange partitionKeyRange) {

        checkNotNull(partitionKeyRange, "Supplied partitionKeyRange cannot be null!");

        PartitionLevelLocationUnavailabilityInfo partitionLevelLocationUnavailabilityInfoSnapshot =
            this.partitionKeyRangeToFailoverInfo.get(partitionKeyRange);

        List<URI> unavailableLocations = new ArrayList<>();

        if (partitionLevelLocationUnavailabilityInfoSnapshot != null) {
            Map<URI, FailureMetricsForPartition> locationEndpointToFailureMetricsForPartition =
                partitionLevelLocationUnavailabilityInfoSnapshot.locationEndpointToFailureMetricsForPartition;

            for (Map.Entry<URI, FailureMetricsForPartition> pair : locationEndpointToFailureMetricsForPartition.entrySet()) {
                URI location = pair.getKey();
                FailureMetricsForPartition failureMetricsForPartition = pair.getValue();

                if (failureMetricsForPartition.partitionScopedRegionUnavailabilityStatus.get() == PartitionScopedRegionUnavailabilityStatus.FreshUnavailable) {
                    unavailableLocations.add(location);
                }
            }
        }

        return UnmodifiableList.unmodifiableList(unavailableLocations);
    }

    private Flux<Object> updateStaleLocationInfo() {
        return Mono.just(1)
            .publishOn(CosmosSchedulers.PARTITION_AVAILABILITY_STALENESS_CHECK_SINGLE)
            .repeat()
            .delayElements(Duration.ofSeconds(60))
            .flatMap(ignore -> {

                logger.info("Background updateStaleLocationInfo kicking in...");

                for (Map.Entry<PartitionKeyRange, PartitionLevelLocationUnavailabilityInfo> pkRangeToFailoverInfo : this.partitionKeyRangeToFailoverInfo.entrySet()) {

                    PartitionLevelLocationUnavailabilityInfo partitionLevelLocationUnavailabilityInfo = pkRangeToFailoverInfo.getValue();

                    for (Map.Entry<URI, FailureMetricsForPartition> locationToLocationLevelMetrics : partitionLevelLocationUnavailabilityInfo.locationEndpointToFailureMetricsForPartition.entrySet()) {

                        FailureMetricsForPartition failureMetricsForPartition = locationToLocationLevelMetrics.getValue();
                        failureMetricsForPartition.handleSuccess(false);
                    }
                }

                return Mono.empty();
            });
    }

    private static class PartitionLevelLocationUnavailabilityInfo {

        private final ConcurrentHashMap<URI, FailureMetricsForPartition> locationEndpointToFailureMetricsForPartition;

        PartitionLevelLocationUnavailabilityInfo() {
            this.locationEndpointToFailureMetricsForPartition = new ConcurrentHashMap<>();
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

                    this.locationEndpointToFailureMetricsForPartition.compute(location, (locationAsKey, failureMetricsForPartitionAsVal) -> {

                        if (failureMetricsForPartitionAsVal == null) {
                            failureMetricsForPartitionAsVal = new FailureMetricsForPartition();
                        }

                        for (Map.Entry<ErrorKey, Integer> countForError : errorCounts.entrySet()) {
                            failureMetricsForPartitionAsVal.handleFailure(countForError.getValue());
                        }

                        isFailureThresholdBreached.set(failureMetricsForPartitionAsVal.isFailureThresholdBreached());
                        return failureMetricsForPartitionAsVal;
                    });
                }
            }

            return isFailureThresholdBreached.get();
        }

        public void bookmarkSuccess(URI succeededLocation) {
            this.locationEndpointToFailureMetricsForPartition.compute(succeededLocation, (locationAsKey, failureMetricsForPartitionAsVal) -> {

                if (failureMetricsForPartitionAsVal != null) {
                    failureMetricsForPartitionAsVal.handleSuccess(false);;
                }

                return failureMetricsForPartitionAsVal;
            });
        }

        public boolean areLocationsAvailableForPartitionKeyRange(List<URI> availableLocationsAtAccountLevel) {

            for (URI availableLocation : availableLocationsAtAccountLevel) {
                if (!this.locationEndpointToFailureMetricsForPartition.containsKey(availableLocation)) {
                    return true;
                } else {
                    FailureMetricsForPartition failureMetricsForPartition = this.locationEndpointToFailureMetricsForPartition.get(availableLocation);

                    if (failureMetricsForPartition.isRegionAvailableToProcessRequests()) {
                        return true;
                    }
                }
            }

            Instant mostStaleUnavailableTimeAcrossRegions = Instant.MAX;
            FailureMetricsForPartition locationLevelFailureMetadataForMostStaleLocation = null;

            // find region with most 'stale' unavailability
            for (Map.Entry<URI, FailureMetricsForPartition> uriToLocationLevelFailureMetadata : this.locationEndpointToFailureMetricsForPartition.entrySet()) {
                FailureMetricsForPartition failureMetricsForPartition = uriToLocationLevelFailureMetadata.getValue();

                if (failureMetricsForPartition.isRegionAvailableToProcessRequests()) {
                    return true;
                }

                Instant unavailableSinceSnapshot = failureMetricsForPartition.unavailableSince.get();

                if (mostStaleUnavailableTimeAcrossRegions.isAfter(unavailableSinceSnapshot)) {
                    mostStaleUnavailableTimeAcrossRegions = unavailableSinceSnapshot;
                    locationLevelFailureMetadataForMostStaleLocation = failureMetricsForPartition;
                }
            }

            if (locationLevelFailureMetadataForMostStaleLocation != null) {
                locationLevelFailureMetadataForMostStaleLocation.handleSuccess(true);
                return true;
            }

            return false;
        }
    }

    private static class FailureMetricsForPartition {
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
                        successCount.incrementAndGet();
                        if (successCount.get() > 10 && (double) failureCount.get() / (double) successCount.get() < allowedFailureRatio) {
                            this.setHealthStatus(PartitionScopedRegionUnavailabilityStatus.Available);
                            logger.info("Partition marked as Available");
                        }
                    }
                    break;
                case FreshUnavailable:
                    if (!forceStateChange) {
                        if (Duration.between(this.unavailableSince.get(), Instant.now()).compareTo(Duration.ofSeconds(30)) > 0) {
                            this.setHealthStatus(PartitionScopedRegionUnavailabilityStatus.StaleUnavailable);
                            logger.info("Partition marked as StaleUnavailable");
                        }
                    } else {
                        this.setHealthStatus(PartitionScopedRegionUnavailabilityStatus.StaleUnavailable);
                        logger.info("Partition marked as StaleUnavailable");
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
                        logger.info("Partition marked as FreshUnavailable from Available");
                    }
                    break;
                case StaleUnavailable:
                    if (failureCount.get() < allowedFailureCount) {
                        failureCount.addAndGet(errorCount);
                    } else {
                        this.setHealthStatus(PartitionScopedRegionUnavailabilityStatus.FreshUnavailable);
                        logger.info("Partition marked as FreshUnavailable from StaleUnavailable");
                    }
                    break;
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
                    return 0d;
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

    private enum PartitionScopedRegionUnavailabilityStatus {
        Available(100),
        FreshUnavailable(200),
        StaleUnavailable(300);

        private int priority;

        PartitionScopedRegionUnavailabilityStatus(int priority) {
            this.priority = priority;
        }
    }
}
