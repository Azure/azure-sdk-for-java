// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.throughputControl.config.ThroughputControlGroupInternal;
import com.azure.cosmos.implementation.throughputControl.controller.IThroughputController;
import com.azure.cosmos.implementation.throughputControl.controller.container.EmptyThroughputContainerController;
import com.azure.cosmos.implementation.throughputControl.controller.container.IThroughputContainerController;
import com.azure.cosmos.implementation.throughputControl.controller.container.ThroughputContainerController;
import com.azure.cosmos.implementation.throughputControl.exceptions.ThroughputControlInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

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
    private final ConcurrentHashMap<String, ContainerThroughputControlGroupProperties> containerMap;
    private final RxPartitionKeyRangeCache partitionKeyRangeCache;

    private final LinkedCancellationTokenSource cancellationTokenSource;
    private final ConcurrentHashMap<String, LinkedCancellationToken> cancellationTokenMap;

    public ThroughputControlStore(
        RxClientCollectionCache collectionCache,
        ConnectionMode connectionMode,
        RxPartitionKeyRangeCache partitionKeyRangeCache) {

        checkNotNull(collectionCache,"RxClientCollectionCache can not be null");
        checkNotNull(partitionKeyRangeCache, "PartitionKeyRangeCache can not be null");

        this.collectionCache = collectionCache;
        this.connectionMode = connectionMode;
        this.containerControllerCache = new AsyncCache<>();
        this.containerMap = new ConcurrentHashMap<>();
        this.partitionKeyRangeCache = partitionKeyRangeCache;

        this.cancellationTokenSource = new LinkedCancellationTokenSource();
        this.cancellationTokenMap = new ConcurrentHashMap<>();
    }

    public void enableThroughputControlGroup(ThroughputControlGroupInternal group) {
        checkNotNull(group, "Throughput control group cannot be null");

        String containerNameLink = Utils.trimBeginningAndEndingSlashes(BridgeInternal.extractContainerSelfLink(group.getTargetContainer()));
        this.containerMap.compute(containerNameLink, (key, throughputControlContainerProperties) -> {
            if (throughputControlContainerProperties == null) {
                throughputControlContainerProperties = new ContainerThroughputControlGroupProperties();
            }

            int groupSizeBefore = throughputControlContainerProperties.getThroughputControlGroupSet().size();
            Pair<Integer, Boolean> stateAfterEnabling =
                throughputControlContainerProperties.enableThroughputControlGroup(group);

            int groupSizeAfter = stateAfterEnabling.getLeft();
            boolean wasGroupConfigUpdated = stateAfterEnabling.getRight();

            if ((groupSizeAfter > groupSizeBefore && groupSizeAfter == 1) || wasGroupConfigUpdated) {
                // This is the first enabled group for the target container or an existing group was modified
                // Clean the current cache in case we have built EmptyThroughputContainerController or an existing
                // group was modified
                this.containerControllerCache.remove(containerNameLink);
            }

            return throughputControlContainerProperties;
        });
    }

    public <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> originalRequestMono) {
        checkNotNull(request, "Request can not be null");
        checkNotNull(originalRequestMono, "originalRequestMono can not be null");

        // Currently, we will only target two resource types.
        // If in the future we find other useful scenarios for throughput control, add more resource type here.
        if (request.getResourceType() != ResourceType.Document && request.getResourceType() != ResourceType.StoredProcedure) {
            return originalRequestMono;
        }

        String collectionNameLink = Utils.getCollectionName(request.getResourceAddress());
        return this.resolveContainerController(collectionNameLink)
            .flatMap(containerController -> {
                if (containerController.canHandleRequest(request)) {
                    return containerController.processRequest(request, originalRequestMono)
                        .doOnError(throwable -> this.handleException(collectionNameLink, request, throwable));
                }

                // Unable to find container controller to handle the request,
                // It is caused by control store out of sync or the request has staled info.
                // We will handle the first scenario by creating a new container controller,
                // while fall back to original request Mono for the second scenario.
                return this.updateControllerAndRetry(collectionNameLink, request, originalRequestMono);
            })
            .onErrorResume(throwable -> {

                Exception unwrappedException = Utils.as(Exceptions.unwrap(throwable), Exception.class);
                if (unwrappedException instanceof ThroughputControlInitializationException) {
                      if (this.shouldContinueRequestOnInitError(request, collectionNameLink, unwrappedException)) {
                          return originalRequestMono;
                      }

                      return Mono.error(unwrappedException.getCause());
               }

               return Mono.error(throwable);
            });
    }

    private boolean shouldContinueRequestOnInitError(RxDocumentServiceRequest request, String collectionNameLink, Throwable throwable) {
        if (throwable instanceof ThroughputControlInitializationException) {
            ContainerThroughputControlGroupProperties throughputControlContainerProperties = this.containerMap.get(collectionNameLink);

            checkNotNull(
                    throughputControlContainerProperties,
                    "Throughput control container properties should not be null");
            checkArgument(
                    throughputControlContainerProperties.getThroughputControlGroupSet().size() > 0,
                    "There should be more than one throughput control group");

            return throughputControlContainerProperties.allowRequestToContinueOnInitError(request);
        }

        return false;
    }

    private <T> Mono<T> updateControllerAndRetry(
        String containerNameLink,
        RxDocumentServiceRequest request,
        Mono<T> originalRequestMono) {

        return this.shouldRefreshContainerController(containerNameLink, request)
            .flatMap(shouldRefresh -> {
                if (shouldRefresh) {
                    this.cancellationTokenMap.compute(containerNameLink, (key, cancellationToken) -> {
                        if (cancellationToken != null) {
                            cancellationToken.cancel();
                        }

                        return null;
                    });

                    this.containerControllerCache.refresh(containerNameLink, cachedValue -> this.createAndInitContainerController(containerNameLink));
                    return this.resolveContainerController(containerNameLink)
                        .flatMap(updatedContainerController -> {
                            if (updatedContainerController.canHandleRequest(request)) {
                                return updatedContainerController.processRequest(request, originalRequestMono)
                                    .doOnError(throwable -> this.handleException(containerNameLink, request, throwable));
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

                return originalRequestMono;
            });
    }

    private Mono<IThroughputContainerController> resolveContainerController(String containerNameLink) {
        checkArgument(StringUtils.isNotEmpty(containerNameLink), "Container name link can not be null or empty");

        return this.containerControllerCache.getAsync(
                    containerNameLink,
                    null,
                    cachedValue -> this.createAndInitContainerController(containerNameLink))
                .onErrorResume(throwable -> Mono.error(new ThroughputControlInitializationException(throwable)));
    }

    private Mono<IThroughputContainerController> createAndInitContainerController(String containerNameLink) {
        checkArgument(StringUtils.isNotEmpty(containerNameLink), "Container link should not be null or empty");

        if (this.containerMap.containsKey(containerNameLink)) {
            return Mono.just(this.containerMap.get(containerNameLink))
                .flatMap(throughputControlContainerProperties -> {
                    LinkedCancellationToken parentToken =
                        this.cancellationTokenMap.compute(
                            containerNameLink,
                            (key, cancellationToken) -> this.cancellationTokenSource.getToken());

                    ThroughputContainerController containerController =
                        new ThroughputContainerController(
                            this.collectionCache,
                            this.connectionMode,
                            throughputControlContainerProperties.getThroughputControlGroupSet(),
                            this.partitionKeyRangeCache,
                            parentToken);

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

    private void handleException(String containerNameLink, RxDocumentServiceRequest request, Throwable throwable) {
        checkArgument(StringUtils.isNotEmpty(containerNameLink), "Container name link can not be null nor empty");
        checkNotNull(request, "Request can not be null");
        checkNotNull(throwable, "Exception can not be null");

        CosmosException cosmosException = Utils.as(Exceptions.unwrap(throwable), CosmosException.class);

        if (cosmosException != null &&
            (isNameCacheStale(cosmosException) || isPartitionKeyMismatchException(cosmosException))) {

            this.cancellationTokenMap.compute(containerNameLink,(key, cancellationToken) -> {
                if (cancellationToken != null) {
                    cancellationToken.cancel();
                }
                return null;
            });

            String containerLink = Utils.getCollectionName(request.getResourceAddress());

            this.collectionCache.refresh(null, containerLink, null);
            this.containerControllerCache.refresh(
                containerLink,
                cachedValue -> createAndInitContainerController(containerLink)
            );
        }
    }

    public void close() {
        this.cancellationTokenSource.close();
    }
}
