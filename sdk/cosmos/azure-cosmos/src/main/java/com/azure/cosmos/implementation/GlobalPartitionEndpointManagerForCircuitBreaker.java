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

                isFailureThresholdBreached.set(partitionKeyRangeFailoverInfoAsVal.handleException(partitionKeyRangeWrapperAsKey, failedLocation, request.isReadOnlyRequest()));

                if (isFailureThresholdBreached.get()) {

                    UnmodifiableList<URI> applicableEndpoints = request.isReadOnlyRequest() ?
                        this.globalEndpointManager.getApplicableReadEndpoints(request.requestContext.getExcludeRegions()) :
                        this.globalEndpointManager.getApplicableWriteEndpoints(request.requestContext.getExcludeRegions());

                    isFailoverPossible.set(
                        partitionKeyRangeFailoverInfoAsVal.areLocationsAvailableForPartitionKeyRange(partitionKeyRangeWrapperAsKey, applicableEndpoints, request.isReadOnlyRequest()));
                }

                return partitionKeyRangeFailoverInfoAsVal;
            });
        } else {
            isFailureThresholdBreached.set(partitionLevelLocationUnavailabilityInfoSnapshot.handleException(partitionKeyRangeWrapper, failedLocation, request.isReadOnlyRequest()));

            if (isFailureThresholdBreached.get()) {

                UnmodifiableList<URI> applicableEndpoints = request.isReadOnlyRequest() ?
                    this.globalEndpointManager.getApplicableReadEndpoints(request.requestContext.getExcludeRegions()) :
                    this.globalEndpointManager.getApplicableWriteEndpoints(request.requestContext.getExcludeRegions());

                isFailoverPossible.set(
                    partitionLevelLocationUnavailabilityInfoSnapshot.areLocationsAvailableForPartitionKeyRange(
                        partitionKeyRangeWrapper,
                        applicableEndpoints,
                        request.isReadOnlyRequest()));
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
            partitionLevelLocationUnavailabilityInfoSnapshot.handleSuccess(
                partitionKeyRangeWrapper,
                succeededLocation,
                request.isReadOnlyRequest());
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
        boolean doesPartitionHaveUnhealthyLocations = false;

        if (partitionLevelLocationUnavailabilityInfoSnapshot != null) {
            Map<URI, LocationSpecificContext> locationEndpointToFailureMetricsForPartition =
                partitionLevelLocationUnavailabilityInfoSnapshot.locationEndpointToLocationSpecificContextForPartition;

            for (Map.Entry<URI, LocationSpecificContext> pair : locationEndpointToFailureMetricsForPartition.entrySet()) {
                URI location = pair.getKey();
                LocationSpecificContext locationSpecificContext = pair.getValue();

                if (locationSpecificContext.locationUnavailabilityStatus == LocationUnavailabilityStatus.FreshUnavailable) {
                    unavailableLocations.add(location);
                    doesPartitionHaveUnhealthyLocations = true;
                } else if (locationSpecificContext.locationUnavailabilityStatus == LocationUnavailabilityStatus.StaleUnavailable) {
                    doesPartitionHaveUnhealthyLocations = true;
                } else if (locationSpecificContext.exceptionCountForWrite >= 1 || locationSpecificContext.exceptionCountForRead >= 1) {
                    doesPartitionHaveUnhealthyLocations = true;
                }
            }
        }

        if (!doesPartitionHaveUnhealthyLocations) {
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
                                locationSpecificContextAsVal = GlobalPartitionEndpointManagerForCircuitBreaker
                                    .this.locationContextTransitionHandler.handleSuccess(
                                    locationSpecificContextAsVal,
                                    partitionKeyRangeWrapper,
                                    locationWithStaleUnavailabilityInfoAsKey,
                                    false,
                                    true);
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

        public boolean handleException(PartitionKeyRangeWrapper partitionKeyRangeWrapper, URI locationWithException, boolean isReadOnlyRequest) {

            AtomicBoolean isExceptionThresholdBreached = new AtomicBoolean(false);

            this.locationEndpointToLocationSpecificContextForPartition.compute(locationWithException, (locationAsKey, locationSpecificContextAsVal) -> {

                if (locationSpecificContextAsVal == null) {
                    locationSpecificContextAsVal = new LocationSpecificContext(
                        0,
                        0,
                        0,
                        0,
                        Instant.MAX,
                        LocationUnavailabilityStatus.Available,
                        false);
                }

                LocationSpecificContext locationSpecificContextAfterTransition = GlobalPartitionEndpointManagerForCircuitBreaker
                    .this.locationContextTransitionHandler.handleException(
                    locationSpecificContextAsVal,
                    partitionKeyRangeWrapper,
                    locationWithException,
                    isReadOnlyRequest);

                isExceptionThresholdBreached.set(locationSpecificContextAfterTransition.isExceptionThresholdBreached());
                return locationSpecificContextAfterTransition;
            });

            return isExceptionThresholdBreached.get();
        }

        public void handleSuccess(PartitionKeyRangeWrapper partitionKeyRangeWrapper, URI succeededLocation, boolean isReadOnlyRequest) {
            this.locationEndpointToLocationSpecificContextForPartition.compute(succeededLocation, (locationAsKey, locationSpecificContextAsVal) -> {

                if (locationSpecificContextAsVal != null) {
                    locationSpecificContextAsVal = GlobalPartitionEndpointManagerForCircuitBreaker
                        .this.locationContextTransitionHandler.handleSuccess(
                        locationSpecificContextAsVal,
                        partitionKeyRangeWrapper,
                        succeededLocation,
                        false,
                        isReadOnlyRequest);
                }

                return locationSpecificContextAsVal;
            });
        }

        public boolean areLocationsAvailableForPartitionKeyRange(PartitionKeyRangeWrapper partitionKeyRangeWrapper, List<URI> availableLocationsAtAccountLevel, boolean isReadOnlyRequest) {

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
                        locationSpecificStatusAsVal = GlobalPartitionEndpointManagerForCircuitBreaker
                            .this.locationContextTransitionHandler.handleSuccess(
                            locationSpecificStatusAsVal,
                            partitionKeyRangeWrapper,
                            mostStaleUnavailableLocationAsKey,
                            true,
                            isReadOnlyRequest);
                    }

                    return locationSpecificStatusAsVal;
                });
            }

            return false;
        }
    }

    private static class LocationSpecificContext {
        private final int exceptionCountForWrite;
        private final int successCountForWrite;
        private final int exceptionCountForRead;
        private final int successCountForRead;
        private final Instant unavailableSince;
        private final LocationUnavailabilityStatus locationUnavailabilityStatus;
        private final boolean isExceptionThresholdBreached;

        public LocationSpecificContext(
            int successCountForWrite,
            int exceptionCountForWrite,
            int successCountForRead,
            int exceptionCountForRead,
            Instant unavailableSince,
            LocationUnavailabilityStatus locationUnavailabilityStatus,
            boolean isExceptionThresholdBreached) {

            this.successCountForWrite = successCountForWrite;
            this.exceptionCountForWrite = exceptionCountForWrite;
            this.exceptionCountForRead = exceptionCountForRead;
            this.successCountForRead = successCountForRead;
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
            boolean forceStatusChange,
            boolean isReadOnlyRequest) {

            logger.info("Handling success");

            LocationUnavailabilityStatus currentStatusSnapshot = locationSpecificContext.locationUnavailabilityStatus;
            double allowedFailureRatio = getAllowedExceptionToSuccessRatio(currentStatusSnapshot, isReadOnlyRequest);

            int minSuccessCountForStatusUpgrade = getMinimumSuccessCountForStatusUpgrade(currentStatusSnapshot, isReadOnlyRequest);

            int exceptionCountActual = isReadOnlyRequest ? locationSpecificContext.exceptionCountForRead : locationSpecificContext.exceptionCountForWrite;
            int successCountActual = isReadOnlyRequest ? locationSpecificContext.exceptionCountForRead : locationSpecificContext.successCountForWrite;

            switch (currentStatusSnapshot) {
                case Available:
                    if (!forceStatusChange) {
                        if (exceptionCountActual > 0) {

                            exceptionCountActual -= 1;

                            if (isReadOnlyRequest) {
                                return new LocationSpecificContext(
                                    locationSpecificContext.successCountForWrite,
                                    locationSpecificContext.exceptionCountForWrite,
                                    locationSpecificContext.successCountForRead,
                                    exceptionCountActual,
                                    locationSpecificContext.unavailableSince,
                                    locationSpecificContext.locationUnavailabilityStatus,
                                    locationSpecificContext.isExceptionThresholdBreached);
                            } else {
                                return new LocationSpecificContext(
                                    locationSpecificContext.successCountForWrite,
                                    exceptionCountActual,
                                    locationSpecificContext.successCountForRead,
                                    locationSpecificContext.exceptionCountForRead,
                                    locationSpecificContext.unavailableSince,
                                    locationSpecificContext.locationUnavailabilityStatus,
                                    locationSpecificContext.isExceptionThresholdBreached);
                            }
                        }
                    }

                    break;
                case StaleUnavailable:
                    if (!forceStatusChange) {

                        successCountActual += 1;

                        logger.info("Try to switch to Available but actual success count : {}", successCountActual);

                        if (successCountActual > minSuccessCountForStatusUpgrade && (double) exceptionCountActual / (double) successCountActual < allowedFailureRatio) {
                            logger.info("Partition {}-{} of collection : {} marked as Available from StaleUnavailable for region : {}",
                                partitionKeyRangeWrapper.partitionKeyRange.getMinInclusive(),
                                partitionKeyRangeWrapper.partitionKeyRange.getMaxExclusive(),
                                partitionKeyRangeWrapper.resourceId,
                                GlobalPartitionEndpointManagerForCircuitBreaker.this.globalEndpointManager
                                    .getRegionName(locationWithSuccess, isReadOnlyRequest ? OperationType.Read : OperationType.Create));
                            return this.transitionHealthStatus(LocationUnavailabilityStatus.Available);
                        } else {

                            if (isReadOnlyRequest) {
                                return new LocationSpecificContext(
                                    locationSpecificContext.successCountForWrite,
                                    locationSpecificContext.exceptionCountForWrite,
                                    successCountActual,
                                    locationSpecificContext.exceptionCountForRead,
                                    locationSpecificContext.unavailableSince,
                                    locationSpecificContext.locationUnavailabilityStatus,
                                    locationSpecificContext.isExceptionThresholdBreached);
                            } else {
                                return new LocationSpecificContext(
                                    successCountActual,
                                    locationSpecificContext.exceptionCountForWrite,
                                    locationSpecificContext.successCountForRead,
                                    locationSpecificContext.exceptionCountForRead,
                                    locationSpecificContext.unavailableSince,
                                    locationSpecificContext.locationUnavailabilityStatus,
                                    locationSpecificContext.isExceptionThresholdBreached);
                            }
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
                                    .getRegionName(locationWithSuccess, isReadOnlyRequest ? OperationType.Read : OperationType.Create));

                            return this.transitionHealthStatus(LocationUnavailabilityStatus.StaleUnavailable);
                        }
                    } else {
                        logger.info("Partition {}-{} of collection : {} marked as StaleUnavailable from FreshAvailable for region : {}",
                            partitionKeyRangeWrapper.partitionKeyRange.getMinInclusive(),
                            partitionKeyRangeWrapper.partitionKeyRange.getMaxExclusive(),
                            partitionKeyRangeWrapper.resourceId,
                            GlobalPartitionEndpointManagerForCircuitBreaker.this.globalEndpointManager
                                .getRegionName(locationWithSuccess, isReadOnlyRequest ? OperationType.Read : OperationType.Create));
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
            URI locationWithException,
            boolean isReadOnlyRequest) {

            logger.warn("Handling exception");

            LocationUnavailabilityStatus currentStatusSnapshot = locationSpecificContext.locationUnavailabilityStatus;
            int allowedExceptionCount = getAllowedExceptionCount(currentStatusSnapshot, isReadOnlyRequest);

            int exceptionCountActual = isReadOnlyRequest ? locationSpecificContext.exceptionCountForRead : locationSpecificContext.exceptionCountForWrite;

            switch (currentStatusSnapshot) {
                case Available:
                    if (exceptionCountActual < allowedExceptionCount) {

                        exceptionCountActual += 1;

                        logger.info("Exception count : {}", exceptionCountActual);

                        if (isReadOnlyRequest) {
                            return new LocationSpecificContext(
                                locationSpecificContext.successCountForWrite,
                                locationSpecificContext.exceptionCountForWrite,
                                locationSpecificContext.successCountForRead,
                                exceptionCountActual,
                                locationSpecificContext.unavailableSince,
                                locationSpecificContext.locationUnavailabilityStatus,
                                locationSpecificContext.isExceptionThresholdBreached);
                        } else {
                            return new LocationSpecificContext(
                                locationSpecificContext.successCountForWrite,
                                exceptionCountActual,
                                locationSpecificContext.successCountForRead,
                                locationSpecificContext.exceptionCountForRead,
                                locationSpecificContext.unavailableSince,
                                locationSpecificContext.locationUnavailabilityStatus,
                                locationSpecificContext.isExceptionThresholdBreached);
                        }
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

                        exceptionCountActual += 1;

                        if (isReadOnlyRequest) {
                            return new LocationSpecificContext(
                                locationSpecificContext.successCountForWrite,
                                locationSpecificContext.exceptionCountForWrite,
                                locationSpecificContext.successCountForRead,
                                exceptionCountActual,
                                locationSpecificContext.unavailableSince,
                                locationSpecificContext.locationUnavailabilityStatus,
                                locationSpecificContext.isExceptionThresholdBreached);
                        } else {
                            return new LocationSpecificContext(
                                locationSpecificContext.successCountForWrite,
                                exceptionCountActual,
                                locationSpecificContext.successCountForRead,
                                locationSpecificContext.exceptionCountForRead,
                                locationSpecificContext.unavailableSince,
                                locationSpecificContext.locationUnavailabilityStatus,
                                locationSpecificContext.isExceptionThresholdBreached);
                        }
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
                        0,
                        0,
                        Instant.MAX,
                        LocationUnavailabilityStatus.Available,
                        false);
                case FreshUnavailable:
                    return new LocationSpecificContext(
                        0,
                        0,
                        0,
                        0,
                        Instant.now(),
                        LocationUnavailabilityStatus.FreshUnavailable,
                        true);
                case StaleUnavailable:
                    return new LocationSpecificContext(
                        0,
                        0,
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

    private static double getAllowedExceptionToSuccessRatio(LocationUnavailabilityStatus status, boolean isReadOnlyRequest) {

        if (isReadOnlyRequest) {
            switch (status) {
                case Available:
                    return 0.3d;
                case StaleUnavailable:
                    return 0.1d;
                default:
                    return 0d;
            }
        } else {
            switch (status) {
                case Available:
                    return 0.2d;
                case StaleUnavailable:
                    return 0.05d;
                default:
                    return 0d;
            }
        }
    }

    private static int getAllowedExceptionCount(LocationUnavailabilityStatus status, boolean isReadOnlyRequest) {

        if (isReadOnlyRequest) {
            switch (status) {
                case Available:
                    return 10;
                case StaleUnavailable:
                    return 5;
                default:
                    throw new IllegalStateException("Unsupported health status: " + status);
            }
        } else {
            switch (status) {
                case Available:
                    return 5;
                case StaleUnavailable:
                    return 2;
                default:
                    throw new IllegalStateException("Unsupported health status: " + status);
            }
        }
    }

    private static int getMinimumSuccessCountForStatusUpgrade(LocationUnavailabilityStatus status, boolean isReadOnlyRequest) {
        if (isReadOnlyRequest) {
            switch (status) {
                case StaleUnavailable:
                    return 5;
                case FreshUnavailable:
                case Available:
                    return 0;
                default:
                    throw new IllegalStateException("Unsupported health status: " + status);
            }
        } else {
            switch (status) {
                case StaleUnavailable:
                    return 10;
                case FreshUnavailable:
                case Available:
                    return 0;
                default:
                    throw new IllegalStateException("Unsupported health status: " + status);
            }
        }
    }
}
