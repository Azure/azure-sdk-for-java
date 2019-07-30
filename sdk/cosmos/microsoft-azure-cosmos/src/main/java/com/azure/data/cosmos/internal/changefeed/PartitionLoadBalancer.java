// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import reactor.core.publisher.Mono;

/**
 * Interface for a partition load balancer.
 */
public interface PartitionLoadBalancer {
    /**
     * Starts the load balancer.
     *
     * @return a representation of the deferred computation of this call.
     */
    Mono<Void> start();

    /**
     * Stops the load balancer.
     *
     * @return a representation of the deferred computation of this call.
     */
    Mono<Void> stop();
}
