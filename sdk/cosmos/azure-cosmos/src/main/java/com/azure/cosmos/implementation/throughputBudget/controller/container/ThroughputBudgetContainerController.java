// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputBudget.controller.container;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.ThroughputBudgetGroupConfig;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.changefeed.CancellationToken;
import com.azure.cosmos.implementation.changefeed.CancellationTokenSource;
import com.azure.cosmos.implementation.throughputBudget.ThroughputResolveLevel;
import com.azure.cosmos.implementation.throughputBudget.controller.IThroughputBudgetController;
import com.azure.cosmos.implementation.throughputBudget.controller.group.ThroughputBudgetGroupControllerBase;
import com.azure.cosmos.implementation.throughputBudget.controller.group.ThroughputBudgetGroupControllerFactory;
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

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ThroughputBudgetContainerController implements IThroughputBudgetController {
    private static final Logger logger = LoggerFactory.getLogger(ThroughputBudgetContainerController.class);

    private final Duration DEFAULT_REFRESH_THROUGHPUT_INTERVAL = Duration.ofSeconds(60); //TODO: should this be longer time period?

    private final CancellationTokenSource cancellationTokenSource;
    private final AsyncDocumentClient client;
    private final ConnectionMode connectionMode;
    private final ConcurrentHashMap<String, ThroughputBudgetGroupControllerBase> groupControllers;
    private final List<ThroughputBudgetGroupConfig> groupConfigs;
    private final String hostName;
    private final AtomicReference<Integer> maxContainerThroughput;
    private final RxPartitionKeyRangeCache partitionKeyRangeCache;
    private final Scheduler scheduler;
    private final CosmosAsyncContainer targetContainer;

    private ThroughputBudgetGroupControllerBase defaultGroupController;
    private String resolvedContainerRid;
    private String resolvedDatabaseRid;
    private ThroughputResolveLevel throughputResolveLevel;


    public ThroughputBudgetContainerController(
        ConnectionMode connectionMode,
        List<ThroughputBudgetGroupConfig> groups,
        String hostName,
        RxPartitionKeyRangeCache partitionKeyRangeCache) {

        checkArgument(groups != null && groups.size() > 0, "Throughput budget groups can not be null or empty");
        checkArgument(StringUtils.isNotEmpty(hostName), "Host name cannot be null or empty");
        checkNotNull(partitionKeyRangeCache, "PartitionKeyRange cache can not be null");

        this.cancellationTokenSource = new CancellationTokenSource();
        this.connectionMode = connectionMode;
        this.groupControllers = new ConcurrentHashMap<>();
        this.groupConfigs = groups;
        this.hostName = hostName;
        this.maxContainerThroughput = new AtomicReference<>(null);
        this.partitionKeyRangeCache = partitionKeyRangeCache;

        this.targetContainer = groups.get(0).getTargetContainer();
        this.client = CosmosBridgeInternal.getContextClient(this.targetContainer);

        this.throughputResolveLevel = this.getThroughputRefreshLevel(groups);
        this.scheduler = Schedulers.elastic();
    }

    @Override
    public Mono<Void> close() {
        this.cancellationTokenSource.cancel();
        return Flux.fromIterable(this.groupControllers.values())
            .flatMap(groupController -> groupController.close())
            .then();
    }

    public Mono<ThroughputBudgetContainerController> init() {
        return this.resolveDatabaseResourceId()
            .then(this.resolveContainerResourceId())
            .then(this.resolveProvisionedThroughput())
            .thenMany(this.createAndInitializeGroupControllers())
            .flatMap(groupController -> {
                if (groupController.isUseByDefault()) {
                    if (this.defaultGroupController != null) {
                        // We should never reach here since we do config validation to make sure only one default group configured
                        // when enable throughput budget control
                        return Mono.error(new IllegalArgumentException("There should only be one default group"));
                    }

                    this.defaultGroupController = groupController;
                }

                return Mono.empty();
            })
            .doOnComplete(() -> scheduler.schedule(() -> this.refreshThroughputTask(this.cancellationTokenSource.getToken())))
            .then(Mono.just(this));
    }

    @Override
    public <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> nextProcessRequestMono) {
        return Mono.just(request)
            .flatMap(request1 -> {
                if (request1.getThroughputGroup() == null) {
                    return Mono.just(this.defaultGroupController);
                } else {
                    return Mono.justOrEmpty(this.groupControllers.get(request.getThroughputGroup()))
                        .defaultIfEmpty(this.defaultGroupController);
                }
            })
            .flatMap(groupController -> {
                if (groupController != null) {
                    return groupController.processRequest(request, nextProcessRequestMono);
                }

                return nextProcessRequestMono;
            });
    }

    public String getResolvedContainerRid() {
        return this.resolvedContainerRid;
    }

    public boolean canHandleRequest(RxDocumentServiceRequest request) {
        checkNotNull(request, "Request can not be null");
        if (StringUtils.equals(this.resolvedContainerRid, request.requestContext.resolvedCollectionRid)) {
            return true;
        }

        return false;
    }

    private Flux<ThroughputBudgetGroupControllerBase> createAndInitializeGroupControllers() {
        return Flux.fromIterable(this.groupConfigs)
            .flatMap(groupConfig -> {
                ThroughputBudgetGroupControllerBase groupController =
                    ThroughputBudgetGroupControllerFactory.createController(
                        this.connectionMode,
                        groupConfig,
                        this.hostName,
                        this.maxContainerThroughput.get(),
                        this.partitionKeyRangeCache,
                        this.resolvedContainerRid);

                this.groupControllers.compute(groupConfig.getGroupName(), (groupName, controller) -> {
                    if (controller != null) {
                        // This should never happen, since we validate group configuration when enable throughput budget control
                        throw new IllegalArgumentException(String.format("Duplicate group %s configuration", groupName));
                    }

                    return groupController;
                });
                return Mono.just(groupController);
            })
            .flatMap(groupController -> groupController.init());
    }

    private ThroughputResolveLevel getThroughputRefreshLevel(List<ThroughputBudgetGroupConfig> groupConfigs) {
        if (groupConfigs.stream().anyMatch(groupConfig -> groupConfig.getThroughputLimitThreshold() != null)) {
            // Throughput can be provisioned on container level or database level, will start from container
            return ThroughputResolveLevel.CONTAINER;
        } else {
            return ThroughputResolveLevel.NONE;
        }
    }

    private boolean isNoOfferException(Throwable throwable) {
        checkNotNull(throwable, "Throwable should not be null");

        CosmosException cosmosException = Utils.as(throwable, CosmosException.class);
        return cosmosException != null
            && cosmosException.getStatusCode() == HttpConstants.StatusCodes.BADREQUEST
            && cosmosException.getSubStatusCode() == HttpConstants.SubStatusCodes.UNKNOWN;
    }

    private Flux<Void> refreshThroughputTask(CancellationToken cancellationToken) {
        if (this.throughputResolveLevel == ThroughputResolveLevel.NONE) {
            return Flux.empty();
        }

        return this.resolveProvisionedThroughput()
            .flatMapIterable((avoid) -> this.groupControllers.values())
            .flatMap(groupController -> groupController.onMaxContainerThroughputRefresh(this.maxContainerThroughput.get()))
            .onErrorResume(throwable -> {
                if (this.isNoOfferException(throwable)) {
                    // The resource does not exist any more
                    this.close();
                }

                logger.warn("Refersh throughput failed with reason %s", throwable);
                return Mono.empty();
            })
            .delayElements(DEFAULT_REFRESH_THROUGHPUT_INTERVAL)
            .then()
            .repeat(() -> !cancellationToken.isCancellationRequested());
    }

    private Mono<ThroughputResponse> resolveThroughputByResourceId(String resourceId) {
        checkArgument(StringUtils.isNotEmpty(resourceId), "ResourceId can not be null or empty");
        return this.client.queryOffers(resourceId, new CosmosQueryRequestOptions())
            .single()
            .flatMap(offerFeedResponse -> {
                if (offerFeedResponse.getResults().isEmpty()) {
                    return Mono.error(
                        BridgeInternal.createCosmosException(HttpConstants.StatusCodes.BADREQUEST, "No offers found for the resource " + resourceId));
                }

                return this.client.readOffer(offerFeedResponse.getResults().get(0).getSelfLink()).single();
            })
            .map(ModelBridgeInternal::createThroughputRespose);
    }

    private Mono<Void> resolveContainerResourceId() {
        return this.targetContainer.read()
            .flatMap(response -> {
                this.resolvedContainerRid = response.getProperties().getResourceId();
                return Mono.empty();
            });
    }

    private Mono<Void> resolveDatabaseResourceId() {
        return this.targetContainer.getDatabase().read()
            .flatMap(response -> {
                this.resolvedDatabaseRid = response.getProperties().getResourceId();
                return Mono.empty();
            });
    }

    private Mono<Void> resolveProvisionedThroughput() {
        return Mono.defer(() -> Mono.just(this.throughputResolveLevel))
            .flatMap(throughputResolveLevel -> {
                if (throughputResolveLevel == ThroughputResolveLevel.CONTAINER) {
                    return this.resolveThroughputByResourceId(this.resolvedContainerRid)
                        .doOnError(throwable -> {
                            if (this.isNoOfferException(throwable)) {
                                this.throughputResolveLevel = ThroughputResolveLevel.DATABASE;
                            }
                        });
                } else if (throughputResolveLevel == ThroughputResolveLevel.DATABASE) {
                    return this.resolveThroughputByResourceId(this.resolvedDatabaseRid)
                        .doOnError(throwable -> {
                            if (this.isNoOfferException(throwable)) {
                                this.throughputResolveLevel = ThroughputResolveLevel.CONTAINER;
                            }
                        });
                }

                // All he underying throughput groups are using constant throughput, no need to resolve throughput
                return Mono.empty();
            })
            .flatMap(throughputResponse -> {
                this.updateMaxContainerThroughput(throughputResponse);
                return Mono.empty();
            })
            .retryWhen(
                // Throughput can be configured on database level or container level
                // Retry at most 1 time so we can try on database and container both
                RetrySpec.max(1).filter(throwable -> this.isNoOfferException(throwable))
            ).then();
    }

    private void updateMaxContainerThroughput(ThroughputResponse throughputResponse) {
        checkNotNull(throughputResponse, "Throughput response can not be null");

        ThroughputProperties throughputProperties = throughputResponse.getProperties();
        this.maxContainerThroughput.set(
            Math.max(throughputProperties.getAutoscaleMaxThroughput(), throughputProperties.getManualThroughput()));
    }
}
