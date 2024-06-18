// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.circuitBreaker;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ResourceType;
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
    private final ConsecutiveExceptionBasedCircuitBreaker consecutiveExceptionBasedCircuitBreaker;

    public GlobalPartitionEndpointManagerForCircuitBreaker(GlobalEndpointManager globalEndpointManager) {
        this.partitionKeyRangeToLocationSpecificUnavailabilityInfo = new ConcurrentHashMap<>();
        this.partitionsWithPossibleUnavailableRegions = new ConcurrentHashMap<>();
        this.globalEndpointManager = globalEndpointManager;

        PartitionLevelCircuitBreakerConfig partitionLevelCircuitBreakerConfig = Configs.getPartitionLevelCircuitBreakerConfig();
        this.consecutiveExceptionBasedCircuitBreaker = new ConsecutiveExceptionBasedCircuitBreaker(partitionLevelCircuitBreakerConfig);
        this.locationContextTransitionHandler = new LocationContextTransitionHandler(this.globalEndpointManager, this.consecutiveExceptionBasedCircuitBreaker);
    }

    public void init() {
        this.updateStaleLocationInfo().subscribeOn(CosmosSchedulers.PARTITION_AVAILABILITY_STALENESS_CHECK_SINGLE).subscribe();
    }

    public void handleLocationExceptionForPartitionKeyRange(RxDocumentServiceRequest request, URI failedLocation) {

        checkNotNull(request, "Argument 'request' cannot be null!");
        checkNotNull(request.requestContext, "Argument 'request.requestContext' cannot be null!");

        PartitionKeyRange partitionKeyRange = request.requestContext.resolvedPartitionKeyRange;

        checkNotNull(request.requestContext.resolvedPartitionKeyRange, "Argument 'request.requestContext.resolvedPartitionKeyRange' cannot be null!");

        String collectionResourceId = request.getResourceId();
        checkNotNull(collectionResourceId, "Argument 'collectionResourceId' cannot be null!");

        PartitionKeyRangeWrapper partitionKeyRangeWrapper = new PartitionKeyRangeWrapper(partitionKeyRange, collectionResourceId);

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

            request.requestContext.setRegionToHealthStatusesForPartitionKeyRange(partitionLevelLocationUnavailabilityInfoAsVal.regionToHealthStatus);
            return partitionLevelLocationUnavailabilityInfoAsVal;
        });

        // set to true if and only if failure threshold exceeded for the region
        // and if failover is possible
        // a failover is only possible when there are available regions left to fail over to
        if (isFailoverPossible.get()) {
            return;
        }

        if (logger.isWarnEnabled()) {
            logger.warn("It is not possible to mark region {} as Unavailable for partition key range {}-{} and collection rid {} " +
                    "as all regions will be Unavailable in that case, will remove health status tracking for this partition!",
                this.globalEndpointManager.getRegionName(
                    failedLocation, request.isReadOnlyRequest() ? OperationType.Read : OperationType.Create),
                partitionKeyRange.getMinInclusive(),
                partitionKeyRange.getMaxExclusive(),
                collectionResourceId);
        }

        // no regions to fail over to
        this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.remove(partitionKeyRangeWrapper);
    }

    public void handleLocationSuccessForPartitionKeyRange(RxDocumentServiceRequest request) {

        checkNotNull(request, "Argument 'request' cannot be null!");
        checkNotNull(request.requestContext, "Argument 'request.requestContext' cannot be null!");

        PartitionKeyRange partitionKeyRange = request.requestContext.resolvedPartitionKeyRange;

        checkNotNull(request.requestContext.resolvedPartitionKeyRange, "Argument 'request.requestContext.resolvedPartitionKeyRange' cannot be null!");

        String resourceId = request.getResourceId();

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

            request.requestContext.setRegionToHealthStatusesForPartitionKeyRange(partitionKeyRangeToFailoverInfoAsVal.regionToHealthStatus);
            return partitionKeyRangeToFailoverInfoAsVal;
        });
    }

    public List<URI> getUnavailableLocationEndpointsForPartitionKeyRange(String collectionResourceId, PartitionKeyRange partitionKeyRange) {

        checkNotNull(partitionKeyRange, "Argument 'partitionKeyRange' cannot be null!");
        checkNotNull(collectionResourceId, "Argument 'collectionResourceId' cannot be null!");

        PartitionKeyRangeWrapper partitionKeyRangeWrapper = new PartitionKeyRangeWrapper(partitionKeyRange, collectionResourceId);

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

        if (request.getResourceType() != ResourceType.Document) {
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
        private final ConcurrentHashMap<String, String> regionToHealthStatus;
        private final LocationContextTransitionHandler locationContextTransitionHandler;

        private PartitionLevelLocationUnavailabilityInfo() {
            this.locationEndpointToLocationSpecificContextForPartition = new ConcurrentHashMap<>();
            this.regionToHealthStatus = new ConcurrentHashMap<>();
            this.locationContextTransitionHandler = GlobalPartitionEndpointManagerForCircuitBreaker.this.locationContextTransitionHandler;
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

                LocationSpecificContext locationSpecificContextAfterTransition = this.locationContextTransitionHandler.handleException(
                    locationSpecificContextAsVal,
                    partitionKeyRangeWrapper,
                    GlobalPartitionEndpointManagerForCircuitBreaker.this.partitionsWithPossibleUnavailableRegions,
                    locationWithException,
                    isReadOnlyRequest);

                this.regionToHealthStatus.put(
                    GlobalPartitionEndpointManagerForCircuitBreaker
                        .this.globalEndpointManager
                        .getRegionName(locationAsKey, isReadOnlyRequest ? OperationType.Read : OperationType.Create),
                    locationSpecificContextAfterTransition.getLocationHealthStatus().getStringifiedLocationHealthStatus());

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

                locationSpecificContextAfterTransition = this.locationContextTransitionHandler.handleSuccess(
                    locationSpecificContextAsVal,
                    partitionKeyRangeWrapper,
                    succeededLocation,
                    false,
                    isReadOnlyRequest);

                this.regionToHealthStatus.put(
                    GlobalPartitionEndpointManagerForCircuitBreaker
                        .this.globalEndpointManager
                        .getRegionName(locationAsKey, isReadOnlyRequest ? OperationType.Read : OperationType.Create),
                    locationSpecificContextAfterTransition.getLocationHealthStatus().getStringifiedLocationHealthStatus());

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
                        locationSpecificStatusAsVal = this.locationContextTransitionHandler.handleSuccess(
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

        public ConcurrentHashMap<String, String> getRegionToHealthStatus() {
            return regionToHealthStatus;
        }
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

    public PartitionLevelCircuitBreakerConfig getCircuitBreakerConfig() {
        return this.consecutiveExceptionBasedCircuitBreaker.getPartitionLevelCircuitBreakerConfig();
    }
}
