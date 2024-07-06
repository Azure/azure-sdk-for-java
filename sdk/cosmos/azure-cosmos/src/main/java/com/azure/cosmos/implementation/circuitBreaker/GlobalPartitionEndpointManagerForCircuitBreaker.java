// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.circuitBreaker;

import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.FeedOperationContextForCircuitBreaker;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.PathsHelper;
import com.azure.cosmos.implementation.PointOperationContextForCircuitBreaker;
import com.azure.cosmos.implementation.QueryFeedOperationState;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class GlobalPartitionEndpointManagerForCircuitBreaker {

    private static final Logger logger = LoggerFactory.getLogger(GlobalPartitionEndpointManagerForCircuitBreaker.class);

    private static final ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.CosmosQueryRequestOptionsAccessor queryRequestOptionsAccessor
        = ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor();
    private final GlobalEndpointManager globalEndpointManager;
    private final ConcurrentHashMap<PartitionKeyRangeWrapper, PartitionLevelLocationUnavailabilityInfo> partitionKeyRangeToLocationSpecificUnavailabilityInfo;
    private final ConcurrentHashMap<PartitionKeyRangeWrapper, PartitionKeyRangeWrapper> partitionKeyRangesWithPossibleUnavailableRegions;
    private final LocationSpecificHealthContextTransitionHandler locationSpecificHealthContextTransitionHandler;
    private final ConsecutiveExceptionBasedCircuitBreaker consecutiveExceptionBasedCircuitBreaker;
    private final AtomicReference<RxDocumentClientImpl> rxDocClientSnapshot;

    public GlobalPartitionEndpointManagerForCircuitBreaker(GlobalEndpointManager globalEndpointManager) {
        this.partitionKeyRangeToLocationSpecificUnavailabilityInfo = new ConcurrentHashMap<>();
        this.partitionKeyRangesWithPossibleUnavailableRegions = new ConcurrentHashMap<>();
        this.globalEndpointManager = globalEndpointManager;

        PartitionLevelCircuitBreakerConfig partitionLevelCircuitBreakerConfig = Configs.getPartitionLevelCircuitBreakerConfig();
        this.consecutiveExceptionBasedCircuitBreaker = new ConsecutiveExceptionBasedCircuitBreaker(partitionLevelCircuitBreakerConfig);
        this.locationSpecificHealthContextTransitionHandler
            = new LocationSpecificHealthContextTransitionHandler(this.globalEndpointManager, this.consecutiveExceptionBasedCircuitBreaker);
        this.rxDocClientSnapshot = new AtomicReference<>();
    }

    public void init() {
        if (this.consecutiveExceptionBasedCircuitBreaker.isPartitionLevelCircuitBreakerEnabled()) {
            this.updateStaleLocationInfo().subscribeOn(CosmosSchedulers.PARTITION_AVAILABILITY_STALENESS_CHECK_SINGLE).subscribe();
        }
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

        String collectionLink = getCollectionLink(request);

        this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.compute(partitionKeyRangeWrapper, (partitionKeyRangeWrapperAsKey, partitionLevelLocationUnavailabilityInfoAsVal) -> {

            if (partitionLevelLocationUnavailabilityInfoAsVal == null) {
                partitionLevelLocationUnavailabilityInfoAsVal = new PartitionLevelLocationUnavailabilityInfo();
            }

            isFailureThresholdBreached.set(partitionLevelLocationUnavailabilityInfoAsVal.handleException(
                partitionKeyRangeWrapperAsKey,
                collectionLink,
                failedLocation,
                request.isReadOnlyRequest()));

            if (isFailureThresholdBreached.get()) {

                UnmodifiableList<URI> applicableEndpoints = request.isReadOnlyRequest() ?
                    this.globalEndpointManager.getApplicableReadEndpoints(request.requestContext.getExcludeRegions()) :
                    this.globalEndpointManager.getApplicableWriteEndpoints(request.requestContext.getExcludeRegions());

                isFailoverPossible.set(
                    partitionLevelLocationUnavailabilityInfoAsVal.areLocationsAvailableForPartitionKeyRange(applicableEndpoints));
            }

            request.requestContext.setLocationToLocationSpecificHealthContext(partitionLevelLocationUnavailabilityInfoAsVal.regionToLocationSpecificHealthContext);
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

        String collectionLink = getCollectionLink(request);

        this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.compute(partitionKeyRangeWrapper, (partitionKeyRangeWrapperAsKey, partitionKeyRangeToFailoverInfoAsVal) -> {

            if (partitionKeyRangeToFailoverInfoAsVal == null) {
                partitionKeyRangeToFailoverInfoAsVal = new PartitionLevelLocationUnavailabilityInfo();
            }

            partitionKeyRangeToFailoverInfoAsVal.handleSuccess(
                partitionKeyRangeWrapper,
                collectionLink,
                succeededLocation,
                request.isReadOnlyRequest());

            request.requestContext.setLocationToLocationSpecificHealthContext(partitionKeyRangeToFailoverInfoAsVal.regionToLocationSpecificHealthContext);
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
            Map<URI, LocationSpecificHealthContext> locationEndpointToFailureMetricsForPartition =
                partitionLevelLocationUnavailabilityInfoSnapshot.locationEndpointToLocationSpecificContextForPartition;

            for (Map.Entry<URI, LocationSpecificHealthContext> pair : locationEndpointToFailureMetricsForPartition.entrySet()) {
                URI location = pair.getKey();
                LocationSpecificHealthContext locationSpecificHealthContext = pair.getValue();

                if (locationSpecificHealthContext.getLocationHealthStatus() == LocationHealthStatus.Unavailable) {
                    unavailableLocations.add(location);
                }
            }
        }

        return UnmodifiableList.unmodifiableList(unavailableLocations);
    }

    private Flux<?> updateStaleLocationInfo() {
        return Mono.just(1)
            .delayElement(Duration.ofSeconds(Configs.getStalePartitionUnavailabilityRefreshIntervalInSeconds()))
            .repeat()
            .flatMap(ignore -> Flux.fromIterable(this.partitionKeyRangesWithPossibleUnavailableRegions.entrySet()))
            .publishOn(CosmosSchedulers.PARTITION_AVAILABILITY_STALENESS_CHECK_SINGLE)
            .flatMap(partitionKeyRangeWrapperToPartitionKeyRangeWrapperPair -> {

                logger.debug("Background updateStaleLocationInfo kicking in...");

                PartitionKeyRangeWrapper partitionKeyRangeWrapper = partitionKeyRangeWrapperToPartitionKeyRangeWrapperPair.getKey();

                PartitionLevelLocationUnavailabilityInfo partitionLevelLocationUnavailabilityInfo = this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.get(partitionKeyRangeWrapper);

                if (partitionLevelLocationUnavailabilityInfo != null) {

                    List<Pair<PartitionKeyRangeWrapper, Pair<URI, LocationSpecificHealthContext>>> locationToLocationSpecificHealthContextList = new ArrayList<>();

                    for (Map.Entry<URI, LocationSpecificHealthContext> locationToLocationLevelMetrics : partitionLevelLocationUnavailabilityInfo.locationEndpointToLocationSpecificContextForPartition.entrySet()) {

                        URI locationWithStaleUnavailabilityInfo = locationToLocationLevelMetrics.getKey();
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

                    return Flux.fromIterable(locationToLocationSpecificHealthContextList);
                } else {
                    this.partitionKeyRangesWithPossibleUnavailableRegions.remove(partitionKeyRangeWrapper);
                    return Mono.empty();
                }
            })
            .flatMap(locationToLocationSpecificHealthContextPair -> {

                PartitionKeyRangeWrapper partitionKeyRangeWrapper = locationToLocationSpecificHealthContextPair.getLeft();
                URI locationWithStaleUnavailabilityInfo = locationToLocationSpecificHealthContextPair.getRight().getLeft();
                LocationSpecificHealthContext locationSpecificHealthContext = locationToLocationSpecificHealthContextPair.getRight().getRight();

                String collectionLink = locationSpecificHealthContext.getLastCollectionLinkSeen();
                CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
                queryRequestOptions.setFeedRange(new FeedRangeEpkImpl(partitionKeyRangeWrapper.getPartitionKeyRange().toRange()));
                queryRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(2)).build());

                List<String> applicableReadEndpoints = globalEndpointManager
                    .getApplicableReadEndpoints(Collections.emptyList())
                    .stream()
                    .map(locationEndpoint -> globalEndpointManager.getRegionName(locationEndpoint, OperationType.Read))
                    .collect(Collectors.toList());

                applicableReadEndpoints.remove(globalEndpointManager.getRegionName(locationWithStaleUnavailabilityInfo, OperationType.Read));

                queryRequestOptions.setExcludedRegions(applicableReadEndpoints);
                queryRequestOptionsAccessor.disablePerPartitionCircuitBreaker(queryRequestOptions);

                String spanName = "queryItems." + collectionLink;

                QueryFeedOperationState queryFeedOperationState = new QueryFeedOperationState(
                    this.rxDocClientSnapshot.get().getCachedCosmosAsyncClientSnapshot(),
                    spanName,
                    PathsHelper.getDatabasePath(collectionLink),
                    collectionLink,
                    ResourceType.Document,
                    OperationType.Read,
                    spanName,
                    queryRequestOptions,
                    new CosmosPagedFluxOptions());

                RxDocumentClientImpl rxDocumentClient = this.rxDocClientSnapshot.get();

                PartitionLevelLocationUnavailabilityInfo partitionLevelLocationUnavailabilityInfo = this.partitionKeyRangeToLocationSpecificUnavailabilityInfo.get(partitionKeyRangeWrapper);

                if (partitionLevelLocationUnavailabilityInfo != null) {
                    return rxDocumentClient
                        .queryDocuments(collectionLink, "SELECT * FROM C OFFSET 0 LIMIT 1", queryFeedOperationState, Document.class)
                        .publishOn(CosmosSchedulers.PARTITION_AVAILABILITY_STALENESS_CHECK_SINGLE)
                        .doOnComplete(() -> {

                            if (logger.isDebugEnabled()) {
                                logger.debug("Partition health recovery query for partition key range : {}-{} and " +
                                        "collection rid : {} has succeeded...",
                                    partitionKeyRangeWrapper.getPartitionKeyRange().getMinInclusive(),
                                    partitionKeyRangeWrapper.getPartitionKeyRange().getMaxExclusive(),
                                    partitionKeyRangeWrapper.getResourceId());
                            }

                            partitionLevelLocationUnavailabilityInfo.locationEndpointToLocationSpecificContextForPartition.compute(locationWithStaleUnavailabilityInfo, (locationWithStaleUnavailabilityInfoAsKey, locationSpecificContextAsVal) -> {

                                if (locationSpecificContextAsVal != null) {
                                    locationSpecificContextAsVal = GlobalPartitionEndpointManagerForCircuitBreaker
                                        .this.locationSpecificHealthContextTransitionHandler.handleSuccess(
                                        locationSpecificContextAsVal,
                                        partitionKeyRangeWrapper,
                                        locationWithStaleUnavailabilityInfoAsKey,
                                        false,
                                        true);
                                }
                                return locationSpecificContextAsVal;
                            });
                        });
                }

                return Flux.empty();
            })
            .onErrorResume(throwable -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("An exception was thrown trying to recover an Unavailable partition key range!");
                }
                return Flux.empty();
            });
    }

    public boolean isPartitionLevelCircuitBreakingApplicable(RxDocumentServiceRequest request) {

        if (!this.consecutiveExceptionBasedCircuitBreaker.isPartitionLevelCircuitBreakerEnabled()) {
            return false;
        }

        if (request.requestContext.isPerPartitionCircuitBreakerDisabledForRequest()) {
            return false;
        }

        if (request.getResourceType() != ResourceType.Document) {
            return false;
        }

        if (request.getOperationType() == OperationType.QueryPlan) {
            return false;
        }

        GlobalEndpointManager globalEndpointManager = this.globalEndpointManager;

        if (!globalEndpointManager.canUseMultipleWriteLocations(request)) {
            return false;
        }

        UnmodifiableList<URI> applicableWriteEndpoints = globalEndpointManager.getApplicableWriteEndpoints(Collections.emptyList());

        return applicableWriteEndpoints != null && applicableWriteEndpoints.size() > 1;
    }

    public void setRxDocumentClientImplSnapshot(RxDocumentClientImpl rxDocumentClient) {
        this.rxDocClientSnapshot.set(rxDocumentClient);
    }

    private class PartitionLevelLocationUnavailabilityInfo {

        private final ConcurrentHashMap<URI, LocationSpecificHealthContext> locationEndpointToLocationSpecificContextForPartition;
        private final ConcurrentHashMap<String, LocationSpecificHealthContext> regionToLocationSpecificHealthContext;
        private final LocationSpecificHealthContextTransitionHandler locationSpecificHealthContextTransitionHandler;

        private PartitionLevelLocationUnavailabilityInfo() {
            this.locationEndpointToLocationSpecificContextForPartition = new ConcurrentHashMap<>();
            this.regionToLocationSpecificHealthContext = new ConcurrentHashMap<>();
            this.locationSpecificHealthContextTransitionHandler = GlobalPartitionEndpointManagerForCircuitBreaker.this.locationSpecificHealthContextTransitionHandler;
        }

        private boolean handleException(
            PartitionKeyRangeWrapper partitionKeyRangeWrapper,
            String collectionLink,
            URI locationWithException,
            boolean isReadOnlyRequest) {

            AtomicBoolean isExceptionThresholdBreached = new AtomicBoolean(false);

            this.locationEndpointToLocationSpecificContextForPartition.compute(locationWithException, (locationAsKey, locationSpecificContextAsVal) -> {

                if (locationSpecificContextAsVal == null) {

                    locationSpecificContextAsVal = new LocationSpecificHealthContext.Builder()
                        .withSuccessCountForWriteForRecovery(0)
                        .withExceptionCountForWriteForCircuitBreaking(0)
                        .withSuccessCountForReadForRecovery(0)
                        .withExceptionCountForReadForCircuitBreaking(0)
                        .withUnavailableSince(Instant.MAX)
                        .withLocationHealthStatus(LocationHealthStatus.HealthyWithFailures)
                        .withExceptionThresholdBreached(false)
                        .withLastCollectionLinkSeen(collectionLink)
                        .build();
                }

                LocationSpecificHealthContext locationSpecificHealthContextAfterTransition = this.locationSpecificHealthContextTransitionHandler.handleException(
                    locationSpecificContextAsVal,
                    partitionKeyRangeWrapper,
                    GlobalPartitionEndpointManagerForCircuitBreaker.this.partitionKeyRangesWithPossibleUnavailableRegions,
                    locationWithException,
                    isReadOnlyRequest);

                this.regionToLocationSpecificHealthContext.put(
                    GlobalPartitionEndpointManagerForCircuitBreaker
                        .this.globalEndpointManager
                        .getRegionName(locationAsKey, isReadOnlyRequest ? OperationType.Read : OperationType.Create),
                    locationSpecificHealthContextAfterTransition);

                isExceptionThresholdBreached.set(locationSpecificHealthContextAfterTransition.isExceptionThresholdBreached());
                return locationSpecificHealthContextAfterTransition;
            });

            return isExceptionThresholdBreached.get();
        }

        private void handleSuccess(
            PartitionKeyRangeWrapper partitionKeyRangeWrapper,
            String collectionLink,
            URI succeededLocation,
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
                        .withLastCollectionLinkSeen(collectionLink)
                        .build();
                }

                locationSpecificHealthContextAfterTransition = this.locationSpecificHealthContextTransitionHandler.handleSuccess(
                    locationSpecificContextAsVal,
                    partitionKeyRangeWrapper,
                    succeededLocation,
                    false,
                    isReadOnlyRequest);

                this.regionToLocationSpecificHealthContext.put(
                    GlobalPartitionEndpointManagerForCircuitBreaker
                        .this.globalEndpointManager
                        .getRegionName(locationAsKey, isReadOnlyRequest ? OperationType.Read : OperationType.Create),
                    locationSpecificHealthContextAfterTransition);

                return locationSpecificHealthContextAfterTransition;
            });
        }

        public boolean areLocationsAvailableForPartitionKeyRange(List<URI> availableLocationsAtAccountLevel) {

            for (URI availableLocation : availableLocationsAtAccountLevel) {
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

    private static String getCollectionLink(RxDocumentServiceRequest request) {

        checkNotNull(request, "Argument 'request' cannot be null!");
        checkNotNull(request.requestContext, "Argument 'request.requestContext' cannot be null!");

        PointOperationContextForCircuitBreaker pointOperationContextForCircuitBreaker
            = request.requestContext.getPointOperationContextForCircuitBreaker();
        FeedOperationContextForCircuitBreaker feedOperationContextForCircuitBreaker
            = request.requestContext.getFeedOperationContextForCircuitBreaker();

        if (pointOperationContextForCircuitBreaker != null) {
            checkNotNull(
                pointOperationContextForCircuitBreaker.getCollectionLink(),
                "Argument 'pointOperationContextForCircuitBreaker.getCollectionLink()' cannot be null!");
            return pointOperationContextForCircuitBreaker.getCollectionLink();
        }

        if (feedOperationContextForCircuitBreaker != null) {
            checkNotNull(
                feedOperationContextForCircuitBreaker.getCollectionLink(),
                "Argument 'feedOperationContextForCircuitBreaker.getCollectionLink()' cannot be null!");
            return feedOperationContextForCircuitBreaker.getCollectionLink();
        }

        throw new IllegalStateException("Both pointOperationContextForCircuitBreaker [or] feedOperationContextForCircuitBreaker cannot be null!");
    }
}
