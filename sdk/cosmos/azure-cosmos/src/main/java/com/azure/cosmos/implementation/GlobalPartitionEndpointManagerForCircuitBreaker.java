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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class GlobalPartitionEndpointManagerForCircuitBreaker {

    private static final Logger logger = LoggerFactory.getLogger(GlobalPartitionEndpointManagerForCircuitBreaker.class);

    private final GlobalEndpointManager globalEndpointManager;
    private final ConcurrentHashMap<PartitionKeyRangeWrapper, PartitionLevelLocationUnavailabilityInfo> partitionKeyRangeToFailoverInfo;
    private final ConcurrentHashMap<PartitionKeyRangeWrapper, PartitionKeyRangeWrapper> partitionsWithPossibleUnavailableRegions;
    private final LocationContextTransitionHandler locationContextTransitionHandler;

    public GlobalPartitionEndpointManagerForCircuitBreaker(GlobalEndpointManager globalEndpointManager) {
        this.partitionKeyRangeToFailoverInfo = new ConcurrentHashMap<>();
        this.partitionsWithPossibleUnavailableRegions = new ConcurrentHashMap<>();
        this.globalEndpointManager = globalEndpointManager;
        this.locationContextTransitionHandler = new LocationContextTransitionHandler();
    }

    public void init() {
        this.updateStaleLocationInfo().subscribeOn(CosmosSchedulers.PARTITION_AVAILABILITY_STALENESS_CHECK_SINGLE).subscribe();
    }

    public void handleLocationExceptionForPartitionKeyRange(RxDocumentServiceRequest request, URI failedLocation) {

        checkNotNull(request, "request cannot be null!");
        checkNotNull(request.requestContext, "requestContext cannot be null!");

        PartitionKeyRange partitionKeyRange = request.requestContext.resolvedPartitionKeyRange;

        if (partitionKeyRange == null) {
            return;
        }

        String resourceId = request.getResourceId();
        checkNotNull(resourceId, "resourceId cannot be null!");

        logger.info("Handling exception : {}", resourceId);

        PartitionKeyRangeWrapper partitionKeyRangeWrapper = new PartitionKeyRangeWrapper(partitionKeyRange, resourceId);

        AtomicBoolean isFailoverPossible = new AtomicBoolean(true);
        AtomicBoolean isFailureThresholdBreached = new AtomicBoolean(false);

        PartitionLevelLocationUnavailabilityInfo partitionLevelLocationUnavailabilityInfoSnapshot = this.partitionKeyRangeToFailoverInfo.get(partitionKeyRangeWrapper);

        if (partitionLevelLocationUnavailabilityInfoSnapshot == null) {
            this.partitionKeyRangeToFailoverInfo.compute(partitionKeyRangeWrapper, (partitionKeyRangeWrapperAsKey, partitionKeyRangeFailoverInfoAsVal) -> {

                if (partitionKeyRangeFailoverInfoAsVal == null) {
                    partitionKeyRangeFailoverInfoAsVal = new PartitionLevelLocationUnavailabilityInfo();
                }

                isFailureThresholdBreached.set(partitionKeyRangeFailoverInfoAsVal.handleException(partitionKeyRangeWrapperAsKey, failedLocation));

                if (isFailureThresholdBreached.get()) {

                    UnmodifiableList<URI> applicableEndpoints = request.isReadOnly() ?
                        this.globalEndpointManager.getApplicableReadEndpoints(request.requestContext.getExcludeRegions()) :
                        this.globalEndpointManager.getApplicableWriteEndpoints(request.requestContext.getExcludeRegions());

                    isFailoverPossible.set(
                        partitionKeyRangeFailoverInfoAsVal.areLocationsAvailableForPartitionKeyRange(partitionKeyRangeWrapperAsKey, applicableEndpoints));
                }

                return partitionKeyRangeFailoverInfoAsVal;
            });
        } else {
            isFailureThresholdBreached.set(partitionLevelLocationUnavailabilityInfoSnapshot.handleException(partitionKeyRangeWrapper, failedLocation));

            if (isFailureThresholdBreached.get()) {

                UnmodifiableList<URI> applicableEndpoints = request.isReadOnly() ?
                    this.globalEndpointManager.getApplicableReadEndpoints(request.requestContext.getExcludeRegions()) :
                    this.globalEndpointManager.getApplicableWriteEndpoints(request.requestContext.getExcludeRegions());

                isFailoverPossible.set(
                    partitionLevelLocationUnavailabilityInfoSnapshot.areLocationsAvailableForPartitionKeyRange(partitionKeyRangeWrapper, applicableEndpoints));
            }
        }

        // set to true if and only if failure threshold exceeded for the region
        // and if failover is possible
        // a failover is only possible when there are available regions left to fail over to
        if (isFailoverPossible.get()) {
            return;
        }

        // no regions to fail over to
        this.partitionKeyRangeToFailoverInfo.remove(partitionKeyRangeWrapper);
    }

    public void handleLocationSuccessForPartitionKeyRange(RxDocumentServiceRequest request) {

        checkNotNull(request, "request cannot be null!");
        checkNotNull(request.requestContext, "requestContext cannot be null!");

        PartitionKeyRange partitionKeyRange = request.requestContext.resolvedPartitionKeyRange;

        if (partitionKeyRange == null) {
            return;
        }

        String resourceId = request.getResourceId();
        logger.info("Handling success : {}", resourceId);

        PartitionKeyRangeWrapper partitionKeyRangeWrapper = new PartitionKeyRangeWrapper(partitionKeyRange, resourceId);

        URI succeededLocation = request.requestContext.locationEndpointToRoute;

        PartitionLevelLocationUnavailabilityInfo partitionLevelLocationUnavailabilityInfoSnapshot
            = this.partitionKeyRangeToFailoverInfo.get(partitionKeyRangeWrapper);

        if (partitionLevelLocationUnavailabilityInfoSnapshot != null) {
            partitionLevelLocationUnavailabilityInfoSnapshot.handleSuccess(partitionKeyRangeWrapper, succeededLocation);
        }
    }

    public List<URI> getUnavailableLocationEndpointsForPartitionKeyRange(String resourceId, PartitionKeyRange partitionKeyRange) {

        checkNotNull(partitionKeyRange, "Supplied partitionKeyRange cannot be null!");
        checkNotNull(resourceId, "Supplied resourceId cannot be null!");

        logger.info("Fetching unavailable regions for resource address : {}", resourceId);

        PartitionKeyRangeWrapper partitionKeyRangeWrapper = new PartitionKeyRangeWrapper(partitionKeyRange, resourceId);

        PartitionLevelLocationUnavailabilityInfo partitionLevelLocationUnavailabilityInfoSnapshot =
            this.partitionKeyRangeToFailoverInfo.get(partitionKeyRangeWrapper);

        List<URI> unavailableLocations = new ArrayList<>();
        boolean doesPartitionHaveUnavailableLocations = false;

        if (partitionLevelLocationUnavailabilityInfoSnapshot != null) {
            Map<URI, LocationSpecificContext> locationEndpointToFailureMetricsForPartition =
                partitionLevelLocationUnavailabilityInfoSnapshot.locationEndpointToLocationSpecificContextForPartition;

            for (Map.Entry<URI, LocationSpecificContext> pair : locationEndpointToFailureMetricsForPartition.entrySet()) {
                URI location = pair.getKey();
                LocationSpecificContext locationSpecificContext = pair.getValue();

                if (locationSpecificContext.locationUnavailabilityStatus == LocationUnavailabilityStatus.FreshUnavailable) {
                    unavailableLocations.add(location);
                    doesPartitionHaveUnavailableLocations = true;
                } else if (locationSpecificContext.locationUnavailabilityStatus == LocationUnavailabilityStatus.StaleUnavailable) {
                    doesPartitionHaveUnavailableLocations = true;
                } else if (locationSpecificContext.exceptionCount >= 1) {
                    doesPartitionHaveUnavailableLocations = true;
                }
            }
        }

        if (!doesPartitionHaveUnavailableLocations) {
            this.partitionKeyRangeToFailoverInfo.remove(partitionKeyRangeWrapper);
        }

        return UnmodifiableList.unmodifiableList(unavailableLocations);
    }

    private Flux<Object> updateStaleLocationInfo() {
        return Mono.just(1)
            .delayElement(Duration.ofSeconds(60))
            .repeat()
            .flatMap(ignore -> Flux.fromIterable(this.partitionsWithPossibleUnavailableRegions.entrySet()))
            .publishOn(CosmosSchedulers.PARTITION_AVAILABILITY_STALENESS_CHECK_SINGLE)
            .flatMap(partitionKeyRangeWrapperToPartitionKeyRangeWrapperPair -> {

                logger.info("Background updateStaleLocationInfo kicking in...");

                PartitionKeyRangeWrapper partitionKeyRangeWrapper = partitionKeyRangeWrapperToPartitionKeyRangeWrapperPair.getKey();

                PartitionLevelLocationUnavailabilityInfo partitionLevelLocationUnavailabilityInfo = this.partitionKeyRangeToFailoverInfo.get(partitionKeyRangeWrapper);

                if (partitionLevelLocationUnavailabilityInfo != null) {
                    for (Map.Entry<URI, LocationSpecificContext> locationToLocationLevelMetrics : partitionLevelLocationUnavailabilityInfo.locationEndpointToLocationSpecificContextForPartition.entrySet()) {

                        URI locationWithStaleUnavailabilityInfo = locationToLocationLevelMetrics.getKey();

                        partitionLevelLocationUnavailabilityInfo.locationEndpointToLocationSpecificContextForPartition.compute(locationWithStaleUnavailabilityInfo, (locationWithStaleUnavailabilityInfoAsKey, locationSpecificContextAsVal) -> {

                            if (locationSpecificContextAsVal != null) {
                                locationSpecificContextAsVal = GlobalPartitionEndpointManagerForCircuitBreaker.this.locationContextTransitionHandler.handleSuccess(locationSpecificContextAsVal, partitionKeyRangeWrapper, locationWithStaleUnavailabilityInfoAsKey, false);
                            }

                            return locationSpecificContextAsVal;
                        });
                    }
                } else {
                    this.partitionsWithPossibleUnavailableRegions.remove(partitionKeyRangeWrapper);
                }

                return Mono.empty();
            });
    }

    private class PartitionLevelLocationUnavailabilityInfo {

        private final ConcurrentHashMap<URI, LocationSpecificContext> locationEndpointToLocationSpecificContextForPartition;

        PartitionLevelLocationUnavailabilityInfo() {
            this.locationEndpointToLocationSpecificContextForPartition = new ConcurrentHashMap<>();
        }

        public boolean handleException(PartitionKeyRangeWrapper partitionKeyRangeWrapper, URI locationWithException) {

            AtomicBoolean isExceptionThresholdBreached = new AtomicBoolean(false);

            this.locationEndpointToLocationSpecificContextForPartition.compute(locationWithException, (locationAsKey, locationSpecificContextAsVal) -> {

                if (locationSpecificContextAsVal == null) {
                    locationSpecificContextAsVal = new LocationSpecificContext(0, 0, Instant.MAX, LocationUnavailabilityStatus.Available, false);
                }

                LocationSpecificContext locationSpecificContextAfterTransition = GlobalPartitionEndpointManagerForCircuitBreaker
                    .this.locationContextTransitionHandler.handleException(locationSpecificContextAsVal, partitionKeyRangeWrapper, locationWithException);

                isExceptionThresholdBreached.set(locationSpecificContextAfterTransition.isExceptionThresholdBreached());
                return locationSpecificContextAfterTransition;
            });

            return isExceptionThresholdBreached.get();
        }

        public void handleSuccess(PartitionKeyRangeWrapper partitionKeyRangeWrapper, URI succeededLocation) {
            this.locationEndpointToLocationSpecificContextForPartition.compute(succeededLocation, (locationAsKey, locationSpecificContextAsVal) -> {

                if (locationSpecificContextAsVal != null) {
                    locationSpecificContextAsVal = GlobalPartitionEndpointManagerForCircuitBreaker
                        .this.locationContextTransitionHandler.handleSuccess(locationSpecificContextAsVal, partitionKeyRangeWrapper, succeededLocation, false);
                }

                return locationSpecificContextAsVal;
            });
        }

        public boolean areLocationsAvailableForPartitionKeyRange(PartitionKeyRangeWrapper partitionKeyRangeWrapper, List<URI> availableLocationsAtAccountLevel) {

            for (URI availableLocation : availableLocationsAtAccountLevel) {
                if (!this.locationEndpointToLocationSpecificContextForPartition.containsKey(availableLocation)) {
                    return true;
                } else {
                    LocationSpecificContext locationSpecificContextSnapshot = this.locationEndpointToLocationSpecificContextForPartition.get(availableLocation);

                    if (locationSpecificContextSnapshot.isRegionAvailableToProcessRequests()) {
                        return true;
                    }
                }
            }

            Instant mostStaleUnavailableTimeAcrossRegions = Instant.MAX;
            LocationSpecificContext locationLevelFailureMetadataForMostStaleLocation = null;
            URI mostStaleUnavailableLocation = null;

            // find region with most 'stale' unavailability
            for (Map.Entry<URI, LocationSpecificContext> uriToLocationLevelFailureMetadata : this.locationEndpointToLocationSpecificContextForPartition.entrySet()) {
                LocationSpecificContext locationSpecificContext = uriToLocationLevelFailureMetadata.getValue();

                if (locationSpecificContext.isRegionAvailableToProcessRequests()) {
                    return true;
                }

                Instant unavailableSinceSnapshot = locationSpecificContext.unavailableSince;

                if (mostStaleUnavailableTimeAcrossRegions.isAfter(unavailableSinceSnapshot)) {
                    mostStaleUnavailableTimeAcrossRegions = unavailableSinceSnapshot;
                    mostStaleUnavailableLocation = uriToLocationLevelFailureMetadata.getKey();
                    locationLevelFailureMetadataForMostStaleLocation = locationSpecificContext;
                }
            }

            if (locationLevelFailureMetadataForMostStaleLocation != null) {
                this.locationEndpointToLocationSpecificContextForPartition.compute(mostStaleUnavailableLocation, (mostStaleUnavailableLocationAsKey, locationSpecificStatusAsVal) -> {

                    if (locationSpecificStatusAsVal != null) {
                        locationSpecificStatusAsVal = GlobalPartitionEndpointManagerForCircuitBreaker.this.locationContextTransitionHandler.handleSuccess(locationSpecificStatusAsVal, partitionKeyRangeWrapper, mostStaleUnavailableLocationAsKey, true);
                    }

                    return locationSpecificStatusAsVal;
                });
            }

            return false;
        }
    }

    private static class LocationSpecificContext {
        private final int exceptionCount;
        private final int successCount;
        private final Instant unavailableSince;
        private final LocationUnavailabilityStatus locationUnavailabilityStatus;
        private final boolean isExceptionThresholdBreached;

        public LocationSpecificContext(
            int successCount,
            int exceptionCount,
            Instant unavailableSince,
            LocationUnavailabilityStatus locationUnavailabilityStatus,
            boolean isExceptionThresholdBreached) {

            this.successCount = successCount;
            this.exceptionCount = exceptionCount;
            this.unavailableSince = unavailableSince;
            this.locationUnavailabilityStatus = locationUnavailabilityStatus;
            this.isExceptionThresholdBreached = isExceptionThresholdBreached;
        }

        public boolean isExceptionThresholdBreached() {
            return this.isExceptionThresholdBreached;
        }

        public boolean isRegionAvailableToProcessRequests() {
            return this.locationUnavailabilityStatus == LocationUnavailabilityStatus.Available ||
                this.locationUnavailabilityStatus == LocationUnavailabilityStatus.StaleUnavailable;
        }
    }

    private class LocationContextTransitionHandler {

        public LocationSpecificContext handleSuccess(
            LocationSpecificContext locationSpecificContext,
            PartitionKeyRangeWrapper partitionKeyRangeWrapper,
            URI locationWithSuccess,
            boolean forceStatusChange) {

            logger.info("Handling success");

            LocationUnavailabilityStatus currentStatusSnapshot = locationSpecificContext.locationUnavailabilityStatus;
            double allowedFailureRatio = getAllowedFailureRatioByStatus(currentStatusSnapshot);

            int exceptionCountActual = locationSpecificContext.exceptionCount;
            int successCountActual = locationSpecificContext.successCount;

            switch (currentStatusSnapshot) {
                case Available:
                    if (!forceStatusChange) {
                        if (exceptionCountActual > 0) {
                            exceptionCountActual -= 1;
                            return new LocationSpecificContext(
                                locationSpecificContext.successCount,
                                exceptionCountActual,
                                locationSpecificContext.unavailableSince,
                                locationSpecificContext.locationUnavailabilityStatus,
                                locationSpecificContext.isExceptionThresholdBreached
                            );
                        }
                    }
                    break;
                case StaleUnavailable:
                    if (!forceStatusChange) {
                        successCountActual += 1;
                        logger.info("Try to switch to Available but actual success count : {}", successCountActual);
                        if (successCountActual > 10 && (double) exceptionCountActual / (double) successCountActual < allowedFailureRatio) {
                            logger.info("Partition {}-{} of collection : {} marked as Available from StaleUnavailable for region : {}",
                                partitionKeyRangeWrapper.partitionKeyRange.getMinInclusive(),
                                partitionKeyRangeWrapper.partitionKeyRange.getMaxExclusive(),
                                partitionKeyRangeWrapper.resourceId,
                                GlobalPartitionEndpointManagerForCircuitBreaker.this.globalEndpointManager
                                    .getRegionName(locationWithSuccess, OperationType.Read));
                            return this.transitionHealthStatus(LocationUnavailabilityStatus.Available);
                        } else {
                            return new LocationSpecificContext(
                                successCountActual,
                                exceptionCountActual,
                                locationSpecificContext.unavailableSince,
                                locationSpecificContext.locationUnavailabilityStatus,
                                locationSpecificContext.isExceptionThresholdBreached);
                        }
                    }
                    break;
                case FreshUnavailable:
                    Instant unavailableSinceActual = locationSpecificContext.unavailableSince;
                    if (!forceStatusChange) {
                        if (Duration.between(unavailableSinceActual, Instant.now()).compareTo(Duration.ofSeconds(30)) > 0) {
                            logger.info("Partition {}-{} of collection : {} marked as StaleUnavailable from FreshUnavailable for region : {}",
                                partitionKeyRangeWrapper.partitionKeyRange.getMinInclusive(),
                                partitionKeyRangeWrapper.partitionKeyRange.getMaxExclusive(),
                                partitionKeyRangeWrapper.resourceId,
                                GlobalPartitionEndpointManagerForCircuitBreaker.this.globalEndpointManager
                                    .getRegionName(locationWithSuccess, OperationType.Read));

                            return this.transitionHealthStatus(LocationUnavailabilityStatus.StaleUnavailable);
                        }
                    } else {
                        logger.info("Partition {}-{} of collection : {} marked as StaleUnavailable from FreshAvailable for region : {}",
                            partitionKeyRangeWrapper.partitionKeyRange.getMinInclusive(),
                            partitionKeyRangeWrapper.partitionKeyRange.getMaxExclusive(),
                            partitionKeyRangeWrapper.resourceId,
                            GlobalPartitionEndpointManagerForCircuitBreaker.this.globalEndpointManager
                                .getRegionName(locationWithSuccess, OperationType.Read));
                        return this.transitionHealthStatus(LocationUnavailabilityStatus.StaleUnavailable);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unsupported health status: " + currentStatusSnapshot);
            }

            return locationSpecificContext;
        }

        public LocationSpecificContext handleException(
            LocationSpecificContext locationSpecificContext,
            PartitionKeyRangeWrapper partitionKeyRangeWrapper,
            URI locationWithException) {

            logger.warn("Handling exception");

            LocationUnavailabilityStatus currentStatusSnapshot = locationSpecificContext.locationUnavailabilityStatus;
            int allowedExceptionCount = getAllowedFailureCountByStatus(currentStatusSnapshot);

            int exceptionCountActual = locationSpecificContext.exceptionCount;

            switch (currentStatusSnapshot) {
                case Available:
                    if (exceptionCountActual < allowedExceptionCount) {
                        exceptionCountActual++;
                        logger.info("Exception count : {}", exceptionCountActual);
                        return new LocationSpecificContext(
                            locationSpecificContext.successCount,
                            exceptionCountActual,
                            locationSpecificContext.unavailableSince,
                            locationSpecificContext.locationUnavailabilityStatus,
                            locationSpecificContext.isExceptionThresholdBreached);
                    } else {
                        GlobalPartitionEndpointManagerForCircuitBreaker
                            .this.partitionsWithPossibleUnavailableRegions.put(partitionKeyRangeWrapper, partitionKeyRangeWrapper);
                        logger.info("Partition {}-{} of collection : {} marked as FreshUnavailable from Available for region : {}",
                            partitionKeyRangeWrapper.partitionKeyRange.getMinInclusive(),
                            partitionKeyRangeWrapper.partitionKeyRange.getMaxExclusive(),
                            partitionKeyRangeWrapper.resourceId,
                            GlobalPartitionEndpointManagerForCircuitBreaker.this.globalEndpointManager
                                .getRegionName(locationWithException, OperationType.Read));
                        return this.transitionHealthStatus(LocationUnavailabilityStatus.FreshUnavailable);
                    }
                case StaleUnavailable:
                    if (exceptionCountActual < allowedExceptionCount) {
                        exceptionCountActual++;
                        return new LocationSpecificContext(
                            locationSpecificContext.successCount,
                            exceptionCountActual,
                            locationSpecificContext.unavailableSince,
                            locationSpecificContext.locationUnavailabilityStatus,
                            locationSpecificContext.isExceptionThresholdBreached);
                    } else {
                        logger.info("Partition {}-{} of collection : {} marked as FreshUnavailable from StaleUnavailable for region : {}",
                            partitionKeyRangeWrapper.partitionKeyRange.getMinInclusive(),
                            partitionKeyRangeWrapper.partitionKeyRange.getMaxExclusive(),
                            partitionKeyRangeWrapper.resourceId,
                            GlobalPartitionEndpointManagerForCircuitBreaker.this.globalEndpointManager
                                .getRegionName(locationWithException, OperationType.Read));
                        return this.transitionHealthStatus(LocationUnavailabilityStatus.FreshUnavailable);
                    }
                default:
                    throw new IllegalStateException("Unsupported health status: " + currentStatusSnapshot);
            }
        }

        public LocationSpecificContext transitionHealthStatus(LocationUnavailabilityStatus newStatus) {

            switch (newStatus) {
                case Available:
                    return new LocationSpecificContext(
                        0,
                        0,
                        Instant.MAX,
                        LocationUnavailabilityStatus.Available,
                        false
                    );
                case FreshUnavailable:
                    return new LocationSpecificContext(
                        0,
                        0,
                        Instant.now(),
                        LocationUnavailabilityStatus.FreshUnavailable,
                        true
                    );
                case StaleUnavailable:
                    return new LocationSpecificContext(
                        0,
                        0,
                        Instant.MAX,
                        LocationUnavailabilityStatus.StaleUnavailable,
                        false
                    );
                default:
                    throw new IllegalStateException("Unsupported health status: " + newStatus);
            }
        }
    }

    public static class PartitionKeyRangeWrapper {
        final PartitionKeyRange partitionKeyRange;
        final String resourceId;

        public PartitionKeyRangeWrapper(PartitionKeyRange partitionKeyRange, String resourceId) {
            this.partitionKeyRange = partitionKeyRange;
            this.resourceId = resourceId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PartitionKeyRangeWrapper that = (PartitionKeyRangeWrapper) o;
            return Objects.equals(partitionKeyRange, that.partitionKeyRange) && Objects.equals(resourceId, that.resourceId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(partitionKeyRange, resourceId);
        }
    }

    private enum LocationUnavailabilityStatus {
        Available, FreshUnavailable, StaleUnavailable;
    }

    private static double getAllowedFailureRatioByStatus(LocationUnavailabilityStatus status) {
        switch (status) {
            case Available:
                return 0.3d;
            case StaleUnavailable:
                return 0.1d;
            default:
                return 0d;
        }
    }

    private static int getAllowedFailureCountByStatus(LocationUnavailabilityStatus status) {
        switch (status) {
            case Available:
                return 10;
            case StaleUnavailable:
                return 5;
            default:
                throw new IllegalStateException("Unsupported health status: " + status);
        }
    }
}
