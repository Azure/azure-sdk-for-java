// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputBudget;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.ThroughputBudgetGroupConfig;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.throughputBudget.controller.IThroughputBudgetController;
import com.azure.cosmos.implementation.throughputBudget.controller.container.ThroughputBudgetContainerController;
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
 * This is the entrance class for the whole throughput budget control work flow pipeline.
 * The pipeline will consist of controllers which is implementation of {@link IThroughputBudgetController} and {@link ThroughputBudgetRequestAuthorizer}.
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
 *                       |              ThroughputBudgetControlStore               |
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
 *         +-------------------+                +---------------------+
 *         | Group 1 controler |                | Group 2 controller  |
 *         +-------------------+     ...        +---------------------+
 *                  |                                        |
 *                  |                                        |
 *                  |                                        |
 *    +-------------------------------------+      +-----------------------------------------+
 *    |Global Request authorizer controller |      | PkRanges Request authorizer controller  |
 *    +-------------------------------------+      +-----------------------------------------+
 *                  |                                         /                   \
 *                  |                                        /                     \
 *                  |                                       /                       \
 *       +------------------+                   +------------------+            +------------------+
 *       |Request authorizer|                   |Request authorizer|            |Request authorizer|
 *       +------------------+                   +------------------+  ...       +------------------+
 *
 *
  */
public class ThroughputBudgetControlStore {

    private static final Logger logger = LoggerFactory.getLogger(ThroughputBudgetControlStore.class);
    private final RxClientCollectionCache collectionCache;
    private final ConcurrentHashMap<String, List<ThroughputBudgetGroupConfig>> configMapByContainer;
    private final ConnectionMode connectionMode;
    private final AsyncCache<String, ThroughputBudgetContainerController> containerControllerCache;
    private final String hostName;
    private final RxPartitionKeyRangeCache partitionKeyRangeCache;

    public ThroughputBudgetControlStore(
        RxClientCollectionCache collectionCache,
        ConnectionMode connectionMode,
        Set<ThroughputBudgetGroupConfig> groupConfigs,
        String hostName,
        RxPartitionKeyRangeCache partitionKeyRangeCache) {

        checkNotNull(collectionCache,"RxClientCollectionCache can not be null");
        checkNotNull(groupConfigs, "Throughput budget group configs can not be null");
        checkArgument(StringUtils.isNotEmpty(hostName), "Host name can not be null or empty");
        checkNotNull(partitionKeyRangeCache, "PartitionKeyRangeCache can not be null");

        this.collectionCache = collectionCache;

        this.configMapByContainer = new ConcurrentHashMap<>();
        this.configMapByContainer.putAll(
            groupConfigs
                .stream()
                .collect(Collectors.groupingBy(groupConfig ->
                    Utils.trimBeginningAndEndingSlashes(BridgeInternal.extractContainerSelfLink(groupConfig.getTargetContainer()))))
        );

        this.connectionMode = connectionMode;
        this.containerControllerCache = new AsyncCache<>();
        this.hostName = hostName;
        this.partitionKeyRangeCache = partitionKeyRangeCache;
    }

    public Mono<Void> init() {
        return Flux.fromIterable(Collections.list(this.configMapByContainer.keys()))
            .flatMap(containerLink -> {
                return this.containerControllerCache.getAsync(
                    containerLink,
                    null,
                    () -> this.createAndInitContainerController(containerLink));
            }).then();
    }

    public <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> nextRequestMono) {
        checkNotNull(request, "Request can not be null");
        checkNotNull(nextRequestMono, "NextRequestMono can not be null");

        if (request.getResourceType() != ResourceType.Document) {
            return nextRequestMono;
        }

        String collectionLink = Utils.getCollectionName(request.getResourceAddress());
        if (this.configMapByContainer.containsKey(collectionLink)) {
            return this.resolveContainerController(collectionLink)
                .flatMap(containerController -> {
                    if (containerController.canHandleRequest(request)) {
                        return Mono.just(containerController);
                    }

                    return this.shouldRefreshContainerController(collectionLink, containerController)
                        .flatMap(shouldRefresh -> {
                            if (shouldRefresh) {
                                containerController.close();
                                this.containerControllerCache.refresh(collectionLink, () -> this.createAndInitContainerController(collectionLink));
                                return this.resolveContainerController(collectionLink);
                            }

                            // The container container controller is up to date, the request has staled info, will let the request pass
                            return Mono.empty();
                        });
                })
                .flatMap(containerController -> {
                    return containerController.processRequest(request, nextRequestMono)
                        .doOnError(throwable -> this.doOnError(request, containerController, throwable));
                });
        } else{
            return nextRequestMono;
        }


    }

    private Mono<ThroughputBudgetContainerController> createAndInitContainerController(String collectionLink) {
        checkArgument(StringUtils.isNotEmpty(collectionLink), "Collection link should not be null or empty");

        return Mono.justOrEmpty(this.configMapByContainer.get(collectionLink))
            .flatMap(groupConfigs -> {
                // Only create container controller when the owner resource exists
                // TODO: populate diagnostics context
                ThroughputBudgetContainerController containerController =
                    new ThroughputBudgetContainerController(
                        this.connectionMode,
                        this.configMapByContainer.get(collectionLink),
                        this.hostName,
                        this.partitionKeyRangeCache);
                return containerController.init();
            });
    }

    private void doOnError(RxDocumentServiceRequest request, IThroughputBudgetController controller, Throwable throwable) {
        checkNotNull(request, "Request can not be null");
        checkNotNull(controller, "Container controller can not be null");
        checkNotNull(throwable, "Exception can not be null");

        CosmosException cosmosException = Utils.as(throwable, CosmosException.class);

        if (cosmosException != null &&
            (this.isInvalidPartitionKeyException(cosmosException) || this.isPartitionKeyMismatchException(cosmosException))) {

            controller.close();
            String containerLink = Utils.getCollectionName(request.getResourceAddress());

            this.collectionCache.refresh(null, containerLink, null);
            this.containerControllerCache.refresh(
                containerLink,
                () -> createAndInitContainerController(containerLink)
            );
        }
    }

    private boolean isInvalidPartitionKeyException(CosmosException cosmosException) {
        return Exceptions.isStatusCode(cosmosException, HttpConstants.StatusCodes.GONE) &&
            Exceptions.isSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE);
    }

    private boolean isPartitionKeyMismatchException(CosmosException cosmosException) {
        return Exceptions.isStatusCode(cosmosException, HttpConstants.StatusCodes.NOTFOUND) &&
            Exceptions.isSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.PARTITION_KEY_MISMATCH);
    }

    private Mono<ThroughputBudgetContainerController> resolveContainerController(String collectionLink) {
        checkArgument(StringUtils.isNotEmpty(collectionLink), "Collection link can not be null or empty");

        return this.containerControllerCache.getAsync(
            collectionLink,
            null,
            () -> this.createAndInitContainerController(collectionLink)
        );
    }

    private Mono<Boolean> shouldRefreshContainerController(String containerLink, ThroughputBudgetContainerController containerController) {
        return this.collectionCache.resolveByNameAsync(null, containerLink, null)
            .flatMap(documentCollection ->
                Mono.just(StringUtils.equals(documentCollection.getResourceId(), containerController.getResolvedContainerRid())));
    }
}
