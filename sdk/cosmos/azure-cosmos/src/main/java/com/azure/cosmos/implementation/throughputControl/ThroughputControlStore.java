// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.ThroughputControlGroup;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.throughputControl.controller.IThroughputController;
import com.azure.cosmos.implementation.throughputControl.controller.container.EmptyThroughputContainerController;
import com.azure.cosmos.implementation.throughputControl.controller.container.IThroughputContainerController;
import com.azure.cosmos.implementation.throughputControl.controller.container.ThroughputContainerController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.Exceptions.isNameCacheStale;
import static com.azure.cosmos.implementation.Exceptions.isPartitionKeyMismatchException;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * This is the entrance class for the whole throughput control work flow pipeline.
 * The pipeline will consist of controllers which is implementation of {@link IThroughputController} and {@link ThroughputRequestThrottler}.
 *
 * Following is a high-level diagram of the pipeline:
 *
 *                                       +-------------------+
 *                                       |       Client      |
 *                                       +-------------------+
 *                                                 |
 *                                                 |
 *                                                 |
 *                       +---------------------------------------------------------+
 *                       |                    ThroughputControlStore               |
 *                       +---------------------------------------------------------+
 *                      /                                                           \
 *                     /                                                             \
 *                    /                                                               \
 *         +---------------------------------------+                   +---------------------------------------+
 *         |       Container A controller          |                   |      Container B controller           |
 *         +---------------------------------------+       ...         +---------------------------------------+
 *                  /                           \
 *                 /                             \
 *                /                               \
 *         +--------------------+                +---------------------+
 *         | Group 1 controller |                | Group 2 controller  |
 *         +--------------------+     ...        +---------------------+
 *                  |          \
 *                  |           \-------------\
 *                  |                          \
 *    +---------------------------+         +-----------------------------+
 *    |Global Request controller |   OR    | PkRanges Request controller  |
 *    +--------------------------+         +------------------------------+
 *                  |                             /                   \
 *                  |                            /                     \
 *                  |                           /                       \
 *       +------------------+        +------------------+            +------------------+
 *       |Request throttler |        |Request throttler |            |Request throttler |
 *       +------------------+        +------------------+  ...       +------------------+
 *
 *
  */
public class ThroughputControlStore {
    private static final Logger logger = LoggerFactory.getLogger(ThroughputControlStore.class);

    private final RxClientCollectionCache collectionCache;
    private final ConnectionMode connectionMode;
    private final AsyncCache<String, IThroughputContainerController> containerControllerCache;
    private final GlobalEndpointManager globalEndpointManager;
    private final ConcurrentHashMap<String, Set<ThroughputControlGroup>> groupMapByContainer;
    private final RxPartitionKeyRangeCache partitionKeyRangeCache;

    public ThroughputControlStore(
        RxClientCollectionCache collectionCache,
        ConnectionMode connectionMode,
        GlobalEndpointManager globalEndpointManager,
        RxPartitionKeyRangeCache partitionKeyRangeCache) {

        checkNotNull(collectionCache,"RxClientCollectionCache can not be null");
        checkNotNull(globalEndpointManager, "Global endpoint manager can not be null");
        checkNotNull(partitionKeyRangeCache, "PartitionKeyRangeCache can not be null");

        this.collectionCache = collectionCache;
        this.connectionMode = connectionMode;
        this.containerControllerCache = new AsyncCache<>();
        this.globalEndpointManager = globalEndpointManager;
        this.groupMapByContainer = new ConcurrentHashMap<>();
        this.partitionKeyRangeCache = partitionKeyRangeCache;
    }

    public void enableThroughputControlGroup(ThroughputControlGroup group) {
        checkNotNull(group, "Throughput control group cannot be null");

        String collectionLink = Utils.trimBeginningAndEndingSlashes(
            BridgeInternal.extractContainerSelfLink(
                BridgeInternal.getTargetContainerFromThroughputControlGroup(group)));
        this.groupMapByContainer.compute(collectionLink, (key, groupSet) -> {
            if (groupSet == null) {
                groupSet = new HashSet<>();
            }

            if (group.isDefault()) {
                if (groupSet.stream().anyMatch(ThroughputControlGroup::isDefault)) {
                    throw new IllegalArgumentException("A default group already exists");
                }
            }

            if (!groupSet.add(group)) {
                logger.warn("Can not add duplicate group");
            }

            if (groupSet.size() == 1) {
                // This is the first enabled group for the target container
                // Clean the current cache in case we have built EmptyThroughputContainerController.
                this.containerControllerCache.remove(collectionLink);
            }

            return groupSet;
        });
    }

    public <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> originalRequestMono) {
        checkNotNull(request, "Request can not be null");
        checkNotNull(originalRequestMono, "originalRequestMono can not be null");

        // Currently, we will only target two resource types.
        // If in the future we find other useful scenarios for throughput control, add more more resource type here.
        if (request.getResourceType() != ResourceType.Document && request.getResourceType() != ResourceType.StoredProcedure) {
            return originalRequestMono;
        }

        String collectionLink = Utils.getCollectionName(request.getResourceAddress());
        return this.resolveContainerController(collectionLink)
            .flatMap(containerController -> {
                if (containerController.canHandleRequest(request)) {
                    return Mono.just(containerController);
                }

                // Unable to find container controller to handle the request,
                // It is caused by control store out of sync or the request has staled info.
                // We will handle the first scenario by creating a new container controller,
                // while fall back to original request Mono for the second scenario.
                return this.shouldRefreshContainerController(collectionLink, request)
                    .flatMap(shouldRefresh -> {
                        if (shouldRefresh) {
                            containerController.close().subscribeOn(Schedulers.parallel()).subscribe();
                            this.containerControllerCache.refresh(collectionLink, () -> this.createAndInitContainerController(collectionLink));
                            return this.resolveContainerController(collectionLink);
                        }

                        // The container container controller is up to date, the request has staled info, will let the request pass
                        return Mono.just(containerController);
                    });
            })
            .flatMap(containerController -> {
                if (containerController.canHandleRequest(request)) {
                    return containerController.processRequest(request, originalRequestMono)
                        .doOnError(throwable -> this.handleException(request, containerController, throwable));
                } else {
                    // still can not handle the request
                    logger.warn(
                        "Can not find container controller to process request {} with collectionRid {} ",
                        request.getActivityId(),
                        request.requestContext.resolvedCollectionRid);

                    return originalRequestMono;
                }
            });
    }

    private Mono<IThroughputContainerController> resolveContainerController(String collectionLink) {
        checkArgument(StringUtils.isNotEmpty(collectionLink), "Collection link can not be null or empty");

        return this.containerControllerCache.getAsync(
            collectionLink,
            null,
            () -> this.createAndInitContainerController(collectionLink)
        );
    }

    private Mono<IThroughputContainerController> createAndInitContainerController(String containerLink) {
        checkArgument(StringUtils.isNotEmpty(containerLink), "Container link should not be null or empty");

        if (this.groupMapByContainer.containsKey(containerLink)) {
            return Mono.just(this.groupMapByContainer.get(containerLink))
                .flatMap(groups -> {
                    ThroughputContainerController containerController =
                        new ThroughputContainerController(
                            this.connectionMode,
                            this.globalEndpointManager,
                            groups,
                            this.partitionKeyRangeCache);

                    return containerController.init();
                });
        } else {
            return Mono.just(new EmptyThroughputContainerController())
                .flatMap(EmptyThroughputContainerController::init);
        }
    }

    private Mono<Boolean> shouldRefreshContainerController(String containerLink, RxDocumentServiceRequest request) {
        // TODO: populate diagnostics
        return this.collectionCache.resolveByNameAsync(null, containerLink, null)
            .flatMap(documentCollection ->
                Mono.just(StringUtils.equals(documentCollection.getResourceId(), request.requestContext.resolvedCollectionRid)));
    }

    private void handleException(RxDocumentServiceRequest request, IThroughputController controller, Throwable throwable) {
        checkNotNull(request, "Request can not be null");
        checkNotNull(controller, "Container controller can not be null");
        checkNotNull(throwable, "Exception can not be null");

        CosmosException cosmosException = Utils.as(Exceptions.unwrap(throwable), CosmosException.class);

        if (cosmosException != null &&
            (isNameCacheStale(cosmosException) || isPartitionKeyMismatchException(cosmosException))) {

            controller.close().subscribeOn(Schedulers.parallel()).subscribe();
            String containerLink = Utils.getCollectionName(request.getResourceAddress());

            this.collectionCache.refresh(null, containerLink, null);
            this.containerControllerCache.refresh(
                containerLink,
                () -> createAndInitContainerController(containerLink)
            );
        }
    }
}
