// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import reactor.core.publisher.Mono;

/**
 * Interface for the partition supervisor.
 */
public interface PartitionSupervisor {
    /**
     * Runs the task.
     *
     * @param cancellationToken the cancellation token.
     * @return a deferred operation of this call.
     */
    Mono<Void> run(CancellationToken cancellationToken);

    /**
     * @return the inner exception if any, otherwise null.
     */
    RuntimeException getResultException();
}
