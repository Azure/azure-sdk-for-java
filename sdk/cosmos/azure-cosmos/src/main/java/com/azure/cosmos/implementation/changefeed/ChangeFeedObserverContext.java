// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed;

import com.azure.cosmos.models.FeedResponse;
import reactor.core.publisher.Mono;

/**
 * Represents the context passed to {@link ChangeFeedObserver} events.
 */
public interface ChangeFeedObserverContext<T> {

    /**
     * Gets the lease token
     *
     * @return the lease token.
     */
    String getLeaseToken();

    /**
     * Gets the response from the underlying call.
     *
     * @return the response from the underlying call.
     */
    FeedResponse<T> getFeedResponse();

    /**
     * Checkpoints progress of a stream. This method is valid only if manual checkpoint was configured.
     * <p>
     *   Client may accept multiple change feed batches to process in parallel.
     *   Once first N document processing was finished the client can call checkpoint on the last completed batches in the row.
     *   In case of automatic checkpointing this is method throws.
     *
     * @return a representation of the deferred computation of this call.
     */
    Mono<Lease> checkpoint();
}
