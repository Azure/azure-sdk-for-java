// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import reactor.core.publisher.Mono;

/**
 * Provides an API to run continious processing on a single partition of some resource.
 * <p>
 * Created by {@link PartitionProcessorFactory}.create() after some lease is acquired by the current host.
 *  Processing can perform the following tasks in a loop:
 *    1. READ some data from the resource partition.
 *    2. Handle possible problems with the read.
 *    3. Pass the obtained data to an observer by calling {@link ChangeFeedObserver}.processChangesAsync{} with the context {@link ChangeFeedObserverContext}.
 */
public interface PartitionProcessor {
    /**
     * Perform partition processing.
     *
     * @param cancellationToken the cancellation token.
     * @return a representation of the deferred computation of this call.
     */
    Mono<Void> run(CancellationToken cancellationToken);

    /**
     * @return the inner exception if any, otherwise null.
     */
    RuntimeException getResultException();
}
