// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionCircuitBreaker;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.CosmosSchedulers;
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
import com.azure.cosmos.models.CosmosMetricName;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker implements AutoCloseable {

    private static final String COLLECTION_RID_TAG_NAME = "CollectionRid";
    private static final Map<String, String> EMPTY_MAP = new HashMap<>();
    private static final String BASE_EXCEPTION_MESSAGE = "FAILED IN Per-Partition Circuit Breaker: ";

    private final Logger logger;
    private final GlobalEndpointManager globalEndpointManager;
    private final ConcurrentHashMap<PartitionKeyRangeWrapper, PartitionLevelLocationUnavailabilityInfo> partitionKeyRangeToLocationSpecificUnavailabilityInfo;
    private LocationSpecificHealthContextTransitionHandler locationSpecificHealthContextTransitionHandler;
    private ConsecutiveExceptionBasedCircuitBreaker consecutiveExceptionBasedCircuitBreaker;
    private final AtomicReference<GlobalAddressResolver> globalAddressResolverSnapshot;
    private final ConcurrentHashMap<RegionalRoutingContext, String> regionalRoutingContextToRegion;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final AtomicBoolean isPartitionRecoveryTaskRunning = new AtomicBoolean(false);
    private final AtomicReference<Disposable> partitionRecoveryDisposable = new AtomicReference<>();
    private final AtomicInteger failbackFailureLogCount;
    private final AtomicInteger failbackBacklogScanCount;
    private final AtomicReference<MultiGauge> failbackPendingRecoveryGauge;
    private final AtomicReference<String> clientCorrelationId;

    public GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker(GlobalEndpointManager globalEndpointManager) {
        this(
            globalEndpointManager,
            LoggerFactory.getLogger(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class));
    }

    GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker(
        GlobalEndpointManager globalEndpointManager,
        Logger logger) {

        this.partitionKeyRangeToLocationSpecificUnavailabilityInfo = new ConcurrentHashMap<>();
        this.globalEndpointManager = globalEndpointManager;
        this.logger = checkNotNull(logger, "Argument 'logger' cannot be null!");
        this.failbackFailureLogCount = new AtomicInteger();
        this.failbackBacklogScanCount = new AtomicInteger();
        this.failbackPendingRecoveryGauge = new AtomicReference<>();
        this.clientCorrelationId = new AtomicReference<>(StringUtils.EMPTY);

        PartitionLevelCircuitBreakerConfig partitionLevelCircuitBreakerConfig = Configs.getPartitionLevelCircuitBreakerConfig();
        this.consecutiveExceptionBasedCircuitBreaker = new ConsecutiveExceptionBasedCircuitBreaker(partitionLevelCircuitBreakerConfig);
        this.locationSpecificHealthContextTransitionHandler
            = new LocationSpecificHealthContextTransitionHandler(this.consecutiveExceptionBasedCircuitBreaker);
        this.globalAddressResolverSnapshot = new AtomicReference<>();
        this.regionalRoutingContextToRegion = new ConcurrentHashMap<>();
    }

    public void init() {
        if (this.consecutiveExceptionBasedCircuitBreaker.isPartitionLevelCircuitBreakerEnabled()
            && this.isPartitionRecoveryTaskRunning.compareAndSet(false, true)) {

            this.partitionRecoveryDisposable.set(this.updateStaleLocationInfo()
                .subscribeOn(CosmosSchedulers.PARTITION_AVAILABILITY_CHECK_BOUNDED_ELASTIC)
                .subscribe());
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

            // in scenarios where partition is splitting or invalid partition (name cache is stale) then resolvedPartitionKeyRange could be set to null
            // no reason to circuit break a partition key range which is effectively won't be used in the future
            if (resolvedPartitionKeyRangeForCircuitBreaker != null && resolvedPartitionKeyRange == null) {
                logger.info("Skipping circuit breaking for partitionKeyRange which is splitting or invalid partition (name cache is stale), partitionKeyRange: " +
                    resolvedPartitionKeyRangeForCircuitBreaker +
                    " and operationType: " +
                    request.getOperationType() +
                    " and collectionResourceId: " +
                    request.getResourceId());
                return;
            }

            // in scenarios where resolvedPartitionKeyRangeForCircuitBreaker is null, we cannot circuit break at partition level
            // if the exception is due to a cancellation, then we don't have enough information to decide if we should circuit break or not
            // so we skip circuit breaking in this case if cancellation kick in ungracefully (e.g. user cancelled the request, or end-to-end timeout on the operation before routing decision is made)
            // if the exception is not due to a cancellation, then we should have enough information to decide if we should circuit break or not
            // so we proceed with circuit breaking in this case
            if (resolvedPartitionKeyRangeForCircuitBreaker == null && isCancellationException) {
                logger.warn("Skipping circuit breaking for operation as partitionKeyRange information isn't available for an e2e timeout cancelled request with operationType: " +
                    request.getOperationType() +
                    " and collectionResourceId: " +
                    request.getResourceId());
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

                this.publishSnapshot(request, partitionLevelLocationUnavailabilityInfoAsVal);
                return partitionLevelLocationUnavailabilityInfoAsVal;
            });

            // set to true if and only if failure threshold exceeded for the region
            // and if failover is possible
            // a failover is only possible when there are available regions left to fail over to
            if (isFailoverPossible.get()) {
                return;
            }

            logger.warn("It is not possible to mark region " +
                this.globalEndpointManager.getRegionName(
                failedRegionalRoutingContext.getGatewayRegionalEndpoint(), request.isReadOnlyRequest() ? OperationType.Read : OperationType.Create) + " as Unavailable as " +
                " all regions will be Unavailable in that case, will remove health status tracking for this partitionKeyRange : " +
                resolvedPartitionKeyRangeForCircuitBreaker +
                " and collectionResourceId : " +
                collectionResourceId +
                " and operationType: " +
                request.getOperationType());

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

                this.publishSnapshot(request, partitionKeyRangeToFailoverInfoAsVal);
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
            this.publishSnapshot(request, partitionLevelLocationUnavailabilityInfoSnapshot);

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

    private void publishSnapshot(
        RxDocumentServiceRequest request,
        PartitionLevelLocationUnavailabilityInfo info) {

        request.requestContext.getPerPartitionCircuitBreakerInfoHolder()
            .setPerPartitionCircuitBreakerInfoHolderSnapshot(
                info == null ? Collections.emptyMap() : info.diagnosticsSnapshot);
    }

    private Flux<?> updateStaleLocationInfo() {
        return Mono.just(1)
            .delayElement(Duration.ofSeconds(Configs.getStalePartitionUnavailabilityRefreshIntervalInSeconds()))
            .repeat(() -> !this.isClosed.get())
            .flatMap(ignore -> {
                this.recordFailbackBacklogSnapshot();
                return Flux.fromIterable(this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.entrySet());
            }, 1, 1)
            .flatMap(partitionKeyRangeToPartitionLevelInfo -> {

                logger.debug("Background updateStaleLocationInfo kicking in...");

                try {
                    PartitionKeyRangeWrapper partitionKeyRangeWrapper = partitionKeyRangeToPartitionLevelInfo.getKey();
                    PartitionLevelLocationUnavailabilityInfo partitionLevelLocationUnavailabilityInfo
                        = this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.get(partitionKeyRangeWrapper);

                    if (partitionLevelLocationUnavailabilityInfo == null) {
                        return Flux.empty();
                    }

                    List<Pair<PartitionKeyRangeWrapper, Pair<RegionalRoutingContext, LocationSpecificHealthContext>>>
                        failbackCandidates = new ArrayList<>();

                    for (Map.Entry<RegionalRoutingContext, LocationSpecificHealthContext> locationToHealthContext
                        : partitionLevelLocationUnavailabilityInfo
                            .locationEndpointToLocationSpecificContextForPartition.entrySet()) {

                        RegionalRoutingContext regionalRoutingContext = locationToHealthContext.getKey();
                        LocationSpecificHealthContext healthContext = locationToHealthContext.getValue();

                        if (!healthContext.isRegionAvailableToProcessRequests()) {
                            failbackCandidates.add(Pair.of(
                                partitionKeyRangeWrapper,
                                Pair.of(regionalRoutingContext, healthContext)));
                        }
                    }

                    return Flux.fromIterable(failbackCandidates);
                } catch (Exception e) {
                    logger.warn("An exception was thrown trying to recover an Unavailable partitionKeyRange!", e);
                    return Flux.empty();
                }
            }, 1, 1)
            .flatMap(failbackRecoveryEntry -> {

                try {
                    PartitionKeyRangeWrapper partitionKeyRangeWrapper = failbackRecoveryEntry.getLeft();
                    RegionalRoutingContext locationWithStaleUnavailabilityInfo
                        = failbackRecoveryEntry.getRight().getLeft();

                    PartitionLevelLocationUnavailabilityInfo partitionLevelLocationUnavailabilityInfo
                        = this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.get(partitionKeyRangeWrapper);

                    if (partitionLevelLocationUnavailabilityInfo != null) {

                        GlobalAddressResolver globalAddressResolver = this.globalAddressResolverSnapshot.get();

                        if (globalAddressResolver != null) {

                            GatewayAddressCache gatewayAddressCache = globalAddressResolver.getGatewayAddressCache(locationWithStaleUnavailabilityInfo.getGatewayRegionalEndpoint());

                            if (gatewayAddressCache != null) {

                                return gatewayAddressCache
                                    .submitOpenConnectionTasks(partitionKeyRangeWrapper.getPartitionKeyRange(), partitionKeyRangeWrapper.getCollectionResourceId())
                                    .timeout(Duration.ofSeconds(Configs.getConnectionEstablishmentTimeoutForPartitionRecoveryInSeconds()))
                                    .doOnComplete(() -> {

                                        logger.debug("Partition health recovery query for partitionKeyRange : " +
                                            partitionKeyRangeWrapper.getPartitionKeyRange() +
                                            " and collectionResourceId : "
                                            + partitionKeyRangeWrapper.getCollectionResourceId() +
                                            " has succeeded...");

                                        partitionLevelLocationUnavailabilityInfo.handleSuccess(
                                            partitionKeyRangeWrapper,
                                            locationWithStaleUnavailabilityInfo,
                                            true);
                                    })
                                    .onErrorResume(throwable -> {
                                        logger.warn("An exception was thrown trying to recover an Unavailable partition key range!", throwable);
                                        return Mono.empty();
                                    });
                            }
                        } else {
                            partitionLevelLocationUnavailabilityInfo.handleSuccess(
                                partitionKeyRangeWrapper,
                                locationWithStaleUnavailabilityInfo,
                                true);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("An exception was thrown trying to recover an Unavailable partition key range!", e);
                    return Flux.empty();
                }

                return Flux.empty();
            }, 1, 1)
            .onErrorResume(throwable -> {
                logger.warn("An exception : was thrown trying to recover an Unavailable partitionKeyRange!, fail-back flow won't be executed!", throwable);
                return Flux.empty();
            });
    }

    void recordFailbackBacklogSnapshot() {
        try {
            int failbackBacklogSize = 0;
            Map<String, AtomicInteger> pendingRecoveryCountByCollection = new HashMap<>();

            for (Map.Entry<PartitionKeyRangeWrapper, PartitionLevelLocationUnavailabilityInfo> entry
                : this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.entrySet()) {

                PartitionKeyRangeWrapper partitionKeyRangeWrapper = entry.getKey();
                AtomicInteger pendingRecoveryCount = pendingRecoveryCountByCollection
                    .computeIfAbsent(
                        partitionKeyRangeWrapper.getCollectionResourceId(),
                        ignored -> new AtomicInteger());

                try {
                    PartitionLevelLocationUnavailabilityInfo partitionLevelLocationUnavailabilityInfo = entry.getValue();

                    if (partitionLevelLocationUnavailabilityInfo != null) {

                        for (Map.Entry<RegionalRoutingContext, LocationSpecificHealthContext> locationToLocationLevelMetrics
                            : partitionLevelLocationUnavailabilityInfo.locationEndpointToLocationSpecificContextForPartition.entrySet()) {

                            LocationSpecificHealthContext locationSpecificHealthContext = locationToLocationLevelMetrics.getValue();

                            if (!locationSpecificHealthContext.isRegionAvailableToProcessRequests()) {
                                pendingRecoveryCount.incrementAndGet();
                                failbackBacklogSize++;
                            }
                        }
                    }
                } catch (Exception e) {
                    this.logFailbackFailure(
                        partitionKeyRangeWrapper,
                        null,
                        "SCAN_UNAVAILABLE_PARTITIONS",
                        e);
                }
            }

            this.logFailbackBacklog(failbackBacklogSize);
            this.recordFailbackPendingRecoveryByCollection(pendingRecoveryCountByCollection);
        } catch (Exception e) {
            try {
                this.logFailbackFailure(
                    null,
                    null,
                    "RECORD_BACKLOG_SNAPSHOT",
                    e);
            } catch (Exception ignored) {
                // Failback telemetry must never terminate the recovery flow.
            }
        }
    }

    public synchronized void registerFailbackPendingRecoveryMeter(MeterRegistry meterRegistry, Tag clientCorrelationTag) {
        if (clientCorrelationTag != null) {
            this.clientCorrelationId.set(clientCorrelationTag.getValue());
        }

        if (meterRegistry == null || clientCorrelationTag == null || this.failbackPendingRecoveryGauge.get() != null) {
            return;
        }

        this.failbackPendingRecoveryGauge.set(
            MultiGauge.builder(CosmosMetricName.PPCB_FAILBACK_PENDING_COUNT.toString())
                .description("Number of PPCB partition range-region recovery actions pending failback")
                .baseUnit("recoveries")
                .tags(Collections.singletonList(clientCorrelationTag))
                .register(meterRegistry));
    }

    public void setClientCorrelationId(String clientCorrelationId) {
        this.clientCorrelationId.set(clientCorrelationId == null ? StringUtils.EMPTY : clientCorrelationId);
    }

    synchronized void recordFailbackPendingRecoveryByCollection(Map<String, AtomicInteger> pendingRecoveryCountByCollection) {
        MultiGauge gauge = this.failbackPendingRecoveryGauge.get();
        if (gauge == null) {
            return;
        }

        List<MultiGauge.Row<?>> rows = new ArrayList<>();
        pendingRecoveryCountByCollection.forEach((collectionRid, recoveryCount) ->
            rows.add(MultiGauge.Row.of(
                Tags.of(COLLECTION_RID_TAG_NAME, collectionRid),
                recoveryCount)));
        gauge.register(rows, true);
    }

    public synchronized void removeFailbackPendingRecoveryMeter() {
        MultiGauge gauge = this.failbackPendingRecoveryGauge.getAndSet(null);
        if (gauge != null) {
            gauge.register(Collections.emptyList(), true);
        }
    }

    void logFailbackBacklog(int unavailablePartitionRegionCount) {
        int previousScanCount = unavailablePartitionRegionCount == 0
            ? this.failbackBacklogScanCount.getAndSet(0)
            : this.failbackBacklogScanCount.updateAndGet(
                current -> current == Integer.MAX_VALUE ? 1 : current + 1);

        if (unavailablePartitionRegionCount == 0 && previousScanCount == 0) {
            return;
        }

        String message = "PPCB failback backlog: unavailablePartitionRegionCount: "
            + unavailablePartitionRegionCount
            + ", trackedPartitionKeyRangeCount: "
            + this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.size()
            + ", consecutiveBacklogScanCount: "
            + (unavailablePartitionRegionCount == 0 ? 0 : previousScanCount)
            + ", clientCorrelationId: "
            + this.clientCorrelationId.get();

        if (unavailablePartitionRegionCount == 0 || previousScanCount == 1 || previousScanCount % 10 == 0) {
            this.logger.info(message);
        } else {
            this.logger.debug(message);
        }
    }

    void logFailbackFailure(
        PartitionKeyRangeWrapper partitionKeyRangeWrapper,
        RegionalRoutingContext regionalRoutingContext,
        String stage,
        Throwable throwable) {

        String region = this.resolveRegionName(regionalRoutingContext);
        String reason = throwable == null ? "UNKNOWN" : throwable.getClass().getSimpleName();
        String collectionResourceId = partitionKeyRangeWrapper == null
            ? StringUtils.EMPTY
            : partitionKeyRangeWrapper.getCollectionResourceId();
        String partitionKeyRangeId = partitionKeyRangeWrapper == null
            || partitionKeyRangeWrapper.getPartitionKeyRange() == null
            ? StringUtils.EMPTY
            : partitionKeyRangeWrapper.getPartitionKeyRange().getId();
        String message = "PPCB failback failed for collectionResourceId: "
            + collectionResourceId
            + ", partitionKeyRangeId: "
            + partitionKeyRangeId
            + ", region: "
            + region
            + ", stage: "
            + stage
            + ", reason: "
            + reason
            + ", clientCorrelationId: "
            + this.clientCorrelationId.get();

        if (this.shouldLogFailbackFailureAtWarn()) {
            this.logger.warn(message, throwable);
        } else {
            this.logger.debug(message, throwable);
        }

    }

    private boolean shouldLogFailbackFailureAtWarn() {
        int count = this.failbackFailureLogCount.updateAndGet(
            current -> current == Integer.MAX_VALUE ? 1 : current + 1);
        return count == 1 || count % 10 == 0;
    }

    private String resolveRegionName(RegionalRoutingContext regionalRoutingContext) {
        if (regionalRoutingContext == null) {
            return StringUtils.EMPTY;
        }

        String region = this.regionalRoutingContextToRegion.get(regionalRoutingContext);
        if (!StringUtils.isEmpty(region)) {
            return region;
        }

        return this.globalEndpointManager.getRegionName(
            regionalRoutingContext.getGatewayRegionalEndpoint(),
            OperationType.Read);
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
        this.failbackFailureLogCount.set(0);
        this.failbackBacklogScanCount.set(0);
        this.removeFailbackPendingRecoveryMeter();
        Disposable disposable = this.partitionRecoveryDisposable.getAndSet(null);
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    private class PartitionLevelLocationUnavailabilityInfo {

        private final ConcurrentHashMap<RegionalRoutingContext, LocationSpecificHealthContext> locationEndpointToLocationSpecificContextForPartition;
        private final ConcurrentHashMap<String, LocationSpecificHealthContext> regionToLocationSpecificHealthContext;
        private final LocationSpecificHealthContextTransitionHandler locationSpecificHealthContextTransitionHandler;
        private volatile Map<String, LocationSpecificHealthContext> diagnosticsSnapshot;

        private PartitionLevelLocationUnavailabilityInfo() {
            this.locationEndpointToLocationSpecificContextForPartition = new ConcurrentHashMap<>();
            this.regionToLocationSpecificHealthContext = new ConcurrentHashMap<>();
            this.locationSpecificHealthContextTransitionHandler = GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.this.locationSpecificHealthContextTransitionHandler;
            this.diagnosticsSnapshot = Collections.emptyMap();
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

                LocationSpecificHealthContext locationSpecificHealthContextBeforeTransition = locationSpecificContextAsVal;
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
                if (locationSpecificHealthContextAfterTransition != locationSpecificHealthContextBeforeTransition
                    || !this.regionToLocationSpecificHealthContext.containsKey(region)) {
                    this.regionToLocationSpecificHealthContext.put(region, locationSpecificHealthContextAfterTransition);
                    this.refreshDiagnosticsSnapshot();
                }

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

                LocationSpecificHealthContext locationSpecificHealthContextBeforeTransition = locationSpecificContextAsVal;
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
                if (locationSpecificHealthContextAfterTransition != locationSpecificHealthContextBeforeTransition
                    || !this.regionToLocationSpecificHealthContext.containsKey(region)) {
                    this.regionToLocationSpecificHealthContext.put(region, locationSpecificHealthContextAfterTransition);
                    this.refreshDiagnosticsSnapshot();
                }

                return locationSpecificHealthContextAfterTransition;
            });
        }

        private void refreshDiagnosticsSnapshot() {
            this.diagnosticsSnapshot = Collections.unmodifiableMap(
                new LinkedHashMap<>(this.regionToLocationSpecificHealthContext));
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

    public synchronized void resetCircuitBreakerConfig(PartitionLevelCircuitBreakerConfig partitionLevelCircuitBreakerConfig) {
        this.consecutiveExceptionBasedCircuitBreaker
            = new ConsecutiveExceptionBasedCircuitBreaker(partitionLevelCircuitBreakerConfig);

        this.locationSpecificHealthContextTransitionHandler
            = new LocationSpecificHealthContextTransitionHandler(this.consecutiveExceptionBasedCircuitBreaker);

        this.clear();
    }

    private void clear() {
        this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.clear();
        this.regionalRoutingContextToRegion.clear();
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
