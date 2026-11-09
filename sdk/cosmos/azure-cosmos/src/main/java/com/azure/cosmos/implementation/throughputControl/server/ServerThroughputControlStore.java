// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.server;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.implementation.throughputControl.EmptyThroughputContainerController;
import com.azure.cosmos.implementation.throughputControl.server.config.ServerThroughputControlGroup;
import com.azure.cosmos.implementation.throughputControl.server.controller.ServerThroughputContainerController;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ServerThroughputControlStore {

    private final AsyncCache<String, ServerThroughputContainerController> containerControllerCache;
    private final ConcurrentHashMap<String, ContainerServerThroughputControlGroupProperties> containerMap;

    public ServerThroughputControlStore() {
        this.containerControllerCache = new AsyncCache<>();
        this.containerMap = new ConcurrentHashMap<>();
    }

    public void enableThroughputControlGroup(ServerThroughputControlGroup group) {
        checkNotNull(group, "Throughput control group cannot be null");

        String containerNameLink = Utils.trimBeginningAndEndingSlashes(BridgeInternal.extractContainerSelfLink(group.getTargetContainer()));
        this.containerMap.compute(containerNameLink, (key, throughputControlContainerProperties) -> {
            if (throughputControlContainerProperties == null) {
                throughputControlContainerProperties = new ContainerServerThroughputControlGroupProperties(containerNameLink);
            }

            int groupSizeAfterEnabling = throughputControlContainerProperties.enableThroughputControlGroup(group);
            if (groupSizeAfterEnabling == 1) {
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
            .flatMap(containerController -> containerController.processRequest(request, originalRequestMono));
    }

    public boolean hasDefaultGroup(String containerNameLink) {
        if (this.containerMap.containsKey(containerNameLink)) {
            return this.containerMap.get(containerNameLink).hasDefaultGroup();
        }

        return false;
    }

    public boolean hasGroup(String containerNameLink, String throughputControlGroupName) {
        if (StringUtils.isEmpty(throughputControlGroupName)) {
            return false;
        }

        if (this.containerMap.containsKey(containerNameLink)) {
            return this.containerMap.get(containerNameLink).hasGroup(throughputControlGroupName);
        }

        return false;
    }

    private Mono<ServerThroughputContainerController> resolveContainerController(String containerNameLink) {
        return this.containerControllerCache.getAsync(
            containerNameLink,
            null,
            () -> this.createAndInitContainerController(containerNameLink));
    }

    private Mono<ServerThroughputContainerController> createAndInitContainerController(String containerNameLink) {
        checkArgument(StringUtils.isNotEmpty(containerNameLink), "Container link should not be null or empty");

        if (this.containerMap.containsKey(containerNameLink)) {
            ContainerServerThroughputControlGroupProperties containerProperties =
                this.containerMap.get(containerNameLink);
            return Mono
                .just(new ServerThroughputContainerController(containerProperties.getThroughputControlGroups()))
                .flatMap(ServerThroughputContainerController::init);
        } else {
            return Mono.just(new EmptyThroughputContainerController())
                .flatMap(EmptyThroughputContainerController::init);
        }
    }
}
