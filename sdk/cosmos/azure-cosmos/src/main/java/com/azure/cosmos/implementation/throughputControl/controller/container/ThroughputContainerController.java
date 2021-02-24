// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.container;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.ThroughputControlGroup;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.changefeed.CancellationToken;
import com.azure.cosmos.implementation.changefeed.CancellationTokenSource;
import com.azure.cosmos.implementation.throughputControl.ThroughputResolveLevel;
import com.azure.cosmos.implementation.throughputControl.controller.group.ThroughputGroupControllerBase;
import com.azure.cosmos.implementation.throughputControl.controller.group.ThroughputGroupControllerFactory;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.RetrySpec;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Three main purpose of container controller:
 * 1. Resolve max container throughput
 * 2. Create and initialize throughput group controllers
 * 3. Start throughput refresh task if necessary
 */
public class ThroughputContainerController implements IThroughputContainerController {
    private static final Logger logger = LoggerFactory.getLogger(ThroughputContainerController.class);

    private static final Duration DEFAULT_THROUGHPUT_REFRESH_INTERVAL = Duration.ofMinutes(15);
    private static final int NO_OFFER_EXCEPTION_STATUS_CODE = HttpConstants.StatusCodes.BADREQUEST;
    private static final int NO_OFFER_EXCEPTION_SUB_STATUS_CODE = HttpConstants.SubStatusCodes.UNKNOWN;

    private final AsyncDocumentClient client;
    private final ConnectionMode connectionMode;
    private final GlobalEndpointManager globalEndpointManager;
    private final AsyncCache<String, ThroughputGroupControllerBase> groupControllerCache;
    private final Set<ThroughputControlGroup> groups;
    private final AtomicReference<Integer> maxContainerThroughput;
    private final RxPartitionKeyRangeCache partitionKeyRangeCache;
    private final CosmosAsyncContainer targetContainer;

    private final CancellationTokenSource cancellationTokenSource;

    private ThroughputGroupControllerBase defaultGroupController;
    private String targetContainerRid;
    private String targetDatabaseRid;
    private ThroughputResolveLevel throughputResolveLevel;

    public ThroughputContainerController(
        ConnectionMode connectionMode,
        GlobalEndpointManager globalEndpointManager,
        Set<ThroughputControlGroup> groups,
        RxPartitionKeyRangeCache partitionKeyRangeCache) {

        checkNotNull(globalEndpointManager, "GlobalEndpointManager can not be null");
        checkArgument(groups != null && groups.size() > 0, "Throughput budget groups can not be null or empty");
        checkNotNull(partitionKeyRangeCache, "RxPartitionKeyRangeCache can not be null");

        this.connectionMode = connectionMode;
        this.globalEndpointManager = globalEndpointManager;
        this.groupControllerCache = new AsyncCache<>();
        this.groups = groups;

        this.maxContainerThroughput = new AtomicReference<>();
        this.partitionKeyRangeCache = partitionKeyRangeCache;

        this.targetContainer = BridgeInternal.getTargetContainerFromThroughputControlGroup(groups.iterator().next());
        this.client = CosmosBridgeInternal.getContextClient(this.targetContainer);

        this.throughputResolveLevel = this.getThroughputResolveLevel(groups);

        this.cancellationTokenSource = new CancellationTokenSource();
    }

    private ThroughputResolveLevel getThroughputResolveLevel(Set<ThroughputControlGroup> groupConfigs) {
        if (groupConfigs.stream().anyMatch(groupConfig -> groupConfig.getTargetThroughputThreshold() != null)) {
            // Throughput can be provisioned on container level or database level, will start from container
            return ThroughputResolveLevel.CONTAINER;
        } else {
            // There is no group configured with throughput threshold, so no need to query throughput
            return ThroughputResolveLevel.NONE;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<T> init() {
        return this.resolveDatabaseResourceId()
            .flatMap(controller -> this.resolveContainerResourceId())
            .flatMap(controller -> this.resolveContainerMaxThroughput())
            .flatMap(controller -> this.createAndInitializeGroupControllers())
            .doOnSuccess(controller -> {
                Schedulers.parallel().schedule(() -> this.refreshContainerMaxThroughputTask(this.cancellationTokenSource.getToken()).subscribe());
            })
            .thenReturn((T) this);
    }

    private Mono<ThroughputContainerController> resolveDatabaseResourceId() {
        return this.targetContainer.getDatabase().read()
            .flatMap(response -> {
                this.targetDatabaseRid = response.getProperties().getResourceId();
                return Mono.just(this);
            });
    }

    private Mono<ThroughputContainerController> resolveContainerResourceId() {
        return this.targetContainer.read()
            .flatMap(response -> {
                this.targetContainerRid = response.getProperties().getResourceId();
                return Mono.just(this);
            });
    }

    private Mono<ThroughputContainerController> resolveContainerMaxThroughput() {
        return Mono.just(this.throughputResolveLevel) // TODO: ---> test whether it works without defer
            .flatMap(throughputResolveLevel -> {
                if (throughputResolveLevel == ThroughputResolveLevel.CONTAINER) {
                    return this.resolveThroughputByResourceId(this.targetContainerRid)
                        .onErrorResume(throwable -> {
                            if (this.isOfferNotConfiguredException(throwable)) {
                                this.throughputResolveLevel = ThroughputResolveLevel.DATABASE;
                            }

                            return Mono.error(throwable);
                        });
                } else if (throughputResolveLevel == ThroughputResolveLevel.DATABASE) {
                    return this.resolveThroughputByResourceId(this.targetDatabaseRid)
                        .onErrorResume(throwable -> {
                            if (this.isOfferNotConfiguredException(throwable)) {
                                this.throughputResolveLevel = ThroughputResolveLevel.CONTAINER;
                            }

                            return Mono.error(throwable);
                        });
                }

                // All the underlying throughput control groups are using target throughput,
                // which is constant value, hence no need to resolve throughput
                return Mono.empty();
            })
            .flatMap(throughputResponse -> {
                this.updateMaxContainerThroughput(throughputResponse);
                return Mono.empty();
            })
            .retryWhen(
                // Throughput can be configured on database level or container level
                // Retry at most 1 time so we can try on database and container both
                RetrySpec.max(1).filter(throwable -> this.isOfferNotConfiguredException(throwable))
            ).thenReturn(this);
    }

    private Mono<ThroughputResponse> resolveThroughputByResourceId(String resourceId) {
        // TODO: figure out how this work for serveless account
        // TODO: work item: https://github.com/Azure/azure-sdk-for-java/issues/18776
        checkArgument(StringUtils.isNotEmpty(resourceId), "ResourceId can not be null or empty");
        return this.client.queryOffers(
                    BridgeInternal.getOfferQuerySpecFromResourceId(this.targetContainer, resourceId), new CosmosQueryRequestOptions())
            .single()
            .flatMap(offerFeedResponse -> {
                if (offerFeedResponse.getResults().isEmpty()) {
                    return Mono.error(
                        BridgeInternal.createCosmosException(NO_OFFER_EXCEPTION_STATUS_CODE, "No offers found for the resource " + resourceId));
                }

                return this.client.readOffer(offerFeedResponse.getResults().get(0).getSelfLink()).single();
            })
            .map(ModelBridgeInternal::createThroughputRespose);
    }

    private void updateMaxContainerThroughput(ThroughputResponse throughputResponse) {
        checkNotNull(throughputResponse, "Throughput response can not be null");

        ThroughputProperties throughputProperties = throughputResponse.getProperties();
        this.maxContainerThroughput.set(
            Math.max(throughputProperties.getAutoscaleMaxThroughput(), throughputProperties.getManualThroughput()));
    }

    private boolean isOfferNotConfiguredException(Throwable throwable) {
        checkNotNull(throwable, "Throwable should not be null");

        CosmosException cosmosException = Utils.as(Exceptions.unwrap(throwable), CosmosException.class);
        // the exception here should match what returned from method resolveThroughputByResourceId
        return cosmosException != null
            && cosmosException.getStatusCode() == NO_OFFER_EXCEPTION_STATUS_CODE
            && cosmosException.getSubStatusCode() == NO_OFFER_EXCEPTION_SUB_STATUS_CODE;
    }

    @Override
    public <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> originalRequestMono) {
        checkNotNull(request, "Request can not be null");
        checkNotNull(originalRequestMono, "Original request mono can not be null");

        return Mono.just(request)
            .flatMap(request1 -> {
                if (request1.getThroughputControlGroupName() == null) {
                    return Mono.just(new Utils.ValueHolder<>(this.defaultGroupController));
                }
                else {
                    return this.getOrCreateThroughputGroupController(request.getThroughputControlGroupName())
                        .defaultIfEmpty(this.defaultGroupController)
                        .map(Utils.ValueHolder::new);
                }
            })
            .flatMap(groupController -> {
                if (groupController.v != null) {
                    return groupController.v.processRequest(request, originalRequestMono);
                }

                return originalRequestMono;
            });
    }

    // TODO: a better way to handle throughput control group enabled after the container initialization
    private Mono<ThroughputGroupControllerBase> getOrCreateThroughputGroupController(String groupName) {

        if (StringUtils.isEmpty(groupName)) {
            return Mono.empty();
        }

        ThroughputControlGroup group =
            this.groups.stream().filter(groupConfig -> StringUtils.equals(groupName, groupConfig.getGroupName())).findFirst().orElse(null);
        if (group == null) {
            return Mono.empty();
        }

        return this.resolveThroughputGroupController(group);
    }

    public String getTargetContainerRid() {
        return this.targetContainerRid;
    }

    @Override
    public boolean canHandleRequest(RxDocumentServiceRequest request) {
        checkNotNull(request, "Request can not be null");

        return StringUtils.equals(this.targetContainerRid, request.requestContext.resolvedCollectionRid);
    }

    private Mono<ThroughputContainerController> createAndInitializeGroupControllers() {
        return Flux.fromIterable(this.groups)
            .flatMap(group -> this.resolveThroughputGroupController(group))
            .then(Mono.just(this));
    }

    private Mono<ThroughputGroupControllerBase> resolveThroughputGroupController(ThroughputControlGroup group) {
        return this.groupControllerCache.getAsync(
            group.getGroupName(),
            null,
            () -> this.createAndInitializeGroupController(group));
    }

    private Mono<ThroughputGroupControllerBase> createAndInitializeGroupController(ThroughputControlGroup group) {
        ThroughputGroupControllerBase groupController = ThroughputGroupControllerFactory.createController(
            this.connectionMode,
            this.globalEndpointManager,
            group,
            this.maxContainerThroughput.get(),
            this.partitionKeyRangeCache,
            this.targetContainerRid);

        return groupController
            .init()
            .cast(ThroughputGroupControllerBase.class)
            .doOnSuccess(controller -> {
                if (controller.isDefault()) {
                    this.defaultGroupController = controller;
                }
            });

    }

    private Flux<Void> refreshContainerMaxThroughputTask(CancellationToken cancellationToken) {
        checkNotNull(cancellationToken, "Cancellation token can not be null");

        if (this.throughputResolveLevel == ThroughputResolveLevel.NONE) {
            return Flux.empty();
        }

        return Mono.delay(DEFAULT_THROUGHPUT_REFRESH_INTERVAL)
            .flatMap(t -> this.resolveContainerMaxThroughput())
            .flatMapIterable(controller -> this.groups)
            .flatMap(group -> this.resolveThroughputGroupController(group))
            .doOnNext(groupController -> groupController.onContainerMaxThroughputRefresh(this.maxContainerThroughput.get()))
            .onErrorResume(throwable -> {
                //TODO: Figure out how serverless work
//                if (this.isOfferNotConfiguredException(throwable)) {
//                    // Throughput is not configured on container nor database, a good hint the resource does not exists any more
//                    this.close();
//                }

                logger.warn("Refresh throughput failed with reason %s", throwable);
                return Mono.empty();
            })
            .then()
            .repeat(() -> !cancellationToken.isCancellationRequested());
    }

    @Override
    public Mono<Void> close() {
        this.cancellationTokenSource.cancel();
        return Flux.fromIterable(this.groups)
            .flatMap(group -> this.resolveThroughputGroupController(group))
            .flatMap(groupController -> groupController.close())
            .then();
    }
}
