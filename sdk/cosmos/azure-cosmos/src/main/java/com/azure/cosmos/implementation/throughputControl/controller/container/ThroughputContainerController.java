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
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.RetrySpec;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ThroughputContainerController implements IThroughputContainerController {
    private static final Logger logger = LoggerFactory.getLogger(ThroughputContainerController.class);

    private static final Duration DEFAULT_THROUGHPUT_REFRESH_INTERVAL = Duration.ofSeconds(60);
    private static final int NO_OFFER_EXCEPTION_STATUS_CODE = HttpConstants.StatusCodes.BADREQUEST;
    private static final int NO_OFFER_EXCEPTION_SUB_STATUS_CODE = HttpConstants.SubStatusCodes.UNKNOWN;

    private final AsyncDocumentClient client;
    private final CancellationTokenSource cancellationTokenSource;
    private final ConnectionMode connectionMode;
    private final ConcurrentHashMap<String, ThroughputGroupControllerBase> groupControllers;
    private final List<ThroughputControlGroup> groups;
    private final AtomicReference<Integer> maxContainerThroughput;
    private final RxPartitionKeyRangeCache partitionKeyRangeCache;
    private final Scheduler scheduler;
    private final CosmosAsyncContainer targetContainer;

    private ThroughputGroupControllerBase defaultGroupController;
    private String targetContainerRid;
    private String targetDatabaseRid;
    private ThroughputResolveLevel throughputResolveLevel;

    public ThroughputContainerController(
        ConnectionMode connectionMode,
        List<ThroughputControlGroup> groups,
        RxPartitionKeyRangeCache partitionKeyRangeCache) {

        checkArgument(groups != null && groups.size() > 0, "Throughput budget groups can not be null or empty");
        checkNotNull(partitionKeyRangeCache, "PartitionKeyRange cache can not be null");

        this.cancellationTokenSource = new CancellationTokenSource();
        this.connectionMode = connectionMode;
        this.groupControllers = new ConcurrentHashMap<>();
        this.groups = groups;
        this.maxContainerThroughput = new AtomicReference<>(null);
        this.partitionKeyRangeCache = partitionKeyRangeCache;

        this.targetContainer = groups.get(0).getTargetContainer();
        this.client = CosmosBridgeInternal.getContextClient(this.targetContainer);

        this.throughputResolveLevel = this.getThroughputResolveLevel(groups);
        this.scheduler = Schedulers.elastic();
    }

    private ThroughputResolveLevel getThroughputResolveLevel(List<ThroughputControlGroup> groupConfigs) {
        if (groupConfigs.stream().anyMatch(groupConfig -> groupConfig.getTargetThroughputThreshold() != null)) {
            // Throughput can be provisioned on container level or database level, will start from container
            return ThroughputResolveLevel.CONTAINER;
        } else {
            return ThroughputResolveLevel.NONE;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<T> init() {
        return this.resolveDatabaseResourceId()
            .then(this.resolveContainerResourceId())
            .then(this.resolveContainerMaxThroughput())
            .then(this.createAndInitializeGroupControllers())
            .doOnSuccess(avoid -> {
                this.setDefaultGroupController();
                scheduler.schedule(() -> this.refreshContainerMaxThroughputTask(this.cancellationTokenSource.getToken()).subscribe());
            })
            .thenReturn((T)this);
    }

    private Mono<Void> resolveDatabaseResourceId() {
        return this.targetContainer.getDatabase().read()
            .flatMap(response -> {
                this.targetDatabaseRid = response.getProperties().getResourceId();
                return Mono.empty();
            });
    }

    private Mono<Void> resolveContainerResourceId() {
        return this.targetContainer.read()
            .flatMap(response -> {
                this.targetContainerRid = response.getProperties().getResourceId();
                return Mono.empty();
            });
    }

    private Mono<Void> resolveContainerMaxThroughput() {
        return Mono.defer(() -> Mono.just(this.throughputResolveLevel))
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
            ).then();
    }

    private Mono<ThroughputResponse> resolveThroughputByResourceId(String resourceId) {
        checkArgument(StringUtils.isNotEmpty(resourceId), "ResourceId can not be null or empty");
        return this.client.queryOffers(resourceId, new CosmosQueryRequestOptions())
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

    private void setDefaultGroupController() {
        List<ThroughputGroupControllerBase> defaultGroupControllers =
            this.groupControllers.values().stream().filter(ThroughputGroupControllerBase::isUseByDefault).collect(Collectors.toList());

        if (defaultGroupControllers.size() > 1) {
            // We should never reach here since we do config validation to make sure only one default group configured
            // when enable throughput control

            throw new IllegalArgumentException("There should only be one default throughput control group");
        }
        if (defaultGroupControllers.size() == 1) {
            this.defaultGroupController = defaultGroupControllers.get(0);
        }
    }

    private void updateMaxContainerThroughput(ThroughputResponse throughputResponse) {
        checkNotNull(throughputResponse, "Throughput response can not be null");

        ThroughputProperties throughputProperties = throughputResponse.getProperties();
        this.maxContainerThroughput.set(
            Math.max(throughputProperties.getAutoscaleMaxThroughput(), throughputProperties.getManualThroughput()));
    }

    private boolean isOfferNotConfiguredException(Throwable throwable) {
        checkNotNull(throwable, "Throwable should not be null");

        CosmosException cosmosException = Utils.as(throwable, CosmosException.class);
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
                    // TODO: if customer used a group not defined:
                    //  should we fall back to default? or let it passing through? or throw exception back to client?
                    return Mono.justOrEmpty(this.groupControllers.get(request1.getThroughputControlGroupName()))
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

    public String getTargetContainerRid() {
        return this.targetContainerRid;
    }

    @Override
    public boolean canHandleRequest(RxDocumentServiceRequest request) {
        checkNotNull(request, "Request can not be null");
        if (StringUtils.equals(this.targetContainerRid, request.requestContext.resolvedCollectionRid)) {
            return true;
        }

        return false;
    }

    private Mono<Void> createAndInitializeGroupControllers() {
        return Flux.fromIterable(this.groups)
            .flatMap(group -> {
                ThroughputGroupControllerBase groupController =
                    ThroughputGroupControllerFactory.createController(
                        this.connectionMode,
                        group,
                        this.maxContainerThroughput.get(),
                        this.partitionKeyRangeCache,
                        this.targetContainerRid);

                this.groupControllers.compute(group.getGroupName(), (groupName, controller) -> {
                    if (controller != null) {
                        // This should never happen, since we validate group configuration when enable throughput budget control
                        throw new IllegalArgumentException(String.format("Duplicate group %s configuration", groupName));
                    }

                    return groupController;
                });
                return Mono.just(groupController);
            })
            .flatMap(groupController -> groupController.init())
            .then();
    }

    private Flux<Void> refreshContainerMaxThroughputTask(CancellationToken cancellationToken) {
        if (this.throughputResolveLevel == ThroughputResolveLevel.NONE) {
            return Flux.empty();
        }

        return Mono.just(this)
            .delayElement(DEFAULT_THROUGHPUT_REFRESH_INTERVAL)
            .then(this.resolveContainerMaxThroughput())
            .then(Mono.fromCallable(() -> {
                return Flux.fromIterable(this.groupControllers.values())
                    .flatMap(groupController -> groupController.onContainerMaxThroughputRefresh(this.maxContainerThroughput.get()))
                    .then();
            }))
            .onErrorResume(throwable -> {
                if (this.isOfferNotConfiguredException(throwable)) {
                    // Throughput is not configured on container nor database, a good hint the resource does not exists any more
                    this.close();
                }

                logger.warn("Refresh throughput failed with reason %s", throwable);
                return Mono.empty();
            })
            .then()
            .repeat(() -> !cancellationToken.isCancellationRequested());
    }

    @Override
    public Mono<Void> close() {
        this.cancellationTokenSource.cancel();
        return Flux.fromIterable(this.groupControllers.values())
            .flatMap(groupController -> groupController.close())
            .then();
    }
}
