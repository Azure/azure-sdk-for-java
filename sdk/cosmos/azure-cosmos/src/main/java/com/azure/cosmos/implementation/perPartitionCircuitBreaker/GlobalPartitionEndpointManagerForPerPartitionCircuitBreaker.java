// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionCircuitBreaker;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.PartitionKeyRangeWrapper;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.directconnectivity.GatewayAddressCache;
import com.azure.cosmos.implementation.directconnectivity.GlobalAddressResolver;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class);
    private static final Map<String, String> EMPTY_MAP = new HashMap<>();
    private static final String BASE_EXCEPTION_MESSAGE = "FAILED IN Per-Partition Circuit Breaker: ";

    private final GlobalEndpointManager globalEndpointManager;
    private final ConcurrentHashMap<PartitionKeyRangeWrapper, PartitionLevelLocationUnavailabilityInfo> partitionKeyRangeToLocationSpecificUnavailabilityInfo;
    private LocationSpecificHealthContextTransitionHandler locationSpecificHealthContextTransitionHandler;
    private ConsecutiveExceptionBasedCircuitBreaker consecutiveExceptionBasedCircuitBreaker;
    private final AtomicReference<GlobalAddressResolver> globalAddressResolverSnapshot;
    private final ConcurrentHashMap<RegionalRoutingContext, String> regionalRoutingContextToRegion;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final Scheduler partitionRecoveryScheduler = Schedulers.newSingle(
        "partition-availability-staleness-check",
        true);

    public GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker(GlobalEndpointManager globalEndpointManager) {
        this.partitionKeyRangeToLocationSpecificUnavailabilityInfo = new ConcurrentHashMap<>();
        this.globalEndpointManager = globalEndpointManager;

        PartitionLevelCircuitBreakerConfig partitionLevelCircuitBreakerConfig = Configs.getPartitionLevelCircuitBreakerConfig();
        this.consecutiveExceptionBasedCircuitBreaker = new ConsecutiveExceptionBasedCircuitBreaker(partitionLevelCircuitBreakerConfig);
        this.locationSpecificHealthContextTransitionHandler
            = new LocationSpecificHealthContextTransitionHandler(this.consecutiveExceptionBasedCircuitBreaker);
        this.globalAddressResolverSnapshot = new AtomicReference<>();
        this.regionalRoutingContextToRegion = new ConcurrentHashMap<>();
    }

    public void init() {
        if (this.consecutiveExceptionBasedCircuitBreaker.isPartitionLevelCircuitBreakerEnabled()) {
            this.updateStaleLocationInfo().subscribeOn(this.partitionRecoveryScheduler).subscribe();
        }
    }

    public void handleLocationExceptionForPartitionKeyRange(
        RxDocumentServiceRequest request,
        RegionalRoutingContext failedRegionalRoutingContext,
        boolean isCancellationException) {

        try {
            checkNotNull(request, "Argument 'request' cannot be null!");
            checkNotNull(request.requestContext, "Argument 'request.requestContext' cannot be null!");

            PartitionKeyRange resolvedPartitionKeyRangeForCircuitBreaker = request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker;
            PartitionKeyRange resolvedPartitionKeyRange = request.requestContext.resolvedPartitionKeyRange;

            // in scenarios where partition is splitting or invalid partition then resolvedPartitionKeyRange could be set to null
            // no reason to circuit break a partition key range which is effectively won't be used in the future
            if (resolvedPartitionKeyRangeForCircuitBreaker != null && resolvedPartitionKeyRange == null) {
                return;
            }

            // in scenarios where resolvedPartitionKeyRangeForCircuitBreaker is null, we cannot circuit break at partition level
            // if the exception is due to a cancellation, then we don't have enough information to decide if we should circuit break or not
            // so we skip circuit breaking in this case if cancellation kick in ungracefully (e.g. user cancelled the request, or end-to-end timeout on the operation before routing decision is made)
            // if the exception is not due to a cancellation, then we should have enough information to decide if we should circuit break or not
            // so we proceed with circuit breaking in this case
            if (resolvedPartitionKeyRangeForCircuitBreaker == null && isCancellationException) {
                return;
            }

            checkNotNull(request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker, "Argument 'request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker' cannot be null!");

            String collectionResourceId = request.getResourceId();
            checkNotNull(request, collectionResourceId, "Argument 'collectionResourceId' cannot be null!");

            PartitionKeyRangeWrapper partitionKeyRangeWrapper = new PartitionKeyRangeWrapper(resolvedPartitionKeyRangeForCircuitBreaker, collectionResourceId);

            AtomicBoolean isFailoverPossible = new AtomicBoolean(true);
            AtomicBoolean isFailureThresholdBreached = new AtomicBoolean(false);

            this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.compute(partitionKeyRangeWrapper, (partitionKeyRangeWrapperAsKey, partitionLevelLocationUnavailabilityInfoAsVal) -> {

                if (partitionLevelLocationUnavailabilityInfoAsVal == null) {
                    partitionLevelLocationUnavailabilityInfoAsVal = new PartitionLevelLocationUnavailabilityInfo();
                }

                isFailureThresholdBreached.set(partitionLevelLocationUnavailabilityInfoAsVal.handleException(
                    partitionKeyRangeWrapperAsKey,
                    failedRegionalRoutingContext,
                    request.isReadOnlyRequest()));

                if (isFailureThresholdBreached.get()) {

                    UnmodifiableList<RegionalRoutingContext> applicableRegionalRoutingContexts = request.isReadOnlyRequest() ?
                        this.globalEndpointManager.getApplicableReadRegionalRoutingContexts(request.requestContext.getExcludeRegions()) :
                        this.globalEndpointManager.getApplicableWriteRegionalRoutingContexts(request.requestContext.getExcludeRegions());

                    isFailoverPossible.set(
                        partitionLevelLocationUnavailabilityInfoAsVal.areLocationsAvailableForPartitionKeyRange(applicableRegionalRoutingContexts));
                }

                request.requestContext.setPerPartitionCircuitBreakerInfoHolder(partitionLevelLocationUnavailabilityInfoAsVal.regionToLocationSpecificHealthContext);
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
                        failedRegionalRoutingContext.getGatewayRegionalEndpoint(), request.isReadOnlyRequest() ? OperationType.Read : OperationType.Create),
                    resolvedPartitionKeyRangeForCircuitBreaker.getMinInclusive(),
                    resolvedPartitionKeyRangeForCircuitBreaker.getMaxExclusive(),
                    collectionResourceId);
            }

            // no regions to fail over to
            this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.remove(partitionKeyRangeWrapper);
        } catch (Exception e) {
            throw wrapAsCosmosException(request, e);
        }
    }

    public void handleLocationSuccessForPartitionKeyRange(RxDocumentServiceRequest request) {

        try {
            checkNotNull(request, "Argument 'request' cannot be null!");
            checkNotNull(request.requestContext, "Argument 'request.requestContext' cannot be null!");

            PartitionKeyRange resolvedPartitionKeyRangeForCircuitBreaker = request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker;
            PartitionKeyRange resolvedPartitionKeyRange = request.requestContext.resolvedPartitionKeyRange;

            // in scenarios where partition is splitting or invalid partition then resolvedPartitionKeyRange could be set to null
            // no reason to circuit break a partition key range which is effectively won't be used in the future
            if (resolvedPartitionKeyRangeForCircuitBreaker != null && resolvedPartitionKeyRange == null) {
                return;
            }

            checkNotNull(request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker, "Argument 'request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker' cannot be null!");

            String resourceId = request.getResourceId();

            PartitionKeyRangeWrapper partitionKeyRangeWrapper = new PartitionKeyRangeWrapper(resolvedPartitionKeyRangeForCircuitBreaker, resourceId);
            RegionalRoutingContext succeededRegionalRoutingContext = request.requestContext.regionalRoutingContextToRoute;

            this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.compute(partitionKeyRangeWrapper, (partitionKeyRangeWrapperAsKey, partitionKeyRangeToFailoverInfoAsVal) -> {

                if (partitionKeyRangeToFailoverInfoAsVal == null) {
                    partitionKeyRangeToFailoverInfoAsVal = new PartitionLevelLocationUnavailabilityInfo();
                }

                partitionKeyRangeToFailoverInfoAsVal.handleSuccess(
                    partitionKeyRangeWrapper,
                    succeededRegionalRoutingContext,
                    request.isReadOnlyRequest());

                request.requestContext.setPerPartitionCircuitBreakerInfoHolder(partitionKeyRangeToFailoverInfoAsVal.regionToLocationSpecificHealthContext);
                return partitionKeyRangeToFailoverInfoAsVal;
            });
        } catch (Exception e) {
            throw wrapAsCosmosException(request, e);
        }
    }

    public List<String> getUnavailableRegionsForPartitionKeyRange(
        RxDocumentServiceRequest request,
        String collectionResourceId,
        PartitionKeyRange partitionKeyRange) {

        try {
            if (!this.isPerPartitionLevelCircuitBreakingApplicable(request)) {
                return Collections.emptyList();
            }

            checkNotNull(partitionKeyRange, "Argument 'partitionKeyRange' cannot be null!");
            checkNotNull(collectionResourceId, "Argument 'collectionResourceId' cannot be null!");

            PartitionKeyRangeWrapper partitionKeyRangeWrapper = new PartitionKeyRangeWrapper(partitionKeyRange, collectionResourceId);

            PartitionLevelLocationUnavailabilityInfo partitionLevelLocationUnavailabilityInfoSnapshot =
                this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.get(partitionKeyRangeWrapper);

            List<String> unavailableRegions = new ArrayList<>();

            if (partitionLevelLocationUnavailabilityInfoSnapshot != null) {
                Map<RegionalRoutingContext, LocationSpecificHealthContext> locationEndpointToFailureMetricsForPartition =
                    partitionLevelLocationUnavailabilityInfoSnapshot.locationEndpointToLocationSpecificContextForPartition;

                PriorityQueue<RegionalRoutingContext> unavailableRoutingContexts = new PriorityQueue<>((endpoint1, endpoint2) -> {

                    LocationSpecificHealthContext locationSpecificHealthContextForEndpoint1
                        = locationEndpointToFailureMetricsForPartition.get(endpoint1);
                    LocationSpecificHealthContext locationSpecificHealthContextForEndpoint2
                        = locationEndpointToFailureMetricsForPartition.get(endpoint2);

                    if (locationSpecificHealthContextForEndpoint1 == null || locationSpecificHealthContextForEndpoint2 == null) {
                        return 0;
                    }

                    return locationSpecificHealthContextForEndpoint1.getUnavailableSince().compareTo(locationSpecificHealthContextForEndpoint2.getUnavailableSince());
                });

                for (Map.Entry<RegionalRoutingContext, LocationSpecificHealthContext> pair : locationEndpointToFailureMetricsForPartition.entrySet()) {

                    RegionalRoutingContext regionalRoutingContext = pair.getKey();
                    LocationSpecificHealthContext locationSpecificHealthContext = pair.getValue();

                    if (locationSpecificHealthContext.getLocationHealthStatus() == LocationHealthStatus.Unavailable) {
                        unavailableRoutingContexts.add(regionalRoutingContext);
                    }
                }

                while (!unavailableRoutingContexts.isEmpty()) {
                    RegionalRoutingContext unavailableRegionalRoutingContext = unavailableRoutingContexts.poll();
                    URI unavailableEndpoint = unavailableRegionalRoutingContext.getGatewayRegionalEndpoint();
                    unavailableRegions.add(this.globalEndpointManager.getRegionName(unavailableEndpoint, request.isReadOnlyRequest() ? OperationType.Read : OperationType.Create));
                }
            }

            return UnmodifiableList.unmodifiableList(unavailableRegions);
        } catch (Exception e) {
            throw wrapAsCosmosException(request, e);
        }
    }

    private Flux<?> updateStaleLocationInfo() {
        return Mono.just(1)
            .delayElement(Duration.ofSeconds(Configs.getStalePartitionUnavailabilityRefreshIntervalInSeconds()))
            .repeat(() -> !this.isClosed.get())
            .flatMap(ignore -> Flux.fromIterable(this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.entrySet()), 1, 1)
            .flatMap(partitionKeyRangeWrapperToPartitionKeyRangeWrapperPair -> {

                logger.debug("Background updateStaleLocationInfo kicking in...");

                try {
                    PartitionKeyRangeWrapper partitionKeyRangeWrapper = partitionKeyRangeWrapperToPartitionKeyRangeWrapperPair.getKey();

                    PartitionLevelLocationUnavailabilityInfo partitionLevelLocationUnavailabilityInfo = this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.get(partitionKeyRangeWrapper);

                    if (partitionLevelLocationUnavailabilityInfo != null) {

                        List<Pair<PartitionKeyRangeWrapper, Pair<RegionalRoutingContext, LocationSpecificHealthContext>>> locationToLocationSpecificHealthContextList = new ArrayList<>();

                        for (Map.Entry<RegionalRoutingContext, LocationSpecificHealthContext> locationToLocationLevelMetrics : partitionLevelLocationUnavailabilityInfo.locationEndpointToLocationSpecificContextForPartition.entrySet()) {

                            RegionalRoutingContext locationWithStaleUnavailabilityInfo = locationToLocationLevelMetrics.getKey();
                            LocationSpecificHealthContext locationSpecificHealthContext = locationToLocationLevelMetrics.getValue();

                            if (!locationSpecificHealthContext.isRegionAvailableToProcessRequests()) {
                                locationToLocationSpecificHealthContextList.add(
                                    Pair.of(
                                        partitionKeyRangeWrapper,
                                        Pair.of(
                                            locationWithStaleUnavailabilityInfo,
                                            locationSpecificHealthContext)));
                            }
                        }

                        if (locationToLocationSpecificHealthContextList.isEmpty()) {
                            return Flux.empty();
                        } else {
                            return Flux.fromIterable(locationToLocationSpecificHealthContextList);
                        }
                    } else {
                        return Mono.empty();
                    }
                } catch (Exception e) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("An exception : {} was thrown trying to recover an Unavailable partition key range!", e.getMessage());
                    }

                    return Flux.empty();
                }
            }, 1, 1)
            .flatMap(locationToLocationSpecificHealthContextPair -> {

                try {
                    PartitionKeyRangeWrapper partitionKeyRangeWrapper = locationToLocationSpecificHealthContextPair.getLeft();
                    RegionalRoutingContext locationWithStaleUnavailabilityInfo = locationToLocationSpecificHealthContextPair.getRight().getLeft();

                    PartitionLevelLocationUnavailabilityInfo partitionLevelLocationUnavailabilityInfo = this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.get(partitionKeyRangeWrapper);

                    if (partitionLevelLocationUnavailabilityInfo != null) {

                        GlobalAddressResolver globalAddressResolver = this.globalAddressResolverSnapshot.get();

                        if (globalAddressResolver != null) {

                            GatewayAddressCache gatewayAddressCache = globalAddressResolver.getGatewayAddressCache(locationWithStaleUnavailabilityInfo.getGatewayRegionalEndpoint());

                            if (gatewayAddressCache != null) {

                                return gatewayAddressCache
                                    .submitOpenConnectionTasks(partitionKeyRangeWrapper.getPartitionKeyRange(), partitionKeyRangeWrapper.getCollectionResourceId())
                                    .timeout(Duration.ofSeconds(Configs.getConnectionEstablishmentTimeoutForPartitionRecoveryInSeconds()))
                                    .doOnComplete(() -> {

                                        if (logger.isDebugEnabled()) {
                                            logger.debug("Partition health recovery query for partition key range : {} and " +
                                                    "collection rid : {} has succeeded...",
                                                partitionKeyRangeWrapper.getPartitionKeyRange(),
                                                partitionKeyRangeWrapper.getCollectionResourceId());
                                        }

                                        partitionLevelLocationUnavailabilityInfo.locationEndpointToLocationSpecificContextForPartition.compute(locationWithStaleUnavailabilityInfo, (locationWithStaleUnavailabilityInfoAsKey, locationSpecificContextAsVal) -> {

                                            if (locationSpecificContextAsVal != null) {
                                                locationSpecificContextAsVal = GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker
                                                    .this.locationSpecificHealthContextTransitionHandler.handleSuccess(
                                                    locationSpecificContextAsVal,
                                                    partitionKeyRangeWrapper,
                                                    this.regionalRoutingContextToRegion.getOrDefault(locationWithStaleUnavailabilityInfoAsKey, StringUtils.EMPTY),
                                                    false,
                                                    true);
                                            }
                                            return locationSpecificContextAsVal;
                                        });
                                    })
                                    .onErrorResume(throwable -> {
                                        if (logger.isDebugEnabled()) {
                                            logger.debug("An exception : {} was thrown trying to recover an Unavailable partition key range!", throwable.getMessage());
                                        }

                                        return Mono.empty();
                                    });
                            }
                        } else {
                            partitionLevelLocationUnavailabilityInfo.locationEndpointToLocationSpecificContextForPartition.compute(locationWithStaleUnavailabilityInfo, (locationWithStaleUnavailabilityInfoAsKey, locationSpecificContextAsVal) -> {

                                if (locationSpecificContextAsVal != null) {
                                    locationSpecificContextAsVal = GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker
                                        .this.locationSpecificHealthContextTransitionHandler.handleSuccess(
                                        locationSpecificContextAsVal,
                                        partitionKeyRangeWrapper,
                                        this.regionalRoutingContextToRegion.getOrDefault(locationWithStaleUnavailabilityInfoAsKey, StringUtils.EMPTY),
                                        false,
                                        true);
                                }
                                return locationSpecificContextAsVal;
                            });
                        }
                    }
                } catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("An exception {} was thrown trying to recover an Unavailable partition key range!", e.getMessage());
                    }
                    return Flux.empty();
                }

                return Flux.empty();
            }, 1, 1)
            .onErrorResume(throwable -> {
                if (logger.isWarnEnabled()) {
                    logger.warn("An exception : {} was thrown trying to recover an Unavailable partition key range!, fail-back flow won't be executed!", throwable.getMessage());
                }
                return Flux.empty();
            });
    }

    public boolean isPerPartitionLevelCircuitBreakingApplicable(RxDocumentServiceRequest request) {

        if (!this.consecutiveExceptionBasedCircuitBreaker.isPartitionLevelCircuitBreakerEnabled()) {
            return false;
        }

        // could be a possible scenario when end-to-end timeout set on the operation is negative
        // failing the operation with a NullPointerException would suppress the real issue in this case
        // so when request is null - circuit breaking is effectively disabled
        if (request == null) {
            return false;
        }

        if (request.getResourceType() != ResourceType.Document) {
            return false;
        }

        if (request.getOperationType() == OperationType.QueryPlan) {
            return false;
        }

        if (request.requestContext == null) {
            return false;
        }

        GlobalEndpointManager globalEndpointManager = this.globalEndpointManager;

        if (!globalEndpointManager.canUseMultipleWriteLocations(request)) {

            if (!request.isReadOnlyRequest()) {
                return false;
            }

            UnmodifiableList<RegionalRoutingContext> applicableReadEndpoints = globalEndpointManager.getApplicableReadRegionalRoutingContexts(Collections.emptyList());

            return applicableReadEndpoints != null && applicableReadEndpoints.size() > 1;
        }

        UnmodifiableList<RegionalRoutingContext> applicableWriteEndpoints = globalEndpointManager.getApplicableWriteRegionalRoutingContexts(Collections.emptyList());

        return applicableWriteEndpoints != null && applicableWriteEndpoints.size() > 1;
    }

    public void setGlobalAddressResolver(GlobalAddressResolver globalAddressResolver) {
        this.globalAddressResolverSnapshot.set(globalAddressResolver);
    }

    @Override
    public void close() {
        this.isClosed.set(true);
        this.partitionRecoveryScheduler.dispose();
    }

    private class PartitionLevelLocationUnavailabilityInfo {

        private final ConcurrentHashMap<RegionalRoutingContext, LocationSpecificHealthContext> locationEndpointToLocationSpecificContextForPartition;
        private final ConcurrentHashMap<String, LocationSpecificHealthContext> regionToLocationSpecificHealthContext;
        private final LocationSpecificHealthContextTransitionHandler locationSpecificHealthContextTransitionHandler;

        private PartitionLevelLocationUnavailabilityInfo() {
            this.locationEndpointToLocationSpecificContextForPartition = new ConcurrentHashMap<>();
            this.regionToLocationSpecificHealthContext = new ConcurrentHashMap<>();
            this.locationSpecificHealthContextTransitionHandler = GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.this.locationSpecificHealthContextTransitionHandler;
        }

        private boolean handleException(
            PartitionKeyRangeWrapper partitionKeyRangeWrapper,
            RegionalRoutingContext regionalRoutingContextWithAnException,
            boolean isReadOnlyRequest) {

            AtomicBoolean isExceptionThresholdBreached = new AtomicBoolean(false);

            this.locationEndpointToLocationSpecificContextForPartition.compute(regionalRoutingContextWithAnException, (regionalRoutingContextAsKey, locationSpecificContextAsVal) -> {

                if (locationSpecificContextAsVal == null) {

                    locationSpecificContextAsVal = new LocationSpecificHealthContext.Builder()
                        .withSuccessCountForWriteForRecovery(0)
                        .withExceptionCountForWriteForCircuitBreaking(0)
                        .withSuccessCountForReadForRecovery(0)
                        .withExceptionCountForReadForCircuitBreaking(0)
                        .withUnavailableSince(Instant.MAX)
                        .withLocationHealthStatus(LocationHealthStatus.HealthyWithFailures)
                        .withExceptionThresholdBreached(false)
                        .build();
                }

                LocationSpecificHealthContext locationSpecificHealthContextAfterTransition = this.locationSpecificHealthContextTransitionHandler.handleException(
                    locationSpecificContextAsVal,
                    partitionKeyRangeWrapper,
                    GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.this.regionalRoutingContextToRegion.getOrDefault(regionalRoutingContextWithAnException, StringUtils.EMPTY),
                    isReadOnlyRequest);


                if (GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.this.regionalRoutingContextToRegion.get(regionalRoutingContextAsKey) == null) {

                    GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.this.regionalRoutingContextToRegion.put(
                        regionalRoutingContextAsKey,
                        GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker
                            .this.globalEndpointManager
                            .getRegionName(regionalRoutingContextAsKey.getGatewayRegionalEndpoint(), isReadOnlyRequest ? OperationType.Read : OperationType.Create));
                }

                String region = GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.this.regionalRoutingContextToRegion.get(regionalRoutingContextAsKey);
                this.regionToLocationSpecificHealthContext.put(region, locationSpecificHealthContextAfterTransition);

                isExceptionThresholdBreached.set(locationSpecificHealthContextAfterTransition.isExceptionThresholdBreached());
                return locationSpecificHealthContextAfterTransition;
            });

            return isExceptionThresholdBreached.get();
        }

        private void handleSuccess(
            PartitionKeyRangeWrapper partitionKeyRangeWrapper,
            RegionalRoutingContext succeededLocation,
            boolean isReadOnlyRequest) {

            this.locationEndpointToLocationSpecificContextForPartition.compute(succeededLocation, (locationAsKey, locationSpecificContextAsVal) -> {

                LocationSpecificHealthContext locationSpecificHealthContextAfterTransition;

                if (locationSpecificContextAsVal == null) {

                    locationSpecificContextAsVal = new LocationSpecificHealthContext.Builder()
                        .withSuccessCountForWriteForRecovery(0)
                        .withExceptionCountForWriteForCircuitBreaking(0)
                        .withSuccessCountForReadForRecovery(0)
                        .withExceptionCountForReadForCircuitBreaking(0)
                        .withUnavailableSince(Instant.MAX)
                        .withLocationHealthStatus(LocationHealthStatus.Healthy)
                        .withExceptionThresholdBreached(false)
                        .build();
                }

                locationSpecificHealthContextAfterTransition = this.locationSpecificHealthContextTransitionHandler.handleSuccess(
                    locationSpecificContextAsVal,
                    partitionKeyRangeWrapper,
                    GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.this.regionalRoutingContextToRegion.getOrDefault(succeededLocation, StringUtils.EMPTY),
                    false,
                    isReadOnlyRequest);

                // used only for building diagnostics - so creating a lookup for URI and region name

                if (GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.this.regionalRoutingContextToRegion.get(locationAsKey) == null) {
                    GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.this.regionalRoutingContextToRegion.put(
                        locationAsKey,
                        GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker
                            .this.globalEndpointManager
                            .getRegionName(locationAsKey.getGatewayRegionalEndpoint(), isReadOnlyRequest ? OperationType.Read : OperationType.Create));
                }

                String region = GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.this.regionalRoutingContextToRegion.get(locationAsKey);
                this.regionToLocationSpecificHealthContext.put(region, locationSpecificHealthContextAfterTransition);

                return locationSpecificHealthContextAfterTransition;
            });
        }

        public boolean areLocationsAvailableForPartitionKeyRange(List<RegionalRoutingContext> availableLocationsAtAccountLevel) {

            for (RegionalRoutingContext availableLocation : availableLocationsAtAccountLevel) {
                if (!this.locationEndpointToLocationSpecificContextForPartition.containsKey(availableLocation)) {
                    return true;
                } else {
                    LocationSpecificHealthContext locationSpecificHealthContextSnapshot = this.locationEndpointToLocationSpecificContextForPartition.get(availableLocation);

                    if (locationSpecificHealthContextSnapshot.isRegionAvailableToProcessRequests()) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public ConsecutiveExceptionBasedCircuitBreaker getConsecutiveExceptionBasedCircuitBreaker() {
        return this.consecutiveExceptionBasedCircuitBreaker;
    }

    public PartitionLevelCircuitBreakerConfig getCircuitBreakerConfig() {
        return this.consecutiveExceptionBasedCircuitBreaker.getPartitionLevelCircuitBreakerConfig();
    }

    public synchronized void resetCircuitBreakerConfig() {
        PartitionLevelCircuitBreakerConfig partitionLevelCircuitBreakerConfig
            = Configs.getPartitionLevelCircuitBreakerConfig();

        this.consecutiveExceptionBasedCircuitBreaker
            = new ConsecutiveExceptionBasedCircuitBreaker(partitionLevelCircuitBreakerConfig);

        this.locationSpecificHealthContextTransitionHandler
            = new LocationSpecificHealthContextTransitionHandler(this.consecutiveExceptionBasedCircuitBreaker);
    }

    private static CosmosException wrapAsCosmosException(RxDocumentServiceRequest request, Exception innerException) {
        CosmosDiagnostics cosmosDiagnostics = null;
        String resourceAddress = null;

        if (request != null) {

            if (request.requestContext != null) {
                cosmosDiagnostics = request.requestContext.cosmosDiagnostics;
                resourceAddress = request.requestContext.resourcePhysicalAddress != null
                    ? request.requestContext.resourcePhysicalAddress
                    : "N/A";
            }
        }

        CosmosException cosmosException = BridgeInternal.createCosmosException(
            BASE_EXCEPTION_MESSAGE + innerException.getMessage(),
            innerException,
            EMPTY_MAP,
            HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR,
            resourceAddress);

        cosmosException = BridgeInternal.setCosmosDiagnostics(cosmosException, cosmosDiagnostics);
        BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.PPCB_INVALID_STATE);

        throw cosmosException;
    }
}
