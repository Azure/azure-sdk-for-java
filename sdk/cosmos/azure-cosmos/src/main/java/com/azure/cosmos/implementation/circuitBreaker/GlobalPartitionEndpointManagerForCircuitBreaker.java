// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.circuitBreaker;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class GlobalPartitionEndpointManagerForCircuitBreaker {

    private static final Logger logger = LoggerFactory.getLogger(GlobalPartitionEndpointManagerForCircuitBreaker.class);

    private final GlobalEndpointManager globalEndpointManager;
    private final ConcurrentHashMap<PartitionKeyRangeWrapper, PartitionLevelLocationUnavailabilityInfo> partitionKeyRangeToLocationSpecificUnavailabilityInfo;
    private final ConcurrentHashMap<PartitionKeyRangeWrapper, PartitionKeyRangeWrapper> partitionsWithPossibleUnavailableRegions;
    private final LocationContextTransitionHandler locationContextTransitionHandler;
    private ConsecutiveExceptionBasedCircuitBreaker consecutiveExceptionBasedCircuitBreaker;

    public GlobalPartitionEndpointManagerForCircuitBreaker(GlobalEndpointManager globalEndpointManager) {
        this.partitionKeyRangeToLocationSpecificUnavailabilityInfo = new ConcurrentHashMap<>();
        this.partitionsWithPossibleUnavailableRegions = new ConcurrentHashMap<>();
        this.globalEndpointManager = globalEndpointManager;
        this.locationContextTransitionHandler = new LocationContextTransitionHandler();

        PartitionLevelCircuitBreakerConfig partitionLevelCircuitBreakerConfig = Configs.getPartitionLevelCircuitBreakerConfig();

        if (partitionLevelCircuitBreakerConfig.getCircuitBreakerType().equals("COUNT_BASED")) {
            this.consecutiveExceptionBasedCircuitBreaker = new ConsecutiveExceptionBasedCircuitBreaker(partitionLevelCircuitBreakerConfig);
        }
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

        this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.compute(partitionKeyRangeWrapper, (partitionKeyRangeWrapperAsKey, partitionLevelLocationUnavailabilityInfoAsVal) -> {

            if (partitionLevelLocationUnavailabilityInfoAsVal == null) {
                partitionLevelLocationUnavailabilityInfoAsVal = new PartitionLevelLocationUnavailabilityInfo();
            }

            isFailureThresholdBreached.set(partitionLevelLocationUnavailabilityInfoAsVal.handleException(partitionKeyRangeWrapperAsKey, failedLocation, request.isReadOnlyRequest()));

            if (isFailureThresholdBreached.get()) {

                UnmodifiableList<URI> applicableEndpoints = request.isReadOnlyRequest() ?
                    this.globalEndpointManager.getApplicableReadEndpoints(request.requestContext.getExcludeRegions()) :
                    this.globalEndpointManager.getApplicableWriteEndpoints(request.requestContext.getExcludeRegions());

                isFailoverPossible.set(
                    partitionLevelLocationUnavailabilityInfoAsVal.areLocationsAvailableForPartitionKeyRange(partitionKeyRangeWrapperAsKey, applicableEndpoints, request.isReadOnlyRequest()));
            }

            return partitionLevelLocationUnavailabilityInfoAsVal;
        });

        // set to true if and only if failure threshold exceeded for the region
        // and if failover is possible
        // a failover is only possible when there are available regions left to fail over to
        if (isFailoverPossible.get()) {
            return;
        }

        // no regions to fail over to
        this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.remove(partitionKeyRangeWrapper);
    }

    public void handleLocationSuccessForPartitionKeyRange(RxDocumentServiceRequest request) {

        checkNotNull(request, "request cannot be null!");
        checkNotNull(request.requestContext, "requestContext cannot be null!");

        PartitionKeyRange partitionKeyRange = request.requestContext.resolvedPartitionKeyRange;

        if (partitionKeyRange == null) {
            return;
        }

        String resourceId = request.getResourceId();
//        logger.info("Handling success : {}", resourceId);

        PartitionKeyRangeWrapper partitionKeyRangeWrapper = new PartitionKeyRangeWrapper(partitionKeyRange, resourceId);
        URI succeededLocation = request.requestContext.locationEndpointToRoute;

        this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.compute(partitionKeyRangeWrapper, (partitionKeyRangeWrapperAsKey, partitionKeyRangeToFailoverInfoAsVal) -> {

            if (partitionKeyRangeToFailoverInfoAsVal == null) {
                partitionKeyRangeToFailoverInfoAsVal = new PartitionLevelLocationUnavailabilityInfo();
            }

            partitionKeyRangeToFailoverInfoAsVal.handleSuccess(
                partitionKeyRangeWrapper,
                succeededLocation,
                request.isReadOnlyRequest());

            return partitionKeyRangeToFailoverInfoAsVal;
        });
    }

    public List<URI> getUnavailableLocationEndpointsForPartitionKeyRange(String resourceId, PartitionKeyRange partitionKeyRange) {

        checkNotNull(partitionKeyRange, "Supplied partitionKeyRange cannot be null!");
        checkNotNull(resourceId, "Supplied resourceId cannot be null!");

//        logger.info("Fetching unavailable regions for resource address : {}", resourceId);

        PartitionKeyRangeWrapper partitionKeyRangeWrapper = new PartitionKeyRangeWrapper(partitionKeyRange, resourceId);

        PartitionLevelLocationUnavailabilityInfo partitionLevelLocationUnavailabilityInfoSnapshot =
            this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.get(partitionKeyRangeWrapper);

        List<URI> unavailableLocations = new ArrayList<>();

        if (partitionLevelLocationUnavailabilityInfoSnapshot != null) {
            Map<URI, LocationSpecificContext> locationEndpointToFailureMetricsForPartition =
                partitionLevelLocationUnavailabilityInfoSnapshot.locationEndpointToLocationSpecificContextForPartition;

            for (Map.Entry<URI, LocationSpecificContext> pair : locationEndpointToFailureMetricsForPartition.entrySet()) {
                URI location = pair.getKey();
                LocationSpecificContext locationSpecificContext = pair.getValue();

                if (locationSpecificContext.getLocationHealthStatus() == LocationHealthStatus.Unavailable) {
                    unavailableLocations.add(location);
                }
            }
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

                PartitionLevelLocationUnavailabilityInfo partitionLevelLocationUnavailabilityInfo = this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.get(partitionKeyRangeWrapper);

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

    public boolean isPartitionLevelCircuitBreakingApplicable(RxDocumentServiceRequest request) {

        if (!this.consecutiveExceptionBasedCircuitBreaker.isPartitionLevelCircuitBreakerEnabled()) {
            return false;
        }

        GlobalEndpointManager globalEndpointManager = this.globalEndpointManager;

        if (!globalEndpointManager.canUseMultipleWriteLocations(request)) {
            return false;
        }

        UnmodifiableList<URI> applicableWriteEndpoints = globalEndpointManager.getApplicableWriteEndpoints(Collections.emptyList());

        return applicableWriteEndpoints != null && applicableWriteEndpoints.size() > 1;
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
                        LocationHealthStatus.HealthyWithFailures,
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

                LocationSpecificContext locationSpecificContextAfterTransition;

                if (locationSpecificContextAsVal == null) {
                    locationSpecificContextAsVal = new LocationSpecificContext(
                        0,
                        0,
                        0,
                        0,
                        Instant.MAX,
                        LocationHealthStatus.Healthy,
                        false);
                }

                locationSpecificContextAfterTransition = GlobalPartitionEndpointManagerForCircuitBreaker
                    .this.locationContextTransitionHandler.handleSuccess(
                    locationSpecificContextAsVal,
                    partitionKeyRangeWrapper,
                    succeededLocation,
                    false,
                    isReadOnlyRequest);

                return locationSpecificContextAfterTransition;
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

            Instant mostHealthyTentativeTimeAcrossRegions = Instant.MAX;
            LocationSpecificContext locationLevelFailureMetadataForMostStaleLocation = null;
            URI mostHealthyTentativeLocation = null;

            // find region with most 'stale' unavailability
            for (Map.Entry<URI, LocationSpecificContext> uriToLocationLevelFailureMetadata : this.locationEndpointToLocationSpecificContextForPartition.entrySet()) {
                LocationSpecificContext locationSpecificContext = uriToLocationLevelFailureMetadata.getValue();

                if (locationSpecificContext.isRegionAvailableToProcessRequests()) {
                    return true;
                }

                Instant unavailableSinceSnapshot = locationSpecificContext.getUnavailableSince();

                if (mostHealthyTentativeTimeAcrossRegions.isAfter(unavailableSinceSnapshot)) {
                    mostHealthyTentativeTimeAcrossRegions = unavailableSinceSnapshot;
                    mostHealthyTentativeLocation = uriToLocationLevelFailureMetadata.getKey();
                    locationLevelFailureMetadataForMostStaleLocation = locationSpecificContext;
                }
            }

            if (locationLevelFailureMetadataForMostStaleLocation != null) {
                this.locationEndpointToLocationSpecificContextForPartition.compute(mostHealthyTentativeLocation, (mostHealthyTentativeLocationAsKey, locationSpecificStatusAsVal) -> {

                    if (locationSpecificStatusAsVal != null) {
                        locationSpecificStatusAsVal = GlobalPartitionEndpointManagerForCircuitBreaker
                            .this.locationContextTransitionHandler.handleSuccess(
                            locationSpecificStatusAsVal,
                            partitionKeyRangeWrapper,
                            mostHealthyTentativeLocationAsKey,
                            true,
                            isReadOnlyRequest);
                    }

                    return locationSpecificStatusAsVal;
                });
            }

            return false;
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

            LocationHealthStatus currentStatusSnapshot = locationSpecificContext.getLocationHealthStatus();

            int exceptionCountActual = isReadOnlyRequest ? locationSpecificContext.getExceptionCountForRead() : locationSpecificContext.getExceptionCountForWrite();
            int successCountActual = isReadOnlyRequest ? locationSpecificContext.getSuccessCountForRead() : locationSpecificContext.getSuccessCountForRead();

            switch (currentStatusSnapshot) {
                case Healthy:
                    break;
                case HealthyWithFailures:
                    if (!forceStatusChange) {
                        if (exceptionCountActual > 0) {
                            return GlobalPartitionEndpointManagerForCircuitBreaker
                                .this.consecutiveExceptionBasedCircuitBreaker.handleSuccess(locationSpecificContext, isReadOnlyRequest);
                        }
                    }
                    break;

                case HealthyTentative:
                    if (!forceStatusChange) {

                        LocationSpecificContext locationSpecificContextInner
                            = GlobalPartitionEndpointManagerForCircuitBreaker.this.consecutiveExceptionBasedCircuitBreaker.handleSuccess(locationSpecificContext, isReadOnlyRequest);

                        logger.info("Try to switch to Healthy but actual success count : {}", successCountActual);

                        if (GlobalPartitionEndpointManagerForCircuitBreaker.this.consecutiveExceptionBasedCircuitBreaker.canHealthStatusBeUpgraded(locationSpecificContextInner, isReadOnlyRequest)) {
                            logger.info("Partition {}-{} of collection : {} marked as Healthy from HealthyTentative for region : {}",
                                partitionKeyRangeWrapper.partitionKeyRange.getMinInclusive(),
                                partitionKeyRangeWrapper.partitionKeyRange.getMaxExclusive(),
                                partitionKeyRangeWrapper.resourceId,
                                GlobalPartitionEndpointManagerForCircuitBreaker.this.globalEndpointManager
                                    .getRegionName(locationWithSuccess, (isReadOnlyRequest) ? OperationType.Read : OperationType.Create));
                            return this.transitionHealthStatus(LocationHealthStatus.Healthy);
                        } else {
                            return locationSpecificContextInner;
                        }
                    }
                    break;
                case Unavailable:
                    Instant unavailableSinceActual = locationSpecificContext.getUnavailableSince();
                    if (!forceStatusChange) {
                        if (Duration.between(unavailableSinceActual, Instant.now()).compareTo(Duration.ofSeconds(30)) > 0) {

                            // todo: make debug
                            logger.info("Partition {}-{} of collection : {} marked as HealthyTentative from Unavailable for region : {}",
                                partitionKeyRangeWrapper.partitionKeyRange.getMinInclusive(),
                                partitionKeyRangeWrapper.partitionKeyRange.getMaxExclusive(),
                                partitionKeyRangeWrapper.resourceId,
                                GlobalPartitionEndpointManagerForCircuitBreaker.this.globalEndpointManager
                                    .getRegionName(locationWithSuccess, (isReadOnlyRequest) ? OperationType.Read : OperationType.Create));

                            return this.transitionHealthStatus(LocationHealthStatus.HealthyTentative);
                        }
                    } else {
                        logger.info("Partition {}-{} of collection : {} marked as HealthyTentative from Unavailable for region : {}",
                            partitionKeyRangeWrapper.partitionKeyRange.getMinInclusive(),
                            partitionKeyRangeWrapper.partitionKeyRange.getMaxExclusive(),
                            partitionKeyRangeWrapper.resourceId,
                            GlobalPartitionEndpointManagerForCircuitBreaker.this.globalEndpointManager
                                .getRegionName(locationWithSuccess, (isReadOnlyRequest) ? OperationType.Read : OperationType.Create));
                        return this.transitionHealthStatus(LocationHealthStatus.HealthyTentative);
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

            LocationHealthStatus currentStatusSnapshot = locationSpecificContext.getLocationHealthStatus();

            switch (currentStatusSnapshot) {
                case Healthy:
                    logger.info("Partition {}-{} of collection : {} marked as HealthyWithFailures from Healthy for region : {}",
                        partitionKeyRangeWrapper.partitionKeyRange.getMinInclusive(),
                        partitionKeyRangeWrapper.partitionKeyRange.getMaxExclusive(),
                        partitionKeyRangeWrapper.resourceId,
                        GlobalPartitionEndpointManagerForCircuitBreaker.this.globalEndpointManager
                            .getRegionName(locationWithException, (isReadOnlyRequest) ? OperationType.Read : OperationType.Create));
                    return this.transitionHealthStatus(LocationHealthStatus.HealthyWithFailures);
                case HealthyWithFailures:
                    if (!GlobalPartitionEndpointManagerForCircuitBreaker.this.consecutiveExceptionBasedCircuitBreaker.shouldHealthStatusBeDowngraded(locationSpecificContext, isReadOnlyRequest)) {

                        LocationSpecificContext locationSpecificContextInner = GlobalPartitionEndpointManagerForCircuitBreaker
                            .this.consecutiveExceptionBasedCircuitBreaker.handleException(locationSpecificContext, isReadOnlyRequest);

                        logger.info("Partition {}-{} of collection : {} has exception count of {} for region : {}",
                            partitionKeyRangeWrapper.partitionKeyRange.getMinInclusive(),
                            partitionKeyRangeWrapper.partitionKeyRange.getMaxExclusive(),
                            partitionKeyRangeWrapper.resourceId,
                            isReadOnlyRequest ? locationSpecificContextInner.getExceptionCountForRead() : locationSpecificContextInner.getExceptionCountForWrite(),
                            GlobalPartitionEndpointManagerForCircuitBreaker.this.globalEndpointManager
                                .getRegionName(locationWithException, (isReadOnlyRequest) ? OperationType.Read : OperationType.Create));

                        return locationSpecificContextInner;
                    } else {
                        GlobalPartitionEndpointManagerForCircuitBreaker
                            .this.partitionsWithPossibleUnavailableRegions.put(partitionKeyRangeWrapper, partitionKeyRangeWrapper);
                        logger.info("Partition {}-{} of collection : {} marked as Unavailable from HealthyWithFailures for region : {}",
                            partitionKeyRangeWrapper.partitionKeyRange.getMinInclusive(),
                            partitionKeyRangeWrapper.partitionKeyRange.getMaxExclusive(),
                            partitionKeyRangeWrapper.resourceId,
                            GlobalPartitionEndpointManagerForCircuitBreaker.this.globalEndpointManager
                                .getRegionName(locationWithException, (isReadOnlyRequest) ? OperationType.Read : OperationType.Create));
                        return this.transitionHealthStatus(LocationHealthStatus.Unavailable);
                    }
                case HealthyTentative:
                    if (!GlobalPartitionEndpointManagerForCircuitBreaker.this.consecutiveExceptionBasedCircuitBreaker.shouldHealthStatusBeDowngraded(locationSpecificContext, isReadOnlyRequest)) {

                        return GlobalPartitionEndpointManagerForCircuitBreaker.this.consecutiveExceptionBasedCircuitBreaker.handleException(locationSpecificContext, isReadOnlyRequest);
                    } else {
                        logger.info("Partition {}-{} of collection : {} marked as Unavailable from HealthyTentative for region : {}",
                            partitionKeyRangeWrapper.partitionKeyRange.getMinInclusive(),
                            partitionKeyRangeWrapper.partitionKeyRange.getMaxExclusive(),
                            partitionKeyRangeWrapper.resourceId,
                            GlobalPartitionEndpointManagerForCircuitBreaker.this.globalEndpointManager
                                .getRegionName(locationWithException, (isReadOnlyRequest) ? OperationType.Read : OperationType.Create));
                        return this.transitionHealthStatus(LocationHealthStatus.Unavailable);
                    }
                default:
                    throw new IllegalStateException("Unsupported health status: " + currentStatusSnapshot);
            }
        }

        public LocationSpecificContext transitionHealthStatus(LocationHealthStatus newStatus) {

            switch (newStatus) {
                case Healthy:
                    return new LocationSpecificContext(
                        0,
                        0,
                        0,
                        0,
                        Instant.MAX,
                        LocationHealthStatus.Healthy,
                        false);
                case HealthyWithFailures:
                    return new LocationSpecificContext(
                        0,
                        0,
                        0,
                        0,
                        Instant.MAX,
                        LocationHealthStatus.HealthyWithFailures,
                        false);
                case Unavailable:
                    return new LocationSpecificContext(
                        0,
                        0,
                        0,
                        0,
                        Instant.now(),
                        LocationHealthStatus.Unavailable,
                        true);
                case HealthyTentative:
                    return new LocationSpecificContext(
                        0,
                        0,
                        0,
                        0,
                        Instant.MAX,
                        LocationHealthStatus.HealthyTentative,
                        false);
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

    // todo (abhmohanty): does this need to be public
    public enum LocationHealthStatus {
        Healthy, HealthyWithFailures, Unavailable, HealthyTentative
    }

    // todo: keep private and access through reflection
    public int getExceptionCountByPartitionKeyRange(PartitionKeyRangeWrapper partitionKeyRangeWrapper) {

        PartitionLevelLocationUnavailabilityInfo partitionLevelLocationUnavailabilityInfoSnapshot =
            this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.get(partitionKeyRangeWrapper);

        int count = 0;
        int regionCountWithFailures = 0;
        boolean failuresExist = false;

        for (LocationSpecificContext locationSpecificContext
            : partitionLevelLocationUnavailabilityInfoSnapshot.locationEndpointToLocationSpecificContextForPartition.values()) {
            count += locationSpecificContext.getExceptionCountForRead() + locationSpecificContext.getExceptionCountForWrite();

            if (locationSpecificContext.getExceptionCountForRead() + locationSpecificContext.getExceptionCountForWrite() > 0) {
                failuresExist = true;
                regionCountWithFailures++;
            }
        }

        if (failuresExist) {
            return count / regionCountWithFailures;
        }

        return 0;
    }

    // todo: keep private and access through reflection
    public Map<URI, LocationSpecificContext> getLocationToLocationSpecificContextMappings(PartitionKeyRangeWrapper partitionKeyRangeWrapper) {
        PartitionLevelLocationUnavailabilityInfo partitionLevelLocationUnavailabilityInfoSnapshot =
            this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.get(partitionKeyRangeWrapper);

        if (partitionLevelLocationUnavailabilityInfoSnapshot != null) {
            return partitionLevelLocationUnavailabilityInfoSnapshot.locationEndpointToLocationSpecificContextForPartition;
        }

        return null;
    }

    public ConsecutiveExceptionBasedCircuitBreaker getConsecutiveExceptionBasedCircuitBreaker() {
        return this.consecutiveExceptionBasedCircuitBreaker;
    }
}
