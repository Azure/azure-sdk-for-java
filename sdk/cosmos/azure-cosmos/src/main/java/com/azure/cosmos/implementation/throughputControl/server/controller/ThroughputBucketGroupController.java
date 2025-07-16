package com.azure.cosmos.implementation.throughputControl.server.controller;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.throughputControl.server.config.ThroughputBucketControlGroup;
import reactor.core.publisher.Mono;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ThroughputBucketGroupController extends ServerThroughputGroupControllerBase {
    private final ThroughputBucketControlGroup throughputBucketControlGroup;

    public ThroughputBucketGroupController(ThroughputBucketControlGroup throughputBucketControlGroup) {
        checkNotNull(throughputBucketControlGroup, "Argument 'throughputBucketControlGroup' cannot be null.");
        this.throughputBucketControlGroup = throughputBucketControlGroup;
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
        if (this.throughputBucketControlGroup.getPriorityLevel() != null) {
            request.setPriorityLevel(this.throughputBucketControlGroup.getPriorityLevel());
        }

        if (this.throughputBucketControlGroup.getThroughputBucket() != null) {
            request.setThroughputBucket(this.throughputBucketControlGroup.getThroughputBucket());
        }

        return originalRequestMono;
    }

    public boolean isDefault() {
        return this.throughputBucketControlGroup.isDefault();
    }
}
