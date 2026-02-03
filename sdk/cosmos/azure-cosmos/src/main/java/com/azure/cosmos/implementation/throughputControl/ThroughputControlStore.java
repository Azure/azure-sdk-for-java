// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.throughputControl.sdk.SDKThroughputControlStore;
import com.azure.cosmos.implementation.throughputControl.sdk.config.SDKThroughputControlGroupInternal;
import com.azure.cosmos.implementation.throughputControl.server.ServerThroughputControlStore;
import com.azure.cosmos.implementation.throughputControl.server.config.ServerThroughputControlGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;


public class ThroughputControlStore {
    private static final Logger logger = LoggerFactory.getLogger(ThroughputControlStore.class);
    private final SDKThroughputControlStore sdkThroughputControlStore;
    private final ServerThroughputControlStore serverThroughputControlStore;

    public ThroughputControlStore(
        RxClientCollectionCache collectionCache,
        ConnectionMode connectionMode,
        RxPartitionKeyRangeCache partitionKeyRangeCache) {

        checkNotNull(collectionCache,"RxClientCollectionCache can not be null");
        checkNotNull(partitionKeyRangeCache, "PartitionKeyRangeCache can not be null");

        this.sdkThroughputControlStore = new SDKThroughputControlStore(collectionCache, connectionMode, partitionKeyRangeCache);
        this.serverThroughputControlStore = new ServerThroughputControlStore();
    }

    public synchronized void enableSDKThroughputControlGroup(SDKThroughputControlGroupInternal group, Mono<Integer> throughputQueryMono) {
        checkNotNull(group, "Throughput control group cannot be null");

        if (group.isDefault()) {
            //verify a default group is not being defined in the server throughput control store
            String containerNameLink = Utils.trimBeginningAndEndingSlashes(BridgeInternal.extractContainerSelfLink(group.getTargetContainer()));
            if (this.serverThroughputControlStore.hasDefaultGroup(containerNameLink)) {
                throw new IllegalArgumentException("A default group already exists");
            }
        }

        this.sdkThroughputControlStore.enableThroughputControlGroup(group, throughputQueryMono);
    }

    public synchronized void enableServerThroughputControlGroup(ServerThroughputControlGroup group) {
        checkNotNull(group, "Throughput control group cannot be null");

        if (group.isDefault()) {
            //verify a default group is not being defined in the sdk throughput control store
            String containerNameLink =
                Utils.trimBeginningAndEndingSlashes(BridgeInternal.extractContainerSelfLink(group.getTargetContainer()));
            if (this.sdkThroughputControlStore.hasDefaultGroup(containerNameLink)) {
                throw new IllegalArgumentException("A default group already exists");
            }
        }

        this.serverThroughputControlStore.enableThroughputControlGroup(group);
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
        if (this.serverThroughputControlStore.hasGroup(collectionNameLink, request.getThroughputControlGroupName())) {
            return this.serverThroughputControlStore.processRequest(request, originalRequestMono);
        }

        if (this.sdkThroughputControlStore.hasGroup(collectionNameLink, request.getThroughputControlGroupName())) {
            return this.sdkThroughputControlStore.processRequest(request, originalRequestMono);
        }

        // can not find exact throughput control group mapping, using default group if any
        if (this.serverThroughputControlStore.hasDefaultGroup(collectionNameLink)) {
            return this.serverThroughputControlStore.processRequest(request, originalRequestMono);
        }

        if (this.sdkThroughputControlStore.hasDefaultGroup(collectionNameLink)) {
            return this.sdkThroughputControlStore.processRequest(request, originalRequestMono);
        }

        // neither store can process the request, fallback to just use the original mono
        return originalRequestMono;
    }

    public void close() {
        this.sdkThroughputControlStore.close();
    }
}
