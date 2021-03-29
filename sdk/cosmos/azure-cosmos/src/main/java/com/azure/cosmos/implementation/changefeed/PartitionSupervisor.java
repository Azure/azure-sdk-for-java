// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed;

import reactor.core.publisher.Mono;

/**
 * Interface for the partition supervisor.
 */
public interface PartitionSupervisor {
    /**
     * Runs the task.
     *
     * @param shutdownToken the cancellation token.
     * @param cancellationTokenSource the cancellation token source for other tasks.
     * @return a deferred operation of this call.
     */
    Mono<Void> run(CancellationToken shutdownToken, CancellationTokenSource cancellationTokenSource);

    /**
     * @return the inner exception if any, otherwise null.
     */
    RuntimeException getResultException();
}
