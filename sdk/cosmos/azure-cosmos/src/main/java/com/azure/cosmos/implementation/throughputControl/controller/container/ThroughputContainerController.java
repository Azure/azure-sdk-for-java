// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.container;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.throughputControl.LinkedCancellationToken;
import com.azure.cosmos.implementation.throughputControl.LinkedCancellationTokenSource;
import com.azure.cosmos.implementation.throughputControl.config.ThroughputControlGroupInternal;
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
import reactor.util.retry.RetrySpec;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
    private static final int NO_OFFER_EXCEPTION_SUB_STATUS_CODE = HttpConstants.SubStatusCodes.OFFER_NOT_CONFIGURED;

    private final AsyncDocumentClient client;
    private final RxCollectionCache collectionCache;
    private final ConnectionMode connectionMode;
    private final AsyncCache<String, ThroughputGroupControllerBase> groupControllerCache;
    private final Set<ThroughputControlGroupInternal> groups;
    private final AtomicReference<Integer> maxContainerThroughput;
    private final RxPartitionKeyRangeCache partitionKeyRangeCache;
    private final CosmosAsyncContainer targetContainer;

    private final LinkedCancellationTokenSource cancellationTokenSource;
    private final ConcurrentHashMap<String, LinkedCancellationToken> cancellationTokenMap;

    private ThroughputGroupControllerBase defaultGroupController;
    private String targetContainerRid;
    private String targetDatabaseRid;
    private ThroughputProvisioningScope throughputProvisioningScope;

    public ThroughputContainerController(
        RxCollectionCache collectionCache,
        ConnectionMode connectionMode,
        Set<ThroughputControlGroupInternal> groups,
        RxPartitionKeyRangeCache partitionKeyRangeCache,
        LinkedCancellationToken parentToken) {

        checkNotNull(collectionCache, "Collection cache can not be null");
        checkArgument(groups != null && groups.size() > 0, "Throughput budget groups can not be null or empty");
        checkNotNull(partitionKeyRangeCache, "RxPartitionKeyRangeCache can not be null");

        this.collectionCache = collectionCache;
        this.connectionMode = connectionMode;
        this.groupControllerCache = new AsyncCache<>();
        this.groups = groups;

        this.maxContainerThroughput = new AtomicReference<>();
        this.partitionKeyRangeCache = partitionKeyRangeCache;

        this.targetContainer = groups.iterator().next().getTargetContainer();
        this.client = CosmosBridgeInternal.getContextClient(this.targetContainer);

        this.throughputProvisioningScope = this.getThroughputResolveLevel(groups);

        this.cancellationTokenSource = new LinkedCancellationTokenSource(parentToken);
        this.cancellationTokenMap = new ConcurrentHashMap<>();
    }

    private ThroughputProvisioningScope getThroughputResolveLevel(Set<ThroughputControlGroupInternal> groupConfigs) {
        if (groupConfigs.stream().anyMatch(groupConfig -> groupConfig.getTargetThroughputThreshold() != null)) {
            // Throughput can be provisioned on container level or database level, will start from container
            return ThroughputProvisioningScope.CONTAINER;
        } else {
            // There is no group configured with throughput threshold, so no need to query throughput
            return ThroughputProvisioningScope.NONE;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<T> init() {
        return this.resolveContainerResourceId()
            .flatMap(containerRid -> this.resolveContainerMaxThroughput())
            .flatMap(controller -> this.createAndInitializeGroupControllers())
            .doOnSuccess(controller -> {
                CosmosSchedulers.COSMOS_PARALLEL.schedule(() ->
                    this.refreshContainerMaxThroughputTask(this.cancellationTokenSource.getToken()).subscribe());
            })
            .thenReturn((T) this);
    }

    private Mono<String> resolveDatabaseResourceId() {
        return this.targetContainer.getDatabase().read()
            .flatMap(response -> {
                this.targetDatabaseRid = response.getProperties().getResourceId();
                return Mono.just(this.targetDatabaseRid);
            });
    }

    private Mono<String> resolveContainerResourceId() {
        return this.targetContainer.read()
            .flatMap(response -> {
                this.targetContainerRid = response.getProperties().getResourceId();
                return Mono.just(this.targetContainerRid);
            });
    }

    private Mono<ThroughputResponse> resolveDatabaseThroughput() {
        return Mono.justOrEmpty(this.targetDatabaseRid)
            .switchIfEmpty(this.resolveDatabaseResourceId())
            .flatMap(databaseRid -> this.resolveThroughputByResourceId(databaseRid));
    }

    private Mono<ThroughputResponse> resolveContainerThroughput() {
        if (StringUtils.isEmpty(this.targetContainerRid)) {
            return this.resolveContainerResourceId()
                .flatMap(containerRid -> this.resolveThroughputByResourceId(containerRid))
                .onErrorResume(throwable -> {
                    if (this.isOwnerResourceNotExistsException(throwable)) {
                        // During initialization time, the collection cache may contain staled info,
                        // refresh and retry one more time
                        this.collectionCache.refresh(
                            null,
                            BridgeInternal.getLink(this.targetContainer),
                            null
                        );
                    }

                    return Mono.error(throwable);
                })
                .retryWhen(RetrySpec.max(1).filter(throwable -> this.isOwnerResourceNotExistsException(throwable)));
        } else {
            return Mono.just(this.targetContainerRid)
                .flatMap(containerRid -> this.resolveThroughputByResourceId(containerRid));
        }
    }

    private Mono<ThroughputContainerController> resolveContainerMaxThroughput() {
        return Mono.defer(() -> Mono.just(this.throughputProvisioningScope))
            .flatMap(throughputProvisioningScope -> {
                if (throughputProvisioningScope == ThroughputProvisioningScope.CONTAINER) {
                    return this.resolveContainerThroughput()
                        .onErrorResume(throwable -> {
                            if (this.isOfferNotConfiguredException(throwable)) {
                                this.throughputProvisioningScope = ThroughputProvisioningScope.DATABASE;
                            }

                            return Mono.error(throwable);
                        });
                } else if (throughputProvisioningScope == ThroughputProvisioningScope.DATABASE) {
                    return this.resolveDatabaseThroughput()
                        .onErrorResume(throwable -> {
                            if (this.isOfferNotConfiguredException(throwable)) {
                                this.throughputProvisioningScope = ThroughputProvisioningScope.CONTAINER;
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
            .onErrorResume(throwable -> {
                if (this.isOwnerResourceNotExistsException(throwable)) {
                    this.cancellationTokenSource.close();
                }

                return Mono.error(throwable);
            })
            .retryWhen(
                // Throughput can be configured on database level or container level
                // Retry at most 1 time so we can try on database and container both
                RetrySpec.max(1).filter(throwable -> this.isOfferNotConfiguredException(throwable))
            ).thenReturn(this);
    }

    private Mono<ThroughputResponse> resolveThroughputByResourceId(String resourceId) {
        // Note: for serverless account, when we trying to query offers,
        // we will get 400/0 with error message: Reading or replacing offers is not supported for serverless accounts.
        // We are not supporting serverless account for throughput control for now. But the protocol may change in future,
        // use https://github.com/Azure/azure-sdk-for-java/issues/18776 to keep track for possible future work.
        checkArgument(StringUtils.isNotEmpty(resourceId), "ResourceId can not be null or empty");
        return this.client.queryOffers(
                    BridgeInternal.getOfferQuerySpecFromResourceId(this.targetContainer, resourceId), new CosmosQueryRequestOptions())
            .single()
            .flatMap(offerFeedResponse -> {
                if (offerFeedResponse.getResults().isEmpty()) {
                    CosmosException noOfferException =
                        BridgeInternal.createCosmosException(NO_OFFER_EXCEPTION_STATUS_CODE, "No offers found for the resource " + resourceId);

                    BridgeInternal.setSubStatusCode(noOfferException, NO_OFFER_EXCEPTION_SUB_STATUS_CODE);
                    return Mono.error(noOfferException);
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

    private boolean isOwnerResourceNotExistsException(Throwable throwable) {
        checkNotNull(throwable, "Throwable should not be null");

        CosmosException cosmosException = Utils.as(Exceptions.unwrap(throwable), CosmosException.class);
        return cosmosException != null
            && cosmosException.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND
            && cosmosException.getSubStatusCode() == HttpConstants.SubStatusCodes.OWNER_RESOURCE_NOT_EXISTS;
    }

    @Override
    public <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> originalRequestMono) {
        checkNotNull(request, "Request can not be null");
        checkNotNull(originalRequestMono, "Original request mono can not be null");

        return this.getOrCreateThroughputGroupController(request.getThroughputControlGroupName())
            .flatMap(groupController -> {
                if (groupController.v != null) {
                    return groupController.v.processRequest(request, originalRequestMono);
                }

                return originalRequestMono;
            });
    }

    // TODO: a better way to handle throughput control group enabled after the container initialization
    private Mono<Utils.ValueHolder<ThroughputGroupControllerBase>> getOrCreateThroughputGroupController(String groupName) {

        // If there is no control group defined, using the default group controller
        if (StringUtils.isEmpty(groupName)) {
            return Mono.just(new Utils.ValueHolder<>(this.defaultGroupController));
        }

        for (ThroughputControlGroupInternal group : this.groups) {
            if (StringUtils.equals(groupName, group.getGroupName())) {
                return this.resolveThroughputGroupController(group)
                    .map(Utils.ValueHolder::new);
            }
        }

        // If the request is associated with a group not enabled, will fall back to the default one.
        return Mono.just(new Utils.ValueHolder<>(this.defaultGroupController));
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

    private Mono<ThroughputGroupControllerBase> resolveThroughputGroupController(ThroughputControlGroupInternal group) {
        return this.groupControllerCache.getAsync(
            group.getGroupName(),
            null,
            () -> this.createAndInitializeGroupController(group));
    }

    private Mono<ThroughputGroupControllerBase> createAndInitializeGroupController(ThroughputControlGroupInternal group) {
        LinkedCancellationToken parentToken =
            this.cancellationTokenMap.compute(
                group.getGroupName(),
                (key, cancellationToken) -> this.cancellationTokenSource.getToken());

        ThroughputGroupControllerBase groupController = ThroughputGroupControllerFactory.createController(
            this.connectionMode,
            group,
            this.maxContainerThroughput.get(),
            this.partitionKeyRangeCache,
            this.targetContainerRid,
            parentToken);

        return groupController
            .init()
            .cast(ThroughputGroupControllerBase.class)
            .doOnSuccess(controller -> {
                if (controller.isDefault()) {
                    this.defaultGroupController = controller;
                }
            });

    }

    private Flux<Void> refreshContainerMaxThroughputTask(LinkedCancellationToken cancellationToken) {
        checkNotNull(cancellationToken, "Cancellation token can not be null");

        if (this.throughputProvisioningScope == ThroughputProvisioningScope.NONE) {
            return Flux.empty();
        }

        return Mono.delay(DEFAULT_THROUGHPUT_REFRESH_INTERVAL, CosmosSchedulers.COSMOS_PARALLEL)
            .flatMap(t -> {
                if (cancellationToken.isCancellationRequested()) {
                    return Mono.empty();
                } else {
                    return this.resolveContainerMaxThroughput();
                }
            })
            .flatMapIterable(controller -> this.groups)
            .flatMap(group -> this.resolveThroughputGroupController(group))
            .doOnNext(groupController -> groupController.onContainerMaxThroughputRefresh(this.maxContainerThroughput.get()))
            .onErrorResume(throwable -> {
                logger.warn("Refresh throughput failed with reason %s", throwable);
                return Mono.empty();
            })
            .then()
            .repeat(() -> !cancellationToken.isCancellationRequested());
    }
}
