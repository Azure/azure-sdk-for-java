// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.server.controller;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.implementation.throughputControl.IThroughputContainerController;
import com.azure.cosmos.implementation.throughputControl.server.config.ServerThroughputControlGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ServerThroughputContainerController implements IThroughputContainerController {
    private static final Logger logger = LoggerFactory.getLogger(ServerThroughputContainerController.class);

    private final AsyncCache<String, ServerThroughputGroupController> groupControllerCache;
    private final Map<String, ServerThroughputControlGroup> groups;

    private ServerThroughputGroupController defaultGroupController;
    public ServerThroughputContainerController(Map<String, ServerThroughputControlGroup> groups) {

        checkArgument(groups != null && !groups.isEmpty(), "Throughput groups can not be null or empty");

        this.groupControllerCache = new AsyncCache<>();
        this.groups = groups;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<T> init() {
        return this.createAndInitializeGroupControllers().thenReturn((T) this);
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

    private Mono<Utils.ValueHolder<ServerThroughputGroupController>> getOrCreateThroughputGroupController(String groupName) {

        // If there is no control group defined, using the default group controller
        if (StringUtils.isEmpty(groupName)) {
            return Mono.just(new Utils.ValueHolder<>(this.defaultGroupController));
        }

        ServerThroughputControlGroup group = this.groups.get(groupName);
        if (group == null) {
            // If the request is associated with a group not enabled, will fall back to the default one.
            return Mono.just(new Utils.ValueHolder<>(this.defaultGroupController));
        }

        return this.resolveThroughputGroupController(group).map(Utils.ValueHolder::new);
    }

    @Override
    public boolean canHandleRequest(RxDocumentServiceRequest request) {
        return true;
    }

    private Mono<ServerThroughputContainerController> createAndInitializeGroupControllers() {
        return Flux.fromIterable(this.groups.values())
            .flatMap(this::resolveThroughputGroupController)
            .then(Mono.just(this));
    }

    private Mono<ServerThroughputGroupController> resolveThroughputGroupController(ServerThroughputControlGroup group) {
        return this.groupControllerCache.getAsync(
                group.getGroupName(),
                null,
                () -> this.createAndInitializeGroupController(group));
    }

    private Mono<ServerThroughputGroupController> createAndInitializeGroupController(ServerThroughputControlGroup group) {
        // create throughput bucket group controller
        ServerThroughputGroupController throughputGroupController = new ServerThroughputGroupController(group);
        if (throughputGroupController.isDefault()) {
            this.defaultGroupController = throughputGroupController;
        }
        return Mono.just(throughputGroupController);
    }
}
