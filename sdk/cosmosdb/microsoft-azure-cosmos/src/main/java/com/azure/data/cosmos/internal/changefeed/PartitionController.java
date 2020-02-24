// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import reactor.core.publisher.Mono;

/**
 * Interface for the partition controller.
 */
public interface PartitionController {
    /**
     * Add or update lease item.
     *
     * @return a representation of the deferred computation of this call.
     */
    Mono<Lease> addOrUpdateLease(Lease lease);

    /**
     * Initialize and start the partition controller thread.
     *
     * @return a representation of the deferred computation of this call.
     */
    Mono<Void> initialize();

    /**
     * Shutdown partition controller thread.
     *
     * @return a representation of the deferred computation of this call.
     */
    Mono<Void> shutdown();
}
