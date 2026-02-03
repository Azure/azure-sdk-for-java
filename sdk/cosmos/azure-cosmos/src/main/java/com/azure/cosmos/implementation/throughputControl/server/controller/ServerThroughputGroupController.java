// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.server.controller;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.throughputControl.IThroughputController;
import com.azure.cosmos.implementation.throughputControl.ThroughputControlRequestContext;
import com.azure.cosmos.implementation.throughputControl.server.config.ServerThroughputControlGroup;
import reactor.core.publisher.Mono;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ServerThroughputGroupController implements IThroughputController {
    private final ServerThroughputControlGroup serverThroughputControlGroup;

    public ServerThroughputGroupController(ServerThroughputControlGroup serverThroughputControlGroup) {
        checkNotNull(serverThroughputControlGroup, "Argument 'serverThroughputControlGroup' cannot be null.");
        this.serverThroughputControlGroup = serverThroughputControlGroup;
    }

    @Override
    public boolean canHandleRequest(RxDocumentServiceRequest request) {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<T> init() {
        return (Mono<T>) Mono.just(this);
    }

    @Override
    public <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> originalRequestMono) {
        if (this.serverThroughputControlGroup.getPriorityLevel() != null) {
            request.setPriorityLevel(this.serverThroughputControlGroup.getPriorityLevel());
        }

        if (this.serverThroughputControlGroup.getThroughputBucket() != null) {
            request.setThroughputBucket(this.serverThroughputControlGroup.getThroughputBucket());
        }

        if (request.requestContext != null) {
            request.requestContext.setThroughputControlRequestContext(
                new ThroughputControlRequestContext(this.serverThroughputControlGroup.getDiagnosticsString())
            );
        }

        return originalRequestMono;
    }

    public boolean isDefault() {
        return this.serverThroughputControlGroup.isDefault();
    }
}
