// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.ThroughputControlGroup;
import com.azure.cosmos.implementation.Exceptions;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    private final ConcurrentHashMap<String, List<ThroughputControlGroup>> groupMapByContainer;
    private final ConnectionMode connectionMode;
    private final AsyncCache<String, IThroughputContainerController> containerControllerCache;
    private final RxPartitionKeyRangeCache partitionKeyRangeCache;

    public ThroughputControlStore(
        RxClientCollectionCache collectionCache,
        ConnectionMode connectionMode,
        Set<ThroughputControlGroup> groups,
        RxPartitionKeyRangeCache partitionKeyRangeCache) {

        checkNotNull(collectionCache,"RxClientCollectionCache can not be null");
        checkNotNull(groups, "Throughput budget group configs can not be null");
        checkNotNull(partitionKeyRangeCache, "PartitionKeyRangeCache can not be null");

        this.collectionCache = collectionCache;

        this.groupMapByContainer = new ConcurrentHashMap<>();

        // group throughput control group by container self link.
        this.groupMapByContainer.putAll(
            groups
                .stream()
                .collect(Collectors.groupingBy(groupConfig ->
                    Utils.trimBeginningAndEndingSlashes(BridgeInternal.extractContainerSelfLink(groupConfig.getTargetContainer()))))
        );

        this.connectionMode = connectionMode;
        this.containerControllerCache = new AsyncCache<>();
        this.partitionKeyRangeCache = partitionKeyRangeCache;
    }

    public Mono<Void> init() {
        return Flux.fromIterable(Collections.list(this.groupMapByContainer.keys()))
            .flatMap(containerLink -> this.resolveContainerController(containerLink)).then();
    }

    private Mono<IThroughputContainerController> createAndInitContainerController(String containerLink) {
        checkArgument(StringUtils.isNotEmpty(containerLink), "Container link should not be null or empty");

        if (this.groupMapByContainer.containsKey(containerLink)) {
            return Mono.just(this.groupMapByContainer.get(containerLink))
                .flatMap(groups -> {
                    ThroughputContainerController containerController =
                        new ThroughputContainerController(
                            this.connectionMode,
                            groups,
                            this.partitionKeyRangeCache);

                    return containerController.init();
                });
        } else {
            return Mono.just(new EmptyThroughputContainerController())
                .flatMap(EmptyThroughputContainerController::init);
        }

    }

    public <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> originalRequestMono) {
        checkNotNull(request, "Request can not be null");
        checkNotNull(originalRequestMono, "originalRequestMono can not be null");

        if (request.getResourceType() == ResourceType.Document || request.getResourceType() == ResourceType.StoredProcedure) {
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
                                containerController.close();
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
                            .doOnError(throwable -> this.doOnError(request, containerController, throwable));
                    } else {
                        // still can not handle the request
                        return originalRequestMono;
                    }

                });
        } else {
            return originalRequestMono;
        }
    }

    private Mono<Boolean> shouldRefreshContainerController(String containerLink, RxDocumentServiceRequest request) {
        // TODO: populate diagnostics
        return this.collectionCache.resolveByNameAsync(null, containerLink, null)
            .flatMap(documentCollection ->
                Mono.just(StringUtils.equals(documentCollection.getResourceId(), request.requestContext.resolvedCollectionRid)));
    }

    private void doOnError(RxDocumentServiceRequest request, IThroughputController controller, Throwable throwable) {
        checkNotNull(request, "Request can not be null");
        checkNotNull(controller, "Container controller can not be null");
        checkNotNull(throwable, "Exception can not be null");

        CosmosException cosmosException = Utils.as(throwable, CosmosException.class);

        // TODO: should we handle partitionKeyMismatchException ?
        if (cosmosException != null &&
            (Exceptions.isNameCacheStale(cosmosException) || Exceptions.isPartitionKeyMismatchException(cosmosException))) {

            controller.close();
            String containerLink = Utils.getCollectionName(request.getResourceAddress());

            this.collectionCache.refresh(null, containerLink, null);
            this.containerControllerCache.refresh(
                containerLink,
                () -> createAndInitContainerController(containerLink)
            );
        }
    }

    private Mono<IThroughputContainerController> resolveContainerController(String collectionLink) {
        checkArgument(StringUtils.isNotEmpty(collectionLink), "Collection link can not be null or empty");

        return this.containerControllerCache.getAsync(
            collectionLink,
            null,
            () -> this.createAndInitContainerController(collectionLink)
        );
    }
}
